package net.sourceforge.gjtapi.raw.xtapi;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.telephony.Event;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.media.MediaResourceException;
import javax.telephony.media.PlayerConstants;
import javax.telephony.media.RTC;
import javax.telephony.media.RecorderConstants;
import javax.telephony.media.SignalDetectorConstants;
import javax.telephony.media.Symbol;
import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.RawStateException;
import net.sourceforge.gjtapi.TelephonyListener;
import net.sourceforge.gjtapi.TermData;
import net.sourceforge.gjtapi.capabilities.Capabilities;
import net.sourceforge.gjtapi.media.SymbolConvertor;
import net.sourceforge.gjtapi.raw.MediaTpi;

import net.xtapi.serviceProvider.*;

/*
*  GJTAPI - XTAPI Bridge
*  Copyright (C) 2002 Richard Deadman
* 
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
	
	---
	Licenced changed from GPL to the common GJTAPI licence since the
	Free Software Foundation acknowledges that the X11 licence is compatable
	with the GPL and may link to GPL (i.e. XTAPI) code. - 23May2005 Richard Deadman
 *
 * @author  Richard Deadman
 * @version .01
 */

/**
 * This is a bridge GJTAPI service provider that allows XTAPI service providers
 * to be plugged in underneath.
 */
public class XtapiProvider implements MediaTpi, IXTapiCallBack {
	
	/**
	 * This is identifier for calls known by GJTAPI. As such, it has existance
	 * before an XTAPI id is created for it.
	 * <P>Note that this uses identity for equality, since otherwise we have
	 * to keep track of the disposal of a pool of identity tokens.
	 */
	public class XtapiCallId implements CallId {
		// initially not set.
		private int callNum = -1;
		
		// the local address of the call
		private String localAddress = null;
		
		// the remote address of the call, if known
		private String remoteAddress = null;
		public XtapiCallId() {
			super();
		}
		
		/**
		 * Set the XTAPI id for the call.
		 */
		public void setCallNumber(int num) {
			this.callNum = num;
		}
		
		int getCallNum() { return callNum; }
		
		/**
		 * Ensure equality works
		 * Note that if the callNum's are not set, we default to identity
		 */
		public boolean equals(Object other) {
			int myCallId = this.getCallNum();
			if (myCallId == -1)
				return super.equals(other);

			if (other instanceof XtapiCallId) {
				if (myCallId == ((XtapiCallId)other).getCallNum())
					return true;
			}
			return false;
		}
		
		/**
		 * Ensure hashing works
		 */
		/* Removed since the Call Num changes and this will screw up
		 * Hash Tables.
		 * Removed by: Steve Frare, June 9, 2002
		public int hashCode() {
			return this.getCallNum();
		}
		*/
		
		/**
		 * @return
		 */
		public String getLocalAddress() {
			return localAddress;
		}

		/**
		 * @return
		 */
		public String getRemoteAddress() {
			return remoteAddress;
		}

		/**
		 * @param string
		 */
		public void setLocalAddress(String string) {
			localAddress = string;
		}

		/**
		 * @param string
		 */
		public void setRemoteAddress(String string) {
			remoteAddress = string;
		}

	}
	
	/**
	 * Simple holder for Address information.
	 * XTAPI associates one terminal with each address and also holds
	 * a raw handle for each address.
	 */
	class AddressInfo {
		int line = -1;
		String terminal = null;
		int rawHandle = -1;
		
		AddressInfo(int lineNo, String termName, int handle) {
			super();
			
			this.line = lineNo;
			this.terminal = termName;
			this.rawHandle = handle;
		}
		
		String getName() { return ADDR_PREFIX + line; }

		/**
		 * Ensure equality works
		 */
		public boolean equals(Object other) {
			if (other instanceof AddressInfo) {
				AddressInfo ai = (AddressInfo)other;
				if ((this.line == ai.line) &&
					(this.terminal.equals(ai.terminal)) &&
					(this.rawHandle == ai.rawHandle))
						return true;
			}
			return false;
		}
		
		/**
		 * Ensure hashing works
		 */
		public int hashCode() {
			return this.line + this.terminal.hashCode() + this.rawHandle;
		}
		
	}

	// The initialization map key	
	private final static String IXTAPI_KEY = "XtapiSp";
	
	// The TAPI service provider
	private final static String TAPI_SP = "net.xtapi.serviceProvider.MSTAPI";
	
	// The Comm service provider
	private final static String COMM_SP = "net.xtapi.serviceProvider.Serial";
	
	// The address prefix
	private final static String ADDR_PREFIX = "addr_";
	
	// The XTAPI spervice provider
	private IXTapi realProvider = null;
	
	// The GJTAPI Listener I delegate callbacks to
	private TelephonyListener gjListener = null;
	
	// The number of lines my real provider manages
	private int numLines = 0;
	
