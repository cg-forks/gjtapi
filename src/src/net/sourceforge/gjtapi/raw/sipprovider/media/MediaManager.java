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
import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoDataSourceException;
import javax.media.NoProcessorException;
import javax.media.Player;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SessionAddress;
import javax.sdp.Connection;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SdpConstants;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sdp.SessionName;
import javax.sdp.TimeDescription;
import javax.sdp.Version;

import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
import net.sourceforge.gjtapi.raw.sipprovider.common.NetworkAddressManager;
import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaErrorEvent;
import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaEvent;
import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaListener;

/**
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * <p>Division Chief: Thomas Noel </p>
 * @author Emil Ivov (http://www.emcho.com)
 * @version 1.1
 *
 */
public class MediaManager implements Serializable {
    static final long serialVersionUID = 0L; // never serialized

    protected static Console console = Console.getConsole(MediaManager.class);
    protected Collection<MediaListener> listeners =
        new java.util.ArrayList<MediaListener>();
    protected SdpFactory sdpFactory;
    protected ProcessorUtility procUtility = new ProcessorUtility("MediaManager");
    //media devices
    protected CaptureDeviceInfo audioDevice;
    //Sdp Codes of all formats supported for
    //transmission by the selected datasource

    protected ArrayList transmittableAudioFormats = new ArrayList();
    //Sdp Codes of all formats that we can receive
    //i.e.  all formats supported by JMF
    protected String[] receivableVideoFormats = new String[] {
                                                //sdp format 							   		// corresponding JMF Format
                                                Integer.toString(SdpConstants.
            H263), // javax.media.format.VideoFormat.H263_RTP
                                                Integer.toString(SdpConstants.
            JPEG), // javax.media.format.VideoFormat.JPEG_RTP
                                                Integer.toString(SdpConstants.
            H261) // javax.media.format.VideoFormat.H261_RTP
    };
    protected String[] receivableAudioFormats = new String[] {
                                                //sdp format
                                                Integer.toString(SdpConstants.
            GSM), // javax.media.format.AudioFormat.GSM_RTP;// corresponding JMF Format
                                                Integer.toString(SdpConstants.
            G723), // javax.media.format.AudioFormat.G723_RTP
                                                Integer.toString(SdpConstants.
            PCMU), // javax.media.format.AudioFormat.ULAW_RTP;
                                                Integer.toString(SdpConstants.
            DVI4_8000), // javax.media.format.AudioFormat.DVI_RTP;
                                                Integer.toString(SdpConstants.
            DVI4_16000), // javax.media.format.AudioFormat.DVI_RTP;
                                                Integer.toString(SdpConstants.
            PCMA), // javax.media.format.AudioFormat.ALAW;
                                                Integer.toString(SdpConstants.
            G728), //, // javax.media.format.AudioFormat.G728_RTP;
                                                //g729 is not suppported by JMF
                                                Integer.toString(SdpConstants.
            G729) // javax.media.format.AudioFormat.G729_RTP
    };

    /**
     * A list of currently active RTPManagers mapped against Local session addresses.
     * The list is used by transmitters and receivers so that receiving and transmitting
     * from the same port simultaneousl is possible
     */
    protected Map activeRtpManagers = new Hashtable();
    protected Map sessions = new Hashtable();
    protected RTPManager rtpManager;
    private final String mediaSource;
    /** The data source. */
    protected DataSource source;
    /** The processor to use for sending the data. */
    protected Processor sndProcessor;
    /** The processor to use for receiving the data. */
    protected Processor rcvProcessor;
    protected boolean isStarted = false;
    protected Properties sipProp;
    private final int audioPort;
    protected Collection<AVTransmitter> transmitters =
        new java.util.ArrayList<AVTransmitter>();
    protected Collection<AVReceiver> receivers =
        new java.util.ArrayList<AVReceiver>();
    /** The data sink. */
    protected DataSink sink;
    /** The data sink. */
    private MediaLocator dest;
    /** Reference to the address manager. */
    private final NetworkAddressManager addressManager;

    public MediaManager(Properties sipProp, NetworkAddressManager manager) {
        String port = sipProp.getProperty(
                "net.java.sip.communicator.media.AUDIO_PORT");
        audioPort = Integer.parseInt(port);
        console.debug("using audio port " + audioPort);
        mediaSource = sipProp.getProperty(
            "net.java.sip.communicator.media.MEDIA_SOURCE");
        console.debug("using media source '" + mediaSource + "'");
        this.sipProp = new Properties();
        this.sipProp.putAll(sipProp);
        addressManager = manager;
    }


