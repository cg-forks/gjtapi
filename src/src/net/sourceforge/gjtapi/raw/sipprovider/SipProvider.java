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

import net.sourceforge.gjtapi.raw.sipprovider.sip.SipManager;
import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
import java.util.*;
import java.io.*;
import net.sourceforge.gjtapi.*;
import javax.telephony.*;
/**
 *
 * @author  root
 */
public class SipProvider implements net.sourceforge.gjtapi.raw.MediaTpi
{
    private TelephonyListener listener;
    private List addresses;
    //private TermData terminal;
    private final static String RESOURCE_NAME = "sip.props";
    private Properties properties = System.getProperties();
    protected static Console console = Console.getConsole(SipProvider.class);
    private CallId idd;
    private Vector idVector = new Vector();
    private Vector sipPhoneVector = new Vector();
    
    /** Add an observer for RawEvents
     *
     * @param ro New event listener
     * @return void
     *
     */
    //constructeur
    public SipProvider()
    {
        
    }
    
    //ajoute un listener TelephonyListener
    public void addListener(TelephonyListener ro)
    {
        console.logEntry();
        if (listener == null)
        {
            listener = ro;
        }else
        {
            System.err.println("Request to add a TelephonyListener to "
            + this.getClass().getName() + ", but one is already registered");
        }
        
    }
    
    /** Answer a call that has appeared at a particular terminal
     *
     * @param call The system identifier for the call
     * @param address The address of the connection that is ringing.
     * @param terminal the terminal to answer the call on.
     * @exception RawStateException A holder for a low-level state problem.
     *
     */
    public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException, MethodNotSupportedException, RawStateException
    {
        console.logEntry();
    }
    
    
    /** Tell the provider to reserve a call id for future use.  The provider does not have to hang onto it.
     * Creation date: (2000-02-16 14:48:48)
     * @author: Richard Deadman
     * @return The CallId created by the provider.
     * @param term The address that the call will start on.  Used by muxes to isolate the correct provider.
     * @exception InvalidArgumentException If the Address is not in the provider's domain.
     *
     */
    //prepare un jtapi CallId
    public CallId reserveCallId(String address) throws InvalidArgumentException
    {
        SipCallId id = new SipCallId();
        return id ;
    }
    
    /** Make a call.  Not that this follows the JTAPI sematics of an idle call
     * being created synchronusly (two connections).  Events from the raw provider
     * will indicate state transitions.
     *
     * @param id The callId reserved for the call.
     * @param address The logical address to make a call from
     * @param term The physical address for the call, if applicable
     * @param dest The destination address
     * @return A call Id.  This may be used later to track call progress.
     * @exception RawStateException One of the objects is not in the correct state.
     *
     */
    public CallId createCall(CallId id, String address, String term, String dest) throws
    ResourceUnavailableException, PrivilegeViolationException, InvalidPartyException, InvalidArgumentException, RawStateException, MethodNotSupportedException
    
    {
        console.logEntry();
        console.debug("id = " + id);
        
        console.debug("trentative de connection  a " +address);
        //CREATION D'UN CALL (SIP)
        SipPhone mySP = getSipPhoneByAddress(address);
        mySP.createCall(id, address, term,  dest);
        
        return id;
        
    }
    
    /** Get a list of available addresses.
     * This may be null if the Telephony (raw) Provider does not support Addresses!
     * If the Address set it too large, this will throw a ResourceUnavailableException
     * Creation date: (2000-02-11 12:29:00)
     * @author: Richard Deadman
     * @return An array of address names
     * @exception ResourceUnavailableException if the set it too large to be returned dynamically.
     *
     */
    public String[] getAddresses() throws ResourceUnavailableException
    {
        console.logEntry();
        String [] ret = new String [sipPhoneVector.size()];
        Enumeration enum = sipPhoneVector.elements();
        int i=0;
        while (enum.hasMoreElements())
        {
            SipPhone sp = (SipPhone)enum.nextElement();
            SipManager sm = sp.getSipManager();
            ret[i] = "sip:" + sm.getLocalUser() + "@" + sp.getSipManager().getLocalHostAddress();
            
            i++;
        }
        return ret;
    }
    
