package net.sourceforge.gjtapi.raw.njiax.peer;

import iax.protocol.peer.Peer;
import iax.protocol.peer.PeerListener;
import iax.protocol.user.command.UserCommandFacade;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;

import javax.telephony.ConnectionEvent;
import javax.telephony.Event;

import net.sourceforge.gjtapi.raw.njiax.NjIaxCallId;
import net.sourceforge.gjtapi.raw.njiax.NjIaxProvider;

/**
 * <p>Title: NjIaxAgent</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: L2F | INESC-ID</p>
 *
 * @author Dário Marcelino
 * @version 1.0
 */
public class NjIaxPeer implements PeerListener {

    NjIaxProvider provider;
    Peer peer;
    NjIaxCallId currentCall;
    NjIaxCallId transferedCall;
    String userName;
    Object callLock = new Object();

    InputStreamAdapter inStream = new InputStreamAdapter();
    OutputStreamAdapter outStream = new OutputStreamAdapter();

    public NjIaxPeer(NjIaxProvider provider, String userName, String password,
                     String host, boolean register, int maxCalls) {
        this.provider = provider;
        this.userName = userName;
//        peer = new Peer(this, userName, password, host, register, maxCalls);
        // This is a forked version of the constructor -- See njiax-gjtapi-1.0.0.jar
        // Renato Cassaca's note:
        // "I had to change a lot of files in NjIax source so I copied _all_ njiax files no gjtapi/src/njiax.
        // That means that gjtapi has a fork of njiax. I think that it makes sense because I tried to contact
        // several time njiax developers and I didn't get any feedback. They had some "solutions" that I didn't
        // like so much, like create a new thread each time 160 bytes of audio have to be sent to Asterisk....
        // To work around this unwanted feature I added a PoolExecutor. The problem is that it's only available
        // in Java6...."
        peer = new Peer(this, userName, password, host, register, maxCalls,
                        outStream, inStream);
    }

    public void setCallId(NjIaxCallId id) {
        currentCall = id;
    }

    public NjIaxCallId getCallId() {
        return currentCall;
    }

    public void answerCall(NjIaxCallId call) {
        UserCommandFacade.answerCall(peer, call.getCallParticipant());

        /** When an ingoing call has been accepted */
        provider.connectionConnected(currentCall, call.getCallParticipant(),
                                     ConnectionEvent.CAUSE_NORMAL);
        provider.callActive(currentCall, Event.CAUSE_NORMAL);

    }

    public NjIaxCallId newCall(NjIaxCallId callID, String calledNumber) {
        //System.out.println("New call to "+ calledNumber +" (" + callID.getId() + ")");
        callID.setCallParticipant(calledNumber);
        if (currentCall == null) {
            currentCall = callID;
            //System.out.println("NjIaxPeer, " + userName + " calling " + calledNumber);
            UserCommandFacade.newCall(peer, calledNumber);
        } else
            transferedCall = callID;
        return callID;
    }

    public void release() {
        //System.out.println("Release call");
        synchronized (callLock) {
            if (currentCall != null && currentCall != transferedCall) {
                UserCommandFacade.hangupCall(peer,
                                             currentCall.getCallParticipant());
                currentCall = null;
            }
        }
    }


    //	 ****************************** Media ****************************

    public void play(InputStream src, long duration) {
        //System.out.println("NjIaxPeer, play: starting");
        Timer timer;

        if (duration != javax.telephony.media.ResourceConstants.v_Forever) {
            timer = new Timer("PlayDuration");
            timer.schedule(new StopPlayTask(this), duration);
            //System.out.println("Play: Duration set to: " + duration);
        }
        inStream.setInputStream(src);
        inStream.waitForEnd();
        //System.out.println("NjIaxPeer, play: finished");
    }

    public void record(OutputStream dest, long duration) {
        //System.out.println("NjIaxPeer, record: starting");
        Timer timer;

        if (duration != javax.telephony.media.ResourceConstants.v_Forever) {
            timer = new Timer("RecordDuration");
            timer.schedule(new StopRecordTask(this), duration);
            //System.out.println("Record: Duration set to: " + duration);
        }

        outStream.setOutputStream(dest);
        outStream.waitForEnd();
        //System.out.println("NjIaxPeer, record: finished");
    }

