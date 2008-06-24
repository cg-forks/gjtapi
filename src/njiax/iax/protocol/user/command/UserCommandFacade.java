package iax.protocol.user.command;


import iax.protocol.peer.Peer;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Facade to user commands.
 */
public class UserCommandFacade {

    private static ExecutorService commandExecutor = Executors.newFixedThreadPool(1);

    /**
     * Method that indicates that user has answered an incoming call.
     * @param peer Current peer.
     * @param callingNumber the calling number of the call that is going to be accepted
     */
    public static void answerCall(Peer peer, String callingNumber) {
        commandExecutor.submit(new AnswerCall(peer, callingNumber));
    }

    /**
     * Method to hang up a call.
     * @param peer Current peer.
     * @param calledNumber The number of the hung call.
     */
    public static void hangupCall(Peer peer, String calledNumber) {
        commandExecutor.submit(new HangupCall(peer, calledNumber));
    }

    /**
     * Method to hold a call.
     * @param peer Current peer.
     * @param calledNumber The number of the muted call.
     */
    public static void holdCall(Peer peer, String calledNumber) {
        commandExecutor.submit(new HoldCall(peer, calledNumber));
    }

    /**
     * Method to start a new call.
     * @param peer Current peer.
     * @param calledNumber Number to call to.
     */
    public static void newCall(Peer peer, String calledNumber) {
        commandExecutor.submit(new NewCall(peer, calledNumber));
    }

    /**
     * Exit from the system
     * @param peer Current peer.
     */
    public static void exit(Peer peer) {
        commandExecutor.submit(new Exit(peer));
    }

    /**
     * Method to mute a call.
     * @param peer Current peer.
     * @param calledNumber The number of the muted call.
     */
    public static void muteCall(Peer peer, String calledNumber) {
        commandExecutor.submit(new MuteCall(peer, calledNumber));
    }

    /**
     * Method to unhold a call.
     * @param peer Current peer.
     * @param calledNumber The number of the muted call.
     */
    public static void unHoldCall(Peer peer, String calledNumber) {
        commandExecutor.submit(new UnHoldCall(peer, calledNumber));
    }

    /**
     * Method to unmute a call.
     * @param peer Current peer.
     * @param calledNumber The number of the muted call.
     */
    public static void unMuteCall(Peer peer, String calledNumber) {
        commandExecutor.submit(new UnMuteCall(peer, calledNumber));
    }

    /**
     * Method to send a DTMF tone.
     * @param peer Current peer.
     * @param calledNumber The number of the muted call.
     */
    public static void sendDTMF(Peer peer, String calledNumber, char tone) {
        commandExecutor.submit(new SendDTMF(peer, calledNumber, tone));
    }

    /**
     * Method to transfer a call.
     * @param peer Current peer.
     * @param srcNumber the source number of the transfer
     * @param dstNumber the destination number of the transfer
     */
    public static void transferCall(Peer peer, String srcNumber, String dstNumber) {
        commandExecutor.submit(new TransferCall(peer, srcNumber, dstNumber));
    }
}
