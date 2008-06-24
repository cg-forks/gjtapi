package iax.protocol.peer.command.send;

import iax.protocol.frame.Frame;
import iax.protocol.frame.FullFrame;
import iax.protocol.frame.ProtocolControlFrame;
import iax.protocol.peer.Peer;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class PeerCommandSendFacade{

    private static ExecutorService commandExecutor = Executors.newFixedThreadPool(1);

    /**
     * Sends an ack delegating in the Ack command send
     * @param peer peer for sending the frame
     * @param fullFrame full frame that needs an ack
     */
    public static void ack(Peer peer, FullFrame fullFrame) {
        commandExecutor.submit(new Ack(peer, fullFrame));
    }

    /**
     * Sends a busy delegating in the Busy command send
     * @param peer peer for sending the frame
     * @param fullFrame full frame that need a busy frame
     */
    public static void busy(Peer peer, FullFrame fullFrame) {
        commandExecutor.submit(new Busy(peer, fullFrame));
    }


    /**
     * Sends an inval frame for a frame received without any call that handles it, delegating in the Inval command send
     * @param peer peer for sending the frame
     * @param frame the frame received without any call that handles it
     */
    public static void inval(Peer peer, Frame frame) {
        commandExecutor.submit(new Inval(peer, frame));
    }

    /**
     * Sends a pong delegating in the Pong command send
     * @param call call for sending the frame
     * @param pingFrame ping frame that needs a pong frame
     */
    public static void pong(Peer peer, ProtocolControlFrame poke) {
        commandExecutor.submit(new Pong(peer, poke));
    }

    /**
     * Sends a register release frame, delegating in the RegReq command send
     * @param peer peer for sending the frame
     */
    public static void regrel(Peer peer) {
        commandExecutor.submit(new RegRel(peer));
    }

    /**
     * Sends a register release frame for a regauth frame received, delegating in the RegReq command send
     * @param peer peer for sending the frame
     * @param regauthFrame regauth frame
     */
    public static void regrel(Peer peer, ProtocolControlFrame regauthFrame) {
        commandExecutor.submit(new RegRel(peer, regauthFrame));
    }

    /**
     * Sends a register request frame, delegating in the RegReq command send
     * @param peer peer for sending the frame
     */
    public static void regreq(Peer peer) {
        commandExecutor.submit(new RegReq(peer));
    }

    /**
     * Sends a register request frame for a regauth frame received, delegating in the RegReq command send
     * @param peer peer for sending the frame
     * @param regauthFrame regauth frame
     */
    public static void regreq(Peer peer, ProtocolControlFrame regauthFrame) {
        commandExecutor.submit(new RegReq(peer, regauthFrame));
    }

    /**
     * Sends an unsupported frame for a fullFrame received that is not supported, delegating in the Unsupport command send
     * @param peer peer for sending the frame
     * @param fullFrame full frame not supported that needs an unsupported frame
     */
    public static void unsupport(Peer peer, FullFrame fullFrame) {
        commandExecutor.submit(new Unsupport(peer, fullFrame));
    }
}
