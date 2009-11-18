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
package net.sourceforge.gjtapi.raw.sipprovider.sip;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.TooManyListenersException;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionUnavailableException;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
import net.sourceforge.gjtapi.raw.sipprovider.common.NetworkAddressManager;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallRejectedEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CommunicationsErrorEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CommunicationsListener;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.MessageEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.RegistrationEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.UnknownMessageEvent;
import net.sourceforge.gjtapi.raw.sipprovider.sip.security.SecurityAuthority;
import net.sourceforge.gjtapi.raw.sipprovider.sip.security.SipSecurityManager;
import net.sourceforge.gjtapi.raw.sipprovider.sip.security.UserCredentials;


/**
 * The SipManager provides wrapping of the underlying stack's functionalities.
 * It also implements the SipListener interface and handles incoming
 * SIP messages.
 *
 * @author Emil Ivov
 * @version 1.0
 */
public class SipManager implements SipListener
{
    /**
     * Specifies the number of retries that should be attempted when deleting
     * a sipProvider
     */
    protected static final int  RETRY_OBJECT_DELETES       = 10;
    /**
     * Specifies the time to wait before retrying delete of a sipProvider.
     */
    protected static final long RETRY_OBJECT_DELETES_AFTER = 500;


    protected static final Console console = Console.getConsole(SipManager.class);
    protected static final String DEFAULT_TRANSPORT = "udp";
    //jain-sip objects - package accessibility as they should be
    //available for XxxProcessing classes
    /**
     * The SipFactory instance used to create the SipStack and the Address
     * Message and Header Factories.
     */
    public SipFactory sipFactory;

    /**
     * The AddressFactory used to create URLs and Address objects.
     */
    public AddressFactory addressFactory;

    /**
     * The HeaderFactory used to create SIP message headers.
     */
    public HeaderFactory headerFactory;

    /**
     * The Message Factory used to create SIP messages.
     */
    public MessageFactory messageFactory;

    /**
     * The sipStack instance that handles SIP communications.
     */
    SipStack sipStack;

    /**
     * The default (and currently the only) SIP listening point of the
     * application.
     */
    ListeningPoint listeningPoint;

    /**
     * The JAIN SIP SipProvider instance.
     */
    public  SipProvider sipProvider;

    /**
     * An instance used to provide user credentials
     */
    private SecurityAuthority securityAuthority = null;


    /**
     * Used for the contact header to provide firewall support.
     */
    private InetSocketAddress publicIpAddress = null;

    //properties
    protected String sipStackPath = null;
    public String currentlyUsedURI = null;
    protected String displayName = null;
    protected String transport = null;
    protected String registrarAddress = null;
    protected int localPort = -1;
    protected int registrarPort = -1;
    protected int registrationsExpiration = -1;
    protected String registrarTransport = null;

    //mandatory stack properties
    protected String stackAddress = null;
    protected String stackName = "sip-communicator";

    //Prebuilt Message headers
    protected FromHeader fromHeader = null;
    protected ContactHeader contactHeader = null;
    protected ArrayList viaHeaders = null;
    protected static final int  MAX_FORWARDS = 70;
    protected MaxForwardsHeader maxForwardsHeader = null;
    protected long registrationTransaction = -1;
    protected Collection listeners = new java.util.ArrayList();

    //XxxProcessing managers
    /**
     * The instance that handles all registration associated activity such as
     * registering, unregistering and keeping track of expirations.
     */
    RegisterProcessing registerProcessing = null;

    /**
     * The instance that handles all call associated activity such as
     * establishing, managing, and terminating calls.
     */
    CallProcessing callProcessing = null;

    /**
     * The instance that handles subscriptions.
     */
    //public Watcher watcher = null;

    /**
     * The instance that informs others of our avaibility.
     */
    // public PresenceAgent presenceAgent = null;

    /**
     * The instance that handles status management and notifications.
     */
    //   public PresenceUserAgent presenceUserAgent = null;

    /**
     * The instance that handles incoming/outgoing REFER requests.
     */
    TransferProcessing transferProcessing = null;

    /**
     * Authentication manager.
     */
    public SipSecurityManager sipSecurityManager = null;

    protected boolean isStarted = false;

    private final Properties sipProp;

    /** Reference to the address manager. */
    private final NetworkAddressManager addressManager;

    /**
     * Constructor. It only creates a SipManager instance without initializing
     * the stack itself.
     */
    public SipManager(Properties sipProp, NetworkAddressManager manager)
    {
        this.sipProp = new Properties() ;
        this.sipProp.putAll(sipProp);
        addressManager = manager;

        registerProcessing  = new RegisterProcessing(this);
        callProcessing      = new CallProcessing(this, sipProp);
        //  watcher = new Watcher(this);

        sipSecurityManager  = new SipSecurityManager(this.sipProp);
    }

