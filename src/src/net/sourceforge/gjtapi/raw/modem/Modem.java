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

package net.sourceforge.gjtapi.raw.modem;

import java.io.InputStream;
import java.io.OutputStream;

import net.sourceforge.gjtapi.CallId;

/**
 * An interface which defines the action which a modem should perform.
 *
 * @author <a href="mailto:ray@westhawk.co.uk">Ray Tran</a>
 * @version $Revision$ $Date$
 */
public interface Modem {
    static final String     version_id =
            "@(#)$Id$ Copyright Westhawk Ltd";

    public static final int INVALID = 0;
    public static final int IDLE = 1;
    public static final int BUSY = 2;
    public static final int RINGING = 3;
    public static final int DROPPING = 4;
    
    /**
     * Initialize the modem.
     * 
     * @param portname - The name of the port which is attached to the modem.
     * @return boolean - whether the initialization succeeded of not.
     */
    public boolean initialize(String portname);
    
    /**
     * Drop the current call.
     * 
     * @param id - CallId of the current call.
     */
    public void drop(CallId id);
    
    /**
     * Place a call to the specified destination.
     * 
     * @param id - A unique CallId to identify this call.
     * @param dest - The phone number to dial.
     * @return boolean - whether the call was placed successfully
     */
    public boolean call(CallId id, String dest);
    
    /**
     * Answer an incoming call.
     * 
     * @param id -  unique CallId to identify this call.
     */
    public void answer(CallId id);
    
    /**
     * Shutdown the modem and return any resources.
     */
    public void shutdown();
    
    /**
     * Return the current state of the modem.
     * 
     * @return int - the current state of the modem.
     */
    public int getState();
    
    /**
     * Return the DTMF digits which have been detected.
     * 
     * @param num - the maximum number of tones to return.
     * @return String - The digits (0-9, *, #) received.
     */
    public String reportDTMF(int num);
    
    /**
     * Send some DTMF digits out.
     * 
     * @param tones - The digits (0-9, *, #) to be sent.
     */
    public void sendDTMF(String tones);
    
    /**
     * Output sounds over the modem.
     * 
     * @param is - An InputStream which streams the sounds.
     */
    public void play(InputStream is);
    
    /**
     * Receive sounds over the modem.
     * 
     * @param os - An OutputStream which the sounds are streamed to.
     */
    public void record(OutputStream os);
    
    /**
     * Deal with the DLE shielded character which has been received
     * 
     * @param shielded - The character which must be handled.
     */
    public void dleReceived(char shielded);
    
    /**
     * The modem is ringing.
     */
     public void ringing();
}