	// Map of address name to AddressInfo for XTAPI
	private Map addInfoMap = new HashMap();
	
	// Map of Terminal name to AddressInfo
	private Map termToAddr = new HashMap();
	
	// Map of line name (new Integer(rawHandle)) to AddressInfo
	private Map lineToAddr = new HashMap();
	
	/**
	 * Map of terminals to CallHandles
	 * This is used by the media methods to map media terminal to calls
	 */
	private Map termToCalls = new HashMap();
	
	/**
	 * Map of line ids to CallHandles
	 */
	private Map lineToCalls = new HashMap();
	
	/**
	 * map call handles to calls
	 */
	private Map callNumToCalls = new HashMap();
	
	// now hold onto the last remote number and name reported
	private String remoteName = null;
	private String remoteNumber = null;
	
	// The digit buckets to record digits for various terminals
	// This contains a Map of Terminal names to digit StringBuffers.
	private Map termToBuckets = new HashMap();

	/**
	 * Raw constructor used by the GenericJtapiPeer factory
	 * Creation date: (2002-04-12 10:28:55)
	 * @author: Richard Deadman
	 */
	public XtapiProvider() {
		super();
	}

	/**
	 * @see BasicJtapiTpi#getAddresses()
	 */
	public String[] getAddresses() throws ResourceUnavailableException {
		if (this.numLines > 0) {
			String addresses[] = new String[numLines];
			for (int i = 0; i < numLines; i++) {
				addresses[i] = ADDR_PREFIX + i;
			}
			return addresses;
		} else
			return null;
	}

	/**
	 * @see BasicJtapiTpi#getAddresses(String)
	 */
	public String[] getAddresses(String terminal) throws InvalidArgumentException {
		if (terminal != null) {
			AddressInfo addInfo = (AddressInfo)this.termToAddr.get(terminal);
			if (addInfo == null)
				return null;
			String result[] = { addInfo.getName() };
			return result;
		} else
			return null;
	}

