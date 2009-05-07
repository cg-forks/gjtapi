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

import javax.telephony.Connection;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlConnection;
import javax.telephony.callcontrol.CallControlTerminalConnection;

public class TapiUtil {
    /**
     * Do not instantiate.
     */
    private TapiUtil() {
    }
    
    public static int getTerminalConnectionState(TerminalConnection terminalConnection) {
        int state = CallControlTerminalConnection.UNKNOWN;
        if(terminalConnection != null) {
            if(terminalConnection instanceof CallControlTerminalConnection) {
                state = ((CallControlTerminalConnection)terminalConnection).getCallControlState();
            } else {
                switch(terminalConnection.getState()) {
                    case TerminalConnection.IDLE:
                        state = CallControlTerminalConnection.IDLE;
                        break; 
                    case TerminalConnection.RINGING:
                        state = CallControlTerminalConnection.RINGING;
                        break; 
                    case TerminalConnection.ACTIVE:
                        state = CallControlTerminalConnection.TALKING;
                        break; 
                    case TerminalConnection.PASSIVE:
                        state = CallControlTerminalConnection.INUSE;
                        break; 
                    case TerminalConnection.DROPPED:
                        state = CallControlTerminalConnection.DROPPED;
                        break; 
                    default:
                        state = CallControlTerminalConnection.UNKNOWN;
                    break;
                }
            }
        }
        return state;
    }

    public static String getTerminalConnectionStateName(TerminalConnection terminalConnection) {        
        switch(getTerminalConnectionState(terminalConnection)) {
            case CallControlTerminalConnection.IDLE:
                return "IDLE"; 
            case CallControlTerminalConnection.RINGING:
                return "RINGING"; 
            case CallControlTerminalConnection.TALKING:
                return "TALKING"; 
            case CallControlTerminalConnection.HELD:
                return "HELD"; 
            case CallControlTerminalConnection.INUSE:
                return "INUSE"; 
            case CallControlTerminalConnection.BRIDGED:
                return "BRIDGED"; 
            case CallControlTerminalConnection.DROPPED:
                return "DROPPED"; 
            default:
                return "UNKNOWN";
        }
    }
    
    public static String getConnectionStateName(Connection connection) {
        switch(connection.getState()) {
            case Connection.IDLE:
                return "IDLE";
            case Connection.INPROGRESS:
                return "INPROGRESS";
            case Connection.ALERTING:
                return "ALERTING";
            case Connection.CONNECTED:
                return "CONNECTED";
            case Connection.DISCONNECTED:
                return "DISCONNECTED";
            case Connection.FAILED:
                return "FAILED";
            default:
                return "UNKNOWN";
        }
    }

}
