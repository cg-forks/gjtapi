package iax.protocol.call;

import iax.audio.AudioListener;
import iax.audio.Player;
import iax.audio.PlayerException;
import iax.audio.Recorder;
import iax.audio.RecorderException;
import iax.audio.ulaw.ULAWPlayer;
import iax.audio.ulaw.ULAWRecorder;
import iax.protocol.call.command.send.CallCommandSendFacade;
import iax.protocol.call.state.CallState;
import iax.protocol.call.state.Initial;
import iax.protocol.frame.Frame;
import iax.protocol.frame.FullFrame;
import iax.protocol.frame.MiniFrame;
import iax.protocol.frame.ProtocolControlFrame;
import iax.protocol.peer.Peer;
import iax.protocol.peer.PeerException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Class that encapsulates the funcionality of a iax call and implements the interface AudioListener
 * for playing and recording audio.
 */
public class Call implements AudioListener {

    /**
     * Interval in milliseconds that the miniframe's timestamp is reset
     */
    public static final int TIMESTAMP_MINIFRAME_RESET = 65536;

    // Maximun size of a sequence number
    private final int SEQNO_MAXSIZE = 256;
    //  ***** PING DESACTIVATED *****
    //Peer's ping refresh in seconds
    //private final int PING_REFRESH = 5;
    //Call's retry max
    private final int RETRY_MAXCOUNT = 5;
    //Peer's retry refresh in seconds
    private final int RETRY_REFRESH = 1;

    // HashMap with the frames that are waiting for an ack
    private HashMap<Long, FullFrame> framesWaitingAck;
    // HashMap with the frames that are waiting for a specific reply
    private HashMap framesWaitingReply;
    // Audio player
    private Player player;
    // Audo Recorder
    private Recorder recorder;
    // Flag to known if the audio is being played or not
    private boolean playing = false;
    // Actual call's state
    private CallState state;
    // Source call number of this call (number to identify this call)
    private int srcCallNo;
    // Destination call number of this call (number got from the remote call)
    private int destCallNo;
    // Source timestamp for full frames from the first full frame sent by this call
    private long srcTimestamp;
    // Source timestamp for mini frames from the first mini frame sent by this call or the last mini frame's timestamp reset
    private long srcTimestampMiniFrame;
    // Oubound sequence number.
    private int oseqno;
    // Inbound sequence number.
    private int iseqno;
    // Called number (number or extension's indentifier)
    private String calledNumber;
    // Flag to known if the firstVoiceFrame was sent or not
    private boolean firstVoiceFrameSent;
    // Peer that handles this call
    private Peer peer;
    // Ping timer task to send ping frames
    private Timer pingTimer;
    // Retry timer task to retry frames not commited whith a specific frame or an ack frame
    private Timer retryTimer;
    //Flag to determinate if the call is hold or not
    private boolean hold;
    //Flag to determinate if the call is mute or not
    private boolean mute;

    //Audio Streams
    private OutputStream outStream;
    private InputStream inStream;

    /**
     * Constructor. Initialize the player and the recorder
     * @param peer the peer that handles this call
     * @param srcCallNo source call number of this call
     * @throws CallException if an error occurred
     */
    public Call(Peer peer, int srcCallNo, OutputStream outStream,
                InputStream inStream) throws CallException {
        this.peer = peer;
        this.srcCallNo = srcCallNo;
        this.hold = false;
        this.mute = false;
        this.framesWaitingAck = new HashMap<Long, FullFrame>();
        this.framesWaitingReply = new HashMap();
        this.outStream = outStream;
        this.inStream = inStream;

        try {
            //this.player = new GSMPlayer();
            //this.recorder = new GSMRecorder();
            this.player = new ULAWPlayer(outStream);
            this.recorder = new ULAWRecorder(this, inStream);
        } catch (Exception e) {
            throw new CallException(e);
        }
    }


    /**
     * Gets the peer that handles this call
     * @return the peer that handles this call
     */
    public Peer getPeer() {
        return peer;
    }

    /**
     * Gets the source call number of this call
     * @return the source call number of this call
     */
    public int getSrcCallNo() {
        return srcCallNo;
    }

