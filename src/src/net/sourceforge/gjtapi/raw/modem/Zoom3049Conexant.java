// NAME
//      $RCSfile$
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision$
// CREATED
//      $Date$
// COPYRIGHT
//      Deadman Consulting
// TO DO
//

package net.sourceforge.gjtapi.raw.modem;

import java.io.*;

import net.sourceforge.gjtapi.CallId;

/**
 * A concrete implementation of Modem for the Zoom 3049C V92 external modem
 *
 * @author <a href="mailto:rdeadman@deadman.ca">Richard Deadman</a>
 * @version $Revision$ $Date$
 */
public class Zoom3049Conexant extends AbstractModem {
    private static final String     version_id =
            "@(#)$Id$ Copyright Westhawk Ltd";

    //Temporary values for using the modem.
    //todo - read these from a props file or similar.
    private static final String INIT = "ATZE1V1";
    private static final String INIT_OK = "OK";
    private static final String INIT_ERR = "ERROR";
    private static final String DIAL = "ATD";
    private static final String DIAL_OK = "OK";
    private static final String DIAL_ERR = "BUSY";
    private static final String ANSWER = "ATA";
    private static final String ANSWER_OK = "OK";
    private static final String ANSWER_ERR = "ERROR";
    private static final String HANGUP = "ATH";
    private static final String HANGUP_OK = "OK";
    private static final String HANGUP_ERR = "ERROR";
    private static final String VOICE = "AT+FCLASS=8";
    private static final String VOICE_OK = "OK";
    private static final String VOICE_ERR = "ERROR";
    private static final String TONE_SEND = "AT+VTS=";
    private static final String TONE_SEND_OK = "OK";
    private static final String TONE_SEND_ERR = "ERROR";
    
    private CallId currId;
    private StringBuffer digitBucket;

    public Zoom3049Conexant(ModemListener prov) {
        super(prov);
        digitBucket = new StringBuffer();
    }
    
    public boolean initialize(String portname){
        boolean result = super.initialize(portname);
        //Now try to initialize the modem
        if (result) {
            //need to send the initialize string and wait for the
            //correct response
            try {
                io.writeLine(INIT);
                if (io.match(5000, INIT_OK, INIT_ERR) == ModemIO.GOOD_MATCH) {
                    state = IDLE;
                    result = true;
                }
                else {
                    state = INVALID;
                    //result = false; //default
                    System.err.println("Modem initialization failed");
                }
            }
            catch (IOException ex) {
ex.printStackTrace();
                state = INVALID;
                //result = false;
                System.err.println("Modem initialization failed");
            }
        }
        
        return result;
    }

    public void drop(CallId id){
        //this method needs some thought about how it interacts with calls
        //which are in progresss.
        state = DROPPING;
        try {
            io.writeLine(HANGUP);
            if (io.match(5000, HANGUP_OK, HANGUP_ERR) == ModemIO.GOOD_MATCH) {
                state = IDLE;
                listener.modemDisconnected(id);
                currId = null;
            }
            else {
                state = INVALID;
                System.err.println("Modem could not hangup");
            }
        }
        catch (IOException ex) {
            state = INVALID; //state possible not "BUSY" at time of hangup
            System.err.println("Modem could not hangup");
        }
    }
    
