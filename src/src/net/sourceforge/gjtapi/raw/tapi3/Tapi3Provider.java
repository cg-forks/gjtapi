/*
	Copyright (c) 2005 Serban Iordache 
	
	All rights reserved. 
	
	Permission is hereby granted, free of charge, to any person obtaining a 
	copy of this software and associated documentation files (the 
	"Software"), to deal in the Software without restriction, including 
	without limitation the rights to use, copy, modify, merge, publish, 
	distribute, and/or sell copies of the Software, and to permit persons 
	to whom the Software is furnished to do so, provided that the above 
	copyright notice(s) and this permission notice appear in all copies of 
	the Software and that both the above copyright notice(s) and this 
	permission notice appear in supporting documentation. 
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
	OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
	HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
	INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
	FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
	NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
	WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
	
	Except as contained in this notice, the name of a copyright holder 
	shall not be used in advertising or otherwise to promote the sale, use 
	or other dealings in this Software without prior written authorization 
	of the copyright holder.
*/
package net.sourceforge.gjtapi.raw.tapi3;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.telephony.Event;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.TerminalConnection;
import javax.telephony.media.MediaResourceException;
import javax.telephony.media.MediaRuntimeException;
import javax.telephony.media.RTC;
import javax.telephony.media.SignalConstants;
import javax.telephony.media.Symbol;

import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.RawStateException;
import net.sourceforge.gjtapi.TelephonyListener;
import net.sourceforge.gjtapi.TermData;
import net.sourceforge.gjtapi.capabilities.Capabilities;
import net.sourceforge.gjtapi.raw.CCTpi;
import net.sourceforge.gjtapi.raw.MediaTpi;
import net.sourceforge.gjtapi.raw.tapi3.logging.ConsoleLogger;
import net.sourceforge.gjtapi.raw.tapi3.logging.Logger;
import net.sourceforge.gjtapi.raw.tapi3.logging.PrintStreamLogger;

public class Tapi3Provider implements CCTpi, MediaTpi {
    private static Logger logger = new ConsoleLogger(); // new NullLogger();

    public static final int METHOD_ADDRESS_PRIVATE_DATA = 1;
    public static final int METHOD_CALL_ACTIVE = 2;
    public static final int METHOD_CALL_INVALID = 3;
    public static final int METHOD_CALL_PRIVATE_DATA = 4;
    public static final int METHOD_CONNECTION_ALERTING = 5;
    public static final int METHOD_CONNECTION_CONNECTED = 6;
    public static final int METHOD_CONNECTION_DISCONNECTED = 7;
    public static final int METHOD_CONNECTION_FAILED = 8;
    public static final int METHOD_CONNECTION_IN_PROGRESS = 9;
    public static final int METHOD_PROVIDER_PRIVATE_DATA = 10;
    public static final int METHOD_TERMINAL_CONNECTION_CREATED = 11;
    public static final int METHOD_TERMINAL_CONNECTION_DROPPED = 12;
    public static final int METHOD_TERMINAL_CONNECTION_HELD = 13;
    public static final int METHOD_TERMINAL_CONNECTION_RINGING = 14;
    public static final int METHOD_TERMINAL_CONNECTION_TALKING = 15;
    public static final int METHOD_TERMINAL_PRIVATE_DATA = 16;

    private static final String[] METHOD_NAMES = {
            "METHOD_NONE",
            "METHOD_ADDRESS_PRIVATE_DATA",
            "METHOD_CALL_ACTIVE",
            "METHOD_CALL_INVALID",
            "METHOD_CALL_PRIVATE_DATA",
            "METHOD_CONNECTION_ALERTING",
            "METHOD_CONNECTION_CONNECTED",
            "METHOD_CONNECTION_DISCONNECTED",
            "METHOD_CONNECTION_FAILED",
            "METHOD_CONNECTION_IN_PROGRESS",
            "METHOD_PROVIDER_PRIVATE_DATA",
            "METHOD_TERMINAL_CONNECTION_CREATED",
            "METHOD_TERMINAL_CONNECTION_DROPPED",
            "METHOD_TERMINAL_CONNECTION_HELD",
            "METHODTERMINAL_CONNECTION_RINGING",
            "METHOD_TERMINAL_CONNECTION_TALKING",
            "METHOD_TERMINAL_PRIVATE_DATA",
    };