    /**
     * Gets the destination call number of this call
     * @return the destination call number of this call
     */
    public int getDestCallNo() {
        return destCallNo;
    }

    /**
     * Gets the inbound sequence number of this call
     * @return the inbound sequence number of this call
     */
    public int getIseqno() {
        return iseqno;
    }

    /**
     * Gets the outbound sequence number of this call
     * @return the outbound sequence number of this call
     */
    public int getOseqno() {
        return oseqno;
    }

    /**
     * Gets the called number (number or extension's identifier) of this call
     * @return the called number (number or extension's identifier) of this call
     */
    public String getCalledNumber() {
        return calledNumber;
    }

    /**
     * Gets the timestamp from the first full frame sent
     * @return the timestamp from the first full frame sent
     */
    public long getTimestamp() {
        long now = System.currentTimeMillis();

        long fullFrameTimestamp = now - srcTimestamp;
        //fullFrameTimestamp = now;
        miniFrameTimestamp = fullFrameTimestamp;

        return fullFrameTimestamp;
    }

    //private long fullFrameTimestamp = System.currentTimeMillis();
    private long miniFrameTimestamp = srcTimestampMiniFrame;

    /**
     * Gets the timestamp from the first mini frame sent or the last reset
     * @return the timestamp from the first mini frame sent or the last reset
     */
    public long getTimestampMiniFrame() {
        miniFrameTimestamp += 20;
        return miniFrameTimestamp;
    }

    /**
     * Updates the inbound sequence number with the number passed in the argumens
     * @param value to update the inbound sequence number
     */
    private synchronized void incIseqno(int value) {
        iseqno = value;
        if (iseqno == SEQNO_MAXSIZE)
            iseqno = 0;
    }

    /**
     * Updates the outbound sequence number with the number passed in the argumens
     * @param value to update the outbound sequence number
     */
    private synchronized void incOseqno(int value) {
        oseqno = value;
        if (oseqno == SEQNO_MAXSIZE)
            oseqno = 0;
    }

    /**
     * Gets if the first voice frame was sent or not
     * @return true if the first voice frame was sent, or false if not
     */
    public boolean isFirstVoiceFrameSent() {
        return firstVoiceFrameSent;
    }

    /**
     * Sets that the first voice frame was sent
     */
    public void firstVoiceFrameSent() {
        this.firstVoiceFrameSent = false;
    }

    /**
     * Gets if the call is hold or not
     * @return true if the call is hold, false if not
     */
    public boolean isHold() {
        return hold;
    }

    /**
     * Gets if the call is mute or not
     * @return true if the call is mute, false if not
     */
    public boolean isMute() {
        return hold;
    }

    /**
     * Starts the call. For that initialize the sequence's numbers of this call and the destination call number to zero,
     * the flag to known if the first voice frame was sent to false, the call's state to initial and the sources timestamps
     * to the actual timestamp. Also starts the timers (ping and retry)
     * @param calledNumber the called number (number or extension's identifier) of this call
     */
    public void startCall(String calledNumber) {
        this.iseqno = 0;
        this.oseqno = 0;
        this.destCallNo = 0;
        this.firstVoiceFrameSent = true;
        this.framesWaitingAck.clear();
        this.calledNumber = calledNumber;
        this.state = Initial.getInstance();
        this.srcTimestamp = System.currentTimeMillis();
        this.srcTimestampMiniFrame = srcTimestamp;
//      ***** PING DESACTIVATED *****
//      this.pingTimer = new Timer();
//      TimerTask pingTimerTask = new TimerTask() {
//      public void run() {
//      ping();
//      }
//      };
//      pingTimer.schedule(pingTimerTask, PING_REFRESH*1000, PING_REFRESH*1000);
        this.retryTimer = new Timer();
      /******RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
        TimerTask retryTimerTask = new TimerTask() {
            public void run() {
                retryFramesWaiting();
            }
        };
        retryTimer.schedule(retryTimerTask, RETRY_REFRESH * 1000,
                            RETRY_REFRESH * 1000);*/

    }

    /**
     * Notifies an answered
     */
    public void answeredCall() {
        peer.answeredCall(this);
        startRecorder();
    }