    /**
     * Creates and initializes JAIN SIP objects (factories, stack, listening
     * point and provider). Once this method is called the application is ready
     * to handle (incoming and outgoing) sip messages.
     *
     * @throws CommunicationsException if an exception should occur during the
     * initialization process
     */
    public void start() throws CommunicationsException
    {
        try
        {
            console.logEntry();
            initProperties();


            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            try {
                addressFactory = sipFactory.createAddressFactory();
                headerFactory = sipFactory.createHeaderFactory();
                messageFactory = sipFactory.createMessageFactory();
            } catch (PeerUnavailableException ex) {
                console.error("Could not could not create factories!", ex);
                throw new CommunicationsException(
                "Could not could not create factories!",
                ex
                );
            }

            try
            {
                sipStack = sipFactory.createSipStack(sipProp);
            }
            catch (PeerUnavailableException ex)
            {
                console.error("Could not could not create SipStack!", ex);
                throw new CommunicationsException(
                "Could not could not create SipStack!\n"
                +
                "A possible reason is an incorrect OUTBOUND_PROXY property\n"
                + "(Syntax:<proxy_address:port/transport>)",
                ex
                );
            }
            try
            {
                //try and capture the firewall mapping for this address
                //just before it gets occuppied by the stack
                publicIpAddress = addressManager.getPublicAddressFor(localPort);

                listeningPoint = sipStack.createListeningPoint(localPort, transport);
            }
            catch (InvalidArgumentException ex)
            {
                //choose another port between 1024 and 65000
                console.error("error binging stack to port " + localPort + ".",
                        ex);

                throw new CommunicationsException(
                        "error binging stack to port " + localPort, ex);
            }
            catch (TransportNotSupportedException ex)
            {
                console.error(
                "Transport " + transport
                +
                " is not suppported by the stack!\n Try specifying another"
                + " transport in SipCommunicator property files.\n",
                ex);
                throw new CommunicationsException(
                "Transport " + transport
                +
                " is not suppported by the stack!\n Try specifying another"
                + " transport in SipCommunicator property files.\n",
                ex);
            }
            try
            {
                sipProvider = sipStack.createSipProvider(listeningPoint);
            }
            catch (ObjectInUseException ex)
            {
                console.error("Could not could not create factories!\n", ex);
                throw new CommunicationsException(
                "Could not could not create factories!\n", ex);
            }
            try
            {

                sipProvider.addSipListener(this);
            }
            catch (TooManyListenersException exc)
            {
                console.error(
                "Could not register SipManager as a sip listener!", exc);
                throw new CommunicationsException(
                "Could not register SipManager as a sip listener!", exc);
            }

            // we should have a security authority to be able to handle
            // authentication
            if(sipSecurityManager.getSecurityAuthority() == null)
            {
                throw new CommunicationsException(
                "No SecurityAuthority was provided to SipManager!");
            }
            sipSecurityManager.setHeaderFactory(headerFactory);
            sipSecurityManager.setTransactionCreator(sipProvider);
            sipSecurityManager.setSipManCallback(this);


            //Make sure prebuilt headers are nulled so that they get reinited
            //if this is a restart
            contactHeader = null;
            fromHeader = null;
            viaHeaders = null;
            maxForwardsHeader = null;
            isStarted = true;
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Unregisters listening points, deletes sip providers, and generally
     * prepares the stack for a re-start(). This method is meant to be used
     * when properties are changed and should be reread by the stack.
     * @throws CommunicationsException
     */
    public void stop() throws CommunicationsException
    {
        try
        {
            console.logEntry();

            //Delete SipProvider
            int tries = 0;
            for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++)
            {
                try
                {
                    sipStack.deleteSipProvider(sipProvider);
                }
                catch (ObjectInUseException ex)
                {
                    // System.err.println("Retrying delete of riSipProvider!");
                    sleep(RETRY_OBJECT_DELETES_AFTER);
                    continue;
                }
                break;
            }
            if (tries >= RETRY_OBJECT_DELETES)
                throw new CommunicationsException("Failed to delete the sipProvider!");

            //Delete RI ListeningPoint
            for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++)
            {
                try
                {
                    sipStack.deleteListeningPoint(listeningPoint);
                }
                catch (ObjectInUseException ex)
                {
                    //System.err.println("Retrying delete of riListeningPoint!");
                    sleep(RETRY_OBJECT_DELETES_AFTER);
                    continue;
                }
                break;
            }
            if (tries >= RETRY_OBJECT_DELETES)
                throw new CommunicationsException("Failed to delete a listeningPoint!");

            sipProvider = null;
            listeningPoint = null;
            addressFactory = null;
            messageFactory = null;
            headerFactory = null;
            sipStack = null;

            viaHeaders = null;
            contactHeader = null;
            fromHeader = null;
        }finally
        {
            console.logExit();
        }
    }

    /**
     * Waits during _no_less_ than sleepFor milliseconds.
     * Had to implement it on top of Thread.sleep() to guarantee minimum
     * sleep time.
     *
     * @param sleepFor the number of miliseconds to wait
     */
    protected static void sleep(long sleepFor)
    {
        try
        {
            console.logEntry();

            long startTime = System.currentTimeMillis();
            long haveBeenSleeping = 0;
            while (haveBeenSleeping < sleepFor)
            {
                try
                {
                    Thread.sleep(sleepFor - haveBeenSleeping);
                }
                catch (InterruptedException ex)
                {
                    //we-ll have to wait again!
                }
                haveBeenSleeping = (System.currentTimeMillis() - startTime);
            }
        }finally
        {
            console.logExit();
        }

    }

    public void setCurrentlyUsedURI(String uri)
    {
        this.currentlyUsedURI = uri;
    }

    /**
     * Causes the RegisterProcessing object to send a registration request
     * to the registrar defined in
     * net.java.sip.communicator.sip.REGISTRAR_ADDRESS and to register with
     * the address defined in the net.java.sip.communicator.sip.PUBLIC_ADDRESS
     * property
     *
     * @throws CommunicationsException if an exception is thrown by the
     * underlying stack. The exception that caused this CommunicationsException
     * may be extracted with CommunicationsException.getCause()
     */
    public void register() throws CommunicationsException
    {
        register(currentlyUsedURI);
    }

    /**
     * Registers using the specified public address. If public add
     * @param publicAddress
     * @throws CommunicationsException
     */
    public void register(String publicAddress) throws CommunicationsException
    {
        try
        {
            console.logEntry();



            if(publicAddress == null || publicAddress.trim().length() == 0)
                return; //maybe throw an exception?


            //Handle default domain name (i.e. transform 1234 -> 1234@sip.com
            String defaultDomainName =
            sipProp.getProperty("net.java.sip.communicator.sip.DEFAULT_DOMAIN_NAME");

            //feature request, Michael Robertson (sipphone.com)
            //strip the following chars of their user names: ( - ) <space>
            if(publicAddress.toLowerCase().indexOf("sipphone.com") != -1
            || defaultDomainName.indexOf("sipphone.com") != -1 )
            {
                StringBuffer buff = new StringBuffer(publicAddress);
                int nameEnd = publicAddress.indexOf('@');
                nameEnd = nameEnd==-1?Integer.MAX_VALUE:nameEnd;
                nameEnd = Math.min(nameEnd, buff.length())-1;

                int nameStart = publicAddress.indexOf("sip:");
                nameStart = nameStart == -1 ? 0 : nameStart + "sip:".length();

                for(int i = nameEnd; i >= nameStart; i--)
                    if(!Character.isLetter( buff.charAt(i) )
                    && !Character.isDigit( buff.charAt(i)))
                        buff.deleteCharAt(i);
                publicAddress = buff.toString();
            }


            // if user didn't provide a domain name in the URL and someone
            // has defined the DEFAULT_DOMAIN_NAME property - let's fill in the blank.
            if (defaultDomainName != null
            && publicAddress.indexOf('@') == -1 //most probably a sip uri
            )
            {
                publicAddress = publicAddress + "@" + defaultDomainName;
            }

            if (!publicAddress.trim().toLowerCase().startsWith("sip:"))
            {
                publicAddress = "sip:" + publicAddress;
            }

            this.currentlyUsedURI = publicAddress;
            registerProcessing.register( registrarAddress, registrarPort,
            registrarTransport, registrationsExpiration);

            //at this point we are sure we have a sip: prefix in the uri
            // we construct our pres: uri by replacing that prefix.
//            String presenceUri = "pres"
//            + publicAddress.substring(publicAddress.indexOf(':'));


        }
        finally
        {
            console.logExit();
        }
    }

