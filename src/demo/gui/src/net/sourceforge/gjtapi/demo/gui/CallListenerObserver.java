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
package net.sourceforge.gjtapi.demo.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.Event;
import javax.telephony.TerminalConnection;
import javax.telephony.TerminalConnectionEvent;
import javax.telephony.TerminalConnectionListener;
import javax.telephony.callcontrol.CallControlCall;
import javax.telephony.callcontrol.CallControlCallObserver;
import javax.telephony.callcontrol.CallControlTerminalConnection;
import javax.telephony.callcontrol.events.CallCtlTermConnBridgedEv;
import javax.telephony.callcontrol.events.CallCtlTermConnDroppedEv;
import javax.telephony.callcontrol.events.CallCtlTermConnHeldEv;
import javax.telephony.callcontrol.events.CallCtlTermConnInUseEv;
import javax.telephony.callcontrol.events.CallCtlTermConnRingingEv;
import javax.telephony.callcontrol.events.CallCtlTermConnTalkingEv;
import javax.telephony.callcontrol.events.CallCtlTermConnUnknownEv;
import javax.telephony.events.CallActiveEv;
import javax.telephony.events.CallEv;
import javax.telephony.events.CallInvalidEv;
import javax.telephony.events.CallObservationEndedEv;
import javax.telephony.events.ConnAlertingEv;
import javax.telephony.events.ConnConnectedEv;
import javax.telephony.events.ConnCreatedEv;
import javax.telephony.events.ConnDisconnectedEv;
import javax.telephony.events.ConnEv;
import javax.telephony.events.ConnFailedEv;
import javax.telephony.events.ConnInProgressEv;
import javax.telephony.events.ConnUnknownEv;
import javax.telephony.events.Ev;
import javax.telephony.events.TermConnActiveEv;
import javax.telephony.events.TermConnCreatedEv;
import javax.telephony.events.TermConnDroppedEv;
import javax.telephony.events.TermConnEv;
import javax.telephony.events.TermConnPassiveEv;
import javax.telephony.events.TermConnRingingEv;
import javax.telephony.events.TermConnUnknownEv;
import javax.telephony.media.events.MediaTermConnAvailableEv;
import javax.telephony.media.events.MediaTermConnUnavailableEv;
import javax.telephony.privatedata.events.PrivateCallEv;