    public void stopRecord() {
        try {
            if (outStream != null) {
                outStream.close();
                outStream.waitForEnd();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlay() {
        try {
            if (inStream != null) {
                int prevAvail = -1;
                int stalledCounter = 0;
                do {
                    int avail = inStream.available();
                    if (avail < 1)
                        break;
                    else {
                        if (prevAvail < 0) {
                            prevAvail = avail;
                            stalledCounter = 0;
                        }
                        else {
                            if (avail < prevAvail) {
                                stalledCounter = 0;
                                //Consuming....
                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException ex) {
                                }
                            }
                            else if (avail > prevAvail) {
                                stalledCounter = 0;
                                //Still receiving audio
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException ex1) {
                                }
                            }
                            else if (avail == prevAvail) {
                                stalledCounter += 1;
                                if (stalledCounter == 5) {
                                    System.err.println("NjIAX stopPlay: stopping because data is stalled @ " + avail);
                                    break;
                                }
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException ex2) {
                                }
                            }
                            prevAvail = avail;
                        }
                    }


                } while (true);
                inStream.close();
                inStream.waitForEnd();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        //System.out.println("GJTAPI: Media stop");
        stopPlay();
        stopRecord();
    }

    //	 *************************** Call Control *************************
    public void hold(NjIaxCallId callID) {
        //System.out.println("NjIaxPeer, holding call with " + callID.getCallParticipant());
        //UserCommandFacade.holdCall(peer, callID.getCallParticipant());
    }

    public void unHold(NjIaxCallId callID) {
        //System.out.println("NjIaxPeer, unholding call with " + callID.getCallParticipant());
        UserCommandFacade.unHoldCall(peer, callID.getCallParticipant());
    }

    public void transfer(NjIaxCallId callID1, NjIaxCallId callID2) {
        transferedCall = callID1;
        //System.out.println("NjIaxProvider, Transfering " + callID1.getCallParticipant() + " to " + callID2.getCallParticipant());
        UserCommandFacade.transferCall(peer, callID1.getCallParticipant(), callID2.getCallParticipant());
    }


    //	 *************************** PeerListener *************************

    public void hungup(String calledNumber) {
        //System.out.println(calledNumber + " Hungup.");
        /** When a call has been locally or remotely closed */
        synchronized (callLock) {
            if (currentCall != null) {
                provider.connectionDisconnected(currentCall, calledNumber,
                                                Event.CAUSE_NORMAL);
                currentCall = null;
            }
        }
    }

    public void recvCall(String callingName, String callingNumber) {
        //System.out.println(userName + " peer received call from " + callingName + " (" + callingNumber + ")");

        currentCall = new NjIaxCallId();
        currentCall.setCallParticipant(callingNumber);

        provider.terminalConnectionRinging(currentCall, userName,
                                           userName,
                                           ConnectionEvent.CAUSE_NORMAL);
        provider.connectionInProgress(currentCall, userName,
                                      Event.CAUSE_NORMAL);
        provider.connectionAlerting(currentCall, userName,
                                    ConnectionEvent.CAUSE_NORMAL);
    }

    public void registered() {
        //System.out.println(userName + " has registered.");
    }

    public void waiting() {
        //System.out.println(userName + " is waiting for calls.");
    }

    public void unregistered() {
        //System.out.println(userName + " has unregistered.");
    }

    public void exited() {
        //System.out.println(userName + " has exited.");
    }

    public void answered(String calledNumber) {
        //System.out.println(calledNumber + " has answered the call from " + userName);
        /** When an outgoing call has been accepted */
        provider.connectionConnected(currentCall, calledNumber,
                                     ConnectionEvent.CAUSE_NORMAL);
        provider.callActive(currentCall, Event.CAUSE_NORMAL);
    }

    public void playWaitTones(String calledNumber) {
        //System.out.println(calledNumber + " is ringing, call from " + userName);
        /** When an outgoing call is remotly ringing */
        provider.terminalConnectionCreated(currentCall, calledNumber,
                                           calledNumber,
                                           ConnectionEvent.CAUSE_NORMAL);
        provider.connectionInProgress(currentCall, calledNumber,
                                      Event.CAUSE_NORMAL);
        provider.connectionAlerting(currentCall, calledNumber,
                                    ConnectionEvent.CAUSE_NORMAL);
    }

    public void stopWaitTones(String calledNumber) {
        throw new RuntimeException("stopWaitTones: NOT IMPLEMENTED");
    }


}