    /** Get all the addresses associated with a terminal.
     * Creation date: (2000-06-02 12:30:54)
     * @author: Richard Deadman
     * @return An array of address numbers.
     * @param terminal The terminal name we want address numbers for.
     * @throws InvalidArgumentException indicating that the terminal is unknown.
     *
     */
    public String[] getAddresses(String terminal) throws InvalidArgumentException
    {
        console.logEntry();
        Enumeration enum = sipPhoneVector.elements();
        int size = 0;
        while (enum.hasMoreElements())
        {
            SipPhone sp = (SipPhone)enum.nextElement();
            SipManager sm = sp.getSipManager();
            if (sm.getAddress().equals(terminal));
            size++;
            
        }
        String [] ret = new String [size];
         enum = sipPhoneVector.elements();
        int i=0;
        while (enum.hasMoreElements())
        {
            SipPhone sp = (SipPhone)enum.nextElement();
            SipManager sm = sp.getSipManager();
            if (sm.getAddress().equals(terminal));
            ret[i] = sm.getAddress();
            
            i++;
        }
        return ret;
    }
    
    /** Ask the raw provider to update the capabilities offered by the provider
     * This is expected to return a map of capability names to strings.  If the string starts with
     * 't' or 'T', the capability is turned on, otherwise it is turned off.  Alternatively Boolean values
     * can be used.  If the value is not found for a key, the default value is used.
     * <P>To use this feature, the RawProvider needs to copy the GenericCapabilities.props file and change any
     * properties that are supported differently.  The the RawProvider could load the properties file into
     * a Properties object and return it.  If the default value is supported, then the corresponding line
     * may be omitted from the file.
     * Creation date: (2000-03-14 14:48:36)
     * @author: Richard Deadman
     * @return A properties file with name to value pairs for the basic raw provider functions.
     *
     */
    public java.util.Properties getCapabilities()
    {
        console.logEntry();
        return null;
    }
    
    /** Get a list of available terminals.
     * This may be null if the Telephony (raw) Provider does not support Terminals.
     * If the Terminal set it too large, this will throw a ResourceUnavailableException
     * <P>Since we went to lazy connecting between Addresses and Terminals, this is called so
     * we don't have to follow all Address->Terminal associations to get the full set of Terminals.
     * Creation date: (2000-02-11 12:29:00)
     * @author: Richard Deadman
     * @return An array of terminal names, media type containers.
     * @exception ResourceUnavailableException if the set it too large to be returned dynamically.
     *
     */
    public TermData[] getTerminals() throws ResourceUnavailableException
    {
        console.logEntry();
        TermData[] ret = new TermData [sipPhoneVector.size()];
        Enumeration enum = sipPhoneVector.elements();
        int i=0;
        while (enum.hasMoreElements())
        {
            SipPhone sp = (SipPhone)enum.nextElement();
            SipManager sm = sp.getSipManager();
            ret[i] =  new TermData(sm.getAddress(), true);
            
            i++;
        }
        return ret;
        
    }
    
    /** Get all the terminals associated with an address.
     * Creation date: (2000-02-11 12:30:54)
     * @author: Richard Deadman
     * @return An array of terminal name, media type containers.
     * @param address The address number we want terminal names for.
     * @throws InvalidArgumentException indicating that the address is unknown.
     *
     */
    public TermData[] getTerminals(String address) throws InvalidArgumentException
    {
        console.logEntry();
        Enumeration enum = sipPhoneVector.elements();
        int size = 0;
        while (enum.hasMoreElements())
        {
            SipPhone sp = (SipPhone)enum.nextElement();
            SipManager sm = sp.getSipManager();
            if (sm.getAddress().equals(address));
            size++;
            
        }
        TermData[] ret = new TermData [size];
        enum = sipPhoneVector.elements();
        int i=0;
        while (enum.hasMoreElements())
        {
            SipPhone sp = (SipPhone)enum.nextElement();
            SipManager sm = sp.getSipManager();
            if (sm.getAddress().equals(address));
            ret[i] =  new TermData(sm.getAddress(), true);
            
            i++;
        }
        return ret;
    }
    
