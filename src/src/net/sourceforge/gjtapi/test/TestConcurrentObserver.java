package net.sourceforge.gjtapi.test;

import java.util.ConcurrentModificationException;

import javax.telephony.Call;
import javax.telephony.CallObserver;
import javax.telephony.Connection;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;
import javax.telephony.callcontrol.events.CallCtlTermConnTalkingEv;
import javax.telephony.events.CallActiveEv;
import javax.telephony.events.CallEv;
import javax.telephony.events.CallInvalidEv;
import javax.telephony.events.CallObservationEndedEv;
import javax.telephony.events.ConnAlertingEv;
import javax.telephony.events.ConnConnectedEv;
import javax.telephony.events.ConnCreatedEv;
import javax.telephony.events.ConnDisconnectedEv;
import javax.telephony.events.ConnFailedEv;
import javax.telephony.events.ConnInProgressEv;
import javax.telephony.events.ConnUnknownEv;
import javax.telephony.events.TermConnActiveEv;
import javax.telephony.events.TermConnCreatedEv;
import javax.telephony.events.TermConnDroppedEv;
import javax.telephony.events.TermConnPassiveEv;
import javax.telephony.events.TermConnRingingEv;
import javax.telephony.events.TermConnUnknownEv;
import javax.telephony.media.events.MediaTermConnAvailableEv;
import javax.telephony.media.events.MediaTermConnUnavailableEv;

import net.sourceforge.gjtapi.FreeTerminal;

/*
Copyright (c) 2009 Deadman Consulting Inc. (www.deadman.ca) 

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

/**
 * Helper class for the TestConcurrentHandler.
 * This tracks a terminal and puts an observer on it
 * to ensure that when the call ends the observer's callbacks
 * don't prevent the call cleanup.
 * @author richard
 *
 */
@SuppressWarnings("deprecation")
class TestConcurrentObserver implements CallObserver {

	private FreeTerminal term;
	private String name;
	//private String _otherTerm;	
	
    public TestConcurrentObserver(Terminal terminal) throws MethodNotSupportedException, ResourceUnavailableException {
        term = (FreeTerminal) terminal;
        name = term.getName();
        term.addCallObserver(this);
    }


	public void callChangedEvent(CallEv[] eventList) {
        for (int i = 0; i < eventList.length; i++) {
            CallEv ce = eventList[i];
            Call c = ce.getCall();
            
            Connection[] cons = null;
            try {
                // This will try to get all connections and may trigger
                // a ConcurrentModificationException
            	cons = c.getConnections();
            	
            	StringBuilder builder = new StringBuilder("Event for terminal ").append(this.name).append(". Event ");
            	switch(ce.getID()) {
	            	case CallActiveEv.ID:
	            		builder.append("Call Active");
	            		break;
	            	case CallInvalidEv.ID:
	            		builder.append("Call Invalid");
	            		break;
	            	case CallObservationEndedEv.ID:
	            		builder.append("Call Observation Ended");
	            		break;
	            	case ConnAlertingEv.ID:
	            		builder.append("Connection Alerting");
	            		break;
	            	case ConnConnectedEv.ID:
	            		builder.append("Connection Connected");
	            		break;
	            	case ConnCreatedEv.ID:
	            		builder.append("Connection Created");
	            		break;
	            	case ConnDisconnectedEv.ID:
	            		builder.append("Connection Disconnected");
	            		break;
	            	case ConnFailedEv.ID:
	            		builder.append("Connection Failed");
	            		break;
	            	case ConnInProgressEv.ID:
	            		builder.append("Connection In Progress");
	            		break;
	            	case ConnUnknownEv.ID:
	            		builder.append("Connection Unknown");
	            		break;
	            	case TermConnActiveEv.ID:
	            		builder.append("Terminal Connection Active");
	            		break;
	            	case TermConnCreatedEv.ID:
	            		builder.append("Terminal Connection Created");
	            		break;
	            	case TermConnDroppedEv.ID:
	            		builder.append("Terminal Connection Dropped");
	            		break;
	            	case TermConnPassiveEv.ID:
	            		builder.append("Terminal Connection Passive");
	            		break;
	            	case TermConnRingingEv.ID:
	            		builder.append("Terminal Connection Ringing");
	            		break;
	            	case TermConnUnknownEv.ID:
	            		builder.append("Terminal Connection Unknown");
	            		break;
	            	case CallCtlTermConnTalkingEv.ID:
	            		builder.append("Call Control Terminal Connection Talking");
	            		break;
	            	case MediaTermConnAvailableEv.ID:
	            		builder.append("Media Terminal Connection Available");
	            		break;
	            	case MediaTermConnUnavailableEv.ID:
	            		builder.append("Media Terminal Connection Unavailable");
	            		break;
	        		default:
	        			builder.append(ce.toString()).append(' ').append(ce.getID());
	        			break;
            	}
            	
            	if(cons != null) {
            		builder.append(". Connection Count: ").append(cons.length);
            	} else {
            		builder.append(". Connection Count: 0");
            	}
            	System.out.println(builder.toString());
            } catch (ConcurrentModificationException cme) {
            	System.err.println("Concurrent Modification Exception:");
            	cme.printStackTrace();
            } catch (Throwable e) {
            	e.printStackTrace();
            }
            
            // mimics an error found in production
            // Sometimes cons is null and this will throw an exception
            // back into the event dispatcher
            for (int j = 0; j < cons.length; j++) {
                Connection con = cons[j];
                System.out.println("\t address= " + con.getAddress().getName());
                Terminal[] mts = con.getAddress().getTerminals();

                if (mts != null) {
                    for (int k = 0; k < mts.length; k++) {
                        Terminal t = mts[k];
                        System.out.println("\t terminal= " + t.getName());
                        if (!t.getName().equals(name)) {
                            //_otherTerm = t.getName();
                        }
                    }
                }
            }

        }
	}

}
