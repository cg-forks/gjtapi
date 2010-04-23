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
// *    the documentation and/or other materials provided with the
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
import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.media.IncompatibleSourceException;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;

import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.RawStateException;
import net.sourceforge.gjtapi.ResourceConfigurable;
import net.sourceforge.gjtapi.ResourceFinder;
import net.sourceforge.gjtapi.TelephonyListener;
import net.sourceforge.gjtapi.TermData;
import net.sourceforge.gjtapi.raw.MediaTpi;
import net.sourceforge.gjtapi.raw.PrivateDataTpi;
import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
import net.sourceforge.gjtapi.raw.sipprovider.common.NetworkAddressManager;
import net.sourceforge.gjtapi.raw.sipprovider.sip.SipManager;

/**
 * This is a provider that hooks into the SIP Communicator to provided Session
 * Initiation Protocol support for GJTAPI
 */
public class SipProvider
    implements MediaTpi, PrivateDataTpi, ResourceConfigurable {
    // Used by sendPrivateData to allow a client to get a MediaSource for a
    // terminal.
    public static final String GET_MEDIA_SOURCE = "GetMediaSource";

    private TelephonyListener listener;
    // private List addresses;
    // private TermData terminal;
    // private final static String RESOURCE_NAME = "sip.props";
    /** Logger instance. */
    protected static Console console = Console.getConsole(SipProvider.class);
    // private CallId idd;

    /** Known sip phones. */
    private final Collection<SipPhone> sipPhones =
        new java.util.ArrayList<SipPhone>();

    /** The address manager. */
    private NetworkAddressManager addressManager;

    /**
     * Constructs a new object.
     */
    public SipProvider() {
    }

    /**
     * Add an observer for RawEvents
     * 
     * @param ro
     *                New event listener
     * @return void
     * 
     */
    public void addListener(TelephonyListener ro) {
        console.logEntry();
        if (listener == null) {
            listener = ro;
        } else {
            System.err.println("Request to add a TelephonyListener to "
                    + this.getClass().getName()
                    + ", but one is already registered");
        }
    }

    /**
     * Answer a call that has appeared at a particular terminal
     * 
     * @param call
     *                The system identifier for the call
     * @param address
     *                The address of the connection that is ringing.
     * @param terminal
     *                the terminal to answer the call on.
     * @exception RawStateException
     *                    A holder for a low-level state problem.
     * 
     */
    public void answerCall(CallId call, String address, String terminal)
            throws PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {
        console.logEntry();
        SipPhone mySP = getSipPhoneByAddress(address);
        mySP.answerCall(call, address, terminal);
    }

    /**
     * Tell the provider to reserve a call id for future use. The provider does
     * not have to hang onto it. Creation date: (2000-02-16 14:48:48)
     * 
     * @author: Richard Deadman
     * @return The CallId created by the provider.
     * @param term
     *                The address that the call will start on. Used by muxes to
     *                isolate the correct provider.
     * @exception InvalidArgumentException
     *                    If the Address is not in the provider's domain.
     * 
     */
    // prepare un jtapi CallId
    public CallId reserveCallId(String address) throws InvalidArgumentException {
        return new SipCallId();
    }

    /**
     * Make a call. Not that this follows the JTAPI semantics of an idle call
     * being created synchronously (two connections). Events from the raw
     * provider will indicate state transitions.
     * 
     * @param id
     *                The callId reserved for the call.
     * @param address
     *                The logical address to make a call from
     * @param term
     *                The physical address for the call, if applicable
     * @param dest
     *                The destination address
     * @return A call Id. This may be used later to track call progress.
     * @exception RawStateException
     *                    One of the objects is not in the correct state.
     * 
     */
    public CallId createCall(CallId id, String address, String term, String dest)
            throws ResourceUnavailableException, PrivilegeViolationException,
            InvalidPartyException, InvalidArgumentException, RawStateException,
            MethodNotSupportedException

    {
        console.logEntry();
        if (console.isDebugEnabled()) {
            console.debug("id = " + id);

            console.debug("trentative de connection  a " + address);
        }
        // CREATION D'UN CALL (SIP)
        final SipPhone mySP = getSipPhoneByAddress(address);
        mySP.createCall(id, address, term, dest);

        return id;

    }

    /**
     * Get a list of available addresses. This may be null if the Telephony
     * (raw) Provider does not support Addresses! If the Address set it too
     * large, this will throw a ResourceUnavailableException Creation date:
     * (2000-02-11 12:29:00)
     * 
     * @author: Richard Deadman
     * @return An array of address names
     * @exception ResourceUnavailableException
     *                    if the set it too large to be returned dynamically.
     * 
     */
    public String[] getAddresses() throws ResourceUnavailableException {
        console.logEntry();
        String[] ret = new String[sipPhones.size()];
        Iterator<SipPhone> iter = sipPhones.iterator();
        int i = 0;
        while (iter.hasNext()) {
            final SipPhone sp = iter.next();
            ret[i] = sp.getAddress();
            i++;
        }
        return ret;
    }

    /**
     * Get all the addresses associated with a terminal. Creation date:
     * (2000-06-02 12:30:54)
     * 
     * @author: Richard Deadman
     * @return An array of address numbers.
     * @param terminal
     *                The terminal name we want address numbers for.
     * @throws InvalidArgumentException
     *                 indicating that the terminal is unknown.
     * 
     */
    public String[] getAddresses(String terminal)
            throws InvalidArgumentException {
        console.logEntry();
        Iterator<SipPhone> iter = sipPhones.iterator();
        int size = 0;
        while (iter.hasNext()) {
            final SipPhone sp = iter.next();
            SipManager sm = sp.getSipManager();
            size++;

        }
        String[] ret = new String[size];
        iter = sipPhones.iterator();
        int i = 0;
        while (iter.hasNext()) {
            SipPhone sp = (SipPhone) iter.next();
            SipManager sm = sp.getSipManager();
            if (sm.getAddress().equals(terminal))
                ;
            ret[i] = sp.getAddress();
            i++;
        }
        return ret;
    }

    /**
     * Ask the raw provider to update the capabilities offered by the provider
     * This is expected to return a map of capability names to strings. If the
     * string starts with 't' or 'T', the capability is turned on, otherwise it
     * is turned off. Alternatively Boolean values can be used. If the value is
     * not found for a key, the default value is used.
     * <P>
     * To use this feature, the RawProvider needs to copy the
     * GenericCapabilities.props file and change any properties that are
     * supported differently. The the RawProvider could load the properties file
     * into a Properties object and return it. If the default value is
     * supported, then the corresponding line may be omitted from the file.
     * Creation date: (2000-03-14 14:48:36)
     * 
     * @author: Richard Deadman
     * @return A properties file with name to value pairs for the basic raw
     *         provider functions.
     * 
     */
    public java.util.Properties getCapabilities() {
        console.logEntry();
        return null;
    }

    /**
     * Get a list of available terminals. This may be null if the Telephony
     * (raw) Provider does not support Terminals. If the Terminal set it too
     * large, this will throw a ResourceUnavailableException
     * <P>
     * Since we went to lazy connecting between Addresses and Terminals, this is
     * called so we don't have to follow all Address->Terminal associations to
     * get the full set of Terminals. Creation date: (2000-02-11 12:29:00)
     * 
     * @author: Richard Deadman
     * @return An array of terminal names, media type containers.
     * @exception ResourceUnavailableException
     *                    if the set it too large to be returned dynamically.
     * 
     */
    public TermData[] getTerminals() throws ResourceUnavailableException {
        console.logEntry();
        TermData[] ret = new TermData[sipPhones.size()];
        Iterator<SipPhone> iter = sipPhones.iterator();
        int i = 0;
        while (iter.hasNext()) {
            SipPhone sp = iter.next();
            SipManager sm = sp.getSipManager();
            ret[i] = new TermData(sm.getAddress(), true);
            ret[i] = new TermData(sp.getAddress(), true);
            i++;
        }
        return ret;

    }

    /**
     * Get all the terminals associated with an address. Creation date:
     * (2000-02-11 12:30:54)
     * 
     * @author: Richard Deadman
     * @return An array of terminal name, media type containers.
     * @param address
     *                The address number we want terminal names for.
     * @throws InvalidArgumentException
     *                 indicating that the address is unknown.
     * 
     */
    public TermData[] getTerminals(String address)
            throws InvalidArgumentException {
        Iterator<SipPhone> iter = sipPhones.iterator();
        int size = 0;
        while (iter.hasNext()) {
            SipPhone sp = iter.next();
            SipManager sm = sp.getSipManager();
            if (sm.getAddress().equals(address))
                ;
            size++;

        }
        TermData[] ret = new TermData[size];
        iter = sipPhones.iterator();
        int i = 0;
        while (iter.hasNext()) {
            SipPhone sp = (SipPhone) iter.next();
            SipManager sm = sp.getSipManager();
            if (sm.getAddress().equals(address))
                ;
            ret[i] = new TermData(sp.getAddress(), true);

            i++;
        }
        return ret;
    }

    public void initialize(Map props) throws ProviderUnavailableException {
        addressManager = new NetworkAddressManager();
        addressManager.init(props);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void initializeResources(Properties props, ResourceFinder finder)
        throws ProviderUnavailableException {
        String sipPhones = (String) props.get("gjtapi.sip.sip_phone");
        if (sipPhones == null) {
            throw new ProviderUnavailableException("No phones defined!");
        }
        String[] phones = sipPhones.split(",");
        for (String phone : phones) {
            try {
                addPhone(phone, finder);
            } catch (IOException e) {
                throw new ProviderUnavailableException(e.getMessage());
            }
        }
    }

    /**
     * Adds the phone with the given configuration to the list of known
     * phones.
     * @param resource name of the properties resource.
     * @param finder utility to resolve resources
     * @throws IOException
     *         Error reading the resource.
     */
    public void addPhone(String resource, ResourceFinder finder)
        throws IOException {
        SipPhone phone = new SipPhone(resource, finder, this, addressManager);
        sipPhones.add(phone);
        console.debug("added phone " + phone.getAddress());
    }


    /**
     * Release a connection to a call (Connection). This should block until the
     * connection is released. The service provider does not have to send a
     * disconnect message for this connection, since it is generated by the
     * framework.
     * 
     * @param address
     *                The address that we want to release
     * @param call
     *                The call to disconnect from
     * 
     * @exception RawException
     *                    Some low-level state exception occured.
     * 
     */
    public void release(String address, CallId call)
            throws PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {
        console.logEntry();
        final SipPhone mySP = getSipPhoneByAddress(address);
        mySP.sipHangup(call);
    }

    /**
     * Tell the provider that it may release a call id for future use. This is
     * necessary to ensure that TelephonyProvider call ids are not reused until
     * the Generic JTAPI Framework layer is notified of the death of the call.
     * Creation date: (2000-02-17 22:25:48)
     * 
     * @author: Richard Deadman
     * @param id
     *                The CallId that may be freed.
     * 
     */
    public void releaseCallId(CallId id) {
        console.logEntry();
    }

    /**
     * Remove a listener for RawEvents
     * 
     * @param ro
     *                Listener to remove
     * @return void
     * 
     */
    public void removeListener(TelephonyListener ro) {
        if (ro == listener) {
            listener = null;
        } else {
            System.err.println("Request to remove a TelephonyListener from "
                    + this.getClass().getName() + ", but it wasn't registered");
        }
    }

    /**
     * Perform any cleanup after my holder has finished with me. Creation date:
     * (2000-02-11 13:07:46)
     * 
     * @author: Richard Deadman
     * 
     */
    public void shutdown() {
        console.logEntry();
    }

    // methode utilitaires--------------------------------------

    private SipPhone getSipPhoneByAddress(String address) {
        SipPhone ret = null;
        Iterator<SipPhone> iter = sipPhones.iterator();
        while (iter.hasNext()) {
            SipPhone sp = iter.next();
            if (sp.getAddress().equals(address)) {
                ret = sp;
            }
        }
        return ret;
    }

    // fin methodes utilitaires

    // methode de l'interphace SipPhoneListener
    public void sipCallActive(CallId id, int cause) {
        console.logEntry();
        listener.callActive(id, cause);
    }

    public void sipConnectionInProgress(CallId id, String address, int cause) {
        console.logEntry();
        listener.connectionInProgress(id, address, cause);
    }

    void sipConnectionAlerting(CallId id, String address, int cause) {
        console.logEntry();
        listener.connectionAlerting(id, address, cause);
    }

    public void sipConnectionAuthorizeCallAttempt(CallId id, String address,
            int cause) {
        console.logEntry();
    }

    public void sipConnectionCallDelivery(CallId id, String address, int cause) {
        console.logEntry();
    }

    public void sipConnectionConnected(CallId id, String address, int cause) {
        console.logEntry();
        listener.connectionConnected(id, address, cause);
    }

    public void sipConnectionDisconnected(CallId id, String address, int cause) {
        this.releaseCallId(id);
        listener.connectionDisconnected(id, address, cause);
        console.logEntry();
    }

    public void sipConnectionFailed(CallId id, String address, int cause) {
        console.logEntry();
    }

    public void sipTerminalConnectionCreated(CallId id, String address,
            String terminal, int cause) {
        console.logEntry();
        listener.terminalConnectionCreated(id, address, terminal, cause);
    }

    public void sipTerminalConnectionRinging(CallId id, String address,
            String terminal, int cause) {
        console.logEntry();
        listener.terminalConnectionRinging(id, address, terminal, cause);
    }

    // media
    // methods---------------------------------------------------------------------
    // ----------------------------------------------------------------------------------
    public boolean allocateMedia(String terminal, int type,
            Dictionary resourceArgs) {
        return true;
    }

    public boolean freeMedia(String terminal, int type) {
        return true;
    }

    public boolean isMediaTerminal(String terminal) {
        return true;
    }

    public void play(String terminal, String[] streamIds, int offset,
            javax.telephony.media.RTC[] rtcs, Dictionary optArgs)
            throws javax.telephony.media.MediaResourceException {
        console.logEntry();
        try {
            final String[] add = this.getAddresses(terminal);
            for (int i = 0; i < add.length; i++) {
                final SipPhone sipPhone = this.getSipPhoneByAddress(add[i]);
                sipPhone.play(streamIds[0]);
            }
        } catch (javax.telephony.InvalidArgumentException ex) {
            console.debug(ex.toString());
        }
    }

    public void record(String terminal, String streamId,
            javax.telephony.media.RTC[] rtcs, Dictionary optArgs)
            throws javax.telephony.media.MediaResourceException {
        console.logEntry();
        try {
            final String[] add = this.getAddresses(terminal);
            for (int i = 0; i < add.length; i++) {
                final SipPhone sipPhone = this.getSipPhoneByAddress(add[i]);
                sipPhone.record(streamId);
            }
        } catch (javax.telephony.InvalidArgumentException ex) {
            console.debug(ex.toString());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public RawSigDetectEvent retrieveSignals(String terminal, int num,
            javax.telephony.media.Symbol[] patterns,
            javax.telephony.media.RTC[] rtcs, Dictionary optArgs)
            throws javax.telephony.media.MediaResourceException {
        return null;
    }

    public void sendSignals(String terminal,
            javax.telephony.media.Symbol[] syms,
            javax.telephony.media.RTC[] rtcs, Dictionary optArgs)
            throws javax.telephony.media.MediaResourceException {
    }

    public void stop(String terminal) {
        console.logEntry();
        try {
            String[] add = this.getAddresses(terminal);
            for (int i = 0; i < add.length; i++) {
                SipPhone sipPhone = this.getSipPhoneByAddress(add[i]);
                sipPhone.stop();
            }
        } catch (javax.telephony.InvalidArgumentException ex) {
            console.debug(ex.toString());

        }
    }

    public void triggerRTC(String terminal, javax.telephony.media.Symbol action) {
    }

    public Object getPrivateData(CallId call, String address, String terminal) {
        // we don't support this
        return null;
    }

    /**
     * This is used to allow an application to retrieve JMF Media streams from
     * the provider so they can be used in real time for handling media
     */
    public Object sendPrivateData(CallId call, String address, String terminal,
            Object data) {
        // we allow setting a media stream on a Terminal
        if ((terminal != null) && (data instanceof String)
                && (((String) data).equals(GET_MEDIA_SOURCE))) {
            // get the media stream
            try {
                // find the stream on the first successful phone
                String[] add = this.getAddresses(terminal);
                for (int i = 0; i < add.length; i++) {
                    try {
                        SipPhone sipPhone = this.getSipPhoneByAddress(add[i]);
                        return sipPhone.mediaManager.getDataSource();
                    } catch (IncompatibleSourceException isEx) {
                        // keep looping
                    } catch (IOException ioEx) {
                        // keep looping
                    }
                }
            } catch (javax.telephony.InvalidArgumentException ex) {
                // couldn't get the addresses -- fall through and return null
            }
        }
        return null;
    }

    public void setPrivateData(CallId call, String address, String terminal,
            Object data) {
        // we don't support this
    }

}
