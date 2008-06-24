package net.sourceforge.gjtapi.raw.njiax.peer;

import net.sourceforge.gjtapi.raw.njiax.NjIaxCallId;
import net.sourceforge.gjtapi.raw.njiax.NjIaxProvider;

import javax.telephony.Event;
import javax.telephony.ConnectionEvent;

import iax.protocol.peer.Peer;
import iax.protocol.peer.PeerListener;
import iax.protocol.user.command.UserCommandFacade;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Timer;

/**
 * <p>Title: NjIaxAgent</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: L2F | INESC-ID</p>
 *
 * @author D�rio Marcelino
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
        peer = new Peer((PeerListener)this, userName, password, host, register, maxCalls,
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


}