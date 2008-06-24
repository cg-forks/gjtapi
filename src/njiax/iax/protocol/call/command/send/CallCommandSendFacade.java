package iax.protocol.call.command.send;

import iax.protocol.call.Call;
import iax.protocol.frame.FullFrame;
import iax.protocol.frame.ProtocolControlFrame;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Facade of the commands valids for sending a frame
 * Delegates its funcionality in a specific CommandSend.
 */
public class CallCommandSendFacade {

    private static ExecutorService commandExecutor = Executors.newFixedThreadPool(1);

    /**
     * Sends an ack delegating in the Ack command send
     * @param call call for sending the frame
     * @param fullFrame full frame that needs an ack
     */
    public static void ack(Call call, FullFrame fullFrame) {
        commandExecutor.submit(new Ack(call, fullFrame));
    }

    /**
     * Sends an accept delegating in the Accept command send
     * @param call call for sending the frame
     */
    public static void accept(Call call) {
        commandExecutor.submit(new Accept(call));
    }

    /**
     * Sends an authorization reply delegating in the AuthRep command send
     * @param call call for sending the frame
     * @param authReqFrame authorization request frame that needs an authorization reply frame
     */
    public static void authRep(Call call, ProtocolControlFrame authReqFrame) {
        commandExecutor.submit(new AuthRep(call, authReqFrame));
    }

    /**
     * Sends an hangup delegating in the Hangup command send
     * @param call call for sending the frame
     */
    public static void hangup(Call call) {
    	commandExecutor.submit(new Hangup(call));
    }

    /**
     * Sends a lag reply frame delegating in the LagRp command send
     * @param call call for sending the frame
     * @param lagRpFrame lag request frame that needs the lag reply frame
     */
    public static void lagrp(Call call, ProtocolControlFrame lagRpFrame) {
        commandExecutor.submit(new LagRp(call, lagRpFrame));
    }

    /**
     * Sends a ping delegating in the Ping command send
     * @param call call for sending the frame
     */
    public static void ping(Call call) {
        commandExecutor.submit(new Ping(call));
    }

    /**
     * Sends a pong delegating in the Pong command send
     * @param call call for sending the frame
     * @param pingFrame ping frame that needs a pong frame
     */
    public static void pong(Call call, ProtocolControlFrame pingFrame) {
        commandExecutor.submit(new Pong(call, pingFrame));
    }

    /**
     * Sends a ringing delegating in the Ringing command send
     * @param call call for sending the frame
     */
    public static void ringing(Call call) {
        commandExecutor.submit(new Ringing(call));
    }

    /**
     * Sends a voice frame delegating in the SendVoice command send
     * @param call call for sending the frame
     * @param audioBuffer audio data to send in GSM format
     */
    public static void sendVoice(Call call, byte[] audioBuffer) {
        commandExecutor.submit(new SendVoice(call, audioBuffer));
    }

    /**
     * Sends an unsupported frame for a fullFrame received that is not supported
     * @param call call for sending the frame
     * @param fullFrame full frame not supported that needs an unsupported frame
     */
    public static void unsupport(Call call, ProtocolControlFrame fullFrame) {
        commandExecutor.submit(new Unsupport(call, fullFrame));
    }
}
