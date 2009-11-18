/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Large portions of this software are based upon public domain software
 * https://sip-communicator.dev.java.net/
 *
 */
package net.sourceforge.gjtapi.raw.sipprovider.media;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.media.Controller;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;

import net.sourceforge.gjtapi.raw.sipprovider.common.Console;

class AVTransmitter {
    protected static Console console = Console.getConsole(AVTransmitter.class);
    /** Input MediaLocator. Can be a file or HTTP or capture source */
    protected MediaLocator locator;
    protected String ipAddress;
    protected Processor processor;
    protected RTPManager rtpMgrs[];
    /** Used by mobility - keeps rtpMgrs[] corresponding addresses. */
    protected SessionAddress sessionAddresses[] = null;

    protected DataSource dataOutput = null;
    protected List<Integer> ports;
    protected List formatSets;
    protected MediaManager mediaManCallback = null;
    private SendStream sendStream;
    /** Utility to delay until a processor state has been reached. */
    protected ProcessorUtility procUtility = new ProcessorUtility("AVTransmitter");
    /** <code>true</code> if the transmitter has been started. */
    private boolean started;

    public AVTransmitter(Processor processor,
                         String ipAddress,
                         List<Integer> ports,
                         java.util.ArrayList formatSets) {
        try {
            console.logEntry();
            this.processor = processor;
            this.ipAddress = ipAddress;
            this.ports = ports;
            this.formatSets = formatSets;
            if (console.isDebugEnabled()) {
                console.debug(
                        "Created transmitter for ["
                        + ipAddress
                        + "] at ports: "
                        + ports.toString()
                        + " encoded as: "
                        + formatSets.toString());
            }
        } finally {
            console.logExit();
        }
    }

    void setMediaManagerCallback(MediaManager mediaManager) {
        this.mediaManCallback = mediaManager;
    }

    /**
     * Starts the transmission. Returns null if transmission started ok.
     */
    synchronized void start(final Processor p) throws MediaException {
        try {
            console.logEntry();
            configureProcessor(p);
            // Create an RTP session to transmit the output of the
            // processor to the specified IP address and port no.
            try {
                createTransmitter();
            } catch (MediaException ex) {
                console.error("createTransmitter() failed", ex);
                processor.close();
                processor = null;
                throw ex;
            }
            // Start the transmission
            processor.start();
            started = true;
        } finally {
            console.logExit();
        }
    }

    /**
     * Checks if this transmitter has been started.
     * @return <code>true</code> if the transmitter has been started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Stops the transmission if already started
     */
    void stop(SessionAddress addToStop) {
        try {
            console.logEntry();
            synchronized (this) {
                if (processor != null) {
                    processor.stop();
                    if (rtpMgrs != null) {
                        for (int i = 0; i < rtpMgrs.length; i++) {
                            if (rtpMgrs[i] == null) {
                                continue;
                            }
                            rtpMgrs[i].removeTarget(addToStop, "Session ended.");

                        }
                    }
                }
            }
        } catch (javax.media.rtp.InvalidSessionAddressException ex) {
            console.debug(ex.toString(), ex);
            started = false;
        } finally {
            console.logExit();
        }
    }