    /**
     * Reads the audio from the given URL and publishes it to all transmitters.
     * @param url the URL pointing to an audio data source
     * @throws MediaException
     *         Error playing the audio.
     */
    public void play(final String url) throws MediaException {
        console.logEntry();
        if (console.isDebugEnabled()) {
            console.debug("playing '" + url + "'...");
        }
        final MediaLocator locator = new MediaLocator(url);
        try {
            source = createDataSource(locator);
        } catch (IOException e) {
            throw new MediaException(e.getMessage(), e);
        }
        initSndProcessor(source);
        for (AVTransmitter transmitter : transmitters) {
            if (!transmitter.isStarted()) {
                console.debug("Starting transmission.");
                transmitter.start(sndProcessor);
            }
            transmitter.play(sndProcessor);
        }
        console.logExit();
    }

    /**
     * Stops all transmitters.
     */
    public void stopPlaying() {
        console.logEntry();
        for (AVTransmitter transmitter : transmitters) {
            try {
                transmitter.stopPlaying();
            } catch (IOException ex) {
                console.warn(ex.toString(), ex);
            }
        }
        console.logExit();
    }

    public void record(String url) throws MediaException {
        console.logEntry();
        try {
//            DataSource mergeDs = this.getDataSource();
            // append "file:/" to URL if it is not already there
            String fullUrl = (url.indexOf("file:") == 0) ? url : "file:/" + url;
            dest = new MediaLocator(fullUrl);
            final DataSource ds = createDataSource(dest);
            initRcvProcessor(ds);
            sink = Manager.createDataSink(ds, dest);
            sink.open();
            sink.start();
            for (AVReceiver receiver : receivers) {
                final Processor pro = receiver.getProcessor();
                pro.start();
            }
        } catch (IOException ex) {
            throw new MediaException(ex.getMessage(), ex);
        } catch (NoDataSinkException ex) {
            throw new MediaException(ex.getMessage(), ex);
        }
        console.logExit();
    }

    /**
     * Gets the datasource for a Sip session.
     * @return
     */
    public DataSource getDataSource() throws IncompatibleSourceException,
            IOException {
        DataSource dsTab[] = new DataSource[receivers.size()];
        DataSource ds = null;
        int i = 0;
        for (AVReceiver receiver : receivers) {
            Processor pro = receiver.getProcessor();
            ds = pro.getDataOutput();
            dsTab[i] = ds;
            ++i;
        }

        DataSource mergeDs = Manager.createMergingDataSource(dsTab);
        mergeDs.connect();
        mergeDs.start();
        return mergeDs;
    }

    public void stopRecording() {
        try {
            sink.stop();
            sink.close();
        } catch (Exception ex) {
            console.debug(ex.toString());
        }
    }

    public void start() throws MediaException {
        try {
            console.logEntry();
            sdpFactory = SdpFactory.getInstance();
            isStarted = true;
        } catch (Throwable ex) {
            // also handles SdpException, which is thrown by some SIP implementations
            // (such as older nist-sdp-1.0) and not by others (such as later versions of nist-sdp-1.0 -- not sure why the version number didn't change)
            throw new MediaException(ex);
        } finally {
            console.logExit();
        }

    }

    /**
     * Creates the {@link DataSource} for the given {@link MediaLocator}.
     * @param locator the medi loactor
     * @return created data source.
     * @exception IOException
     *            error creating the data source
     */
    protected DataSource createDataSource(MediaLocator locator)
        throws IOException {
        try {
            console.logEntry();
            try {
                if (console.isDebugEnabled()) {
                    console.debug("Creating datasource for:"
                                  + locator != null
                                  ? locator.toExternalForm()
                                  : "null");
                }
                return Manager.createDataSource(locator);
            } catch (NoDataSourceException ex) {
                throw new IOException("Error creating the data source", ex);
            }
        } finally {
            console.logExit();
        }
    }

