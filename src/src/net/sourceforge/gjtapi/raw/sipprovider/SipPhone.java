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
 *//*
 * SipProvider.java
 *
 * Created on November 18, 2003, 2:18 PM
 */

package net.sourceforge.gjtapi.raw.sipprovider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.telephony.ConnectionEvent;
import javax.telephony.Event;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.media.MediaResourceException;

import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.RawStateException;
import net.sourceforge.gjtapi.ResourceFinder;
import net.sourceforge.gjtapi.TermData;
import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
import net.sourceforge.gjtapi.raw.sipprovider.common.NetworkAddressManager;
import net.sourceforge.gjtapi.raw.sipprovider.media.MediaException;
import net.sourceforge.gjtapi.raw.sipprovider.media.MediaManager;
import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaErrorEvent;
import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaEvent;
import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaListener;
import net.sourceforge.gjtapi.raw.sipprovider.sip.CommunicationsException;
import net.sourceforge.gjtapi.raw.sipprovider.sip.SipManager;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallRejectedEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallStateEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CommunicationsErrorEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CommunicationsListener;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.MessageEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.RegistrationEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.UnknownMessageEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.security.SecurityAuthority;
import net.sourceforge.gjtapi.raw.sipprovider.sip.security.UserCredentials;

// import com.sun.jndi.cosnaming.IiopUrl.Address;
/**
 * 
 * @author root
 */