    public void startRegisterProcess() throws CommunicationsException
    {
        try
        {
            console.logEntry();
            checkIfStarted();
            //Obtain initial credentials

            UserCredentials defaultCredentials = new UserCredentials();

            //avoid nullpointer exceptions
            String uName = sipProp.getProperty(
            "net.java.sip.communicator.sip.USER_NAME");
            defaultCredentials.setUserName(uName == null? "" : uName);
            defaultCredentials.setPassword(new char[0]);

            String realm = sipProp.getProperty(
            "net.java.sip.communicator.sip.DEFAULT_AUTHENTICATION_REALM");
            realm = realm == null ? "" : realm;

            UserCredentials initialCredentials = securityAuthority.obtainCredentials(realm,
            defaultCredentials);

            register(initialCredentials.getUserName());

            //at this point a simple register request has been sent and the global
            //from  header in SipManager has been set to a valid value by the RegisterProcesing
            //class. Use it to extract the valid user name that needs to be cached by
            //the security manager together with the user provider password.
            initialCredentials.setUserName(((SipURI)getFromHeader().getAddress().getURI()).getUser());

            cacheCredentials(realm, initialCredentials);
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Causes the RegisterProcessing object to send a registration request with
     * a 0 "expires" interval to the registrar defined in
     * net.java.sip.communicator.sip.REGISTRAR_ADDRESS.
     *
     * @throws CommunicationsException if an exception is thrown by the
     * underlying stack. The exception that caused this CommunicationsException
     * may be extracted with CommunicationsException.getCause()
     */
    public void unregister() throws CommunicationsException
    {
        try
        {
            console.logEntry();
            if (!isRegistered())
            {
                return;
            }
            checkIfStarted();
            registerProcessing.unregister();

        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Queries the RegisterProcessing object whether the application is registered
     * with a registrar.
     * @return true if the application is registered with a registrar.
     */
    public boolean isRegistered()
    {
        return (registerProcessing != null && registerProcessing.isRegistered());
    }

    /**
     * Determines whether the SipManager was start()ed.
     * @return true if the SipManager was start()ed.
     */
    public boolean isStarted()
    {
        return isStarted;
    }

    //============================ COMMUNICATION FUNCTIONALITIES =========================
    /**
     * Causes the CallProcessing object to send  an INVITE request to the
     * URI specified by <code>callee</code>
     * setting sdpContent as message body. The method generates a Call object
     * that will represent the resulting call and will be used for later
     * references to the same call.
     *
     * @param callee the URI to send the INVITE to.
     * @param sdpContent the sdp session offer.
     * @return the Call object that will represent the call resulting
     *                  from invoking this method.
     * @throws CommunicationsException if an exception occurs while sending and
     * parsing.
     */
    public Call establishCall(String callee, String sdpContent) throws
    CommunicationsException
    {
        try
        {
            console.logEntry();
            checkIfStarted();
            return callProcessing.invite(callee, sdpContent);
        }
        finally
        {
            console.logExit();
        }
    } //CALL

    //------------------ hang up on
    /**
     * Causes the CallProcessing object to send a terminating request (CANCEL,
     * BUSY_HERE or BYE) and thus terminate that call with id <code>callID</code>.
     * @param callID the id of the call to terminate.
     * @throws CommunicationsException if an exception occurs while invoking this
     * method.
     */
    public void endCall(int callID) throws CommunicationsException
    {
        try
        {
            console.logEntry();
            checkIfStarted();
            callProcessing.endCall(callID);
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Calls endCall for all currently active calls.
     * @throws CommunicationsException if an exception occurs while
     */
    public void endAllCalls() throws CommunicationsException
    {
        try
        {
            console.logEntry();
            if (callProcessing == null)
            {
                return;
            }
            Object[] keys = callProcessing.getCallDispatcher().getAllCalls();
            for (int i = 0; i < keys.length; i++)
            {
                endCall( ( (Integer) keys[i]).intValue());
            }
        }
        finally
        {
            console.logExit();
        }
    }


    /**
     * Causes CallProcessing to send a 200 OK response, with the specified
     * sdp description, to the specified call's remote party.
     * @param callID the id of the call that is to be answered.
     * @param sdpContent this party's media description (as defined by SDP).
     * @throws CommunicationsException if an axeption occurs while invoking this
     * method.
     */
    public void answerCall(int callID, String sdpContent) throws
    CommunicationsException
    {
        try
        {
            console.logEntry();
            checkIfStarted();
            callProcessing.sayOK(callID, sdpContent);
        }
        finally
        {
            console.logExit();
        }
    } //answer to

    /**
     * Sends a NOT_IMPLEMENTED response through the specified transaction.
     * @param serverTransaction the transaction to send the response through.
     * @param request the request that is being answered.
     */
    void sendNotImplemented(ServerTransaction serverTransaction,
    Request request)
    {
        try
        {
            console.logEntry();
            Response notImplemented = null;
            try
            {
                notImplemented =
                messageFactory.createResponse(Response.NOT_IMPLEMENTED,
                request);
                attachToTag(notImplemented, serverTransaction.getDialog());
            }
            catch (ParseException ex)
            {
                fireCommunicationsError(
                new CommunicationsException(
                "Failed to create a NOT_IMPLEMENTED response to a "
                + request.getMethod()
                + " request!",
                ex)
                );
                return;
            }
            try
            {
                serverTransaction.sendResponse(notImplemented);
            }
            catch (SipException ex)
            {
                fireCommunicationsError(
                new CommunicationsException(
                "Failed to create a NOT_IMPLEMENTED response to a "
                + request.getMethod()
                + " request!",
                ex)
                );
            }
        }
        finally
        {
            console.logExit();
        }
    }

    //============================= Utility Methods ==================================
    /**
     * Initialises SipManager's fromHeader field in accordance with
     * net.java.sip.communicator.sip.PUBLIC_ADDRESS
     * net.java.sip.communicator.sip.DISPLAY_NAME
     * net.java.sip.communicator.sip.TRANSPORT
     * net.java.sip.communicator.sip.PREFERRED_LOCAL_PORT and returns a
     * reference to it.
     * @return a reference to SipManager's fromHeader field.
     * @throws CommunicationsException if a ParseException occurs while
     * initially composing the FromHeader.
     */
    public FromHeader getFromHeader() throws CommunicationsException
    {
        try
        {
            console.logEntry();
            if (fromHeader != null)
            {
                return fromHeader;
            }
            try
            {
                final SipURI fromURI = (SipURI) addressFactory.createURI(
                        currentlyUsedURI);
                fromURI.setTransportParam(listeningPoint.getTransport());

                fromURI.setPort(listeningPoint.getPort());
                Address fromAddress = addressFactory.createAddress(fromURI);
                if (displayName != null && displayName.trim().length() > 0)
                {
                    fromAddress.setDisplayName(displayName);
                }
                fromHeader = headerFactory.createFromHeader(fromAddress,
                Integer.toString(hashCode()));
                console.debug("Generated from header: " + fromHeader);
            }
            catch (ParseException ex)
            {
                console.error(
                "A ParseException occurred while creating From Header!", ex);
                throw new CommunicationsException(
                "A ParseException occurred while creating From Header!", ex);
            }
            return fromHeader;
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Same as calling getContactHeader(true)
     *
     * @return the result of getContactHeader(true)
     * @throws CommunicationsException if an exception is thrown while calling
     * getContactHeader(false)
     */
    public ContactHeader getContactHeader() throws CommunicationsException
    {
        return getContactHeader(true);
    }

    /**
     * Same as calling getContactHeader(true).
     * @return the result of calling getContactHeader(true).
     * @throws CommunicationsException if an exception occurs while executing
     * getContactHeader(true).
     */
    public ContactHeader getRegistrationContactHeader() throws CommunicationsException
    {
        return getContactHeader(true);
    }

    /**
     * Initialises SipManager's contactHeader field in accordance with
     * javax.sip.IP_ADDRESS
     * net.java.sip.communicator.sip.DISPLAY_NAME
     * net.java.sip.communicator.sip.TRANSPORT
     * net.java.sip.communicator.sip.PREFERRED_LOCAL_PORT and returns a
     * reference to it.
     * @param useLocalHostAddress specifies whether the SipURI in the contact
     * header should contain the value of javax.sip.IP_ADDRESS (true) or that of
     * net.java.sip.communicator.sip.PUBLIC_ADDRESS (false).
     * @return a reference to SipManager's contactHeader field.
     * @throws CommunicationsException if a ParseException occurs while
     * initially composing the FromHeader.
     */
    public ContactHeader getContactHeader(boolean useLocalHostAddress) throws
    CommunicationsException
    {
        try
        {
            console.logEntry();
            if (contactHeader != null)
            {
                return contactHeader;
            }
            try
            {

                SipURI contactURI;
                if (useLocalHostAddress)
                {
                    //ContacHeader it's incomplite
                    /*contactURI = (SipURI) addressFactory.createSipURI(null,
                    publicIpAddress.getAddress().getHostAddress());*/

                    /*In this way, allow multiple terminals located in the same machine
                  and generate a correct ContactHeader
                  before: <sip:IP:port;transport=udp> after: <sip:user@IP:port;transport=udp>>
                    */
                    String _publicAddrsUsed = sipProp.getProperty(
                            "net.java.sip.communicator.sip.PUBLIC_ADDRESS");
                    contactURI = (SipURI) addressFactory.createURI(
                            _publicAddrsUsed);

                }
                else
                {
                    contactURI = (SipURI) addressFactory.createURI(
                    currentlyUsedURI);
                }
                contactURI.setTransportParam(listeningPoint.getTransport());
                contactURI.setPort(publicIpAddress.getPort());
                Address contactAddress = addressFactory.createAddress(
                contactURI);
                if (displayName != null && displayName.trim().length() > 0)
                {
                    contactAddress.setDisplayName(displayName);
                }
                contactHeader = headerFactory.createContactHeader(
                contactAddress);
                if (console.isDebugEnabled())
                {
                    console.debug("generated contactHeader:" + contactHeader);
                }
            }
            catch (ParseException ex)
            {
                console.error(
                "A ParseException occurred while creating From Header!", ex);
                throw new CommunicationsException(
                "A ParseException occurred while creating From Header!", ex);
            }
            return contactHeader;
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Initializes (if null) and returns an ArrayList with a single ViaHeader
     * containing localhost's address. This ArrayList may be used when sending
     * requests.
     * @return ViaHeader-s list to be used when sending requests.
     * @throws CommunicationsException if a ParseException is to occur while
     * initializing the array list.
     */
    public ArrayList getLocalViaHeaders() throws CommunicationsException
    {
        try
        {
            console.logEntry();
            if (viaHeaders != null)
            {
                return viaHeaders;
            }
            ListeningPoint lp = sipProvider.getListeningPoint();
            viaHeaders = new ArrayList();
            try
            {
                ViaHeader viaHeader = headerFactory.createViaHeader(
                sipStack.getIPAddress(),
                lp.getPort(),
                lp.getTransport(),
                null
                );
                viaHeaders.add(viaHeader);
                if (console.isDebugEnabled())
                {
                    console.debug("generated via headers:" + viaHeader);
                }
                return viaHeaders;
            }
            catch (ParseException ex)
            {
                console.error(
                "A ParseException occurred while creating Via Headers!");
                throw new CommunicationsException(
                "A ParseException occurred while creating Via Headers!");
            }
            catch (InvalidArgumentException ex)
            {
                console.error(
                "Unable to create a via header for port " + lp.getPort(),
                ex);
                throw new CommunicationsException(
                "Unable to create a via header for port " + lp.getPort(),
                ex);
            }
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Initializes and returns SipManager's maxForwardsHeader field using the
     * value specified by MAX_FORWARDS.
     * @return an instance of a MaxForwardsHeader that can be used when
     * sending requests
     * @throws CommunicationsException if MAX_FORWARDS has an invalid value.
     */
    public MaxForwardsHeader getMaxForwardsHeader() throws CommunicationsException
    {
        try
        {
            console.logEntry();
            if (maxForwardsHeader != null)
            {
                return maxForwardsHeader;
            }
            try
            {
                maxForwardsHeader = headerFactory.createMaxForwardsHeader(MAX_FORWARDS);
                if (console.isDebugEnabled())
                {
                    console.debug("generate max forwards: "
                    + maxForwardsHeader.toString());
                }
                return maxForwardsHeader;
            }
            catch (InvalidArgumentException ex)
            {
                throw new CommunicationsException(
                "A problem occurred while creating MaxForwardsHeader", ex);
            }
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Returns the user used to create the From Header URI.
     * @return the user used to create the From Header URI.
     */
    public String getLocalUser()
    {
        try
        {
            console.logEntry();
            final FromHeader header = getFromHeader();
            final Address address = header.getAddress();
            final SipURI uri = (SipURI) address.getURI();
            return uri.getUser();
        }
        catch (CommunicationsException ex)
        {
            return "";
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * Generates a ToTag (the containingDialog's hashCode())and attaches it to
     * response's ToHeader.
     * @param response the response that is to get the ToTag.
     * @param containingDialog the Dialog instance that is to extract a unique
     * Tag value (containingDialog.hashCode())
     */
    public void attachToTag(Response response, Dialog containingDialog)
    {
        try
        {
            console.logEntry();
            ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
            if (to == null)
            {
                fireCommunicationsError(
                new CommunicationsException(
                "No TO header found in, attaching a to tag is therefore impossible"));
            }
            try
            {
                if (to.getTag() == null || to.getTag().trim().length() == 0)
                {
                    if (console.isDebugEnabled())
                    {
                        console.debug("generated to tag: " +
                        containingDialog.hashCode());
                    }
                    to.setTag(Integer.toString(containingDialog.hashCode()));
                }
            }
            catch (ParseException ex)
            {
                fireCommunicationsError(
                new CommunicationsException(
                "Failed to attach a TO tag to an outgoing response"));
            }
        }
        finally
        {
            console.logExit();
        }
    }

    //================================ PROPERTIES ================================
    protected void initProperties()
    {
        try
        {
            console.logEntry();
            // ------------------ stack properties --------------
            stackAddress = sipProp.getProperty("javax.sip.IP_ADDRESS");
            if (stackAddress == null)
            {
                stackAddress = getLocalHostAddress();
                //Add the host address to the properties that will pass the stack
                sipProp.setProperty("javax.sip.IP_ADDRESS", stackAddress);
            }
            //ensure IPv6 address compliance
            if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
            && stackAddress.charAt(0) != '['
            )
            {
                stackAddress = '[' + stackAddress.trim() + ']';
            }
            if (console.isDebugEnabled())
            {
                console.debug("stack address=" + stackAddress);
            }
            stackName = sipProp.getProperty("javax.sip.STACK_NAME");
            if (stackName == null)
            {
                stackName = "SipCommunicator@" + Integer.toString(hashCode());
                //Add the stack name to the properties that will pass the stack
                sipProp.setProperty("javax.sip.STACK_NAME", stackName);
            }
            if (console.isDebugEnabled())
            {
                console.debug("stack name is:" + stackName);
            }

            String retransmissionFilter = sipProp.getProperty("javax.sip.RETRANSMISSION_FILTER");
            if (retransmissionFilter == null)
            {
                retransmissionFilter = "true";
                //Add the retransmission filter param to the properties that will pass the stack
                sipProp.setProperty("javax.sip.RETRANSMISSION_FILTER", retransmissionFilter);
            }
            if (console.isDebugEnabled())
            {
                console.debug("retransmission filter is:" + stackName);
            }
            //------------ application properties --------------
            currentlyUsedURI = sipProp.getProperty(
            "net.java.sip.communicator.sip.PUBLIC_ADDRESS");
            if (currentlyUsedURI == null)
            {
                currentlyUsedURI = sipProp.getProperty("user.name") + "@" +
                stackAddress;
            }
            if (!currentlyUsedURI.trim().toLowerCase().startsWith("sip:"))
            {
                currentlyUsedURI = "sip:" + currentlyUsedURI.trim();
            }

            if (console.isDebugEnabled())
            {
                console.debug("public address=" + currentlyUsedURI);
            }
            registrarAddress = sipProp.getProperty(
            "net.java.sip.communicator.sip.REGISTRAR_ADDRESS");
            if (console.isDebugEnabled())
            {
                console.debug("registrar address=" + registrarAddress);
            }
            try
            {
                registrarPort = Integer.parseInt(sipProp.getProperty(
                "net.java.sip.communicator.sip.REGISTRAR_PORT"));
            }
            catch (NumberFormatException ex)
            {
                registrarPort = 5060;
            }
            if (console.isDebugEnabled())
            {
                console.debug("registrar port=" + registrarPort);
            }
            registrarTransport = sipProp.getProperty(
            "net.java.sip.communicator.sip.REGISTRAR_TRANSPORT");
            if (registrarTransport == null)
            {
                registrarTransport = DEFAULT_TRANSPORT;
            }
            try
            {
                registrationsExpiration = Integer.parseInt(sipProp.getProperty(
                "net.java.sip.communicator.sip.REGISTRATIONS_EXPIRATION"));
            }
            catch (NumberFormatException ex)
            {
                registrationsExpiration = 3600;
            }
            if (console.isDebugEnabled())
            {
                console.debug("registrar transport=" + registrarTransport);
                // Added by mranga
            }
            String serverLog = sipProp.getProperty
            ("gov.nist.javax.sip.SERVER_LOG");
            if (serverLog != null)
            {
                sipProp.setProperty
                ("gov.nist.javax.sip.TRACE_LEVEL", "16");
            }
            if (console.isDebugEnabled())
            {
                console.debug("server log=" + serverLog);
            }
            sipStackPath = sipProp.getProperty(
            "net.java.sip.communicator.sip.STACK_PATH");
            if (sipStackPath == null)
            {
                sipStackPath = "gov.nist";
            }
            if (console.isDebugEnabled())
            {
                console.debug("stack path=" + sipStackPath);
            }
            String routerPath = sipProp.getProperty("javax.sip.ROUTER_PATH");
            if (routerPath == null)
            {
              sipProp.setProperty("javax.sip.ROUTER_PATH",
            "net.sourceforge.gjtapi.raw.sipprovider.sip.SipCommRouter");

            }
            if (console.isDebugEnabled())
            {
                console.debug("router path=" + routerPath);
            }
            transport = sipProp.getProperty("net.java.sip.communicator.sip.TRANSPORT");
            if (transport == null)
            {
                transport = DEFAULT_TRANSPORT;
            }
            if (console.isDebugEnabled())
            {
                console.debug("transport=" + transport);
            }
            String localPortStr = sipProp.getProperty(
            "net.java.sip.communicator.sip.PREFERRED_LOCAL_PORT");
            try
            {
                localPort = Integer.parseInt(localPortStr);
            }
            catch (NumberFormatException exc)
            {
                localPort = 5060;
            }
            if (console.isDebugEnabled())
            {
                console.debug("preferred local port=" + localPort);
            }
            displayName = sipProp.getProperty(
            "net.java.sip.communicator.sip.DISPLAY_NAME");
            if (console.isDebugEnabled())
            {
                console.debug("display name=" + displayName);
            }
        }
        finally
        {
            console.logExit();
        }
    }

    //============================     SECURITY     ================================
    /**
     * Sets the SecurityAuthority instance that should be consulted later on for
     * user credentials.
     *
     * @param authority the SecurityAuthority instance that should be consulted
     * later on for user credentials.
     */
    public void setSecurityAuthority(SecurityAuthority authority)
    {
        //keep a copty
        this.securityAuthority = authority;
        sipSecurityManager.setSecurityAuthority(authority);
    }

    /**
     * Adds the specified credentials to the security manager's credentials cache
     * so that they get tried next time they're needed.
     *
     * @param realm the realm these credentials should apply for.
     * @param credentials a set of credentials (username and pass)
     */
    public void cacheCredentials(String realm, UserCredentials credentials )
    {
        sipSecurityManager.cacheCredentials(realm, credentials);
    }
    //============================ EVENT DISPATHING ================================
    /**
     * Adds a CommunicationsListener to SipManager.
     * @param listener The CommunicationsListener to be added.
     */
    public void addCommunicationsListener(CommunicationsListener listener) {
        listeners.add(listener);
    }

    //------------ call received dispatch
    public void fireCallReceived(Call call)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("received call" + call);
            }
            CallEvent evt = new CallEvent(call);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.callReceived(evt);
            }
        }
        finally
        {
            console.logExit();
        }
    } //call received

    //------------ call received dispatch
    void fireMessageReceived(Request message)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("received instant message=" + message);
            }
            MessageEvent evt = new MessageEvent(message);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.messageReceived(evt);
            }
        }
        finally
        {
            console.logExit();
        }
    } //call received

    //------------ registerred
    public void fireRegistered(String address)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("registered with address = " + address);
            }
            RegistrationEvent evt = new RegistrationEvent(address);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.registered(evt);
            }
        }
        finally
        {
            console.logExit();
        }
    } //call received

    //------------ registering
    public void fireRegistering(String address)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("registering with address=" + address);
            }
            RegistrationEvent evt = new RegistrationEvent(address);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.registering(evt);
            }
        }
        finally
        {
            console.logExit();
        }
    } //call received