    public void openMediaStreams(String sdpData) throws MediaException {
        try {
            console.logEntry();

            if (console.isDebugEnabled()) {
                console.debug("sdpData arg - " + sdpData);
            }
            checkIfStarted();
            final SessionDescription sessionDescription;
            if (sdpData == null) {
                console.error("The SDP data was null! Cannot open " +
                              "a stream withour an SDP Description!");
                throw new MediaException(
                        "The SDP data was null! Cannot open " +
                        "a stream withour an SDP Description!");
            }
            try {
                sessionDescription = sdpFactory.createSessionDescription(
                        sdpData);
            } catch (SdpParseException ex) {
                console.error("Incorrect SDP data!", ex);
                throw new MediaException("Incorrect SDP data!", ex);
            }
            final Collection<MediaDescription> mediaDescriptions;
            try {
                mediaDescriptions = sessionDescription.
                                    getMediaDescriptions(true);
            } catch (SdpException ex) {
                console.error(
                        "Failed to extract media descriptions from provided session description!",
                        ex);
                throw new MediaException(
                        "Failed to extract media descriptions from provided session description!",
                        ex);
            }
            final Connection connection = sessionDescription.getConnection();
            if (connection == null) {
                console.error(
                        "A connection parameter was not present in provided session description");
                throw new MediaException(
                        "A connection parameter was not present in provided session description");
            }
            final String remoteAddress;
            try {
                remoteAddress = connection.getAddress();
            } catch (SdpParseException ex) {
                console.error(
                        "Failed to extract the connection address parameter"
                        + "from privided session description", ex);
                throw new MediaException(
                        "Failed to extract the connection address parameter"
                        + "from privided session description", ex);
            }
            int mediaPort = -1;
            boolean atLeastOneTransmitterStarted = false;
            List<Integer> ports = new java.util.ArrayList<Integer>();
            ArrayList formatSets = new ArrayList();
            for (MediaDescription mediaDescription : mediaDescriptions) {
                final Media media = mediaDescription.getMedia();
                //Media Type
                final String mediaType;
                try {
                    mediaType = media.getMediaType();
                } catch (SdpParseException ex) {
                    console.error(
                            "Failed to extract the media type for one of the provided media descriptions!\n"
                            + "Ignoring description!",
                            ex);
                    fireNonFatalMediaError(new MediaException(
                            "Failed to extract the media type for one of the provided media descriptions!\n"
                            + "Ignoring description!",
                            ex
                                           ));
                    continue;
                }
                //Find ports
                try {
                    mediaPort = media.getMediaPort();
                } catch (SdpParseException ex) {
                    console.error("Failed to extract port for media type ["
                                  + mediaType + "]. Ignoring description!",
                                  ex);
                    fireNonFatalMediaError(new MediaException(
                            "Failed to extract port for media type ["
                            + mediaType + "]. Ignoring description!",
                            ex
                                           ));
                    continue;
                }
                //Find  formats
                Collection<String> sdpFormats;
                try {
                    sdpFormats = media.getMediaFormats(true);
                } catch (SdpParseException ex) {
                    console.error(
                            "Failed to extract media formats for media type ["
                            + mediaType + "]. Ignoring description!",
                            ex);
                    fireNonFatalMediaError(new MediaException(
                            "Failed to extract media formats for media type ["
                            + mediaType + "]. Ignoring description!",
                            ex
                                           ));
                    continue;
                }
                //START TRANSMISSION
                try {
                    if (isMediaTransmittable(mediaType)) {
                        ports.add(new Integer(mediaPort));
                        formatSets.add(extractTransmittableJmfFormats(
                                sdpFormats));
                    } else {
                        //nothing to transmit here so skip setting the flag
                        //bug report and fix - Gary M. Levin - Telecordia
                        continue;
                    }
                } catch (MediaException ex) {
                    console.error(
                            "Could not start a transmitter for media type ["
                            + mediaType + "]\nIgnoring media [" + mediaType +
                            "]!",
                            ex
                            );
                    fireNonFatalMediaError(new MediaException(
                            "Could not start a transmitter for media type ["
                            + mediaType + "]\nIgnoring media [" + mediaType +
                            "]!",
                            ex
                                           ));
                    continue;
                }
                atLeastOneTransmitterStarted = true;
            }
            //startReceiver(remoteAddress);
            //open corrects ports for RTP Session
            createReceiver(remoteAddress, ports);
            softStartReceiver();
            if (!atLeastOneTransmitterStarted) {
                console.error(
                        "Apparently all media descriptions failed to initialise!\n" +
                        "SIP COMMUNICATOR won't be able to open a media stream!");
                throw new MediaException(
                        "Apparently all media descriptions failed to initialise!\n" +
                        "SIP COMMUNICATOR won't be able to open a media stream!");
            } else {
                createTransmitter(remoteAddress, ports, formatSets);
            }
        } finally {
            console.logExit();
        }
    }

