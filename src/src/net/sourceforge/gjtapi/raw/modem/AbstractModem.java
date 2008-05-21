package net.sourceforge.gjtapi.raw.modem;
// NAME
//      $RCSfile$
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision$
// CREATED
//      $Date$
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

import java.io.IOException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

import net.sourceforge.gjtapi.CallId;

/**
 * An abstract implementation of Modem. This should be extended to support
 * specific modems.
 * 
 * @author <a href="mailto:ray@westhawk.co.uk">Ray Tran</a>
 * @version $Revision$ $Date$
 */
public abstract class AbstractModem implements Modem{

    protected int state;
    protected ModemIO io;
    protected ModemListener listener;

    private CallId ringId;
    private Timer watchdog;

    public AbstractModem(ModemListener prov) {
        state = INVALID;
        listener = prov;
    }

    public boolean initialize(String portname){
        //Try to open a port to the modem
        io = new ModemIO(portname, this);
        
        watchdog = new Timer(5500, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ringingStopped();
            }
        });
        watchdog.setRepeats(false);


        return (io != null);
    }

    public void shutdown() {
        state = INVALID;
        try {
            io.close();
            //disconnects provider..
            System.out.println("Disconnected Provider");
        }
        catch (IOException ex) {
            System.err.println("IOException in Modem.shutdown()");
        }
    }
    
    /**
     * Incomplete implementation which just does enough to allow ringing() &
     * ringingStopped() to work correctly. Subclasses must call this method
     * in their answer(CallId) implementation.
     * 
     * @param id - the CallId of the call to answer.
     */
    public void answer(CallId id){
        ringId = null;
    }

    public int getState(){
        return state;
    }
    
    /**
     * The phone line connected to the modem is ringing
     */
    public void ringing(){
        watchdog.restart();
        if (state == IDLE){
            state = RINGING;
            ringId = listener.modemRinging();
        }
    }
    
    /**
     * The phone line connected to the modem has stopped ringing.
     * n.b. Ringing may have stopped because the call has been answered or
     * the caller has hung up.
     */
    private void ringingStopped(){
        //Only do something if the caller has hung up
        if (state == RINGING){
            state = IDLE;
            if (ringId != null){
                listener.ringingStopped(ringId);
            }
            //We don't need a ref to the ringId any more
            ringId = null;
        }
    }
}