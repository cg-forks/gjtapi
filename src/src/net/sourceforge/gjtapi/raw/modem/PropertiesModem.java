package net.sourceforge.gjtapi.raw.modem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import net.sourceforge.gjtapi.CallId;

/**
 * A concrete implementation of Modem used with properties files. This is based on
 * the AccuraV92.java modem config.
 *
 * This implementation is not tested. Still working on it.
 * Usage:
 * 
 * Replace 2 lines in Modem.props
 * ModemClass=net.sourceforge.gjtapi.raw.modem.PropertiesModem
 * properties.modem.config=DefaultModem.props
 * 
 * 
 * @author Henry Voyer
 * @version $Revision$ $Date$
 */
public class PropertiesModem extends AbstractModem implements
        PropertiesModemValues {

     private static final String version_id = "@(#)$Id: PropertyModem 1.2 2004/12/18 by Henry Voyer";

    private CallId currId;

    private StringBuffer digitBucket;

    Properties currentConfig;

    public PropertiesModem(ModemListener listener) throws Exception {

        super(listener);

        digitBucket = new StringBuffer();

        Properties configProperties = loadResources(MODEM_RESOURCE_NAME);

        String modemConfigFile = configProperties
                .getProperty(PROPERTIES_MODEM_CONFIG);

        currentConfig = loadResources(PROPERTIES_FOLDER + modemConfigFile);

    }

    public boolean initialize(String portname) {


        boolean result = super.initialize(portname);
        //Now try to initialize the modem
        if (result) {
            //need to send the initialize string and wait for the
            //correct response
            try {
                io.writeLine(currentConfig.getProperty(INIT));
                if (io.match(5000, currentConfig.getProperty(INIT_OK),
                        currentConfig.getProperty(INIT_ERR)) == ModemIO.GOOD_MATCH) {
                    state = IDLE;
                    result = true;
                } else {
                    state = INVALID;
                    //result = false; //default
                }
            } catch (IOException ex) {
                state = INVALID;
                //result = false;
            }
        }
        return result;
    }

    public void drop(CallId id) {
        //this method needs some thought about how it interacts with calls
        //which are in progresss.
        state = DROPPING;
        try {
            io.writeLine(currentConfig.getProperty(HANGUP));
            if (io.match(5000, currentConfig.getProperty(HANGUP_OK),
                    currentConfig.getProperty(HANGUP_ERR)) == ModemIO.GOOD_MATCH) {
                state = IDLE;
                listener.modemDisconnected(id);
                currId = null;
            } else {
                state = INVALID;
            }
        } catch (IOException ex) {
            state = INVALID; //state possible not "BUSY" at time of hangup
        }
    }

    public boolean call(CallId id, String dest) {
        boolean result = false;

        // first note that the local leg is connected
        listener.modemConnected(id);

        try {
            //go to voice mode
            io.writeLine(currentConfig.getProperty(VOICE));
            int matchState = io.match(5000,
                    currentConfig.getProperty(VOICE_OK), currentConfig
                            .getProperty(VOICE_ERR));
            if (matchState == ModemIO.GOOD_MATCH) {
                //set "Ringback-Goes-Away Timer" to suitable value (what
                // units?)
                io.writeLine(currentConfig.getProperty(AWAY_TONE));
                matchState = io.match(5000, OK, ERROR);
            }

            // note that we are dialing
            listener.modemDialing(id, dest);

            if (matchState == ModemIO.GOOD_MATCH) {
                //dial the number
                io.writeLine(currentConfig.getProperty(DIAL) + dest);
                //Need to loop until terminal is answered, line is busy
                //or call is dropped
                do {
                    matchState = io.match(1000, currentConfig
                            .getProperty(DIAL_OK), currentConfig
                            .getProperty(DIAL_ERR));
                } while (matchState == ModemIO.TIMEOUT | state == DROPPING);
            }
            if (matchState == ModemIO.GOOD_MATCH) {
                state = BUSY;
                // remote end now connected
                listener.modemConnected(id, dest);
                currId = id;
                result = true;
            } else {
                state = INVALID;
                if (matchState == ModemIO.TIMEOUT) {
                } else {
                }
            }
        } catch (IOException ex) {
            state = INVALID;
        }
        return result;
    }

    public void answer(CallId id) {
        super.answer(id);
        try {
            io.writeLine(currentConfig.getProperty(VOICE));
            int matchState = io.match(5000,
                    currentConfig.getProperty(VOICE_OK), currentConfig
                            .getProperty(VOICE_ERR));
            if (matchState == ModemIO.GOOD_MATCH) {
                io.writeLine(AT_VALIDATEMATCH);
                matchState = io.match(5000, OK, ERROR);
            }
            if (matchState == ModemIO.GOOD_MATCH) {
                io.writeLine(currentConfig.getProperty(ANSWER));
                matchState = io.match(5000, currentConfig
                        .getProperty(ANSWER_OK), currentConfig
                        .getProperty(ANSWER_ERR));
            }
            if (matchState == ModemIO.GOOD_MATCH) {
                listener.modemConnected(id);
                state = BUSY;
                currId = id;
            } else {
                state = INVALID;
            }
        } catch (IOException ex) {
            state = INVALID;
        }
    }

    public String reportDTMF(int num) {
        String result = "";
        try {
            synchronized (digitBucket) {
                int len = digitBucket.length();
                num = (num > len) ? len : num;
                result = digitBucket.substring(0, num);
                digitBucket.delete(0, num);
            }
        } catch (Exception ex) {
        }
        return result;
    }

    public void sendDTMF(String tones) {
        io.writeLine(currentConfig.getProperty(TONE_SEND) + tones);
        try {
            while (io.match(5000, currentConfig.getProperty(TONE_SEND_OK),
                    currentConfig.getProperty(TONE_SEND_ERR)) == ModemIO.TIMEOUT) {
                //nop
            }
        } catch (IOException ex) {
        }
    }

    public void play(InputStream is) {
        try {
            io.writeLine(currentConfig.getProperty(AT_PLAY_STEP_1));
            int matchState = io.match(1000, OK, ERROR);
            if (matchState == ModemIO.GOOD_MATCH) {
                io.writeLine(currentConfig.getProperty(AT_PLAY_STEP_2));
                matchState = io.match(1000, "CONNECT", ERROR);
            }
            if (matchState == ModemIO.GOOD_MATCH) {
                int val;
                while ((val = is.read()) != -1) {
                    io.write(val);
                }
                //Tell the modem that we have finished transmitting
                io.write(ModemIO.DLE);
                io.write(ModemIO.ETX);
            }
        } catch (Exception ex) {
        }
    }

    public void record(OutputStream os) {
        try {
            io.writeLine(currentConfig.getProperty(AT_RECORD_START_STEP_1));
            int matchState = io.match(1000, OK, ERROR);
            if (matchState == ModemIO.GOOD_MATCH) {
                io.writeLine(currentConfig.getProperty(AT_RECORD_START_STEP_2));
                matchState = io.match(1000, "CONNECT", ERROR);
            }
            if (matchState == ModemIO.GOOD_MATCH) {
                int val;
                while ((val = io.read()) != -1) {
                    os.write(val);
                    //the modem can be brought out of record by sending <DLE>!
                    //the modem will send us <DLE>s or <DLE>q after detecting
                    //silence for longer than the silence detection timer
                }
            }
        } catch (Exception ex) {
        }
    }

    public void dleReceived(char shielded) {
        try {
            if ((shielded >= '0' && shielded <= '9') || shielded == '*'
                    || shielded == '#') {
                synchronized (digitBucket) {
                    digitBucket.append(shielded);
                }
            } else {
                switch (shielded) {
                case 'd':
                case 's':
                case 'q':
                    drop(currId);
                    break;
                }
            }
        } catch (Exception ex) {
        }

    }

    private Properties loadResources(String propertyFile) throws Exception {
        Properties property = new Properties();
        property.load(this.getClass().getResourceAsStream("/" + propertyFile));

        return property;
    }

}