    /**
     * Notifies a received ringing for playing wait tones
     */
    public void ringingCall() {
        peer.ringingCall(this);
    }

    /**
     * Notifies a received stop ringing for stopping wait tones
     * (STOP SOUNDS DOESN'T EXIST IN THE DRAFT)
     */
    public void stopRingingCall() {
        peer.stopRingingCall(this);
    }

    /**
     * Saves the destination call number of this call to the destination call number passed in the arguments, and notifies
     * to the peer for the binding of this call
     * @param destCallNo the destination call number of this call
     */
    public void bindCall(int destCallNo) {
        this.destCallNo = destCallNo;
        peer.bindCall(this);
    }

    /**
     * Ends a call. For that stops the player and the recorder, and notifies the peer that this call is finished
     */
    public void endCall() {
//      ***** PING DESACTIVATED *****
//      pingTimer.cancel();
        retryTimer.cancel();
        player.stop();
        recorder.stop();
        peer.endCall(this);
    }

    /**
     * Holds a call. For that stops the recorder and the player
     */
    public void holdCall() {
        if (!hold) {
            hold = true;
            player.stop();
            try {
                //player = new GSMPlayer();
                player = new ULAWPlayer(outStream);
            } catch (PlayerException e) {
                e.printStackTrace();
            }
            if (!mute) {
                recorder.stop();
                try {
                    //recorder = new GSMRecorder();
                    recorder = new ULAWRecorder(this, inStream);
                } catch (RecorderException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Unholds a call. For that starts the recorder and the player
     */
    public void unHoldCall() {
        if (hold) {
            player.play();
            if (!mute) {
                recorder.record(this);
            }
            hold = false;
        }
    }

    /**
     * Mutes a call. For that stops the recorder.
     */
    public void muteCall() {
        if (!mute) {
            mute = true;
            recorder.stop();
            try {
                //recorder = new GSMRecorder();
                recorder = new ULAWRecorder(this, inStream);
            } catch (RecorderException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Unmutes a call. For that starts the recorder.
     */
    public void unMuteCall() {
        if (mute) {
            recorder.record(this);
            mute = false;
        }
    }

    /**
     * Resets the mini frame's source timestamp becouse of the overflow, that is the base for calculating the mini frame's
     * timestamp.
     */
    public synchronized void resetTimestampMiniFrame() {
        srcTimestampMiniFrame = System.currentTimeMillis();
    }

    /**
     * Send a ping
     */
    public void ping() {
        CallCommandSendFacade.ping(this);
    }

    /**
     * Sets the call's state to the state passed in the arguments
     * @param state the new call's state
     */
    public void setState(CallState state) {
        this.state = state;
    }

    /**
     * Handles the full frame received. For that update the outbound sequence number of this call to the inbound sequence
     * number of the full frame and, if it isn't an ack, the inbound sequence number of this call to the outbound sequence
     * number of the full frame + 1. After that, delegates in the call's state for handling the full frame.
     * @param fullFrame the full frame received
     */
    public void handleRecvFrame(FullFrame fullFrame) {
        if (fullFrame.getFrameType() != FullFrame.PROTOCOLCONTROLFRAME_T) {
            incIseqno(fullFrame.getOseqno() + 1);
        } else if (fullFrame.getSubclass() != ProtocolControlFrame.ACK_SC) {
            incIseqno(fullFrame.getOseqno() + 1);
        }
        incOseqno(fullFrame.getIseqno());
        state.handleRecvFrame(this, fullFrame);
    }

    /**
     * Handles the mini frame received. For that, delegates in the call's state for handling the mini frame.
     * @param frame the mini frame received
     */
    public void handleRecvFrame(MiniFrame frame) {
        state.handleRecvFrame(this, frame);
    }

    /**
     * Handles the sending of a frame. For that, delegates in the call's state for handling this sending.
     * @param frame the frame to send
     */
    public void handleSendFrame(Frame frame) {
        state.handleSendFrame(this, frame);
    }

    /**
     * Ackes a full frame waiting for that removing it from the list of full frames waiting for ack
     * @param timeStamp the timestamp of the full frame acked
     */
    public synchronized void ackedFrame(int oSeqNumber) {
        for (FullFrame frame : framesWaitingAck.values()) {
            if (frame.getIseqno() == oSeqNumber) {
                framesWaitingAck.remove(frame.getTimestamp());
                break;
            }
        }
    }

    /**
     * Replied a full frame waiting for that removing it from the list of full frames waiting for reply
     * @param id the id of the full frame reply
     */
    public synchronized void repliedFrame(int id) {
        framesWaitingReply.remove(id);
    }

    /**
     * Sends a frame without wait reply or ack. For that delegates in the iax peer.
     * @param frame the mini frame to send
     */
    public void sendFrameAndNoWait(Frame frame) {
        peer.sendFrame(frame);
    }

    /**
     * Sends a full frame that needs to be acked. For that added it to the list of frame waiting to be acked and after that
     * delegates in the iax peer to send it
     * @param fullFrame the full frame to send
     */
    public synchronized void sendFullFrameAndWaitForAck(FullFrame fullFrame) {
        framesWaitingAck.put(fullFrame.getTimestamp(), fullFrame);
        peer.sendFrame(fullFrame);
    }

    /**
     * Sends a full frame that needs to be replied. For that added it to the list of frame waiting to be replied and after that
     * delegates in the iax peer to send it
     * @param fullFrame the full frame to send
     */
    public synchronized void sendFullFrameAndWaitForRep(FullFrame fullFrame) {
        framesWaitingReply.put(fullFrame.getSubclass(), fullFrame);
        peer.sendFrame(fullFrame);
    }

    /**
     * Plays audio from the audio frames received through the player. If the player is stopped, starts it.
     * @param timestamp the timestamp of the frame
     * @param data the audio data of the frame
     * @param absolute if the timestamp absolute or not
     */
    public void writeAudioIn(long timestamp, byte[] data, boolean absolute) {
        if (!hold) {
            player.write(timestamp, data, absolute);
            if (!playing) {
                player.play();
                playing = true;
            }
        }
    }

    /**
     * Starts to record audio from the microphone through the recorder
     */
    public void startRecorder() {
        recorder.record(this);
    }

    // Method to retry frames that haven't been commited whith a specific frame or an ack frame
    private synchronized void retryFramesWaiting() {
        try {
            Iterator iterator = framesWaitingAck.values().iterator();
            while (iterator.hasNext()) {
                FullFrame retryFullFrame = (FullFrame) iterator.next();
                retryFullFrame.incRetryCount();
                if (retryFullFrame.getRetryCount() < RETRY_MAXCOUNT) {
                    peer.sendFrame(retryFullFrame);
                } else
                    throw new PeerException("Reached retries maximum (" +
                                            RETRY_MAXCOUNT + ") in the call " +
                                            srcCallNo +
                                            " for a full frame of type " +
                                            retryFullFrame.getFrameType() +
                                            ", subclass " +
                                            retryFullFrame.getSubclass());
            }
            iterator = framesWaitingReply.values().iterator();
            while (iterator.hasNext()) {
                FullFrame retryFullFrame = (FullFrame) iterator.next();
                retryFullFrame.incRetryCount();
                if (retryFullFrame.getRetryCount() < RETRY_MAXCOUNT) {
                    peer.sendFrame(retryFullFrame);
                } else
                    throw new PeerException(
                            "Reached retries maximun in the call " + srcCallNo +
                            " for a full frame of type " +
                            retryFullFrame.getFrameType() + ", subclass " +
                            retryFullFrame.getSubclass());
            }
        } catch (PeerException e) {
            framesWaitingAck.clear();
            framesWaitingReply.clear();
            endCall();
            e.printStackTrace();
        }
    }


    public void listen(byte[] buffer, int pos, int length) {
        //byte[] audioBuffer = new byte[length];
        byte[] audioBuffer = new byte[length - pos];
        //System.arraycopy(buffer, pos, audioBuffer, 0, length);
        System.arraycopy(buffer, pos, audioBuffer, 0, length - pos);
        CallCommandSendFacade.sendVoice(this, audioBuffer);
    }

}