public class SipPhone
        implements MediaListener, CommunicationsListener, SecurityAuthority,
        net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallListener {
    private static Console console = Console.getConsole(SipPhone.class);
    private TermData terminal;
    protected MediaManager mediaManager;
    protected SipManager sipManager;
    private final Collection ids;
    private final String address;
    private final SipProvider sipProvider;
    private final String password;
    private final Map settings;

    /**
     * Constructs a new object.
     * 
     * @param propResource
     *                Name of the resource containing properties for this phone
     * @param finder
     *                utility to resolve resource
     * @param sipProvider
     *                the provider
     * @throws IOException
     *                 Error loading the properties.
     */
    public SipPhone(String propResource, ResourceFinder finder,
            SipProvider sipProvider, NetworkAddressManager manager)
            throws IOException {
        Properties sipProp = new Properties();
        InputStream in = null;
        try {
            in = finder.findResource(propResource);
            sipProp.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        settings = new java.util.HashMap();
        settings.putAll(sipProp);
        password = sipProp.getProperty
            ("net.java.sip.communicator.sip.PASSWORD");
        ids = new java.util.ArrayList();
        mediaManager = new MediaManager(sipProp, manager);

        this.sipProvider = sipProvider;
        sipManager = new SipManager(sipProp, manager);
        this.launch();
        address = "sip:" + sipManager.getLocalUser() + "@"
                + sipManager.getLocalHostAddress();
    }

    public String getAddress() {
        return address;
    }

    // sip call control
    // section-------------------------------------------------------------------------
    public void createCall(CallId id, String address, String term, String dest)
            throws ResourceUnavailableException, PrivilegeViolationException,
            InvalidPartyException, InvalidArgumentException, RawStateException,
            MethodNotSupportedException {
        console.logEntry();
        console.debug("id = " + id);

        try {

            console.debug("trentative de connection  a " + address);
            // CREATION D'UN CALL (SIP)
            net.sourceforge.gjtapi.raw.sipprovider.sip.Call call = sipManager
                    .establishCall(dest, mediaManager.generateSdpDescription());

            SipCallId sipCallId = (SipCallId) (id);
            sipCallId.setSipId(call.getID());
            call.setCallId(sipCallId); // hook the call up to it's id
            ids.add(new ListIdElement(id, call.getID(), term, dest));

            call.addStateChangeListener(this);

        } catch (net.sourceforge.gjtapi.raw.sipprovider.media.MediaException ex) {
            console.debug(ex.toString());
        } catch (net.sourceforge.gjtapi.raw.sipprovider.sip.CommunicationsException ex) {
            console.debug(ex.toString());
        }

    }

    public void answerCall(CallId callId, String address, String term)
            throws ResourceUnavailableException, PrivilegeViolationException,
            RawStateException, MethodNotSupportedException {
        console.logEntry();
        try {
            ListIdElement listId = this.getElementIdListByJtapiId(callId);
            sipManager.answerCall(listId.getSipId(), mediaManager
                    .generateSdpDescription());
        } catch (net.sourceforge.gjtapi.raw.sipprovider.media.MediaException ex) {
            console.debug(ex.toString());
        } catch (net.sourceforge.gjtapi.raw.sipprovider.sip.CommunicationsException ex) {
            console.debug(ex.toString());
        }

    }

    public void sipHangup(CallId callId) {
        try {
            console.logEntry();
            ListIdElement lI = this.getElementIdListByJtapiId(callId);
            sipManager.endCall(lI.getSipId());

        } catch (CommunicationsException exc) {
            console.warn("Could not properly terminate call!\n"
                    + "(This is not a fatal error)", exc);
        } finally {
            console.logExit();
        }
    }

    // ======================= CALL LISTENER ==============================
    // callback du sipManager
    public void callStateChanged(CallStateEvent evt) {
        console.logEntry();
        final String oldState = evt.getOldState();
        final String newState = evt.getNewState();
        if (console.isDebugEnabled()) {
            console.debug("new state: " + newState + ", old state: "
                    + oldState);
        }
        try {
            net.sourceforge.gjtapi.raw.sipprovider.sip.Call call = evt
                    .getSourceCall();
            int sipId = evt.getSourceCall().getID();
            ListIdElement el = getElementIdListBySipId(sipId);

            if (newState == net.sourceforge.gjtapi.raw.sipprovider.sip.Call.ALERTING) {
                if (console.isDebugEnabled()) {
                    console.debug("remote address = " + el.getAddress());
                }

                sipProvider.sipTerminalConnectionRinging(el.getJtapiId(), el
                        .getAddress(), el.getTerminal(),
                        ConnectionEvent.CAUSE_NORMAL);
                sipProvider.sipConnectionInProgress(el.getJtapiId(), el
                        .getAddress(), Event.CAUSE_NORMAL);
                sipProvider.sipConnectionAlerting(el.getJtapiId(), el
                        .getAddress(), ConnectionEvent.CAUSE_NORMAL);

            }
            if (newState == net.sourceforge.gjtapi.raw.sipprovider.sip.Call.RINGING) {
                if (oldState == net.sourceforge.gjtapi.raw.sipprovider.sip.Call.DIALING) {
                    console.debug("remote address = " + el.getAddress());

                    sipProvider.sipTerminalConnectionCreated(el.getJtapiId(),
                            el.getAddress(), "remote",
                            ConnectionEvent.CAUSE_NORMAL);
                    sipProvider.sipConnectionInProgress(el.getJtapiId(), el
                            .getAddress(), Event.CAUSE_NORMAL);
                    sipProvider.sipConnectionAlerting(el.getJtapiId(), el
                            .getAddress(), ConnectionEvent.CAUSE_NORMAL);

                }

            }
            if (newState == net.sourceforge.gjtapi.raw.sipprovider.sip.Call.CONNECTED) {
                console.debug("connected");

                // sipProvider.sipConnectionConnected(el.getJtapiId(),
                // el.getAddress(), ConnectionEvent.CAUSE_NORMAL);
                sipProvider.sipConnectionConnected(el.getJtapiId(), el
                        .getAddress(), ConnectionEvent.CAUSE_NORMAL);
                sipProvider.sipCallActive(el.getJtapiId(), Event.CAUSE_NORMAL);
                try {
                    final String sdp = call.getRemoteSdpDescription();
                    mediaManager.openMediaStreams(sdp);
                } catch (net.sourceforge.gjtapi.raw.sipprovider.media.MediaException ex) {
                    console.warn(ex.toString(), ex);
                }

            } else if (newState == net.sourceforge.gjtapi.raw.sipprovider.sip.Call.DISCONNECTED) {
                console.debug("disconnected");
                // listener.connectionSuspended(el.getJtapiId(),
                // el.getAddress(), ConnectionEvent.CAUSE_NORMAL);
                try {
                    if (call.getRemoteSdpDescription() != null) {
                        mediaManager.closeStreams(call
                                .getRemoteSdpDescription());
                    }
                } catch (MediaException ex) {
                    console.error(
                            "The following exception occurred while trying to open media connection:\n"
                                    + ex.getMessage(), ex);
                }
                console.debug("disconnected " + call.getRemoteName());
                sipProvider.sipConnectionDisconnected(el.getJtapiId(), el
                        .getAddress(), ConnectionEvent.CAUSE_NORMAL);
            }
        } finally {
            console.logExit();
        }
    }

    public void messageReceived(MessageEvent evt) {
        /*
         * try { console.logEntry(); String fromAddress = evt.getFromAddress();
         * String fromName = evt.getFromName(); String messageBody =
         * evt.getBody(); console.showDetailedMsg( "Incoming MESSAGE", "You
         * received a MESSAGE\n" + "From: " + fromName + "\n" + "Address: " +
         * fromAddress + "\n" + "Message: " + messageBody + "\n"); } finally {
         * console.logExit(); }
         */
    }

    public void nonFatalMediaErrorOccurred(MediaErrorEvent evt) {
        console.logEntry();
    }

    /**
     * Returns a Credentials object associated with the specified realm.
     * 
     * @param realm
     *                The realm that the credentials are needed for.
     * @param defaultValues
     *                the values to propose the user by default
     * @return The credentials associated with the specified realm or null if
     *         none could be provided.
     * 
     */
    public UserCredentials obtainCredentials(String realm,
            UserCredentials defaultValues) {
        console.logEntry();
        console.debug("Retrieving credentials");

        UserCredentials credentials = new UserCredentials();
        credentials.setUserName(defaultValues.getUserName());

        char[] pass;
        if (password == null) {
            console.debug("Phone without password", new Throwable(
                    "verify file properties of the phone"));
            pass = new char[0];
        } else {
            pass = password.toCharArray();
        }

        credentials.setPassword(pass);

        console.logExit();
        return credentials;

    }

    public void playerStarting(MediaEvent evt) {
        console.logEntry();
    }

    public void playerStopped() {
        console.logEntry();
    }

    public void receivedUnknownMessage(UnknownMessageEvent evt) {
        console.logEntry();
    }

    public void registered(RegistrationEvent evt) {
        console.logEntry();
    }

    public void registering(RegistrationEvent evt) {
    }

    public void callReceived(
            net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallEvent evt) {
        console.logEntry();

        // register as a listener on the call
        net.sourceforge.gjtapi.raw.sipprovider.sip.Call call = evt
                .getSourceCall();
        CallId callId = call.getCallId();
        SipCallId sipCallId = (SipCallId) (callId);
        sipCallId.setSipId(call.getID());
        call.setCallId(sipCallId); // hook the call up to it's id
        ids.add(new ListIdElement(callId, call.getID(),
                this.terminal.terminal, this.address));
        evt.getSourceCall().addStateChangeListener(this);
    }

    public void callRejectedLocally(CallRejectedEvent evt) {
        console.logEntry();
    }

    public void callRejectedRemotely(CallRejectedEvent evt) {
        console.logEntry();
    }

    public void communicationsErrorOccurred(CommunicationsErrorEvent evt) {
        console.logEntry();
    }

    public void unregistered(RegistrationEvent evt) {
        console.logEntry();
    }

    // init methode
    // section---------------------------------------------------------------
    public void launch() {
        try {
            console.logEntry();
            // mode = PHONE_MODE;
            try {
                mediaManager.start();

            } catch (MediaException exc) {
                console.error("Failed to start mediaManager", exc);
            }
            mediaManager.addMediaListener(this);

            sipManager.addCommunicationsListener(this);
            sipManager.setSecurityAuthority(this);

            try {
                sipManager.start();
                if (sipManager.isStarted()) {
                    terminal = new TermData("sip:" + sipManager.getLocalUser()
                            + "@" + sipManager.getLocalHostAddress(), true);

                    console
                            .trace("sipManager appears to be successfully started");
                    // guiManager.setCommunicationActionsEnabled(true);
                }
            } catch (CommunicationsException exc) {
                console.warn(
                        "An exception occurred while initializing communication stack!\n"
                                + "You won't be able to send or receive calls",
                        exc);
                return;
            }
            try {
                // sipManager.register();
                sipManager.startRegisterProcess();
            } catch (CommunicationsException exc) {
                console
                        .error("An exception occurred while trying to register, exc");
            }
        } finally {
            console.logExit();
        }
    }

    // end of init method
    // section-------------------------------------------------------

    private ListIdElement getElementIdListBySipId(int sipId) {
        ListIdElement ret = null;
        Iterator iterator = ids.iterator();
        while (iterator.hasNext()) {
            ListIdElement lst = (ListIdElement) iterator.next();
            if (lst.getSipId() == sipId) {
                ret = lst;
            }
        }
        return ret;
    }

    private ListIdElement getElementIdListByJtapiId(CallId callid) {
        ListIdElement ret = null;
        Iterator iterator = ids.iterator();
        while (iterator.hasNext()) {
            ListIdElement lst = (ListIdElement) iterator.next();
            if (lst.getJtapiId().equals(callid)) {
                ret = lst;
            }
        }
        return ret;
    }

    public SipManager getSipManager() {
        return sipManager;
    }

    public void unregistering(RegistrationEvent evt) {
    }

    public void play(String url) throws MediaResourceException {
        try {
            mediaManager.play(url);
        } catch (MediaException ex) {
            throw new MediaResourceException(ex.getMessage());
        }
    }

    public void record(String url) throws MediaResourceException {
        try {
            mediaManager.record(url);
        } catch (MediaException ex) {
            throw new MediaResourceException(ex.getMessage());
        }
    }

    public void stop() {
        mediaManager.stopPlaying();
        mediaManager.stopRecording();
    }
}