    protected void closeProcessor() {
        try {
            console.logEntry();
            if (sndProcessor != null) {
                sndProcessor.stop();
                sndProcessor.close();
                sndProcessor = null;
            }
            if (source != null) {
                source.disconnect();
                source = null;
            }
            if (rcvProcessor != null) {
                rcvProcessor.stop();
                rcvProcessor.close();
                rcvProcessor = null;
            }
            if (sink != null) {
                sink.close();
                sink = null;
            }
        } finally {
            console.logExit();
        }
    }

    public void stop() throws MediaException {
        try {
            console.logEntry();
            //   closeStreams();
            closeProcessor();

        } finally {
            console.logExit();
        }
    }


    public void closeStreams(String sdpData) throws MediaException {
        SessionDescription sessionDescription = null;
        int mediaPort = -1; //remote port
        String remoteAddress = null; //remote address

        try {
            sessionDescription = sdpFactory.createSessionDescription(sdpData);
        } catch (SdpParseException ex) {
            console.error("Incorrect SDP data!", ex);
        }
        Vector mediaDescriptions = null;
        try {
            mediaDescriptions = sessionDescription.getMediaDescriptions(true);
        } catch (SdpException ex) {
            console.error(
                    "Failed to extract media descriptions from provided session description!",
                    ex);

        }
        final Connection connection = sessionDescription.getConnection();
        if (connection == null) {
            console.error(
                    "A connection parameter was not present in provided session description");
            throw new MediaException(
                    "A connection parameter was not present in provided session description");
        }
        try {
            remoteAddress = connection.getAddress();
        } catch (SdpParseException ex) {
            console.error(
                    "Failed to extract the connection address parameter"
                    + "from privided session description", ex);
            throw new MediaException(
                    "Failed to extract the connection address parameter"
                    + "from privided session description", ex);
        }

        //boolean atLeastOneTransmitterStarted = false;
        //ArrayList ports = new ArrayList();
        //ArrayList formatSets = new ArrayList();
        for (int i = 0; i < mediaDescriptions.size(); i++) {
            Media media = ((MediaDescription) mediaDescriptions.get(i)).
                          getMedia();
            //Media Type
            String mediaType = null;
            try {
                mediaType = media.getMediaType();
            } catch (SdpParseException ex) {
                console.error(
                        "Failed to extract the media type for one of the provided media descriptions!\n"
                        + "Ignoring description!",
                        ex);
                fireNonFatalMediaError(new MediaException(
                        "Failed to extract the media type for one of the provided media descriptions!\n"
                        + "Ignoring description!",
                        ex
                                       ));
                continue;
            }
            //Find ports
            try {
                mediaPort = media.getMediaPort();
            } catch (SdpParseException ex) {
                console.error("Failed to extract port for media type ["
                              + mediaType + "]. Ignoring description!",
                              ex);
                fireNonFatalMediaError(new MediaException(
                        "Failed to extract port for media type ["
                        + mediaType + "]. Ignoring description!",
                        ex
                                       ));
                continue;
            }

        }

        //======================
        try {
            //removeAllRtpManagers();
            console.logEntry();
            SessionAddress addToStop = new SessionAddress(InetAddress.getByName(
                    remoteAddress), mediaPort);
            stopTransmitters(addToStop);
            if (transmitters.size() == 0) {
                stopReceiver("localhost");
            }
            firePlayerStopped();
        }

        catch (java.net.UnknownHostException ex) {
            console.debug(ex.toString());
        }

        finally {
            console.logExit();
        }
    }


    protected void createTransmitter(String destHost, List<Integer> ports,
                                    ArrayList formatSets) throws MediaException {
        try {
            console.logEntry();
            final AVTransmitter transmitter =
                new AVTransmitter(sndProcessor, destHost, ports, formatSets);
            transmitter.setMediaManagerCallback(this);
            transmitters.add(transmitter);
        } finally {
            console.logExit();
        }
    }

    protected void stopTransmitters(SessionAddress addToStop) {
        try {
            console.logEntry();
            for (AVTransmitter transmitter : transmitters) {
                try {
                    transmitter.stop(addToStop);
                } //Catch everything that comes out as we wouldn't want
                //Some null pointer prevent us from closing a device and thus
                //render it unusable
                catch (Exception exc) {
                    console.error("Could not close transmitter " + transmitter,
                            exc);
                }
            }
            transmitters.clear();
        } finally {
            console.logExit();
        }
    }

    protected void createReceiver(String remoteAddress,
            List<Integer> ports) throws MediaException {
        try {
            console.logEntry();
            final AVReceiver receiver = new AVReceiver(new String[] {
                                        remoteAddress + "/" + getAudioPort() +
                                        "/1"}, sipProp);
            receiver.setMediaManager(this);
            receiver.initialize2(ports);
            receivers.add(receiver);
        } finally {
            console.logExit();
        }
    }