    //------------ unregistered
    public void fireUnregistered(String address)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("unregistered, address is " + address);
            }
            RegistrationEvent evt = new RegistrationEvent(address);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.unregistered(evt);
            }
        }
        finally
        {
            console.logExit();
        }
    } //call received

    public void fireUnregistering(String address)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("unregistering, address is " + address);
            }
            RegistrationEvent evt = new RegistrationEvent(address);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.unregistering(evt);
            }
        }
        finally
        {
            console.logExit();
        }
    } //call received


    //---------------- received unknown message
    public void fireUnknownMessageReceived(Message message)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("unknown message=" + message);
            }
            UnknownMessageEvent evt = new UnknownMessageEvent(message);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.receivedUnknownMessage(evt);
            }
        }
        finally
        {
            console.logExit();
        }
    } //unknown message

    //---------------- rejected a call
    public void fireCallRejectedLocally(String reason, Message invite)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("locally rejected call. reason="
                + reason
                + "\ninvite message=" + invite);
            }
            CallRejectedEvent evt = new CallRejectedEvent(reason, invite);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.callRejectedLocally(
                evt);
            }
        }
        finally
        {
            console.logExit();
        }
    }

    public void fireCallRejectedRemotely(String reason, Message invite)
    {
        try
        {
            console.logEntry();
            if (console.isDebugEnabled())
            {
                console.debug("call rejected remotely. reason="
                + reason
                + "\ninvite message=" + invite);
            }
            CallRejectedEvent evt = new CallRejectedEvent(reason, invite);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.callRejectedRemotely(
                evt);
            }
        }
        finally
        {
            console.logExit();
        }
    }

    //call rejected
    //---------------- error occurred
    public void fireCommunicationsError(Throwable throwable)
    {
        try
        {
            console.logEntry();
            console.error(throwable);
            CommunicationsErrorEvent evt = new CommunicationsErrorEvent(
            throwable);
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                CommunicationsListener current =
                    (CommunicationsListener) iterator.next();
                current.communicationsErrorOccurred(evt);
            }
        }
        finally
        {
            console.logExit();
        }
    } //error occurred

    //============================= SIP LISTENER METHODS ==============================
    public void processRequest(RequestEvent requestReceivedEvent)
    {
        try
        {
            console.logEntry();
            Request request = requestReceivedEvent.getRequest();
            if (console.isDebugEnabled())
            {
                console.debug("received request=" + request.getMethod());
            }
            ServerTransaction serverTransaction = requestReceivedEvent.
            getServerTransaction();
            if (serverTransaction == null)
            {
                try
                {
                    serverTransaction = sipProvider.getNewServerTransaction(
                    request);
                }
                catch (TransactionAlreadyExistsException ex)
                {
                    /*fireCommunicationsError(
                        new CommunicationsException(
                        "Failed to create a new server"
                        + "transaction for an incoming request\n"
                        + "(Next message contains the request)",
                        ex));
                    fireUnknownMessageReceived(request);*/
                    //let's not scare the user
                    console.error("Failed to create a new server"
                    + "transaction for an incoming request\n"
                    + "(Next message contains the request)",
                    ex
                    );

                    return;
                }
                catch (TransactionUnavailableException ex)
                {
                    /**
                     * fireCommunicationsError(
                     * new CommunicationsException(
                     * "Failed to create a new server"
                     * + "transaction for an incoming request\n"
                     * + "(Next message contains the request)",
                     * ex));
                     * fireUnknownMessageReceived(request);*/
                    //let's not scare the user
                    console.error("Failed to create a new server"
                    + "transaction for an incoming request\n"
                    + "(Next message contains the request)",
                    ex
                    );
                    return;
                }
            }
            Dialog dialog = serverTransaction.getDialog();
            String method = request.getMethod();
            //INVITE
            if (method.equals(Request.INVITE))
            {
                console.debug("received INVITE");
                if(serverTransaction.getDialog().getState() == null)
                {
                    if(console.isDebugEnabled())
                        console.debug("request is an INVITE. Dialog state="
                        +serverTransaction.getDialog().getState());
                    callProcessing.processInvite(serverTransaction, request);
                }
                else
                {
                    console.debug("request is a reINVITE. Dialog state="
                    +serverTransaction.getDialog().getState());
                    callProcessing.processReInvite(serverTransaction, request);
                }
            }
            //ACK
            else if (method.equals(Request.ACK))
            {
                if (serverTransaction != null
                && serverTransaction.getDialog().getFirstTransaction().
                getRequest().getMethod().equals(Request.INVITE))
                {
                    callProcessing.processAck(serverTransaction, request);
                }
                else
                {
                    // just ignore
                    console.debug("ignoring ack");
                }
            }
            //BYE
            else if (method.equals(Request.BYE))
            {
                if (dialog.getFirstTransaction().getRequest().getMethod().
                equals(
                Request.INVITE))
                {
                    callProcessing.processBye(serverTransaction, request);
                }
            }
            //CANCEL
            else if (method.equals(Request.CANCEL))
            {
                if (dialog.getFirstTransaction().getRequest().getMethod().
                equals(
                Request.INVITE))
                {
                    callProcessing.processCancel(serverTransaction, request);
                }
                else
                {
                    sendNotImplemented(serverTransaction, request);
                    fireUnknownMessageReceived(requestReceivedEvent.getRequest());
                }
            }
            //REFER
            else if (method.equals(Request.REFER))
            {
                console.debug("Received REFER request");
                transferProcessing.processRefer(serverTransaction, request);
            }
            else if (request.getMethod().equals(Request.INFO))
            {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (method.equals(Request.MESSAGE))
            {
                fireMessageReceived(request);
            }
            else if (method.equals(Request.NOTIFY))
            {
                /** @todo add proper request handling */
                //                try {
                //                	watcher.processNotification(request, serverTransaction.getBranchId());
                //                }
                //                catch (CommunicationsException e) {
                //					// TODO: handle exception
                //				}
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (method.equals(Request.OPTIONS))
            {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (method.equals(Request.PRACK))
            {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (method.equals(Request.REGISTER))
            {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (method.equals(Request.SUBSCRIBE))
            {
                /** @todo add proper request handling */

                //fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (method.equals(Request.UPDATE))
            {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else
            {
                //We couldn't recognize the message
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
        }
        finally
        {
            console.logExit();
        }
    }

    public void processTimeout(TimeoutEvent transactionTimeOutEvent)
    {
        try
        {
            console.logEntry();
            Transaction transaction;
            if (transactionTimeOutEvent.isServerTransaction())
            {
                transaction = transactionTimeOutEvent.getServerTransaction();
            }
            else
            {
                transaction = transactionTimeOutEvent.getClientTransaction();
            }
            Request request = transaction.getRequest();
            String method = request.getMethod();
            if (console.isDebugEnabled())
            {
                console.debug("received time out event: " + method);
            }
            if (method.equals(Request.REGISTER))
            {
                registerProcessing.processTimeout(transaction, request);
            }
            else if (method.equals(Request.INVITE))
            {
                callProcessing.processTimeout(transaction, request);
            }
            else
            {
                //Just show an error for now
                console.error("TimeOut Error!:"
                        + "Received a TimeoutEvent while waiting on a message"
                        + "\n(Check Details to see the message that caused it)\n"
                        + request.toString());
            }
        }
        finally
        {
            console.logExit();
        }
    }

    //-------------------- PROCESS RESPONSE
    public void processResponse(ResponseEvent responseReceivedEvent)
    {
        try
        {
            console.logEntry();
            Response response = responseReceivedEvent.getResponse();
            String method = ( (CSeqHeader) response.getHeader(CSeqHeader.NAME)).
                getMethod();
            if (console.isDebugEnabled())
            {
                console.debug("received response=" + method);
            }
            ClientTransaction clientTransaction = responseReceivedEvent.
            getClientTransaction();
            if (clientTransaction == null)
            {
                console.debug("ignoring a transactionless response");
                return;
            }
            if (response.getStatusCode() == Response.OK)
            {
                //REGISTER
                if (method.equals(Request.REGISTER))
                {
                    registerProcessing.processOK(clientTransaction, response);
                }//INVITE
                else if (method.equals(Request.INVITE))
                {
                    callProcessing.processInviteOK(clientTransaction, response);
                }//BYE
                else if (method.equals(Request.BYE))
                {
                    callProcessing.processByeOK(clientTransaction, response);
                }//CANCEL
                else if (method.equals(Request.CANCEL))
                {
                    callProcessing.processCancelOK(clientTransaction, response);
                }
            }
            //TRYING
            else if (response.getStatusCode() == Response.TRYING
            //process all provisional responses here
            //reported by Les Roger Davis
            || response.getStatusCode() / 100 == 1)
            {
                if (method.equals(Request.INVITE))
                {
                    callProcessing.processTrying(clientTransaction, response);
                }
                //We could also receive a TRYING response to a REGISTER req
                //bug reports by
                //Steven Lass <sltemp at comcast.net>
                //Luis Vazquez <luis at teledata.com.uy>
                else if(method.equals(Request.REGISTER))
                {
                    //do nothing
                }
                else
                {
                    fireUnknownMessageReceived(response);
                }
            }
            //RINGING
            else if (response.getStatusCode() == Response.RINGING)
            {
                if (method.equals(Request.INVITE))
                {
                    callProcessing.processRinging(clientTransaction, response);
                }
                else
                {
                    fireUnknownMessageReceived(response);
                }
            }
            //NOT_FOUND
            else if (response.getStatusCode() == Response.NOT_FOUND)
            {
                if (method.equals(Request.INVITE))
                {
                    callProcessing.processNotFound(clientTransaction, response);
                }
                else
                {
                    fireUnknownMessageReceived(response);
                }
            }
            //NOT_IMPLEMENTED
            else if (response.getStatusCode() == Response.NOT_IMPLEMENTED)
            {
                if (method.equals(Request.INVITE))
                {
                    registerProcessing.processNotImplemented(clientTransaction,
                    response);
                }
                else if (method.equals(Request.REGISTER))
                {
                    callProcessing.processNotImplemented(clientTransaction,
                    response);
                }
                else
                {
                    fireUnknownMessageReceived(response);
                }
            }
            //REQUEST_TERMINATED
            else if (response.getStatusCode() == Response.REQUEST_TERMINATED)
            {
                callProcessing.processRequestTerminated(clientTransaction,
                response);
            }
            //BUSY_HERE
            else if (response.getStatusCode() == Response.BUSY_HERE)
            {
                if (method.equals(Request.INVITE))
                {
                    callProcessing.processBusyHere(clientTransaction, response);
                }
                else
                {
                    fireUnknownMessageReceived(response);
                }
            }
            //401 UNAUTHORIZED
            else if (response.getStatusCode() == Response.UNAUTHORIZED
            || response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED)
            {
                if(method.equals(Request.INVITE))
                    callProcessing.processAuthenticationChallenge(clientTransaction, response);
                else if(method.equals(Request.REGISTER))
                    registerProcessing.processAuthenticationChallenge(clientTransaction, response);
               /* else if(method.equals(Request.SUBSCRIBE))
                    watcher.processAuthenticationChallenge(clientTransaction, response);*/
                else
                    fireUnknownMessageReceived(response);
            }
            //Other Errors
            else if ( //We'll handle all errors the same way so no individual handling
            //is needed
            //response.getStatusCode() == Response.NOT_ACCEPTABLE
            //|| response.getStatusCode() == Response.SESSION_NOT_ACCEPTABLE
            response.getStatusCode() / 100 == 4
            )
            {
                if (method.equals(Request.INVITE))
                {
                    callProcessing.processCallError(clientTransaction, response);
                }
                else
                {
                    fireUnknownMessageReceived(response);
                }

            }
            else if (response.getStatusCode() == Response.ACCEPTED)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.ADDRESS_INCOMPLETE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.ALTERNATIVE_SERVICE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.AMBIGUOUS)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BAD_EVENT)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BAD_EXTENSION)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BAD_GATEWAY)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BAD_REQUEST)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BUSY_EVERYWHERE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
            Response.CALL_IS_BEING_FORWARDED)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
            Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.DECLINE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
            Response.DOES_NOT_EXIST_ANYWHERE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.EXTENSION_REQUIRED)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.FORBIDDEN)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.GONE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.INTERVAL_TOO_BRIEF)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.LOOP_DETECTED)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.MESSAGE_TOO_LARGE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.METHOD_NOT_ALLOWED)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.MOVED_PERMANENTLY)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.MOVED_TEMPORARILY)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.MULTIPLE_CHOICES)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.NOT_ACCEPTABLE_HERE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.PAYMENT_REQUIRED)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.QUEUED)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
            Response.REQUEST_ENTITY_TOO_LARGE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.REQUEST_PENDING)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.REQUEST_TIMEOUT)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.REQUEST_URI_TOO_LONG)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.SERVER_INTERNAL_ERROR)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.SERVER_TIMEOUT)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.SERVICE_UNAVAILABLE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
            Response.SESSION_NOT_ACCEPTABLE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.SESSION_PROGRESS)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
            Response.TEMPORARILY_UNAVAILABLE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.TOO_MANY_HOPS)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.UNDECIPHERABLE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
            Response.UNSUPPORTED_MEDIA_TYPE)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
            Response.UNSUPPORTED_URI_SCHEME)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.USE_PROXY)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.VERSION_NOT_SUPPORTED)
            {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else
            { //We couldn't recognise the message
                fireUnknownMessageReceived(response);
            }
        }
        finally
        {
            console.logExit();
        }
    } //process response

    //--------
    public String getLocalHostAddress()
    {
        try
        {
            console.logEntry();
            String hostAddress = sipProp.getProperty("javax.sip.IP_ADDRESS");
            if (hostAddress == null)
            {
                InetAddress localhost = addressManager.getLocalHost();
                hostAddress = localhost.getHostAddress();
            }
            if (console.isDebugEnabled())
            {
                console.debug("returning addres=" + hostAddress);
            }
            return hostAddress;
        }
        finally
        {
            console.logExit();
        }
    }

    protected void checkIfStarted() throws CommunicationsException
    {
        if (!isStarted)
        {
            console.error("attempt to use the stack while not started");
            throw new CommunicationsException(
            "The underlying SIP Stack had not been"
            + "properly initialised! Impossible to continue");
        }
    }

    public void sendServerInternalError(int callID) throws
    CommunicationsException
    {
        try
        {
            console.logEntry();
            checkIfStarted();
            callProcessing.sayInternalError(callID);
        }
        finally
        {
            console.logExit();
        }
    }

    public String getAddress()
    {
        return "sip:" + this.getLocalUser() + "@" + this.getLocalHostAddress();

    }
    //======================================= SIMPLE ==========================================

    /**
     * Retrieves a Contact List from the specified URL.
     * @param url the location where the list is to be retrieved from.
     * @throws CommunicationsException if we fail to retrieve the list.
     * @return ContactGroup the contact list retrieved from the specified URL
     */








}