	/**
	 * @see BasicJtapiTpi#getTerminals()
	 */
	public TermData[] getTerminals() throws ResourceUnavailableException {
		int size = this.termToAddr.size();
		if (size > 0) {
			TermData terminals[] = new TermData[size];
			Iterator it = this.termToAddr.keySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				String termName = (String)it.next();
					// all terminals support media
				terminals[i] = new TermData(termName, true);
				i++;
			}
			return terminals;
		} else
			return null;
	}

	/**
	 * @see BasicJtapiTpi#getTerminals(String)
	 */
	public TermData[] getTerminals(String address)
		throws InvalidArgumentException {
		if (address != null) {
			AddressInfo addInfo = (AddressInfo)addInfoMap.get(address);
			if (addInfo == null)
				return null;
			TermData result[] = { new TermData(addInfo.terminal, true) };
			return result;
		} else
			return null;
	}

	/**
	 * @see CoreTpi#addListener(TelephonyListener)
	 */
	public void addListener(TelephonyListener ro) {
		this.gjListener = ro;
	}

	/**
	 * @see CoreTpi#answerCall(CallId, String, String)
	 */
	public void answerCall(CallId call, String address, String terminal)
		throws
			PrivilegeViolationException,
			ResourceUnavailableException,
			MethodNotSupportedException,
			RawStateException {
		try {
			this.realProvider.XTAnswerCall(((XtapiCallId)call).getCallNum());
		} catch (InvalidStateException ise) {
			throw new RawStateException(call, ise.getState());
		}
	}

	/**
	 * @see CoreTpi#createCall(CallId, String, String, String)
	 */
	public CallId createCall(CallId id, String address, String term, String dest)
		throws
			ResourceUnavailableException,
			PrivilegeViolationException,
			InvalidPartyException,
			InvalidArgumentException,
			RawStateException,
			MethodNotSupportedException {
		// translate the address into a line
		AddressInfo addInfo = (AddressInfo)this.addInfoMap.get(address);
		if (addInfo == null)
			throw new InvalidArgumentException("Address not known: " + address);

                //sf
		// register the call with the line
		this.lineToCalls.put(new Integer(addInfo.line), id);
                //end sf
                
		// Get the call handle
		int callNum = 0;
		try {
			callNum = this.realProvider.XTConnectCall(addInfo.line,
											dest,
											addInfo.rawHandle);
		} catch (InvalidStateException ise) {
			throw new RawStateException(id, ise.getState());
		}

		// update the call structure
		XtapiCallId xid = (XtapiCallId)id;
		xid.setCallNumber(callNum);
		this.callNumToCalls.put(new Integer(callNum), xid);
		xid.setLocalAddress(address);
		xid.setRemoteAddress(dest);
		
		// register the call with the terminal
		this.termToCalls.put(term, id);
                
                //sf moved up to avoid race condition.
		// register the call with the line
		//this.lineToCalls.put(new Integer(addInfo.line), id);
                //end sf

		// now note that the local leg is connected
		this.gjListener.connectionConnected(id, address, Event.CAUSE_NORMAL);
		this.gjListener.connectionAlerting(id, dest, Event.CAUSE_NORMAL);
		
		return id;
	}

	/**
	 * I support the default capabilities associated with the MediaTpi
	 * @see CoreTpi#getCapabilities()
	 */
	public Properties getCapabilities() {
		Properties caps = new Properties();
		// mark my differences from the default
		caps.put(Capabilities.HOLD, "f");
		caps.put(Capabilities.JOIN, "f");
		caps.put(Capabilities.THROTTLE, "f");
		caps.put(Capabilities.ALLOCATE_MEDIA, "f");
		
		return caps;
	}

	/**
	 * Initialize the Xtapi Bricdge Provider with a Map that defines which xTapi service provider to load beneath me.
	 * This consists of one known property:
	 * <ul>
	 *  <li><B>XtapiSp</B> The name of the IXTapi implementation class, or a known alias.
	 * </ul>
	 * @see CoreTpi#initialize(Map)
	 */
	public void initialize(Map props) throws ProviderUnavailableException {
		
		// see if the propery is set
		String xtapiProvName = TAPI_SP;
		Object value = props.get(IXTAPI_KEY);
		if (value instanceof String && value != null) {
			xtapiProvName = (String)value;
		}
		
		// now check if we should use an alias
		if (xtapiProvName.toLowerCase().equals("tapi"))
			xtapiProvName = TAPI_SP;
		else if (xtapiProvName.toLowerCase().equals("serial"))
			xtapiProvName = COMM_SP;
		
		// try to instantiate xtapi provider
		try {
			realProvider = (IXTapi)Class.forName(xtapiProvName).newInstance();
		} catch (Exception ex) {
			throw new ProviderUnavailableException(ex.getMessage());
		}
		
		// Now hook it up and get the number of lines
		numLines = realProvider.XTinit(this);
		
		// populate the Address and Terminal information
		for (int i = 0; i < numLines; i++) {
			// Get Address information for each line
			StringBuffer termName = new StringBuffer();
			try {
				int handle = realProvider.XTOpenLine(i, termName);
			
				// populate the Address and Terminal information
				/*
				 * If TAPI does not store line information for
				 * several lines, then we have multiple "" terminals
				 * that cause incorrect terminal-address mapping.
				 * The solution is to prepend the terminal name
				 * with its line number.
				 */
				String term = ADDR_PREFIX + i + ":" + termName.toString();
				AddressInfo addInfo = new AddressInfo(i, term, handle);
				this.addInfoMap.put(ADDR_PREFIX + i, addInfo);
				
				this.termToAddr.put(term, addInfo);

				this.lineToAddr.put(new Integer(handle), addInfo);
			} catch (InvalidStateException ise) {
				// try the next line
			} catch (ResourceUnavailableException rue) {
				// try the next line
			}
		}
	}

	/**
	 * The GJTAPI Framework is done with the Call and is releasing the CallId.
	 * @see CoreTpi#releaseCallId(CallId)
	 */
	public void releaseCallId(CallId id) {
		try {
			this.realProvider.XTDropCall(((XtapiCallId)id).getCallNum());
		} catch (ResourceUnavailableException rue) {
			// ignore
		} catch (InvalidStateException ise) {
			// ignore
		}
	}

	/**
	 * I don't really need to do this, but it is here for completeness
	 * @see CoreTpi#removeListener(TelephonyListener)
	 */
	public void removeListener(TelephonyListener ro) {
		if (this.gjListener.equals(ro))
			this.gjListener = null;
	}

	/**
	 * Get a CallId object for later connection.
	 * The address parameter is ignoted.
	 * @see CoreTpi#reserveCallId(String)
	 */
	public CallId reserveCallId(String address) throws InvalidArgumentException {
		return new XtapiCallId();
	}

	/**
	 * @see CoreTpi#shutdown()
	 */
	public void shutdown() {
		try {
			this.realProvider.XTShutdown();
		} catch (InvalidStateException ise) {
			// ignore
		}
	}

	// Media calls
	/**
	 * Allocate media resources.
	 * This is a NO-OP, since XTAPI doesn't need this.
	 * @see MediaTpi#allocateMedia(String, int, Dictionary)
	 */
	public boolean allocateMedia(
		String terminal,
		int type,
		Dictionary resourceArgs) {
		return true;
	}

	/**
	 * Free media resources.
	 * @see MediaTpi#freeMedia(String, int)
	 */
	public boolean freeMedia(String terminal, int type) {
		// clear out the digit bucket for the terminal.
		this.retrieveSignals(terminal);
		return true;
	}

	/**
	 * All known terminals support media
	 * @see MediaTpi#isMediaTerminal(String)
	 */
	public boolean isMediaTerminal(String terminal) {
		if (this.termToAddr.containsKey(terminal))
			return true;
		return false;
	}

	/**
	 * @see MediaTpi#play(String, String[], int, RTC[], Dictionary)
	 */
	public void play(
		String terminal,
		String[] streamIds,
		int offset,
		RTC[] rtcs,
		Dictionary optArgs)
		throws MediaResourceException {
			// look up the id for the terminal
			int id = this.getLineId(terminal);
			int size = streamIds.length;
			
			try {
				// play each id on the found line
				for (int i = 0; i < size; i++)
					this.realProvider.XTPlaySound(streamIds[i], id);
			} catch (InvalidStateException ise) {
				throw new MediaResourceException(ise.toString());
			} catch (MethodNotSupportedException mnse) {
				throw new MediaResourceException(mnse.toString());
			} catch (ResourceUnavailableException rue) {
				throw new MediaResourceException(rue.toString());
			} 
	}

	/**
	 * @see MediaTpi#record(String, String, RTC[], Dictionary)
	 */
	public void record(
		String terminal,
		String streamId,
		RTC[] rtcs,
		Dictionary optArgs)
		throws MediaResourceException {
			try {
				// look up the id for the terminal
				int id = this.getLineId(terminal);
				this.realProvider.XTRecordSound(streamId, id);
			} catch (InvalidStateException ise) {
				throw new MediaResourceException(ise.toString());
			} catch (MethodNotSupportedException mnse) {
				throw new MediaResourceException(mnse.toString());
			} catch (ResourceUnavailableException rue) {
				throw new MediaResourceException(rue.toString());
			}
	}

	/**
	 * @see MediaTpi#retrieveSignals(String, int, Symbol[], RTC[], Dictionary)
	 */
	public RawSigDetectEvent retrieveSignals(
		String terminal,
		int num,
		Symbol[] patterns,
		RTC[] rtcs,
		Dictionary optArgs)
		throws MediaResourceException {
			// look up the id for the terminal
			int id = this.getCallId(terminal);
			try {
				this.realProvider.XTMonitorDigits(id, true);
			} catch (InvalidStateException ise) {
				throw new MediaResourceException(ise.toString());
			} catch (MethodNotSupportedException mnse) {
				throw new MediaResourceException(mnse.toString());
			} catch (ResourceUnavailableException rue) {
				throw new MediaResourceException(rue.toString());
			}
			// wait for signals to come in
			//---------------------------------------------------------------------------
			// Get the local p_Duration (if any).
			//---------------------------------------------------------------------------
			Object timeoutVal = null;
			if (optArgs != null) {
				timeoutVal = optArgs.get(SignalDetectorConstants.p_Duration);
			}

			boolean timed = (timeoutVal != null);
			int timeout = (timed ? ((Integer) timeoutVal).intValue() : 0);
			int timeSoFar = 0;

			StringBuffer sb = this.getSignalBuffer(terminal);
			synchronized (sb) {
				if (sb.length() >= num) {
					return RawSigDetectEvent.maxDetected(terminal, SymbolConvertor.convert(sb.toString()));
				} else {
						// should add support for timeout as well
					while ((sb.length() < num) &&
							((!timed) || (timeSoFar < timeout))) {
						try {
							sb.wait(100);	// wait until another signal comes in
						} catch (InterruptedException ie) {
							// keep going...
						}
						timeSoFar += 100;
					}
					if (sb.length() >= num) {
						return RawSigDetectEvent.maxDetected(terminal, SymbolConvertor.convert(sb.toString()));
					} else {
						return RawSigDetectEvent.timeout(terminal, SymbolConvertor.convert(sb.toString()));
					}
				}
			}
	}

	/**
	 * This should wait until the signals have all been sent.
	 * @see MediaTpi#sendSignals(String, Symbol[], RTC[], Dictionary)
	 */
	public void sendSignals(
		String terminal,
		Symbol[] syms,
		RTC[] rtcs,
		Dictionary optArgs)
		throws MediaResourceException {
			// look up the id for the terminal
			int id = this.getCallId(terminal);
			try {
				this.realProvider.XTSendDigits(id, SymbolConvertor.convert(syms));
			} catch (InvalidStateException ise) {
				throw new MediaResourceException(ise.toString());
			} catch (MethodNotSupportedException mnse) {
				throw new MediaResourceException(mnse.toString());
			} catch (ResourceUnavailableException rue) {
				throw new MediaResourceException(rue.toString());
			}
	}

	/**
	 * @see MediaTpi#stop(String)
	 */
	public void stop(String terminal) {
		// look up the id for the terminal
		int id = this.getLineId(terminal);
		this.realProvider.XTStopPlaying(id);
		this.realProvider.XTStopRecording(id);
		
		int callId = this.getCallId(terminal);
		try {
			this.realProvider.XTMonitorDigits(callId, false);
		} catch (InvalidStateException ise) {
			// ignore
		} catch (MethodNotSupportedException mnse) {
			// ignore
		} catch (ResourceUnavailableException rue) {
			// ignore
		}
	}

	/**
	 * RTCs are not supported by XTAPI, but we do support stopping
	 * @see MediaTpi#triggerRTC(String, Symbol)
	 */
	public void triggerRTC(String terminal, Symbol action) {
		// look up the id for the terminal
		int id = this.getCallId(terminal);
		
		if (action.equals(PlayerConstants.rtca_Stop))
			this.realProvider.XTStopPlaying(id);
			
		if (action.equals(RecorderConstants.rtca_Stop))
			this.realProvider.XTStopRecording(id);
			
		if (action.equals(SignalDetectorConstants.rtca_Stop)) {
			int callId = this.getCallId(terminal);
			try {
				this.realProvider.XTMonitorDigits(callId, false);
			} catch (InvalidStateException ise) {
				// we tried to stop it...
			} catch (MethodNotSupportedException mnse) {
			// ignore
			} catch (ResourceUnavailableException rue) {
			// ignore
			}
		}
	}

	// Callback interface
	/**
	 * Receive the XTAPI service provider callback events and transform them
	 * into JTAPI TelephonyListener events.
	 * The delegation method also stores transformation information that is needed
	 * for other delegation methods, such as media action termination.
	 * @see IXTapiCallBack#callback(int, int, int, int, int, int)
	 */
	public void callback(
		int dwDevice,
		int dwMessage,
		int dwInstance,
		int dwParam1,
		int dwParam2,
		int dwParam3) {
        try {
            
        switch (dwMessage) {
            case LINE_ADDRESSSTATE:
                break;
                
            case LINE_CALLINFO:
            	try {
					// we don't have a callId yet, so we store the remote
					// name and number until we get an OFFERING event
	                String[] s = this.realProvider.XTGetCallInfo(dwDevice);
					if(null != s) {
	                    // We got caller id info
	                    // An array of two strings 1. Name 2. Number
	                    // Set the remote terminal name == name
	                    // Set the remote address name == number
						this.remoteName = s[0];
						this.remoteNumber = s[1];
					}
				} catch(Exception e) {
						System.out.println("Exception " + e.toString() + " in callback()");
				}
                break;
                
            case LINE_CALLSTATE:
                lineCallState(dwDevice,dwInstance,dwParam1,dwParam2,dwParam3);
                break;
                
            case LINE_CLOSE:
                break;
                
            case LINE_DEVSPECIFIC:
                break;
                
            case LINE_DEVSPECIFICFEATURE:
                break;
                
            case LINE_GATHERDIGITS:
                break;
                
            case LINE_GENERATE:
             /*
              * We have been notified that we have generated digits. But we should already know this.              */
                break;
                
            case LINE_MONITORDIGITS:
/*                
                debugString(5,"dwDevice -> " + dwDevice + " dwInstance -> " + dwInstance +
                " dwParam1 -> "  + dwParam1 + " dwParam2 -> " + dwParam2 + 
                " dwParam3 -> " + dwParam3);
*/
				//System.out.println("Monitoring digits");
				//System.out.println("dwDevice -> " + dwDevice + " dwInstance (line) -> " + dwInstance +
                	//" dwParam1 -> "  + dwParam1 + " dwParam2 -> " + dwParam2 + 
                	//" dwParam3 -> " + dwParam3);
                XtapiCallId cid = (XtapiCallId)this.callNumToCalls.get(new Integer(dwDevice));
				String address = null;
				AddressInfo ai = null;
                if (cid != null) {
                	address = cid.getLocalAddress();
                }
                if (address != null)
					ai = (AddressInfo)this.addInfoMap.get(address); 
				//(AddressInfo)this.lineToAddr.get(new Integer(dwInstance));
				if (ai != null) {
					String terminal = ai.terminal;
	                char[] detectedChars = { (char)dwParam1 };
                	Symbol[] syms = SymbolConvertor.convert(new String(detectedChars));
                	this.gjListener.mediaSignalDetectorDetected(terminal, syms);
                	
                	// now drop the digits into the digit bucket
                	this.storeSignals(terminal, detectedChars);
				}
                break;                
                
            case LINE_LINEDEVSTATE:
                lineDevState(dwDevice,dwInstance,dwParam1,dwParam2,dwParam3);
                break;
                
            case LINE_MONITORMEDIA:
                break;
                
            case LINE_MONITORTONE:
                break;
                
            case LINE_REPLY:
                //m_reply = dwParam2;
                //m_device = dwInstance;
                /*
                TODO:   Check LINE_REPLY Data for success
                        Switch on current state, maybe connecting or
                        disconnecting.
                 */
/*
                evt = new XCallActiveEv(m_Call,CallActiveEv.ID,
                    Ev.CAUSE_NEW_CALL,0,false);
                evlist[0] = evt;
                publishEvent(evlist);
 */
                break;
                
            case LINE_REQUEST:
                break;
                
            case PHONE_BUTTON:
                break;
                
            case PHONE_CLOSE:
                break;
                
            case PHONE_DEVSPECIFIC:
                break;
                
            case PHONE_REPLY:
                break;
                
            case PHONE_STATE:
                break;
                
            case LINE_CREATE:
                break;
                
            case PHONE_CREATE:
                break;
                
            default:
                System.out.println("UNKNOWN TAPI EVENT: " + dwMessage);
                break;
        }
        }catch(Exception e){
            System.out.println("Exception " + e.toString() + " in callback()");
        }

	}

    //callback recieved a LINE_CALLSTATE event:
    private void lineCallState(int dwDevice,int dwInstance,
           int dwParam1,int dwParam2,int dwParam3)
    {
    	Integer lineKey = new Integer(dwInstance);
		//System.out.println("Line: " + dwInstance);
		//System.out.println("Params: dwDevice -> " + dwDevice + " dwInstance -> " + dwInstance +
			//" dwParam1 -> "  + dwParam1 + " dwParam2 -> " + dwParam2 + 
			//" dwParam3 -> " + dwParam3);
        switch (dwParam1)
        {
            case LINECALLSTATE_IDLE:
                try{
                	XtapiCallId callId = (XtapiCallId)this.lineToCalls.get(lineKey);
	                if (callId != null) {
	                	AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
	                	if (ai != null) {
	                		this.gjListener.connectionDisconnected(callId, ai.getName(), Event.CAUSE_NORMAL);
	                	}
	                	// now see if we can disconnect the remote leg
	                	String remoteAddress = callId.getRemoteAddress();
	                	if (remoteAddress != null)
	                		this.gjListener.connectionDisconnected(callId, remoteAddress, Event.CAUSE_NORMAL);
	                }
                }catch(Exception e){
                    System.out.println("LINECALLSTATE_IDLE exception: " + e.toString());
                }
                
                //Connection.DISCONNECTED
                break;

            case LINECALLSTATE_OFFERING:
                
				try{
					// create a call
	                //AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
                        //sf
					AddressInfo ai = (AddressInfo)this.addInfoMap.get("addr_" + lineKey);
                        //end sf
	                if (ai != null) {
						XtapiCallId callId = new XtapiCallId();
	                	//callId.setCallNumber(dwInstance); // dwInstance contains the line id (NOT the line handle!)
                                //sf
						callId.setCallNumber(dwDevice); // dwDevice contains the call handle
						this.callNumToCalls.put(new Integer(dwDevice), callId);
						
                		// register the call with the line
		                this.lineToCalls.put(new Integer(ai.line), callId); 
                                //end sf
						// and register the call with a terminal
						this.termToCalls.put(ai.terminal, callId);
                                
	                	// get the calling connection and notify of new connection
	                	this.gjListener.connectionAlerting(callId, ai.getName(), Event.CAUSE_NORMAL);
	                	this.gjListener.terminalConnectionRinging(callId, ai.getName(), ai.terminal, Event.CAUSE_NORMAL);
	                	if (this.remoteNumber != null) {
		                	this.gjListener.connectionConnected(callId, this.remoteNumber, Event.CAUSE_NORMAL);
		                	callId.setRemoteAddress(this.remoteNumber);
		                	// now handle the remote terminal connection
		                	if (this.remoteName != null) {
			                	this.gjListener.terminalConnectionTalking(callId, this.remoteNumber, this.remoteName, Event.CAUSE_NORMAL);
			                	// clear remoteName for next call
			                	this.remoteName = null;
							}
							// clear remoteNumber for next call
							this.remoteNumber = null;
		            	}
	                }
				}catch(Exception e){
					System.out.println("Exception in LINECALLSTATE_OFFERING: " +
						e.toString());
				}
                break;

            case LINECALLSTATE_ACCEPTED:
                break;

            case LINECALLSTATE_DIALTONE:
                break;

            case LINECALLSTATE_DIALING:
                break;

            case LINECALLSTATE_RINGBACK:
                //event.add(new XEv(ConnAlertingEv.ID,Ev.CAUSE_NEW_CALL,                            0,false));                
                break;

            case LINECALLSTATE_BUSY:
                break;

            case LINECALLSTATE_SPECIALINFO:
                break;

            case LINECALLSTATE_CONNECTED:
                try{
                	XtapiCallId callId = (XtapiCallId)this.lineToCalls.get(lineKey);
	                if (callId != null) {
                                //sf
	                	//AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
						AddressInfo ai = (AddressInfo)this.addInfoMap.get("addr_" + dwInstance);
                                //end sf
	                	if (ai != null) {
	                		this.gjListener.connectionConnected(callId, ai.getName(), Event.CAUSE_NORMAL);
//	                		this.gjListener.connectionConnected(callId, "UNKNOWN", Event.CAUSE_NORMAL);
                                        //sf
							this.gjListener.terminalConnectionTalking(callId, ai.getName(), ai.terminal, Event.CAUSE_NORMAL);
                                        //end sf
//							this.gjListener.terminalConnectionTalking(callId, "UNKNOWN", "UNKNOWN", Event.CAUSE_NORMAL);
	                	}
	                	// now note that the remote connection is connected
	                	String remoteAddress = callId.getRemoteAddress();
	                	if (remoteAddress != null)
	                		this.gjListener.connectionConnected(callId, remoteAddress, Event.CAUSE_NORMAL);
	                }
                }catch(Exception e){
                	e.printStackTrace();
                    System.out.println("LINECALLSTATE_CONNECTED exception: " + e.toString());
                }
                break;

            case LINECALLSTATE_PROCEEDING:
                // Outgoing call invokes two JTAPI Connection events.
                
                try{
                	XtapiCallId callId = (XtapiCallId)this.lineToCalls.get(lineKey);
	                if (callId != null) {
                                //sf
	                	//AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
						AddressInfo ai = (AddressInfo)this.addInfoMap.get("addr_" + lineKey);
                                //end sf
	                	if (ai != null) {
	                		this.gjListener.connectionInProgress(callId, ai.getName(), Event.CAUSE_NORMAL);
                                        //sf
							this.gjListener.connectionAlerting(callId, ai.getName(), Event.CAUSE_NORMAL);
                                        //end sf
	                	}
	                }
                }catch(Exception e){
                    //sf
                    System.out.println("LINECALLSTATE_PROCEEDING exception: " + e.toString());
                    //end sf
                }
                break;

            case LINECALLSTATE_ONHOLD:
                break;

            case LINECALLSTATE_CONFERENCED:
                break;

            case LINECALLSTATE_ONHOLDPENDCONF:
                break;

            case LINECALLSTATE_ONHOLDPENDTRANSFER:
                break;

            case LINECALLSTATE_DISCONNECTED:
                try{
                	XtapiCallId callId = (XtapiCallId)this.lineToCalls.get(lineKey);
	                if (callId != null) {
                                //sf
	                	//AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
						AddressInfo ai = (AddressInfo)this.addInfoMap.get("addr_" + dwInstance);
                                //end sf
	                	if (ai != null) {
                                        //sf
							this.gjListener.terminalConnectionDropped(callId, ai.getName(), ai.terminal, Event.CAUSE_NORMAL);
                                        //end sf
	                		this.gjListener.connectionDisconnected(callId, ai.getName(), Event.CAUSE_NORMAL);
//	                		this.gjListener.connectionDisconnected(callId, "UNKNOWN", Event.CAUSE_NORMAL);
								// we can get the terminal, and remove the call from the term -> call map
							this.termToCalls.remove(ai.terminal);
							}
						// disconnect the remote Connection
						String remoteAddress = callId.getRemoteAddress();
						if (remoteAddress != null)
							this.gjListener.connectionDisconnected(callId, remoteAddress, Event.CAUSE_NORMAL);
							// now remove the entry from the line -> CallId map
						this.lineToCalls.remove(lineKey);
							// and the callNum -> call map
						this.callNumToCalls.remove(new Integer(callId.getCallNum()));
	                }
                }catch(Exception e){
                    System.out.println("LINECALLSTATE_IDLE exception: " + e.toString());
                }
                break;

            case LINECALLSTATE_UNKNOWN:
                break;
                
            default:
                break; 
        }
    }

    //callback recieved a LINE_DEVSTATE event:
    private void lineDevState(int dwDevice,int dwInstance,
           int dwParam1,int dwParam2,int dwParam3)
    {
        switch(dwParam1)
        {
            case LINEDEVSTATE_OTHER:
                break;
                
            case LINEDEVSTATE_RINGING:
            	// this may be for subsequent rings, in which case we don't need it.
               //dwParam3 contains the ring count.
                XtapiCallId xCallId = (XtapiCallId)this.lineToCalls.get(new Integer(dwInstance));
                if (xCallId != null) {
                        //sf
                	//AddressInfo ai = (AddressInfo)this.lineToAddr.get(new Integer(dwInstance));
					AddressInfo ai = (AddressInfo)this.addInfoMap.get("addr_" + dwInstance);
                        //end sf
                	if (ai != null) {
                		this.gjListener.connectionAlerting(xCallId, ai.getName(), Event.CAUSE_NEW_CALL);
//                		this.gjListener.connectionAlerting(xCallId, "UNKNOWN", Event.CAUSE_NEW_CALL);
							//sf
						this.gjListener.terminalConnectionRinging(xCallId, ai.getName(), ai.terminal, Event.CAUSE_NORMAL);
							//end sf
                	}
                }
                break;
                
            case LINEDEVSTATE_CONNECTED:
                break;
                
            case LINEDEVSTATE_DISCONNECTED:
                break;
                
            case LINEDEVSTATE_MSGWAITON:
                break;
                
            case LINEDEVSTATE_MSGWAITOFF:
                break;
                
            case LINEDEVSTATE_INSERVICE:
                break;
                
            case LINEDEVSTATE_OUTOFSERVICE:
                break;
                
            case LINEDEVSTATE_MAINTENANCE:
                break;
                
            case LINEDEVSTATE_OPEN:
                break;
                
            case LINEDEVSTATE_CLOSE:
                break;
                
            case LINEDEVSTATE_NUMCALLS:
                break;
                
            case LINEDEVSTATE_NUMCOMPLETIONS:
                break;
                
            case LINEDEVSTATE_TERMINALS:
                break;

            case LINEDEVSTATE_ROAMMODE:
                break;

            case LINEDEVSTATE_BATTERY:
                break;

            case LINEDEVSTATE_SIGNAL:
                break;

            case LINEDEVSTATE_DEVSPECIFIC:
                break;

            case LINEDEVSTATE_REINIT:
                break;

            case LINEDEVSTATE_LOCK:
                break;

            case LINEDEVSTATE_CAPSCHANGE:
                break;

            case LINEDEVSTATE_CONFIGCHANGE:
                break;

            case LINEDEVSTATE_TRANSLATECHANGE:
                break;

            case LINEDEVSTATE_COMPLCANCEL:
                break;

            case LINEDEVSTATE_REMOVED:
                break;

            default:
                break; 
        }
    }
	// end of interface implementation
	
	/**
	 * Helper function for getting a call id from a terminal name
	 */
	private int getCallId(String terminal) {
		return ((XtapiCallId)this.termToCalls.get(terminal)).getCallNum();
	}
	
	/**
	 * Helper function for getting a line id from a terminal name
	 */
	private int getLineId(String terminal) {
		return ((AddressInfo)this.termToAddr.get(terminal)).rawHandle;
	}
	
	/**
	 * Describe myself
	 */
	public String toString() {
		return "GJTAPI bridge to XTAPI service provider: " + realProvider.toString();
	}
	/**
	 * @see BasicJtapiTpi#release(String, CallId)
	 */
	public void release(String address, CallId call)
		throws
			PrivilegeViolationException,
			ResourceUnavailableException,
			MethodNotSupportedException,
			RawStateException {
		// check if this is a local address
		AddressInfo addInfo = (AddressInfo)this.addInfoMap.get(address);
		if (addInfo != null)
			try {
				this.realProvider.XTDropCall(((XtapiCallId)call).getCallNum());
			} catch (InvalidStateException ise) {
				throw new RawStateException(call, ise.getState());
			}
	}

	/**
	 * Stores signals for a terminal and notifies any waiting
	 * threads of the arrival of the digits.
	 * @param terminalName
	 * @param digits
	 */
	private void storeSignals(String terminalName, char[] digits) {
		StringBuffer sb = this.getSignalBuffer(terminalName);
		synchronized (sb) {
			sb.append(digits);
			
			// now we notify anyone waiting for digits
			sb.notify();		}
	}

	/**
	 * Retrieves the latest uncollected signals on a
	 * terminal and clears the terminal signal bucket.
	 * @param terminalName
	 * @return
	 */
	private String retrieveSignals(String terminalName) {
		StringBuffer sb = this.getSignalBuffer(terminalName);
		synchronized (sb) {
			String signals = sb.toString();
			sb.delete(0, sb.length());
			return signals;
		}		
	}
	
	/**
	 * Get a StringBuffer for a terminal, used to store
	 * the latest unretrieved signals on that terminal.
	 * @param terminalName
	 * @return A StringBuffer holding signals for the terminal.
	 */
	private StringBuffer getSignalBuffer(String terminalName) {
		Map bucketSet = this.termToBuckets;
		StringBuffer signalBuffer = (StringBuffer)bucketSet.get(terminalName);
		if (signalBuffer == null) {
			synchronized (bucketSet) {
				// try again to avoid double creation error
				signalBuffer = (StringBuffer)bucketSet.get(terminalName);
				if (signalBuffer == null) {
					signalBuffer = new StringBuffer();
					bucketSet.put(terminalName, signalBuffer);
				}
			}
		}
		return signalBuffer;
	}
}