    protected void stopReceiver(String localAddress) {
        try {
            console.logEntry();
            for (AVReceiver receiver : receivers) {
                try {
                    receiver.close(localAddress);
                    firePlayerStopped();
                } //Catch everything that comes out as we wouldn't want
                //Some null pointer prevent us from closing a device and thus
                //render it unusable
                catch (Exception exc) {
                    console.warn("Could not close receiver " + receiver, exc);
                }
            }
            receivers.clear();
        } finally {
            console.logExit();
        }
    }

    protected void stopReceiver() {
        try {
            console.logEntry();
            for (AVReceiver receiver : receivers) {
                try {
                    receiver.close();
                    firePlayerStopped();
                } //Catch everything that comes out as we wouldn't want
                //Some null pointer prevent us from closing a device and thus
                //render it unusable
                catch (Exception exc) {
                    console.warn("Could not close receiver " + receiver, exc);
                }
            }
            receivers.clear();
        } finally {
            console.logExit();
        }
    }

    /**
     * Only stops the receiver without deleting it. After calling this method
     * one can call softStartReceiver to relauch reception.
     */
    public void softStopReceiver() {
        try {
            console.logEntry();
            for (AVReceiver receiver : receivers) {
                try {
                    receiver.close();
                    firePlayerStopped();
                } //Catch everything that comes out as we wouldn't want
                //Some null pointer prevent us from closing a device and thus
                //render it unusable
                catch (Exception exc) {
                    console.warn("Could not close receiver " + receiver, exc);
                }
            }
        } finally {
            console.logExit();
        }
    }


    /**
     * Starts a receiver that has been stopped using softStopReceiver().
     */
    public void softStartReceiver() {
        try {
            console.logEntry();
            for (AVReceiver receiver : receivers) {
                try {
                    receiver.initialize();
                } //Catch everything that comes out as we wouldn't want
                //Some null pointer prevent us from closing a device and thus
                //render it unusable
                catch (Exception exc) {
                    console.warn("Could not close receiver " + receiver, exc);
                }
            }
        } finally {
            console.logExit();
        }
    }

    void firePlayerStarting(Player player) {
        try {
            console.logEntry();
            final MediaEvent evt = new MediaEvent(player);
            for (MediaListener listener : listeners) {
                listener.playerStarting(evt);
            }
        } finally {
            console.logExit();
        }
    }

    void firePlayerStopped() {
        try {
            console.logEntry();
            for (MediaListener listener : listeners) {
                listener.playerStopped();
            }
        } finally {
            console.logExit();
        }
    }

    void fireNonFatalMediaError(Throwable cause) {
        try {
            console.logEntry();
            final MediaErrorEvent evt = new MediaErrorEvent(cause);
            for (MediaListener listener : listeners) {
                listener.nonFatalMediaErrorOccurred(evt);
            }
        } finally {
            console.logExit();
        }
    }

    public void addMediaListener(MediaListener listener) {
        try {
            console.logEntry();
            listeners.add(listener);
        } finally {
            console.logExit();
        }
    }

    InetAddress getLocalHost() throws MediaException {
        try {
            final String hostAddress = sipProp.getProperty(
                    "net.java.sip.communicator.media.IP_ADDRESS");
            if (console.isDebugEnabled()) {
                console.debug(hostAddress);
            }
            return InetAddress.getByName(hostAddress);
        } catch (Exception ex) {
            throw new MediaException(ex.getMessage(), ex);
        }
    }