    /** This allows for any context-specific parameters to be set.
     * The map may include such pairs as "name"="xxx" or "password"="yyy".
     * The provider is not active until this has
     * been called.  The property map may be null.
     *
     * Creation date: (2000-02-11 12:13:36)
     * @author: Richard Deadman
     * @param props The name value properties map
     *
     */
    public void initialize(java.util.Map props) throws ProviderUnavailableException
    {
        console.logEntry();
        loadProperties();
    }
    
    
    
    
    
    
    /** Release a connection to a call (Connection).
     * This should block until the connection is released. The service provider does
     * not have to send a disconnect message for this connection, since it is generated by the framework.
     *
     * @param address The address that we want to release
     * @param call The call to disconnect from
     *
     * @exception RawException Some low-level state exception occured.
     *
     */
    public void release(String address, CallId call) throws PrivilegeViolationException, ResourceUnavailableException, MethodNotSupportedException, RawStateException
    {
        console.logEntry();
        SipPhone mySP = getSipPhoneByAddress(address);
        mySP.SipHangup(call);
    }
    
    /** Tell the provider that it may release a call id for future use.  This is necessary to ensure that
     * TelephonyProvider call ids are not reused until the Generic JTAPI Framework layer is notified of
     * the death of the call.
     * Creation date: (2000-02-17 22:25:48)
     * @author: Richard Deadman
     * @param id The CallId that may be freed.
     *
     */
    public void releaseCallId(CallId id)
    {
        console.logEntry();
    }
    
    /** Remove a listener for RawEvents
     *
     * @param ro Listener to remove
     * @return void
     *
     */
    public void removeListener(TelephonyListener ro)
    {
        if (ro == listener)
        {
            listener = null;
        }else
        {
            System.err.println("Request to remove a TelephonyListener from "
            + this.getClass().getName() + ", but it wasn't registered");
        }
    }
    
    
    
    /** Perform any cleanup after my holder has finished with me.
     * Creation date: (2000-02-11 13:07:46)
     * @author: Richard Deadman
     *
     */
    public void shutdown()
    {
        console.logEntry();
    }
    
    
    
    
    //initialisation du provider-----------------------------------------------------------
    //lecture des propriet?s et creation des adresees et des terminaux
    public void loadProperties()
    {
        try
        {
            File pFile = new File("sip-provider.properties");
            FileInputStream pIS = new FileInputStream(pFile);
            System.out.println(pIS.toString());
            properties.load(pIS);
            pIS.close();
            String strPhone = properties.getProperty("gjtapi.sip.sip_phone");
            StringTokenizer st = new StringTokenizer(strPhone,",");
            
            
            
            while (st.hasMoreTokens())
            {
                pFile = new File(st.nextToken());
                pIS = new FileInputStream(pFile);
                //Properties sipProperties = new Properties();
                properties.load(pIS);
                SipPhone sipPhone = new SipPhone(properties,this);
                sipPhoneVector.add(sipPhone);
                pIS.close();
            }
            System.getProperties().putAll(properties);
            //terminal = new TermData("sipTerminal", true);
            
        }
        //Catch IO & FileNotFound & NullPointer exceptions
        catch (Throwable exc)
        {
            console.warn(
            "Warning:Failed to load properties!"
            + "\nThis is only a warning.SipCommunicator will use defaults.",
            exc);
        }
        
    }
    
    
    //methode utilitaires--------------------------------------
    private ListIdElement getElementIdListBySipId(int sipId)
    {
        ListIdElement ret = null;
        Enumeration enum = idVector.elements();
        while (enum.hasMoreElements())
        {
            ListIdElement lst = (ListIdElement)enum.nextElement();
            if ( lst.getSipId() == sipId)
            {
                ret = lst;
            }
        }
        return ret;
    }
    
    private SipPhone getSipPhoneByAddress(String address)
    {
        SipPhone ret = null;
        Enumeration enum = sipPhoneVector.elements();
        while (enum.hasMoreElements())
        {
            SipPhone sp = (SipPhone)enum.nextElement();
            if ( sp.getAddress().equals(address))
            {
                ret = sp;
            }
        }
        return ret;
    }
    //fin methodes utilitaires
    