    public static final int JNI_CAUSE_UNKNOWN = -1;
    public static final int JNI_CAUSE_NORMAL = 1;
    public static final int JNI_CAUSE_NEW_CALL = 2;
    public static final int JNI_CAUSE_SNAPSHOT = 3;
    public static final int JNI_CAUSE_DEST_NOT_OBTAINABLE = 4;

    private static Tapi3Native tapi3Native;
    private String[] addresses = new String[0];
    private TermData[] terminals = new TermData[0];
    private ArrayList listenerList = new ArrayList();

    public static Logger getLogger() {
        return logger;
    }

    private static void configureLogger(Map props) {
        String tapi3LogOut = (String)props.get("tapi3.log.out");
        if(tapi3LogOut != null) {
            if("console".equals(tapi3LogOut)) {
                logger = new PrintStreamLogger(System.err);
            } else {
                try {
                    PrintStream logStream = new PrintStream(new FileOutputStream(tapi3LogOut));
                    logger = new PrintStreamLogger(logStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    logger = new PrintStreamLogger(System.err);
                }
            }
        }
        String tapi3LogClass = (String)props.get("tapi3.log.class");
        if(tapi3LogClass != null) {
            try {
                Class cls = Class.forName(tapi3LogClass);
                logger = (Logger)cls.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                logger = new PrintStreamLogger(System.err);
            }
        }
    }

    private static void configureNative(Map props) {
        String tapi3ImplClass = (String)props.get("tapi3.impl.class");
        if(tapi3ImplClass == null) {
            tapi3ImplClass = "net.sourceforge.gjtapi.raw.tapi3.Tapi3NativeImpl";
        }
        logger.debug("Trying to instantiate " + tapi3ImplClass);
        try {
            Class cls = Class.forName(tapi3ImplClass);
            Method m = cls.getMethod("getInstance", null);
            tapi3Native = (Tapi3Native)m.invoke(null, null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        logger.debug("tapi3Native successfully created.");
    }

    private static String getMethodName(int methodID) {
        String name = "<UNKNOWN>";
        if(methodID >= 0 && methodID < METHOD_NAMES.length) {
            name = METHOD_NAMES[methodID];
        }
        return name;
    }

    private static int getEventCause(int jniCause) {
        switch(jniCause) {
            case JNI_CAUSE_NORMAL: return Event.CAUSE_NORMAL;
            case JNI_CAUSE_NEW_CALL: return Event.CAUSE_NEW_CALL;
            case JNI_CAUSE_SNAPSHOT: return Event.CAUSE_SNAPSHOT;
            case JNI_CAUSE_DEST_NOT_OBTAINABLE: return Event.CAUSE_DEST_NOT_OBTAINABLE;
        }
        return Event.CAUSE_UNKNOWN;
    }
    public void callback(int methodID, int callID, String address, int jniCause, String[] callInfo) {
        Tapi3CallID tapi3CallID = new Tapi3CallID(callID);
        Iterator it = listenerList.iterator();
        String terminal = address;  // !!!
//        String terminal = getTerminals(address)[0].terminal;
        Tapi3PrivateData privateData = null;
        if(callInfo != null && callInfo.length == 4) {
            privateData = new Tapi3PrivateData(callInfo[0], callInfo[1], callInfo[2], callInfo[3]);
        }
        int eventCause = getEventCause(jniCause);
        String methodName = getMethodName(methodID);
        logger.info("CALLBACK: " + methodID + " (" + methodName + ") on " + address +
                ": callID=" + tapi3CallID.getCallID() + ", privateData: " + privateData);
        while(it.hasNext()) {
            TelephonyListener listener = (TelephonyListener)it.next();
            switch(methodID) {
                case METHOD_ADDRESS_PRIVATE_DATA:
                    listener.addressPrivateData(address, privateData, eventCause);
                    break;
                case METHOD_CALL_ACTIVE:
                    listener.callActive(tapi3CallID, eventCause);
                    break;
                case METHOD_CALL_INVALID:
                    listener.callInvalid(tapi3CallID, eventCause);
                    break;
                case METHOD_CALL_PRIVATE_DATA:
                	// Called at exit
                    // listener.callPrivateData(tapi3CallID, privateData, eventCause);
                    break;
                case METHOD_CONNECTION_ALERTING:
                    listener.connectionAlerting(tapi3CallID, address, eventCause);
                    break;
                case METHOD_CONNECTION_CONNECTED:
                    listener.connectionConnected(tapi3CallID, address, eventCause);
                    break;
                case METHOD_CONNECTION_DISCONNECTED:
                    listener.connectionDisconnected(tapi3CallID, address, eventCause);
                    break;
                case METHOD_CONNECTION_FAILED:
                    listener.connectionFailed(tapi3CallID, address, eventCause);
                    break;
                case METHOD_CONNECTION_IN_PROGRESS:
                    listener.connectionInProgress(tapi3CallID, address, eventCause);
                    break;
                case METHOD_PROVIDER_PRIVATE_DATA:
                    listener.providerPrivateData(privateData, eventCause);
                    break;
                case METHOD_TERMINAL_CONNECTION_CREATED:
                    listener.terminalConnectionCreated(tapi3CallID, address, terminal, eventCause);
                    break;
                case METHOD_TERMINAL_CONNECTION_DROPPED:
                    listener.terminalConnectionDropped(tapi3CallID, address, terminal, eventCause);
                    break;
                case METHOD_TERMINAL_CONNECTION_HELD:
                    listener.terminalConnectionHeld(tapi3CallID, address, terminal, eventCause);
                    break;
                case METHOD_TERMINAL_CONNECTION_RINGING:
                    listener.terminalConnectionRinging(tapi3CallID, address, terminal, eventCause);
                    break;
                case METHOD_TERMINAL_CONNECTION_TALKING:
                    listener.terminalConnectionTalking(tapi3CallID, address, terminal, eventCause);
                    break;
                case METHOD_TERMINAL_PRIVATE_DATA:
                    listener.terminalPrivateData(terminal, privateData, eventCause);
                    break;
                default:
                    logger.error("CALLBACK: Unknown method: " + methodID + ", callID=" + callID +
                            ", address=" + address + ", jniCause=" + jniCause + ", callInfo=" +
                            (callInfo != null ? Arrays.asList(callInfo).toString() : "null"));
                    break;
            }
            if(privateData != null) {
                listener.callPrivateData(tapi3CallID, privateData, eventCause);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#initialize(java.util.Map)
     */
    public void initialize(Map props) throws ProviderUnavailableException {
    	configureProperties(props);
		configureLogger(props);
        logger.debug("Tapi3 properties: " + props + " ...");
        configureNative(props);        
        logger.debug("Initializing Tapi3 provider...");
        addresses = tapi3Native.tapi3Init(props);
        logger.debug("Registering Tapi3 provider...");
        tapi3Native.registerProvider(this);

        logger.debug("Retrieving addresses...");
        if(addresses == null) {
            addresses = new String[0];
        }
        terminals = new TermData[addresses.length];
        for(int i=0; i<terminals.length; i++) {
            logger.info("Address #" + (i+1) + ": " + addresses[i]);
            terminals[i] = new TermData(addresses[i], false);
        }
        logger.debug("Initialized");
    }

    public static void configureProperties(Map props) {
    	Iterator it = props.entrySet().iterator();
    	while(it.hasNext()) {
        	Map.Entry entry = (Map.Entry) it.next();
			StringBuffer sbufVal = new StringBuffer((String)entry.getValue());
        	while(true) {
        		int startPos = sbufVal.indexOf("${");
        		if(startPos < 0) break;
        		int endPos = sbufVal.indexOf("}", startPos+3);
        		if(endPos < 0) break;
        		String sysProp = sbufVal.substring(startPos+2, endPos);
        		String replaceVal = System.getProperty(sysProp);
        		if(replaceVal != null) {
        			sbufVal.replace(startPos, endPos+1, replaceVal); 
        		}
        	}
        	entry.setValue(sbufVal.toString());
    	}
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#shutdown()
     */
    public void shutdown() {
        logger.debug("Shutting down...");
        int retCode = tapi3Native.tapi3Shutdown();
        logger.debug("Shut down: retCode=" + retCode);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#addListener(net.sourceforge.gjtapi.TelephonyListener)
     */
    public void addListener(TelephonyListener ro) {
        logger.debug("addListener(" + ro.getClass().getName() + ")");
        listenerList.add(ro);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#removeListener(net.sourceforge.gjtapi.TelephonyListener)
     */
    public void removeListener(TelephonyListener ro) {
        logger.debug("removeListener()");
        listenerList.remove(ro);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#getCapabilities()
     */
    public Properties getCapabilities() {
        logger.debug("getCapabilities()");
        Properties caps = new Properties();
        // mark my differences from the default
//      caps.put(Capabilities.HOLD, "f");
//      caps.put(Capabilities.JOIN, "f");
        caps.put(Capabilities.THROTTLE, "f");
        caps.put(Capabilities.MEDIA, "f");
        caps.put(Capabilities.ALL_MEDIA_TERMINALS, "f");
        caps.put(Capabilities.ALLOCATE_MEDIA, "f");

        return caps;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#getAddresses()
     */
    public String[] getAddresses() throws ResourceUnavailableException {
        logger.debug("getAddresses()");
        return addresses;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#getAddresses(java.lang.String)
     */
    public String[] getAddresses(String terminal) throws InvalidArgumentException {
        logger.debug("getAddresses(" + terminal + ")");
        for(int i=0; i<terminals.length; i++) {
            if(terminals[i].terminal.equals(terminal)) {
                return new String[] { addresses[i] };
            }
        }
        return new String[0];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#getTerminals()
     */
    public TermData[] getTerminals() throws ResourceUnavailableException {
        logger.debug("getTerminals()");
        return terminals;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#getTerminals(java.lang.String)
     */
    public TermData[] getTerminals(String address) throws InvalidArgumentException {
        logger.debug("getTerminals(" + address + ")");
        for(int i=0; i<addresses.length; i++) {
            if(addresses[i].equals(address)) {
                return new TermData[] { terminals[i] };
            }
        }
        return new TermData[0];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#reserveCallId(java.lang.String)
     */
    public CallId reserveCallId(String address) throws InvalidArgumentException {
        logger.debug("reserveCallId(" + address + ")");
        int id = tapi3Native.tapi3ReserveCallId(address);
        return (id < 0) ? null : new Tapi3CallID(id);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#releaseCallId(net.sourceforge.gjtapi.CallId)
     */
    public void releaseCallId(CallId id) {
        logger.debug("releaseCallId(" + id + ")");
        if(id instanceof Tapi3CallID) {
            Tapi3CallID tapi3CallID = (Tapi3CallID)id;
            int retCode = tapi3Native.tapi3ReleaseCall(tapi3CallID.getCallID());
            logger.debug("tapi3ReleaseCall() returned: " + retCode);
        } else {
            logger.warn("Not a Tapi3CallID: " + id);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#createCall(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String, java.lang.String)
     */
    public CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException,
            PrivilegeViolationException, InvalidPartyException, InvalidArgumentException, RawStateException,
            MethodNotSupportedException {
        logger.debug("createCall(" + id + ", " + address + ", " + term + ", " + dest + ")");
        if(id instanceof Tapi3CallID) {
            Tapi3CallID tapi3CallID = (Tapi3CallID)id;
            int retCode = tapi3Native.tapi3CreateCall(tapi3CallID.getCallID(), address, dest);
            logger.debug("tapi3CreateCall() returned: " + Integer.toHexString(retCode));
            if(retCode < 0) {
                throw new InvalidPartyException(InvalidPartyException.UNKNOWN_PARTY, "Error code: " + Integer.toHexString(retCode));
            }
            return id;
        } else {
            throw new MethodNotSupportedException("Not a Tapi3CallID: " + id);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CoreTpi#answerCall(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String)
     */
    public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException,
            ResourceUnavailableException, MethodNotSupportedException, RawStateException {
        logger.debug("answerCall(" + call + ", " + address + ", " + terminal + ")");
        if(call instanceof Tapi3CallID) {
            Tapi3CallID tapi3CallID = (Tapi3CallID)call;
            int retCode = tapi3Native.tapi3AnswerCall(tapi3CallID.getCallID());
            logger.debug("tapi3AnswerCall() returned: 0x" + Integer.toHexString(retCode));
            if(retCode != 0) {
                throw new RawStateException(call, TerminalConnection.UNKNOWN);
            }
        } else {
            logger.warn("Not a Tapi3CallID: " + call);
            throw new MethodNotSupportedException("Not a Tapi3CallID: " + call);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#release(java.lang.String, net.sourceforge.gjtapi.CallId)
     */
    public void release(String address, CallId call) throws PrivilegeViolationException, ResourceUnavailableException,
            MethodNotSupportedException, RawStateException {
        logger.debug("release(" + address + ", " + call + ")");
        if(call instanceof Tapi3CallID) {
            Tapi3CallID tapi3CallID = (Tapi3CallID)call;
            int retCode = tapi3Native.tapi3DisconnectCall(tapi3CallID.getCallID());
            logger.debug("tapi3DisconnectCall() returned: 0x" + Integer.toHexString(retCode));
            if(retCode != 0) {
                throw new RawStateException(call, TerminalConnection.UNKNOWN);
            }
        } else {
            logger.warn("Not a Tapi3CallID: " + call);
            throw new MethodNotSupportedException("Not a Tapi3CallID: " + call);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CCTpi#hold(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String)
     */
    public void hold(CallId call, String address, String terminal) throws RawStateException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException {
        logger.debug("hold(" + call + ", " + address + ", " + terminal + ")");
        if(call instanceof Tapi3CallID) {
            Tapi3CallID tapi3CallID = (Tapi3CallID)call;
            int retCode = tapi3Native.tapi3Hold(tapi3CallID.getCallID());
            logger.debug("tapi3Hold() returned: 0x" + Integer.toHexString(retCode));
            if(retCode != 0) {
                throw new RawStateException(call, TerminalConnection.UNKNOWN);
            }
        } else {
            logger.warn("Not a Tapi3CallID: " + call);
            throw new MethodNotSupportedException("Not a Tapi3CallID: " + call);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CCTpi#join(net.sourceforge.gjtapi.CallId, net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String)
     */
    public CallId join(CallId call1, CallId call2, String address, String terminal) throws RawStateException, InvalidArgumentException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException {
        logger.debug("join(" + call1 + ", " + call2 + ", " + address + ", " + terminal + ")");
        if((call1 instanceof Tapi3CallID) && (call2 instanceof Tapi3CallID)) {
            Tapi3CallID tapi3CallID1 = (Tapi3CallID)call1;
            Tapi3CallID tapi3CallID2 = (Tapi3CallID)call2;
            int joinCallID = tapi3Native.tapi3Join(tapi3CallID1.getCallID(), tapi3CallID2.getCallID());
            if(joinCallID >= 0) {
                logger.debug("tapi3Hold() returned callID: " + joinCallID);
                return new Tapi3CallID(joinCallID);
            } else {
                logger.error("Cannot join (errorCode=" + joinCallID + ")");
                throw new RawStateException(call1, TerminalConnection.UNKNOWN);
            }
        } else {
            logger.warn("Not a Tapi3CallID: " + call1 + ", " + call2);
            throw new InvalidArgumentException("Not a Tapi3CallID: " + call1 + ", " + call2);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.gjtapi.raw.CCTpi#unHold(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String)
     */
    public void unHold(CallId call, String address, String terminal) throws RawStateException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException {
        logger.debug("unHold(" + call + ", " + address + ", " + terminal + ")");
        if(call instanceof Tapi3CallID) {
            Tapi3CallID tapi3CallID = (Tapi3CallID)call;
            int retCode = tapi3Native.tapi3UnHold(tapi3CallID.getCallID());
            logger.debug("tapi3UnHold() returned: 0x" + Integer.toHexString(retCode));
            if(retCode != 0) {
                throw new RawStateException(call, TerminalConnection.UNKNOWN);
            }
        } else {
            logger.warn("Not a Tapi3CallID: " + call);
            throw new MethodNotSupportedException("Not a Tapi3CallID: " + call);
        }
    }


    // *** MediaTpi ***    
    public boolean allocateMedia(String terminal, int type, Dictionary resourceArgs) {
        return false;
    }
    public boolean freeMedia(String terminal, int type) {
        return false;
    }
    public boolean isMediaTerminal(String terminal) {
        return false;
    }
    public void play(String terminal, String[] streamIds, int offset, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
        throw new MediaResourceException("Not implemented.");
    }
    public void record(String terminal, String streamId, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
        throw new MediaResourceException("Not implemented.");
    }
    public void stop(String terminal) {
        throw new MediaRuntimeException("Not implemented.") {};
    }
    public void triggerRTC(String terminal, Symbol action) {
        throw new MediaRuntimeException("Not implemented.") {};
    }
    public RawSigDetectEvent retrieveSignals(String terminal, int num, Symbol[] patterns, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
        throw new MediaResourceException("Not implemented.");
    }
    public void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
        logger.debug("sendSignals(" + terminal + ", " + Arrays.asList(syms) + ")");
        int retCode = tapi3Native.tapi3SendSignals(terminal, getSymbolsAsString(syms));
        logger.debug("sendSignals() returned: 0x" + Integer.toHexString(retCode));
        if(retCode != 0) {
            throw new MediaResourceException("Failed to send DTMF tones: errorCode = 0x" + Integer.toHexString(retCode));
        }
    }
    
    public static String getSymbolsAsString(Symbol[] symbols) {
        StringBuffer sbuf = new StringBuffer(symbols.length);
        for(int i=0; i<symbols.length; i++) {
            char ch = getSymbolAsChar(symbols[i]);
            sbuf.append(ch);
        }
        return sbuf.toString();
    }
    public static char getSymbolAsChar(Symbol symbol) {
        if(symbol == SignalConstants.v_DTMF0) return '0';
        if(symbol == SignalConstants.v_DTMF1) return '1';
        if(symbol == SignalConstants.v_DTMF2) return '2';
        if(symbol == SignalConstants.v_DTMF3) return '3';
        if(symbol == SignalConstants.v_DTMF4) return '4';
        if(symbol == SignalConstants.v_DTMF5) return '5';
        if(symbol == SignalConstants.v_DTMF6) return '6';
        if(symbol == SignalConstants.v_DTMF7) return '7';
        if(symbol == SignalConstants.v_DTMF8) return '8';
        if(symbol == SignalConstants.v_DTMF9) return '9';
        if(symbol == SignalConstants.v_DTMFA) return 'A';
        if(symbol == SignalConstants.v_DTMFB) return 'B';
        if(symbol == SignalConstants.v_DTMFC) return 'C';
        if(symbol == SignalConstants.v_DTMFD) return 'D';
        if(symbol == SignalConstants.v_DTMFHash) return '#';
        if(symbol == SignalConstants.v_DTMFStar) return '*';        
        return '\0';
    }
}