    public String generateSdpDescription() throws MediaException {
        try {
            console.logEntry();
            checkIfStarted();
            try {
                SessionDescription sessDescr = sdpFactory.
                                               createSessionDescription();
                //"v=0"
                Version v = sdpFactory.createVersion(0);

                InetSocketAddress publicAudioAddress = addressManager.
                        getPublicAddressFor(getAudioPort());
                InetAddress publicIpAddress = publicAudioAddress.getAddress();
                String addrType = publicIpAddress instanceof Inet6Address ?
                                  "IP6" : "IP4";

                //spaces in the user name mess everything up.
                //bug report - Alessandro Melzi
                Origin o = sdpFactory.createOrigin(
                        System.getProperty("user.name").replace(' ', '_'), 0, 0,
                        "IN", addrType, publicIpAddress.getHostAddress());
                //"s=-"
                SessionName s = sdpFactory.createSessionName("-");
                //c=
                Connection c = sdpFactory.createConnection("IN", addrType,
                        publicIpAddress.getHostAddress());
                //"t=0 0"
                TimeDescription t = sdpFactory.createTimeDescription();
                Vector timeDescs = new Vector();
                timeDescs.add(t);
                //--------Audio media description
                //make sure preferred formats come first
                surfacePreferredEncodings(getReceivableAudioFormats());
                String[] formats = getReceivableAudioFormats();
                MediaDescription am = sdpFactory.createMediaDescription("audio",
                        publicAudioAddress.getPort(), 1, "RTP/AVP", formats);
                if (!isAudioTransmissionSupported()) {
                    am.setAttribute("recvonly", null);
                    //--------Video media description
                }
                surfacePreferredEncodings(getReceivableVideoFormats());
                //"m=video 22222 RTP/AVP 34";
                //String[] vformats = getReceivableVideoFormats();


                Vector mediaDescs = new Vector();

                mediaDescs.add(am);

                sessDescr.setVersion(v);
                sessDescr.setOrigin(o);
                sessDescr.setConnection(c);
                sessDescr.setSessionName(s);
                sessDescr.setTimeDescriptions(timeDescs);
                if (mediaDescs.size() > 0) {
                    sessDescr.setMediaDescriptions(mediaDescs);
                }
                if (console.isDebugEnabled()) {
                    console.debug("Generated SDP - " + sessDescr.toString());
                }
                return sessDescr.toString();
            } catch (SdpException exc) {
                console.error(
                        "An SDP exception occurred while generating local sdp description",
                        exc);
                throw new MediaException(
                        "An SDP exception occurred while generating local sdp description",
                        exc);
            }
        } finally {
            console.logExit();
        }
    }

    public int getAudioPort() {
        return audioPort;
    }


