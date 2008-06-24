package iax.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import iax.protocol.peer.Peer;
import iax.protocol.peer.PeerListener;
import iax.protocol.user.command.UserCommandFacade;

public class IaxPhone implements PeerListener {
    public final static int WAITING = 1;
    public final static int CALLACTIVE = 2;
    public final static int RINGING = 4;
    public final static int CALLING = 8;

    private Peer peer;
    private int state = WAITING;
    private String callParticipant;


    public IaxPhone(String user, String pass, String host, boolean register,
                    int maxCalls, OutputStream out, InputStream in) {
        peer = new Peer(this, user, pass, host, register, maxCalls, out, in);
    }

    public void answered(String calledNumber) {
        System.out.println("IAXphone, answered: " + calledNumber);
        System.out.println("Type \"hangup\" to hangup call");
        System.out.println(
                "Type \"transfer <address>\" to transfer call to address");

        callParticipant = calledNumber;
        state = CALLACTIVE;
    }

    public void exited() {
        System.out.println("IAXphone, exited");

        System.exit(0);
    }

    public void hungup(String calledNumber) {
        System.out.println("IAXphone, hungup: " + calledNumber);

        callParticipant = null;
        state = WAITING;
    }

    public void playWaitTones(String calledNumber) {
        System.out.println("IAXphone, playWaitTones: " + calledNumber);

        state = CALLING;
    }

    public void recvCall(String callingName, String callingNumber) {
        System.out.println("IAXphone, received call from: " + callingName +
                           ", " + callingNumber);
        System.out.println("Press enter to answer, type 'r' to refuse");

        callParticipant = callingNumber;
        state = RINGING;
    }

    public void registered() {
        System.out.println("IAXphone, registered");
    }

    public void unregistered() {
        System.out.println("IAXphone, unregistered");
    }

    public void waiting() {
        System.out.println("IAXphone, waiting");
    }

    public void makeCall(String calledNumber) {
        System.out.println("IAXphone, Calling " + calledNumber);

        callParticipant = calledNumber;

        UserCommandFacade.newCall(peer, calledNumber);
        state = CALLING;
    }

    public void transferCall(String calledNumber) {
        System.out.println("IAXphone, Transfering call from " + callParticipant +
                           " to " + calledNumber);

        UserCommandFacade.transferCall(peer, callParticipant, calledNumber);
    }

    public void answerCall() {
        System.out.println("IAXphone, Answered call from " + callParticipant);
        System.out.println("Type \"hangup\" to hangup call");
        System.out.println(
                "Type \"transfer <address>\" to transfer call to address");

        UserCommandFacade.answerCall(peer, callParticipant);
        state = CALLACTIVE;
    }


    public void hangup() {
        System.out.println("IAXphone, Hungup call from " + callParticipant);

        UserCommandFacade.hangupCall(peer, callParticipant);
        state = WAITING;
    }

    public void shutdown() {
        System.out.println("Shutting down...");

        UserCommandFacade.exit(peer);
    }

    public int getState() {
        return state;
    }


    /**
     * @param args
     */
    public static void main(String[] args) {

        String user = "";
        String pass = "";
        String host = "";
        boolean register = false;
        int maxCalls = 1;
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.
                in));

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-u") && args.length > (i + 1)) {
                user = args[++i];
                continue;
            }
            if (args[i].equals("-p") && args.length > (i + 1)) {
                pass = args[++i];
                continue;
            }
            if (args[i].equals("-h") && args.length > (i + 1)) {
                host = args[++i];
                continue;
            }
            if (args[i].equals("-r")) {
                register = true;
                continue;
            }
            if (args[i].equals("-m") && args.length > (i + 1)) {
                maxCalls = Integer.getInteger(args[++i]);
                continue;
            }

            // else, do:
            if (!args[i].equals("-help"))
                System.out.println("unrecognized param '" + args[i] + "'\n");

            System.out.println("usage:\n   java IaxPhone [options]");
            System.out.println("   options:");
            System.out.println("   -help           this help");
            System.out.println("   -u <username>   Registration user name.");
            System.out.println("   -p <password>   Registration password");
            System.out.println("   -h <host>       Remote host");
            System.out.println(
                    "   -r              if the peer is going to register");
            System.out.println(
                    "   -m <maxCalls>   maximun number of concurrent calls");
            System.exit(0);
        }

        System.out.println("IAXphone Starting...");
        System.out.println("Username: " + user);
        System.out.println("Host: " + host);
        System.out.println("Type \"call <address>\" to make a phone call\n");

        IaxPhone iaxPhone = new IaxPhone(user, pass, host, register, maxCalls, null, null);

        String line;
        String[] words = new String[10];
        try {

            while ((line = stdin.readLine()) != null) {
                words = line.split(" ");
                if (iaxPhone.getState() == IaxPhone.WAITING &&
                    (words[0].equals("call") || words[0].equals("c")) &&
                    words.length == 2) {
                    iaxPhone.makeCall(words[1]);
                } else if (iaxPhone.getState() == IaxPhone.CALLACTIVE &&
                           (words[0].equals("transfer") || words[0].equals("t")) &&
                           words.length == 2) {
                    iaxPhone.transferCall(words[1]);
                } else if (iaxPhone.getState() == IaxPhone.RINGING &&
                           !words[0].equals("r")) {
                    iaxPhone.answerCall();
                } else if (iaxPhone.getState() == IaxPhone.RINGING &&
                           words[0].equals("r")) {
                    iaxPhone.hangup();
                } else if (iaxPhone.getState() != IaxPhone.WAITING &&
                           (words[0].equals("hangup") || words[0].equals("h"))) {
                    iaxPhone.hangup();
                } else if (words[0].equals("exit") || words[0].equals("e")) {
                    iaxPhone.shutdown();
                    System.exit(0);
                } else if (words[0].equals("")) {
                    //Do nothing
                } else {
                    if (!words[0].equals("help"))
                        System.out.println("Command not valid!");
                    System.out.println("Available commands:");
                    System.out.println("   help                    this help");
                    System.out.println(
                            "   c | call <address>      Call address.");
                    System.out.println(
                            "   t | transfer <address>  Transfer active call to address");
                    System.out.println(
                            "   h | hangup              Hangup active call or call in progress");
                    System.out.println("   e | exit                Exit");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @todo Implement it
     * @param calledNumber String
     */
    public void stopWaitTones(String calledNumber) {
    }
}
