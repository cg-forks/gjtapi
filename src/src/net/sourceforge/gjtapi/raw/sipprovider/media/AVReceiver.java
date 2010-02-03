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
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.Player;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.StartEvent;
import javax.media.control.BufferControl;
import javax.media.control.TrackControl;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.SendStreamEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;

import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
/**
 * AVReceiver to receive RTP transmission using the new RTP API.
 * 
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov (http://www.emcho.com)
 * @version 1.1
 */
class AVReceiver implements ReceiveStreamListener, SessionListener,
    ControllerListener, SendStreamListener {
    private static Console console = Console.getConsole(AVReceiver.class);
    private net.sourceforge.gjtapi.raw.sipprovider.media.MediaManager mediaManager;
    private String sessions[] = null;
    private RTPManager mgrs[] = null;
    boolean dataReceived = false;
    //private Object dataSync = new Object();

    private int bindRetries = 3;
    private final Properties sipProp;
    private Processor processor;
    public DataSource ds;
    
    /** Utility to wait for processor state changes. */
    private final ProcessorUtility procUtility = new ProcessorUtility("AVReceiver");
    
    public AVReceiver(String sessions[],Properties sipProp)
    {
        this.sessions = sessions;
        this.sipProp = new Properties() ;
        this.sipProp.putAll(sipProp);
        String retries = null;


        if((retries = sipProp.getProperty("net.java.sip.communicator.media.RECEIVER_BIND_RETRIES")) != null)
        try
        {
            bindRetries = Integer.valueOf(retries).intValue();
        }
        catch (NumberFormatException ex)
        {
            console.error(retries + " is not a valid number. ignoring property", ex);
        }
    }

    void setMediaManager(MediaManager mManager)
    {
        this.mediaManager = mManager;
    }

    protected boolean initialize()
    {
        try
        {
            console.logEntry();
            InetAddress ipAddr;
            SessionAddress localAddr = new SessionAddress();
            SessionAddress destAddr;
            mgrs = new RTPManager[sessions.length];
            SessionLabel session;
            // Open the RTP sessions.
            for (int i = 0; i < sessions.length; i++)
            {
                // Parse the session addresses.
                try
                {
                    session = new SessionLabel(sessions[i]);
                }
                catch (IllegalArgumentException e)
                {
                    console.error(
                    "Failed to parse the session address given: "
                    + sessions[i]);
                    console.logExit();
                    return false;
                }
                if (console.isDebugEnabled())
                {
                    console.debug(
                    " Start listening for RTP @ addr: "
                    + session.addr + " port: " + session.port
                    + " ttl: " + session.ttl);
                }
                //rtpmanager avec localost + port local
                mgrs[i] = mediaManager.getRtpManager(new SessionAddress(mediaManager.getLocalHost(),session.port));
                if(mgrs[i] == null)
                {
                    mgrs[i] = RTPManager.newInstance();
                    mediaManager.putRtpManager(new SessionAddress(mediaManager.getLocalHost(),session.port), mgrs[i]);
                }
                mgrs[i].addSessionListener(this);
                mgrs[i].addReceiveStreamListener(this);
                mgrs[i].addSendStreamListener(this);
                ipAddr = InetAddress.getByName(session.addr);
                int tries = 0;
                while (tries++ < bindRetries)
                {
                    if (ipAddr.isMulticastAddress())
                    {
                        // local and remote address pairs are identical:
                        localAddr = new SessionAddress(ipAddr,
                        session.port,
                        session.ttl);
                        destAddr = new SessionAddress(ipAddr,
                        session.port,
                        session.ttl);
                    }
                    else
                    {
                        localAddr = new SessionAddress(mediaManager.getLocalHost(),session.port);
                        destAddr = new SessionAddress(ipAddr, session.port);
                    }
                    try
                    {
                        mgrs[i].initialize(localAddr);
                    }
                    catch (Exception exc)
                    {
                        if (tries < bindRetries)
                        {
                            continue;
                        }
                        console.error(
                        "Could not initialize rtp manager!",exc);
                        return false;
                    }
                    // You can try out some other buffer size to see
                    // if you can get better smoothness.
                    BufferControl bc = (BufferControl) mgrs[i].getControl("javax.media.control.BufferControl");
                    if (bc != null)
                    {
                        bc.setBufferLength(350);
                    }

                    mgrs[i].addTarget(destAddr);//ajout de l'address
                    break; //port retries
                } //port retries
            }
        }
        catch (Exception e)
        {
            console.error("Cannot create the RTP Session: ", e);
            console.logExit();
            return false;
        }
        console.logExit();
        return true;
    }

    /**
   *
   * @param remoteAddr String
   * @param ports ArrayList
   * @throws MediaException
   */
  protected void initialize2(List<Integer> ports) throws
          MediaException {

      console.logEntry();

      mgrs = new RTPManager[sessions.length];

      for (int i = 0; i < sessions.length; i++) {
          SessionLabel session = new SessionLabel(sessions[0]);
          final int localPort = session.port;
          final String remoteAddress = session.addr;
          final Integer port = ports.get(0);
          final int remotePort = port.intValue();
          if (console.isDebugEnabled()) {
              console.debug("IP localHostAVRECV: " + mediaManager.getLocalHost()
                      + " localPort: " + localPort);
              console.debug("IP remoteHostAVRECV: " + remoteAddress
                      + " remotePort: " +  remotePort);
          }
          InetSocketAddress dialogLocalAddr = new InetSocketAddress(
          mediaManager.getLocalHost(), localPort);

          InetAddress rtpLocalAddress = dialogLocalAddr.getAddress();
          SessionAddress local = new SessionAddress(rtpLocalAddress,
                  localPort);

          final SessionAddress address = new SessionAddress(
                  mediaManager.getLocalHost(), localPort);
          mgrs[i] = mediaManager.getRtpManager(address);
          if (mgrs[i] == null) {
              mgrs[i] = RTPManager.newInstance();
              mediaManager.putRtpManager(new SessionAddress(mediaManager.
                      getLocalHost(), localPort), mgrs[i]);
          }

          mgrs[i].addSessionListener(this);
          mgrs[i].addReceiveStreamListener(this);

          try {
              mgrs[i].initialize(local);
          } catch (IOException ex) {
              throw new MediaException("Error initializing the RTP manager!",
                      ex);
          } catch (InvalidSessionAddressException ex) {
              throw new MediaException("Error initializing the RTP manager!",
                      ex);
          }

          // Now get the real local ports from the manager
          //RTPSessionMgr smgr = (RTPSessionMgr) mgrs[i];
          //int _controlPort = smgr.getLocalSessionAddress().getControlPort();
          //int _dataPort = smgr.getLocalSessionAddress().getDataPort();

          InetSocketAddress dialogRemoteAddr = new InetSocketAddress(
                  remoteAddress, remotePort);

          InetAddress bogusAddress = dialogRemoteAddr.getAddress();
          InetAddress rtpRemoteAddress = null;
          try {
              rtpRemoteAddress = InetAddress.getByAddress(InetAddress.
                      getLocalHost().getHostName(),
                      bogusAddress.getAddress());
          } catch (UnknownHostException ex) {
              throw new MediaException(ex.getMessage(), ex);
          }
          SessionAddress sessionAddress = new SessionAddress(rtpRemoteAddress,
                  remotePort);

          //set buffer parameters
          BufferControl bc = (BufferControl) mgrs[i].getControl(
                  "javax.media.control.BufferControl");
          if (bc != null) {
              bc.setBufferLength(4098);
          }
          //add target to manager
          try {
              mgrs[i].addTarget(sessionAddress);
          } catch (IOException ex) {
              throw new MediaException(
                      "Error adding the target to the RTP Manager!", ex);
          } catch (InvalidSessionAddressException ex) {
              throw new MediaException(
                      "Error adding the target to the RTP Manager!", ex);
          }
          console.logExit();
      }
  }


    public boolean isDone() {
        return false;
    }

    /**
     * Close the players and the session managers.
     */
    protected void close(String LocalAddress)
    {
        try {
            console.logEntry();
            // close the RTP session.
            for (int i = 0; i < mgrs.length; i++) {
                if (mgrs[i] != null) {
                    if (console.isDebugEnabled()) {
                        console.debug("Stopped mgr " +  (i + 1));
                    }
                    try {
                        InetAddress inetAdd =  InetAddress.getByName(LocalAddress);
                        SessionAddress sessionAddress = new SessionAddress(inetAdd, mediaManager.getAudioPort());
                        mgrs[i].removeTarget(sessionAddress ,"bye");
                        mgrs[i].dispose();
                        mgrs[i] = null;
                    } catch (java.net.UnknownHostException ex) {
                        console.warn(ex.toString(), ex);
                    } catch (javax.media.rtp.InvalidSessionAddressException ex) {
                        console.warn(ex.toString(), ex);
                    }
                }
            }
        } finally {
            console.logExit();
        }
    }

    protected void close() {
        try {
            console.logEntry();
            // close the RTP session.
            for (int i = 0; i < mgrs.length; i++) {
                if (mgrs[i] != null) {
                    if (console.isDebugEnabled()) {
                        console.debug("Stopped mgr " + (i + 1));
                    }
                    mgrs[i].removeTargets("Closing session from AVReceiver");
                    mgrs[i].dispose();
                    mgrs[i] = null;
                }
            }
        } finally
        {
            console.logExit();
        }
    }
    /**
     * SessionListener.
     */
    public synchronized void update(SessionEvent evt)
    {
        try
        {
            console.logEntry();
            if (evt instanceof NewParticipantEvent)
            {
                Participant p = ( (NewParticipantEvent) evt).getParticipant();
                if (console.isDebugEnabled())
                {
                    console.debug("A new participant had just joined: "
                    + p.getCNAME());
                }
            }
            else
            {
                if (console.isDebugEnabled())
                {
                    console.debug(
                    "Received a the following JMF Session event - "
                    + "evt.getClass().getName()");
                }
            }
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * ReceiveStreamListener
     */
    public synchronized void update(ReceiveStreamEvent evt)
    {
        try
        {
            console.logEntry();

            Participant participant = evt.getParticipant(); // could be null.
            ReceiveStream stream = evt.getReceiveStream(); // could be null.

            if (evt instanceof NewReceiveStreamEvent)
            {
                try
                {
                    stream = evt.getReceiveStream();
                    ds = stream.getDataSource();
                    // Find out the formats.
                    RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
                    if (console.isDebugEnabled())
                    {
                        if (ctl != null)
                        {
                            console.debug("Recevied new RTP stream: "
                            + ctl.getFormat());
                        }
                        else
                        {
                            console.debug("Recevied new RTP stream");
                        }
                    }

                    processor = Manager.createProcessor(ds);
                    this.configureProcessor(processor);

                    processor.realize();


                }
                catch (Exception e)
                {
                    console.error("NewReceiveStreamEvent exception ", e);
                    return;
                }
            }
            else if (evt instanceof StreamMappedEvent)
            {
                if (stream != null && stream.getDataSource() != null)
                {
                    ds = stream.getDataSource();
                    // Find out the formats.
                    RTPControl ctl = (RTPControl) ds.getControl(
                    "javax.media.rtp.RTPControl");
                    if (console.isDebugEnabled())
                    {
                        String msg = "The previously unidentified stream ";
                        if (ctl != null)
                        {
                            msg += ctl.getFormat();
                        }
                        msg += " had now been identified as sent by: "
                        + participant.getCNAME();
                        console.debug(msg);
                    }
                }
            }
            else if (evt instanceof ByeEvent)
            {
                console.debug("Got \"bye\" from: " + participant.getCNAME());
            }
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * ControllerListener for the Players.
     */
    public synchronized void controllerUpdate(ControllerEvent ce)
    {
        try
        {
            console.logEntry();
            Player p = (Player) ce.getSourceController();
            if (p == null)
            {
                return;
            }
            // Get this when the internal players are realized.
            if (ce instanceof RealizeCompleteEvent)
            {
                console.debug("A player was realized and will be started.");
                p.start();
            }
            if (ce instanceof StartEvent)
            {
                console.debug("Received a StartEvent");
                mediaManager.firePlayerStarting(p);
            }
            if (ce instanceof ControllerErrorEvent)
            {
                console.error(
                "The following error was reported while starting a player"
                + ce);
            }
            if (ce instanceof ControllerClosedEvent)
            {
                console.debug("Received a ControllerClosedEvent");
                mediaManager.firePlayerStopped();
            }
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * A utility class to parse the session addresses.
     */
    class SessionLabel
    {
        public String addr = null;
        public int port;
        public int ttl = 1;
        private final Console console = Console.getConsole(SessionLabel.class);
        SessionLabel(String session) throws IllegalArgumentException
        {
            try
            {
                console.logEntry();
                int off;
                String portStr = null, ttlStr = null;
                if (session != null && session.length() > 0)
                {
                    while (session.length() > 1 && session.charAt(0) == '/')
                    {
                        session = session.substring(1);
                        // Now see if there's a addr specified.
                    }
                    off = session.indexOf('/');
                    if (off == -1)
                    {
                        if (!session.equals(""))
                        {
                            addr = session;
                        }
                    }
                    else
                    {
                        addr = session.substring(0, off);
                        session = session.substring(off + 1);
                        // Now see if there's a port specified
                        off = session.indexOf('/');
                        if (off == -1)
                        {
                            if (!session.equals(""))
                            {
                                portStr = session;
                            }
                        }
                        else
                        {
                            portStr = session.substring(0, off);
                            session = session.substring(off + 1);
                            // Now see if there's a ttl specified
                            off = session.indexOf('/');
                            if (off == -1)
                            {
                                if (!session.equals(""))
                                {
                                    ttlStr = session;
                                }
                            }
                            else
                            {
                                ttlStr = session.substring(0, off);
                            }
                        }
                    }
                }
                if (addr == null)
                {
                    throw new IllegalArgumentException();
                }
                if (portStr != null)
                {

                    Integer integer = Integer.valueOf(portStr);
                    if (integer != null)
                    {
                        port = integer.intValue();
                    }


                }
                else
                {
                    throw new IllegalArgumentException();
                }
                if (ttlStr != null)
                {
                    try
                    {
                        Integer integer = Integer.valueOf(ttlStr);
                        if (integer != null)
                        {
                            ttl = integer.intValue();
                        }
                    }
                    catch (Throwable t)
                    {
                        throw new IllegalArgumentException();
                    }
                }
            }
            finally
            {
                console.logExit();
            }
        }
    }

    public void update(SendStreamEvent event)
    {
        console.debug(
        "received the following JMF Session event - "
        + event.getClass().getName());
    }

    public Processor getProcessor()
    {
        return processor;
    }


    protected void configureProcessor(Processor p) throws MediaException {
        processor = p;
        try {
            console.logEntry();
            if (processor == null) {
                console.error("Processor is null.");
                throw new MediaException("Processor is null.");
            }
            // Wait for the processor to configure
//            processor.addControllerListener(new StateListener());
            boolean result = procUtility.waitForState(processor, 
                    Processor.Configured);
            if (result == false) {
                console.error("Couldn't configure processor");
                throw new MediaException("Couldn't configure processor");
            }
            // Get the tracks from the processor
            TrackControl[] tracks = processor.getTrackControls();

            if (console.isDebugEnabled()) {
                console.debug("-----" + tracks[0].getFormat());
            }
            int t = tracks[0].getSupportedFormats().length;
            if (console.isDebugEnabled()) {
                for(int i = 0; i < t; i++) {
                    console.debug("supported format: "
                            + tracks[0].getSupportedFormats()[i]);
                }
            }

            // Do we have at least one track?
            if (tracks == null || tracks.length < 1) {
                console.error("Couldn't find tracks in processor");
                throw new MediaException("Couldn't find tracks in processor");
            }
            // Set the output content descriptor to RAW_RTP
            // This will limit the supported formats reported from
            // Track.getSupportedFormats to only valid RTP formats.
            //   ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
            //kk

            FileTypeDescriptor fd = new FileTypeDescriptor(FileTypeDescriptor.BASIC_AUDIO);

            processor.setContentDescriptor(fd);


            //
            // processor.setContentDescriptor(new AudioFileFormat(AudioFileFormat.Type.WAVE));


        } finally {
            console.logExit();
        }
    }

    protected int findFirstMatchingFormat(Format[] hayStack, ArrayList needles)
    {
        try
        {
            console.logEntry();
            if (hayStack == null || needles == null)
            {
                return -1;
            }
            for (int j = 0; j < needles.size(); j++)
            {
                ArrayList currentSet = (ArrayList) needles.get(j);
                for (int k = 0; k < currentSet.size(); k++)
                {
                    for (int i = 0; i < hayStack.length; i++)
                    {
                        if (hayStack[i].getEncoding().equals( currentSet.get(k)))
                        {
                            return i;
                        }
                    }
                }
            }
            return -1;
        }
        finally
        {
            console.logExit();
        }
    }
    /****************************************************************
     * Inner Classes
     ****************************************************************/
    class StateListener
    implements ControllerListener
    {
        public void controllerUpdate(ControllerEvent ce)
        {
            try
            {
                console.logEntry();
                if (console.isDebugEnabled()) {
                    console.debug(ce.toString());
                }
                //stop the stream at the end of the audio file
                if (ce instanceof EndOfMediaEvent)
                {
                    try
                    {
                        processor.stop();

                    }catch(Exception ex)
                    {
                        console.debug(ex.toString());
                    }
                }
            }
            finally
            {
                console.logExit();
            }
        }
    }
}