    protected void configureProcessor(Processor p) throws MediaException {
        processor = p;
        try {
            console.logEntry();
            if (processor == null) {
                console.error("Processor is null.");
                throw new MediaException("No processor to configure!");
            }
            // Wait for the processor to configure
            boolean result = true;
            if (processor.getState() < Processor.Configured) {
                result = procUtility.waitForState(processor, 
                        Processor.Configured);
            }
            if (result == false) {
                console.error("Couldn't configure processor");
                throw new MediaException("Couldn't configure processor!");
            }
            // Get the tracks from the processor
            TrackControl[] tracks = processor.getTrackControls();
            // Do we have atleast one track?
            if (tracks == null || tracks.length < 1) {
                console.error("Couldn't find tracks in processor");
                throw new MediaException("Couldn't find tracks in processor!");
            }
            // Set the output content descriptor to RAW_RTP
            // This will limit the supported formats reported from
            // Track.getSupportedFormats to only valid RTP formats.
            ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.
                    RAW_RTP);
            processor.setContentDescriptor(cd);
            Format supported[];

            boolean atLeastOneTrack = false;
            // Program the tracks.
            int i = 0;

            if (tracks[i].isEnabled()) {
                supported = tracks[i].getSupportedFormats();
                if (console.isDebugEnabled()) {
                    console.debug("Available encodings are:");
                    for (int j = 0; j < supported.length; j++) {
                        console.debug("track[" + (i + 1) + "] format[" +
                                      (j + 1) + "]="
                                      + supported[j].getEncoding());
                    }
                }
                // We've set the output content to the RAW_RTP.
                // So all the supported formats should work with RTP.
                // We'll pick one that matches those specified by the constructor.
                if (supported.length > 0) {

                    int index = findFirstMatchingFormat(supported, formatSets);
                    if (index != -1) {
                        tracks[i].setFormat(supported[index]);
                        if (console.isDebugEnabled()) {
                            console.debug("Track " + i +
                                          " is set to transmit as: "
                                          + supported[index]);
                        }
                        atLeastOneTrack = true;
                    } else {
                        tracks[i].setEnabled(false);
                    }

                } else {
                    tracks[i].setEnabled(false);
                }
            } else {
                tracks[i].setEnabled(false);
            }

            if (!atLeastOneTrack) {
                console.error(
                        "Couldn't set any of the tracks to a valid RTP format");
                throw new MediaException(
                        "Couldn't set any of the tracks to a valid RTP format");
            }
            // Realize the processor. This will internally create a flow
            // graph and attempt to create an output datasource
            processor.realize();
            result = procUtility.waitForState(processor, Controller.Realized);
            if (result == false) {
                console.error("Couldn't realize processor");
                throw new MediaException("Couldn't realize processor");
            }

            // Get the output data source of the processor
            dataOutput = processor.getDataOutput();
        } finally {
            console.logExit();
        }
    }

    /**
     * Use the RTPManager API to create sessions for each media
     * track of the processor.
     */
    protected void createTransmitter() throws MediaException {
        try {
            console.logEntry();
            /* PushBufferDataSource pbds = (PushBufferDataSource) dataOutput;
             PushBufferStream pbss[] = pbds.getStreams();*/
            rtpMgrs = new RTPManager[1];
            //used by mobility
            sessionAddresses = new SessionAddress[1];
            SessionAddress localAddr, destAddr;
            InetAddress remoteAddress;

            console.debug("data sources - " + 1);
            int port = 0;
            int i = 0;

            try {
                remoteAddress = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException ex) {
                console.error("Failed to resolve remote address", ex);
                throw new MediaException("Failed to resolve remote address",
                                         ex);
            }
            // port = findPortForFormat(pbss[i].getFormat().getEncoding());
            if (port == -1) {
                console.error("failed to find a format's port");
                throw new MediaException(
                        "Internal error! AVTransmitter failed to find a"
                        + " format's corresponding port");
            }

            //localAddr = new SessionAddress(mediaManCallback.getLocalHost(),((Integer)ports.get(0)).intValue() );
            localAddr = new SessionAddress(mediaManCallback.getLocalHost(),
                    mediaManCallback.getAudioPort());
            destAddr = new SessionAddress(remoteAddress,
                                          ((Integer) ports.get(0)).intValue());

            console.debug("IP localHostAVTRansM: " +
                          mediaManCallback.getLocalHost() + " localPort: " +
                          mediaManCallback.getAudioPort());
            console.debug("IP remoteHostAVTRansM: " + remoteAddress +
                          " remotePort: " + ((Integer) ports.get(0)).intValue());

            rtpMgrs[i] = mediaManCallback.getRtpManager(localAddr);
            if (rtpMgrs[i] == null) {
                rtpMgrs[i] = RTPManager.newInstance();
                mediaManCallback.putRtpManager(localAddr, rtpMgrs[i]);

            }

            try {
                rtpMgrs[i].initialize(localAddr);
                console.debug("Just bound to port " + localAddr.getDataPort());
                rtpMgrs[i].addTarget(destAddr);
                sessionAddresses[i] = destAddr;
            } catch (InvalidSessionAddressException ex) {
                //port was occupied
                if (console.isDebugEnabled()) {
                    console.debug("Couldn't bind to local ports "
                                  + localAddr.getDataPort() + ", " +
                                  localAddr.getControlPort()
                                  + " @ " +
                                  localAddr.getControlHostAddress()
                                  + ".\n Exception message was: " +
                                  ex.getMessage()
                                  + " Will try another pair!");
                }

            } catch (IOException ex) {
                //we should just try to notify user and continue with other tracks
                console.error(
                        "Failed to initialize an RTPManager for address pair:\n"
                        + "Local address:" + localAddr.toString()
                        + " data port:" + localAddr.getDataPort()
                        + " control port:" + localAddr.getControlPort() +
                        "\n"
                        + "Dest  address:" + destAddr
                        + " data port:" + destAddr.getDataPort()
                        + " control port:" + destAddr.getControlPort(),
                        ex);
                mediaManCallback.fireNonFatalMediaError(new
                        MediaException(
                                "Failed to initialize an RTPManager for address pair:\n"
                                + "Local address:" + localAddr.toString()
                                + " data port:" + localAddr.getDataPort()
                                + " control port:" + localAddr.getControlPort() +
                                "\n"
                                + "Dest  address:" + destAddr
                                + " data port:" + destAddr.getDataPort()
                                + " control port:" + destAddr.getControlPort(),
                                ex));

            }
        }

        finally {
            console.logExit();
        }
    }


    protected int findPortForFormat(String format) {
        try {
            console.logEntry();
            for (int i = 0; i < formatSets.size(); i++) {
                List currentSet = (List) formatSets.get(i);
                for (int j = 0; j < currentSet.size(); j++) {
                    if (((String) currentSet.get(j)).equals(format)) {
                        return ((Integer) ports.get(i)).intValue();
                    }
                }
            }
            return -1;
        } finally {
            console.logExit();
        }
    }


    protected int findFirstMatchingFormat(Format[] hayStack, List needles) {
        try {
            console.logEntry();
            if (hayStack == null || needles == null) {
                return -1;
            }
            for (int j = 0; j < needles.size(); j++) {
                List currentSet = (List) needles.get(j);
                for (int k = 0; k < currentSet.size(); k++) {
                    for (int i = 0; i < hayStack.length; i++) {
                        if (hayStack[i].getEncoding().equals(
                                currentSet.get(k))) {
                            return i;
                        }
                    }
                }
            }
            return -1;
        } finally {
            console.logExit();
        }
    }


    public void play(Processor p) throws MediaException {
        console.logEntry();
        configureProcessor(p);
        processor.start();
        PushBufferDataSource pbds = (PushBufferDataSource) dataOutput;
        PushBufferStream pbss[] = pbds.getStreams();
        int i = 0;
        try {
            sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
            sendStream.start();
            if (console.isDebugEnabled()) {
                console.debug("Started transmitting track " + i
                              + " encoded as " +
                              pbss[i].getFormat().getEncoding()
                              + " @ [" + ipAddress + "]");
            }
        } catch (Exception ex) {
            console.error("Session " + i +
                          " failed to start transmitting.", ex);
            throw new MediaException(
                    "Session " + i + " failed to start transmitting.", ex);
        }
        console.logExit();
    }

    public void stopPlaying() throws IOException {
        sendStream.stop();
    }
}