import net.sourceforge.gjtapi.events.GenPrivateCallEv;
import net.sourceforge.gjtapi.raw.tapi3.Tapi3PrivateData;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class CallListenerObserver extends DefaultListModel
    implements TerminalConnectionListener, CallControlCallObserver {
    private static final Logger LOGGER =
        Logger.getLogger(CallListenerObserver.class);

    private boolean usePrivateData = false;
    /** Map of Tapi3PrivateData indexed by Call. */
    private Map<Call, Tapi3PrivateData> callMap =
        new HashMap<Call, Tapi3PrivateData>();

    /** Map of cause descriptions. */
    private final Map<Integer, String> CAUSE_DESCRIPTIONS;
    
    {
        CAUSE_DESCRIPTIONS = new java.util.HashMap<Integer, String>();
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_CALL_CANCELLED, "Call cancelled");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_DEST_NOT_OBTAINABLE,
                "Destination not obtainable");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_INCOMPATIBLE_DESTINATION,
                "Incompatible destination");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_LOCKOUT, "Lockout");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_NETWORK_CONGESTION,
                "Network congestion");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_NETWORK_NOT_OBTAINABLE,
                "Network not obtainable");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_NEW_CALL, "New call");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_NORMAL, "Normal");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_RESOURCES_NOT_AVAILABLE,
                "Resources not available");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_NORMAL, "Snapshot");
        CAUSE_DESCRIPTIONS.put(Event.CAUSE_UNKNOWN, "Unknown");
    }

    public class Item {
        private final Connection connection;
        
        public Item(Connection connection) {
            if(connection == null) {
                throw new IllegalArgumentException("connection is null.");
            }
            this.connection = connection;
        }

        public Connection getConnection() {
            return connection;
        }

        public TerminalConnection getTerminalConnection() {
            TerminalConnection terminalConnection = null;
            TerminalConnection[] tc = connection.getTerminalConnections();
            if(tc != null && tc.length > 0) {
                if(tc.length > 1) {
                    LOGGER.warn("Item: " + tc.length + "");
                }
                terminalConnection = tc[0];
            }
            return terminalConnection;
        }
        
        public Tapi3PrivateData getPrivateData() {
            Tapi3PrivateData privateData = null;
            Call call = connection.getCall();
            if(call != null) {
                privateData = (Tapi3PrivateData)callMap.get(call);
            }
            return privateData;
        }
        
        public String getCallName() {
            if(usePrivateData) {
                Tapi3PrivateData privateData = getPrivateData();
                if(privateData != null) {
                    return privateData.getCalledName() + "(" 
                    + privateData.getCalledNumber() + ") " + "<-- "
                    + privateData.getCallerName() + "(" 
                    + privateData.getCallerNumber() + ")";
                } else {
                    return "???@" + connection.getAddress();
                }
            } else {
                CallControlCall call = (CallControlCall) connection.getCall();
                String calledAddr = (call.getCalledAddress() == null) ? "???" : call.getCalledAddress().getName();
                String callingAddr = (call.getCallingAddress() == null) ? "???" : call.getCallingAddress().getName();
                return calledAddr + " <-- " + callingAddr;
            }
        }
        
        public String toString() {
            TerminalConnection termConn = getTerminalConnection();
            final String stateName;
            if(termConn == null || termConn.getState() == TerminalConnection.UNKNOWN) {
                stateName = TapiUtil.getConnectionStateName(connection);
            } else {
                stateName = TapiUtil.getTerminalConnectionStateName(termConn);
            }
            String s = getCallName() + " - " + stateName;
            return s;
        }

        public boolean equals(Object obj) {
        	boolean eq = false;
            if(obj != null && obj instanceof Item) {
                Item item = (Item)obj;
                eq = (item.connection.getCall() == connection.getCall());
            }
            return eq;
        }

        public int hashCode() {
            return connection.getCall().hashCode();
        }
    };

    public void clear() {
        super.clear();
        callMap.clear();
    }
    
    public void update() {
        LOGGER.debug("CallMap: " + callMap);
        fireContentsChanged(this, 0, getSize());
    }

    public CallListenerObserver() {
    }
    
    public boolean isUsePrivateData() {
        return usePrivateData;
    }

    public void setUsePrivateData(boolean usePrivateData) {
        this.usePrivateData = usePrivateData;
    }

    private void updatePrivateData(Call call, Tapi3PrivateData privateData) {
        callMap.put(call, privateData);
        update();
    }
    
    private void updateConnection(Connection connection) {
        Item item = new Item(connection);
        LOGGER.debug("Updating connection(" + connection + " - "
                + connection.getAddress() + " - " + connection.getCall() + ")");
        if(!contains(item)) {
            LOGGER.debug("Calling addElement(" + connection + " - "
                    + connection.getAddress() + " - " + connection.getCall()
                    + ")");
            addElement(item);
        }
        TerminalConnection tc = item.getTerminalConnection();
        int state = CallControlTerminalConnection.UNKNOWN;
        if(tc != null) {
            state = TapiUtil.getTerminalConnectionState(tc);
        }
        if(state == CallControlTerminalConnection.TALKING) {
            synchronized(tc) {
                tc.notifyAll();
            }
        }
        update();
    }

    private void removeConnection(Connection connection) {
        Item item = new Item(connection);
        LOGGER.debug("removeElement(" + connection + " - " + connection.getAddress() + " - " + connection.getCall() + ")");
        removeElement(item);
        callMap.remove(connection.getCall());
        update();
    }

    
    /**
     * Convert the event cause string to a cause. Creation date: (2000-05-01
     * 9:58:39)
     * 
     * @author: Richard Deadman
     * @return English description of the cause
     * @param cause
     *            The Event cause id.
     */
    public String causeToString(int cause) {
        final String description = CAUSE_DESCRIPTIONS.get(cause);
        if (description != null) {
            return description;
        }
        return "Cause mapping error: " + cause;
    }

    
    /**
     * Describe myself
     * 
     * @return a string representation of myself
     */
    public String toString() {
        return "ListModel with call listener and observer.";
    }

    /**
     * callActive method comment.
     */
    public void callActive(javax.telephony.CallEvent event) {
        LOGGER.debug("Active Call event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * callEventTransmissionEnded method comment.
     */
    public void callEventTransmissionEnded(javax.telephony.CallEvent event) {
        LOGGER.debug("Event Transmission Ended Call event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * callInvalid method comment.
     */
    public void callInvalid(javax.telephony.CallEvent event) {
        LOGGER.debug("Invalid Call event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * multiCallMetaMergeEnded method comment.
     */
    public void multiCallMetaMergeEnded(javax.telephony.MetaEvent event) {
        LOGGER.debug("Multicall merge ended event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * multiCallMetaMergeStarted method comment.
     */
    public void multiCallMetaMergeStarted(javax.telephony.MetaEvent event) {
        LOGGER.debug("Multicall merge started event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * multiCallMetaTransferEnded method comment.
     */
    public void multiCallMetaTransferEnded(javax.telephony.MetaEvent event) {
        LOGGER.debug("Multicall transfer ended event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * multiCallMetaTransferStarted method comment.
     */
    public void multiCallMetaTransferStarted(javax.telephony.MetaEvent event) {
        LOGGER.debug("Multicall transfer started event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * singleCallMetaProgressEnded method comment.
     */
    public void singleCallMetaProgressEnded(javax.telephony.MetaEvent event) {
        LOGGER.debug("Multicall progress ended event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * singleCallMetaProgressStarted method comment.
     */
    public void singleCallMetaProgressStarted(javax.telephony.MetaEvent event) {
        LOGGER.debug("Multicall progress started event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * singleCallMetaSnapshotEnded method comment.
     */
    public void singleCallMetaSnapshotEnded(javax.telephony.MetaEvent event) {
        LOGGER.debug("Singlecall snapshot ended event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * singleCallMetaSnapshotStarted method comment.
     */
    public void singleCallMetaSnapshotStarted(javax.telephony.MetaEvent event) {
        LOGGER.debug("Singlecall snapshot started event with cause: " + this.causeToString(event.getCause()));
        update();
    }

    /**
     * connectionAlerting method comment.
     */
    public void connectionAlerting(javax.telephony.ConnectionEvent event) {
        LOGGER.debug("Alerting Connection event with cause: " + this.causeToString(event.getCause()));
        updateConnection(event.getConnection());
    }

    /**
     * connectionConnected method comment.
     */
    public void connectionConnected(javax.telephony.ConnectionEvent event) {
        LOGGER.debug("Connection Connected event with cause: "
                + this.causeToString(event.getCause()));
        final Connection connection = event.getConnection();
        updateConnection(connection);
    }

    /**
     * connectionCreated method comment.
     */
    public void connectionCreated(javax.telephony.ConnectionEvent event) {
        LOGGER.debug("Connection Created event with cause: " + this.causeToString(event.getCause()));
        updateConnection(event.getConnection());
    }

    /**
     * connectionDisconnected method comment.
     */
    public void connectionDisconnected(javax.telephony.ConnectionEvent event) {
        LOGGER.debug("Connection Disconnected event with cause: " + this.causeToString(event.getCause()));
//        updateConnection(event.getConnection());
        removeConnection(event.getConnection());
    }

    /**
     * connectionFailed method comment.
     */
    public void connectionFailed(javax.telephony.ConnectionEvent event) {
        LOGGER.debug("Connection Failed event with cause: " + this.causeToString(event.getCause()));
        updateConnection(event.getConnection());
    }

    /**
     * connectionInProgress method comment.
     */
    public void connectionInProgress(javax.telephony.ConnectionEvent event) {
        LOGGER.debug("Connection in Progress event with cause: " + this.causeToString(event.getCause()));
        updateConnection(event.getConnection());
    }

    /**
     * connectionUnknown method comment.
     */
    public void connectionUnknown(javax.telephony.ConnectionEvent event) {
        LOGGER.debug("Connection Unknown event with cause: " + this.causeToString(event.getCause()));
        updateConnection(event.getConnection());
    }
    
    /* (non-Javadoc)
     * @see javax.telephony.TerminalConnectionListener#terminalConnectionActive(javax.telephony.TerminalConnectionEvent)
     */
    public void terminalConnectionActive(TerminalConnectionEvent event) {
        LOGGER.debug("TerminalConnection Active event with cause: "
                + this.causeToString(event.getCause()));
        updateConnection(event.getTerminalConnection().getConnection());
    }

    /* (non-Javadoc)
     * @see javax.telephony.TerminalConnectionListener#terminalConnectionCreated(javax.telephony.TerminalConnectionEvent)
     */
    public void terminalConnectionCreated(TerminalConnectionEvent event) {
        LOGGER.debug("TerminalConnection Created event with cause: "
                + this.causeToString(event.getCause()));
        final TerminalConnection connection = event.getTerminalConnection();
        updateConnection(connection.getConnection());
    }

    /* (non-Javadoc)
     * @see javax.telephony.TerminalConnectionListener#terminalConnectionDropped(javax.telephony.TerminalConnectionEvent)
     */
    public void terminalConnectionDropped(TerminalConnectionEvent event) {
        LOGGER.debug("TerminalConnection Dropped event with cause: "
                + this.causeToString(event.getCause()));
        final TerminalConnection connection = event.getTerminalConnection();
        updateConnection(connection.getConnection());
    }

    /* (non-Javadoc)
     * @see javax.telephony.TerminalConnectionListener#terminalConnectionPassive(javax.telephony.TerminalConnectionEvent)
     */
    public void terminalConnectionPassive(TerminalConnectionEvent event) {
        LOGGER.debug("TerminalConnection Passive event with cause: "
                + this.causeToString(event.getCause()));
        final TerminalConnection connection = event.getTerminalConnection();
        updateConnection(connection.getConnection());
    }

    /* (non-Javadoc)
     * @see javax.telephony.TerminalConnectionListener#terminalConnectionRinging(javax.telephony.TerminalConnectionEvent)
     */
    public void terminalConnectionRinging(TerminalConnectionEvent event) {
        LOGGER.debug("TerminalConnection Ringing event with cause: "
                + this.causeToString(event.getCause()));
        final TerminalConnection connection = event.getTerminalConnection();
        updateConnection(connection.getConnection());
    }

    /* (non-Javadoc)
     * @see javax.telephony.TerminalConnectionListener#terminalConnectionUnknown(javax.telephony.TerminalConnectionEvent)
     */
    public void terminalConnectionUnknown(TerminalConnectionEvent event) {
        LOGGER.debug("TerminalConnection Unknown event with cause: "
                + this.causeToString(event.getCause()));
        final TerminalConnection connection = event.getTerminalConnection();
        updateConnection(connection.getConnection());
    }    
    
    
    /**
     * Report old-style observer events on a call attached to the Address.
     * 
     * @see javax.telephony.CallObserver#callChangedEvent(javax.telephony.events.CallEv)
     */
    public void callChangedEvent(CallEv[] eventList) {
        String event = null;
        CallEv ev = eventList[0];
        int id = ev.getID();
        switch (id) {
            case PrivateCallEv.ID: {
                GenPrivateCallEv privCallEv = (GenPrivateCallEv)ev;
                Object privateData = privCallEv.getPrivateData();
                event = "Private data: " + privateData;
                if(privateData instanceof Tapi3PrivateData) {
                    updatePrivateData(privCallEv.getCall(), (Tapi3PrivateData)privateData);
                }
                break;
            }
            case CallActiveEv.ID:
                event = "call active"; 
                break;
            case CallInvalidEv.ID:
                event = "call invalid";
                break;
            case CallObservationEndedEv.ID:
                event = "call obervation ended"; 
                break;
            case ConnAlertingEv.ID:
                event = "Connection alerting"; 
                break;
            case ConnConnectedEv.ID: 
                event = "Connection connected";
                break;
            case ConnCreatedEv.ID:
                event = "Connection created";
                break;
            case ConnDisconnectedEv.ID:
                event = "Connection disconnected";
                break;
            case ConnFailedEv.ID:
                event = "Connection failed";
                break;
            case ConnInProgressEv.ID:
                event = "Connection in progress";
                break;
            case ConnUnknownEv.ID:
                event = "Connection unknown";
                break;
            case TermConnActiveEv.ID:
                event = "Terminal Connection active";
                break;
            case TermConnCreatedEv.ID:
                event = "Terminal Connection created";
                break;
            case TermConnDroppedEv.ID:
                event = "Terminal Connection dropped";
                break;
            case TermConnPassiveEv.ID:
                event = "Terminal Connection passive";
                break;
            case TermConnRingingEv.ID:
                event = "Terminal Connection ringing";
                break;
            case TermConnUnknownEv.ID:
                event = "Terminal Connection unknown";
                break;
            case CallCtlTermConnBridgedEv.ID:
                event = "CallControlTerminalConnection bridged";
                break;
            case CallCtlTermConnDroppedEv.ID:
                event = "CallControlTerminalConnection dropped";
                break;
            case CallCtlTermConnHeldEv.ID:
                event = "CallControlTerminalConnection held";
                break;
            case CallCtlTermConnInUseEv.ID:
                event = "CallControlTerminalConnection in use";
                break;
            case CallCtlTermConnRingingEv.ID:
                event = "CallControlTerminalConnection ringing";
                break;
            case CallCtlTermConnTalkingEv.ID:
                event = "CallControlTerminalConnection talking";
                break;
            case CallCtlTermConnUnknownEv.ID:
                event = "CallControlTerminalConnection unknown";
                break;
            case MediaTermConnAvailableEv.ID:
                event = "MediaTerminalConnection available"
                    ; break;
            case MediaTermConnUnavailableEv.ID:
                event = "MediaTerminalConnection unavailable";
                break;
            default:
                event = "unknown: " + id;
                break;
        }
        LOGGER.debug("Observer event: " + event);
        Connection connection = null;
        if(ev instanceof ConnEv) {
            connection = ((ConnEv)ev).getConnection();
        } else if(ev instanceof TermConnEv) {
            connection = ((TermConnEv)ev).getTerminalConnection().getConnection();
        } else if(id != CallActiveEv.ID && !(ev instanceof PrivateCallEv)) {
            Connection[] connections = ev.getCall().getConnections();
            if(connections != null) {
        		LOGGER.warn(ev.getClass().getName() + " with " + connections.length + " connections.");
                if(ev instanceof CallObservationEndedEv || ev instanceof CallInvalidEv) {
            		LOGGER.info("Removing " + connections.length + " connections...");
                	for(int i=0; i<connections.length; i++) {
                        removeConnection(connections[i]);
                	}
                }
            }
        }
        if(connection != null) {
            final int state;
            if(ev.getCause() == Ev.CAUSE_CALL_CANCELLED 
                    || ev.getCause() == Ev.CAUSE_DEST_NOT_OBTAINABLE) {
                state = Connection.DISCONNECTED;
                try {
                    connection.disconnect();
                } catch(Exception e) {
                    LOGGER.error("Cannot disconnect", e);
                }
            } else {
                state = connection.getState();
            }
            LOGGER.debug("Observer: connection state="
                    + TapiUtil.getConnectionStateName(connection));
            if(state == Connection.DISCONNECTED || state == Connection.FAILED 
                    || state == Connection.UNKNOWN) {
                LOGGER.debug("Observer: Removing connection...");
                removeConnection(connection);
                LOGGER.debug("Observer: Connection removed.");
            } else {
                updateConnection(connection);
            }
        } else {
            update();
        }
    }
}