    public boolean call(CallId id, String dest){
        boolean result = false;
        
        // first note that the local leg is connected
        listener.modemConnected(id);

        try {
            //go to voice mode
            io.writeLine(VOICE);
            int matchState = io.match(5000, VOICE_OK, VOICE_ERR);
            if (matchState == ModemIO.GOOD_MATCH){
                //set "Ringback-Goes-Away Timer" to suitable value (what units?)
                io.writeLine("AT+VRA=10");
                matchState = io.match(5000, "OK", "ERROR");
            }
            
            // note that we are dialing
            listener.modemDialing(id, dest);
            
            if (matchState == ModemIO.GOOD_MATCH){
                //dial the number
                io.writeLine(DIAL + dest);
                //Need to loop until terminal is answered, line is busy
                //or call is dropped
                do{
                    matchState = io.match(20000, DIAL_OK, DIAL_ERR);
                }while (matchState == ModemIO.TIMEOUT | state == DROPPING);
            }
            if (matchState == ModemIO.GOOD_MATCH){
                state = BUSY;
                	// remote end now connected
                listener.modemConnected(id, dest);
                currId = id;
                result = true;
            } else {
                state = INVALID;
                // test if we have a busy end-point
                String resultMsg = io.getMatch();
                if (resultMsg.equals(DIAL_ERR)) {
                	listener.modemFailed(id, dest);
                	result = false;
                } else {
	                if (matchState == ModemIO.TIMEOUT){
	                    // well, we tried...
	                    System.err.println("timed out...");
	                }else {
	                	listener.modemFailed(id, dest);
	                    System.err.println("Modem could not call number: " + resultMsg);
	                }
				}
				// drop the call
				this.drop(id);
            }
        }catch(IOException ex){
            state = INVALID;
            System.err.println("Modem could not call number");
        }
        return result;
    }

    public void answer(CallId id){
        super.answer(id);
        try {
            io.writeLine(VOICE);
            int matchState = io.match(5000, VOICE_OK, VOICE_ERR);
            if (matchState == ModemIO.GOOD_MATCH){
                io.writeLine("AT+VLS=0");
                matchState = io.match(5000, "OK", "ERROR");
            }
            if (matchState == ModemIO.GOOD_MATCH){
                io.writeLine(ANSWER);
                matchState = io.match(5000, ANSWER_OK, ANSWER_ERR);
            }
            if (matchState == ModemIO.GOOD_MATCH){
                listener.modemConnected(id);
                state = BUSY;
                currId = id;
            }else{
                state = INVALID;
                System.err.println("Modem could not answer call");
            }
        }catch(IOException ex){
            state = INVALID;
            System.err.println("Modem could not answer call");
        }
    }
    
    public String reportDTMF(int num){
        String result;
        synchronized (digitBucket) {
            int len = digitBucket.length();
            num = (num>len)?len:num;
            result = digitBucket.substring(0, num);
            digitBucket.delete(0, num);
        }
        return result;
    }

    public void sendDTMF(String tones){
        io.writeLine(TONE_SEND + tones);
        try{
            while (io.match(5000, TONE_SEND_OK, TONE_SEND_ERR) == ModemIO.TIMEOUT){
                //nop
            }
        }catch(IOException ex){}
    }

    public void play(InputStream is){
        try {
//io.writeLine("AT+VSM=?");
            io.writeLine("AT+VSM: 130,8000,0,0");
            int matchState = io.match(1000, "OK", "ERROR");
            if (matchState == ModemIO.GOOD_MATCH){
                io.writeLine("AT+VTX");
                matchState = io.match(1000, "CONNECT", "ERROR");
            }
            if (matchState == ModemIO.GOOD_MATCH){
                int val;
System.out.println("Sending file...");
                while ((val = is.read()) != -1){
                    io.write(val);
                }
                //Tell the modem that we have finished transmitting
                io.write(ModemIO.DLE);
                io.write(ModemIO.ETX);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void record(OutputStream os){
        try {
            io.writeLine("AT+VSM=131,8000");
            int matchState = io.match(1000, "OK", "ERROR");
            if (matchState == ModemIO.GOOD_MATCH){
                io.writeLine("AT+VRX");
                matchState = io.match(1000, "CONNECT", "ERROR");
            }
            if (matchState == ModemIO.GOOD_MATCH){
                int val;
                while ((val = io.read()) != -1){
                    os.write(val);
                    //the modem can be brought out of record by sending <DLE>!
                    //the modem will send us <DLE>s or <DLE>q after detecting
                    //silence for longer than the silence detection timer
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void dleReceived(char shielded){
        if ((shielded >= '0' && shielded <= '9')
            || shielded == '*' || shielded == '#'){
             synchronized (digitBucket){
System.out.println("Placing character in digitbucket: " + shielded);
                 digitBucket.append(shielded);
             }
        }else{
            switch (shielded) {
                case 'd':
                case 's':
                case 'q':
                    drop(currId);
                    break;
            }
        }
    }
}