    // methode de l'interphace SipPhoneListener
    public void sipCallActive(CallId id, int cause)
    {
        console.logEntry();
        listener.callActive(id, cause);
    }
    public void sipConnectionInProgress(CallId id, String address, int cause)
    {
        console.logEntry();
        listener.connectionInProgress(id, address, cause);
    }
    
    void sipConnectionAlerting(CallId id, String address, int cause)
    {
        console.logEntry();
        listener.connectionAlerting(id, address, cause);
    }
    public void sipConnectionAuthorizeCallAttempt(CallId id, String address, int cause)
    {
        console.logEntry();
    }
    public void sipConnectionCallDelivery(CallId id, String address, int cause)
    {console.logEntry();
    }
    
    public void sipConnectionConnected(CallId id, String address, int cause)
    {
        console.logEntry();
        //  listener.callActive(id, cause);
        listener.connectionConnected(id, address, cause);
        //listener.connectionDisconnected(id, address, cause);
    }
    public void sipConnectionDisconnected(CallId id, String address, int cause)
    {
        this.releaseCallId(id);
        console.logEntry();
    }
    public void sipConnectionFailed(CallId id, String address, int cause)
    {
        console.logEntry();
    }
    
    public void sipTerminalConnectionCreated(CallId id, String address, String terminal, int cause)
    {
        console.logEntry();
        listener.terminalConnectionCreated(id, address, terminal, cause);
    }
    //media methods---------------------------------------------------------------------
    //----------------------------------------------------------------------------------
    public boolean allocateMedia(String terminal, int type, Dictionary resourceArgs)
    {
        return true;
    }
    
    public boolean freeMedia(String terminal, int type)
    {
        return true;
    }
    
    public boolean isMediaTerminal(String terminal)
    {
        return true;
    }
    
    public void play(String terminal, String[] streamIds, int offset, javax.telephony.media.RTC[] rtcs, Dictionary optArgs) throws javax.telephony.media.MediaResourceException
    {
        console.logEntry();
        try
        {

            String[] add = this.getAddresses(terminal);
            for(int i=0; i < add.length; i++)
            {
                SipPhone sipPhone = this.getSipPhoneByAddress(add[i]);
                sipPhone.play(streamIds[0]);
            }
        }
        catch (javax.telephony.InvalidArgumentException ex)
        {
            console.debug(ex.toString());
            
        }
        
    }
    
    public void record(String terminal, String streamId, javax.telephony.media.RTC[] rtcs, Dictionary optArgs) throws javax.telephony.media.MediaResourceException
    {
        console.logEntry();
        try
        {

            String[] add = this.getAddresses(terminal);
            for(int i=0; i < add.length; i++)
            {
                SipPhone sipPhone = this.getSipPhoneByAddress(add[i]);
                sipPhone.record(streamId);
            }
        }
        catch (javax.telephony.InvalidArgumentException ex)
        {
            console.debug(ex.toString());
            
        }
        
        
    }
    
    public RawSigDetectEvent retrieveSignals(String terminal, int num, javax.telephony.media.Symbol[] patterns, javax.telephony.media.RTC[] rtcs, Dictionary optArgs) throws javax.telephony.media.MediaResourceException
    {
        return null;
    }
    
    public void sendSignals(String terminal, javax.telephony.media.Symbol[] syms, javax.telephony.media.RTC[] rtcs, Dictionary optArgs) throws javax.telephony.media.MediaResourceException
    {
    }
    
    public void stop(String terminal)
    {
          console.logEntry();
        try
        {
            String[] add = this.getAddresses(terminal);
            for(int i=0; i < add.length; i++)
            {
                SipPhone sipPhone = this.getSipPhoneByAddress(add[i]);
                sipPhone.stop();
            }
        }
        catch (javax.telephony.InvalidArgumentException ex)
        {
            console.debug(ex.toString());
            
        }
    }
    
    public void triggerRTC(String terminal, javax.telephony.media.Symbol action)
    {
    }
    
}