    @Override
    protected void finalize() {
        try {
            console.logEntry();
            try {
                if (source != null) {
                    source.disconnect();
                }
            } catch (Exception exc) {
                console.error("Failed to disconnect data source:" +
                              exc.getMessage());
            }
        } finally {
            console.logExit();
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    protected void checkIfStarted() throws MediaException {
        if (!isStarted()) {
            console.error("The MediaManager has not been properly started! "
                          + "Impossible to continue");
            throw new MediaException(
                    "The MediaManager had not been properly started! "
                    + "Impossible to continue");
        }
    }

    protected boolean isAudioTransmissionSupported() {
        return transmittableAudioFormats.size() > 0;
    }


    protected boolean isMediaTransmittable(String media) {
        if (media.equalsIgnoreCase("audio")
            /*&& isAudioTransmissionSupported()*/) {
            return true;
        } else {
            return false;
        }
    }

    protected String[] getReceivableAudioFormats() {
        return receivableAudioFormats;
    }

    protected String[] getReceivableVideoFormats() {
        return receivableVideoFormats;
    }

    protected String findCorrespondingJmfFormat(String sdpFormatStr) {
        int sdpFormat = -1;
        try {
            sdpFormat = Integer.parseInt(sdpFormatStr);
        } catch (NumberFormatException ex) {
            return null;
        }
        switch (sdpFormat) {
        case SdpConstants.PCMU:
            return AudioFormat.ULAW_RTP;
        case SdpConstants.GSM:
            return AudioFormat.GSM_RTP;
        case SdpConstants.G723:
            return AudioFormat.G723_RTP;
        case SdpConstants.DVI4_8000:
            return AudioFormat.DVI_RTP;
        case SdpConstants.DVI4_16000:
            return AudioFormat.DVI_RTP;
        case SdpConstants.PCMA:
            return AudioFormat.ALAW;
        case SdpConstants.G728:
            return AudioFormat.G728_RTP;
        case SdpConstants.G729:
            return AudioFormat.G729_RTP;
        case SdpConstants.H263:
            return VideoFormat.H263_RTP;
        case SdpConstants.JPEG:
            return VideoFormat.JPEG_RTP;
        case SdpConstants.H261:
            return VideoFormat.H261_RTP;
        default:
            return null;
        }
    }

    protected String findCorrespondingSdpFormat(String jmfFormat) {
        if (jmfFormat == null) {
            return null;
        } else if (jmfFormat.equals(AudioFormat.ULAW_RTP)) {
            return Integer.toString(SdpConstants.PCMU);
        } else if (jmfFormat.equals(AudioFormat.GSM_RTP)) {
            return Integer.toString(SdpConstants.GSM);
        } else if (jmfFormat.equals(AudioFormat.G723_RTP)) {
            return Integer.toString(SdpConstants.G723);
        } else if (jmfFormat.equals(AudioFormat.DVI_RTP)) {
            return Integer.toString(SdpConstants.DVI4_8000);
        } else if (jmfFormat.equals(AudioFormat.DVI_RTP)) {
            return Integer.toString(SdpConstants.DVI4_16000);
        } else if (jmfFormat.equals(AudioFormat.ALAW)) {
            return Integer.toString(SdpConstants.PCMA);
        } else if (jmfFormat.equals(AudioFormat.G728_RTP)) {
            return Integer.toString(SdpConstants.G728);
        } else if (jmfFormat.equals(AudioFormat.G729_RTP)) {
            return Integer.toString(SdpConstants.G729);
        } else if (jmfFormat.equals(VideoFormat.H263_RTP)) {
            return Integer.toString(SdpConstants.H263);
        } else if (jmfFormat.equals(VideoFormat.JPEG_RTP)) {
            return Integer.toString(SdpConstants.JPEG);
        } else if (jmfFormat.equals(VideoFormat.H261_RTP)) {
            return Integer.toString(SdpConstants.H261);
        } else {
            return null;
        }
    }

    /**
     * @param sdpFormats
     * @return
     * @throws MediaException
     */
    protected Collection<String> extractTransmittableJmfFormats(
            Collection<String> sdpFormats) throws MediaException {
        try {
            console.logEntry();
            Collection<String> jmfFormats = new java.util.ArrayList<String>();
            for (String sdpFormat : sdpFormats) {
                final String jmfFormat = findCorrespondingJmfFormat(sdpFormat);
                if (jmfFormat != null) {
                    jmfFormats.add(jmfFormat);
                }
            }
            if (jmfFormats.size() == 0) {
                throw new MediaException(
                        "None of the supplied sdp formats for is supported by SIP COMMUNICATOR");
            }
            return jmfFormats;
        } finally {
            console.logExit();
        }
    }

    //This is the data source that we'll be using to transmit
    //let's see what can it do


    protected void initSndProcessor(DataSource ds) throws MediaException {
        try {
            console.logEntry();
            try {
                try {
                    ds.connect();
                } catch (NullPointerException ex) {
                    //Thrown when operation is not supported by the OS
                    console.error(
                            "An internal error occurred while"
                            + " trying to connec to to datasource!", ex);
                    throw new MediaException(
                            "An internal error occurred while"
                            + " trying to connec to to datasource!", ex);
                }
                sndProcessor = Manager.createProcessor(ds);
                sndProcessor.configure();
                boolean success =
                    procUtility.waitForState(sndProcessor, Processor.Configured);
                if (!success) {
                    throw new MediaException(
                            "Media manager could not create a processor\n"
                            + "for the specified data source");
                }
            } catch (NoProcessorException ex) {
                console.error(
                        "Media manager could not create a processor\n"
                        + "for the specified data source",
                        ex
                        );
                throw new MediaException(
                        "Media manager could not create a processor\n"
                        + "for the specified data source", ex);
            } catch (IOException ex) {
                console.error(
                        "Media manager could not connect "
                        + "to the specified data source",
                        ex);
                throw new MediaException("Media manager could not connect "
                                         + "to the specified data source", ex);
            }
            sndProcessor.setContentDescriptor(new ContentDescriptor(
                    ContentDescriptor.RAW_RTP));
            TrackControl[] trackControls = sndProcessor.getTrackControls();

            if (console.isDebugEnabled()) {
                console.debug("We will be able to transmit in:");
            }
            for (int i = 0; i < trackControls.length; i++) {
                Format[] formats = trackControls[i].getSupportedFormats();
                for (int j = 0; j < formats.length; j++) {
                    Format format = formats[j];
                    String encoding = format.getEncoding();
                    if (format instanceof AudioFormat) {
                        String sdp = findCorrespondingSdpFormat(encoding);
                        if (sdp != null &&
                            !transmittableAudioFormats.contains(sdp)) {
                            if (console.isDebugEnabled()) {
                                console.debug("Audio[" + (j + 1) + "]=" +
                                              encoding + "; sdp=" + sdp);
                            }
                            transmittableAudioFormats.add(sdp);
                        }
                    }

                }
            }
        } finally {
            console.logExit();
        }
    }

    protected void initRcvProcessor(DataSource ds) throws MediaException {
        try {
            console.logEntry();
            try {
                try {
                    ds.connect();
                } catch (NullPointerException ex) {
                    //Thrown when operation is not supported by the OS
                    console.error(
                            "An internal error occurred while"
                            + " trying to connec to to datasource!", ex);
                    throw new MediaException(
                            "An internal error occurred while"
                            + " trying to connec to to datasource!", ex);
                }
                rcvProcessor = Manager.createProcessor(ds);
                rcvProcessor.configure();
                boolean success =
                    procUtility.waitForState(rcvProcessor, Processor.Configured);
                if (!success) {
                    throw new MediaException(
                            "Media manager could not create a processor\n"
                            + "for the specified data source");
                }
            } catch (NoProcessorException ex) {
                console.error(
                        "Media manager could not create a processor\n"
                        + "for the specified data source",
                        ex
                        );
                throw new MediaException(
                        "Media manager could not create a processor\n"
                        + "for the specified data source", ex);
            } catch (IOException ex) {
                console.error(
                        "Media manager could not connect "
                        + "to the specified data source",
                        ex);
                throw new MediaException("Media manager could not connect "
                                         + "to the specified data source", ex);
            }
            rcvProcessor.setContentDescriptor(new ContentDescriptor(
                    ContentDescriptor.RAW_RTP));
            TrackControl[] trackControls = sndProcessor.getTrackControls();

            if (console.isDebugEnabled()) {
                console.debug("We will be able to receive in:");
            }
            for (int i = 0; i < trackControls.length; i++) {
                Format[] formats = trackControls[i].getSupportedFormats();
                for (int j = 0; j < formats.length; j++) {
                    Format format = formats[j];
                    String encoding = format.getEncoding();
                    if (format instanceof AudioFormat) {
                        String sdp = findCorrespondingSdpFormat(encoding);
                        if (sdp != null &&
                            !transmittableAudioFormats.contains(sdp)) {
                            if (console.isDebugEnabled()) {
                                console.debug("Audio[" + (j + 1) + "]=" +
                                              encoding + "; sdp=" + sdp);
                            }
//                            transmittableAudioFormats.add(sdp);
                        }
                    }

                }
            }
        } finally {
            console.logExit();
        }
    }

    /**
     * Returns a cached instance of an RtpManager bound on the specified local
     * address. If no such instance exists null is returned.
     * @param localAddress the address where the rtp manager must be bound locally.
     * @return an rtp manager bound on the specified address or null if such an
     * instance was not found.
     */
    synchronized RTPManager getRtpManager(SessionAddress localAddress) {
        return (RTPManager) activeRtpManagers.get(localAddress);
    }

    /**
     * Maps the specified rtp manager against the specified local address so
     * that it may be later retrieved in case someone wants to operate
     * (transmit/receive) on the same port.
     * @param localAddress the address where the rtp manager is bound
     * @param rtpManager the rtp manager itself
     */
    synchronized void putRtpManager(SessionAddress localAddress,
                                    RTPManager rtpManager) {
        activeRtpManagers.put(localAddress, rtpManager);
    }

    /**
     * Removes all rtp managers from the rtp manager cache.
     */
    synchronized void removeAllRtpManagers() {
        activeRtpManagers.clear();
    }


    /**
     * Moves formats with the specified encoding to the top of the array list
     * so that they are the ones chosen for transmission (if supported by the
     * remote party) (feature request by Vince Fourcade)
     */
    protected void surfacePreferredEncodings(String[] formats) {
        try {
            console.logEntry();
            String preferredAudioEncoding = sipProp.getProperty(
                    "net.java.sip.communicator.media.PREFERRED_AUDIO_ENCODING");
            String preferredVideoEncoding = sipProp.getProperty(
                    "sipProp.java.sip.communicator.media.PREFERRED_VIDEO_ENCODING");
            if (preferredAudioEncoding == null && preferredVideoEncoding == null) {
                return;
            }
            for (int i = 0; i < formats.length; i++) {
                String encoding = formats[i];
                if ((preferredAudioEncoding != null
                     && encoding.equalsIgnoreCase(preferredAudioEncoding))
                    || (preferredVideoEncoding != null
                        && encoding.equalsIgnoreCase(preferredVideoEncoding))) {
                    formats[i] = formats[0];
                    formats[0] = encoding;
                    if (console.isDebugEnabled()) {
                        console.debug("Encoding  [" +
                                      findCorrespondingJmfFormat(encoding) +
                                      "] is set as preferred.");
                    }
                    break;
                }
            }
        } finally {
            console.logExit();
        }
    }
}
