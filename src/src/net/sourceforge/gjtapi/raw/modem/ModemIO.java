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

import java.io.*;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.comm.*;

/**
 * A class which handles all of the IO for a Modem.
 *
 * This class works with modems connected to serial ports. Perhaps it should
 * be an interface and implementation.
 *
 * @author <a href="mailto:ray@westhawk.co.uk">Ray Tran</a>
 * @version $Revision$ $Date$
 */
public class ModemIO implements SerialPortEventListener{
    private static final String     version_id =
        "@(#)$Id$ Copyright Westhawk Ltd";

    public static final int DLE = 0x10;
    public static final int ETX = 0x03;
    public static final int CR = 0x0c;
    public static final int LF = 0x0a;
    private static final int BUF_SIZE = 65536;
    private static final int MARK_INVALID = -1;
    public static final int TIMEOUT = -1;
    public static final int GOOD_MATCH = 0;
    public static final int BAD_MATCH = 1;

    private SerialPort port = null;
    private InputStream in;
    private PrintStream out;
    private byte[] buf;
    private int writePos, readPos;
    private int markPos = MARK_INVALID;
    private int limit = MARK_INVALID;
    private int limitCount;
    private String lastMatch ="";
    private ShieldHandler handler;
    private Modem modem;

    /**
     * Constructor.
     *
     * @param portname - Name of SerialPort used for talking to the modem.
     * @param modem - Reference back to the modem which we are the IO for
     */
    public ModemIO(String portname, Modem modem) {
        buf = new byte[BUF_SIZE];
        writePos = readPos = 0;
        this.modem = modem;
        handler = new ShieldHandler();
        Thread t = new Thread(handler);
        t.start();

        //First try to open a port to the modem
        try {
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portname);
            port = (SerialPort) portID.open("ModemProvider", 5000);
            port.setSerialPortParams(
                115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
            );

            //add ourself as a listener for certain events
            port.notifyOnRingIndicator(true);
            /*
            port.notifyOnDataAvailable(true);
            port.notifyOnBreakInterrupt(false);
            port.notifyOnCarrierDetect(false);
            port.notifyOnCTS(false);
            port.notifyOnDSR(false);
            port.notifyOnFramingError(false);
            port.notifyOnOutputEmpty(false);
            port.notifyOnOverrunError(false);
            port.notifyOnParityError(false);
            */
            port.addEventListener(this);
        }catch (TooManyListenersException ex) {
            System.err.println("Adding event listener to port (" +
                               portname + ") failed");
        }catch (Exception ex) {
            System.err.println("Can't open serial port (" + portname);
            ex.printStackTrace();
        }

        if (port != null){
            try{
                port.enableReceiveTimeout(1000);
            }catch (UnsupportedCommOperationException ex){
                System.err.println("Can't set timeout on port(" + portname + ")");
            }
            try{
                in = port.getInputStream();
                out = new PrintStream(port.getOutputStream());
            }catch (IOException ex){
                System.err.println("IOException in ModemIO(" + port.getName() + ")");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Try to read bytes into the circular buffer until the buffer is full, or
     * no more bytes are available from the underlying device.
     * If there is space in the buffer then we always try to read
     * at least one byte, the read will timeout if nothing is available;
     *
     * @throws IOException
     */
    private void fill() throws IOException{
        //Don't bother if the buffer is nearly full
        if (this.free() > 32){
            do{
                int read = in.read();
                if (read > -1){
                    //If we get a <DLE> we need to deal with it
                    if (read == DLE){
                        read = in.read();
                        //If the next char is <DLE> pass it thru otherwise
                        //handle it (quickly)
                        if (read != DLE){
                            if (read == -1){
                                //this really should NEVER happen!
                                break;
                            }
                            handleDLEShield((char)read);
                        }
                    }
                    buf[writePos++] = (byte)read;
                    if (writePos >= BUF_SIZE){
                        writePos = 0;
                    }
                }
            }while (in.available() > 0 && this.free() > 0);
        }
    }

    /**
     * Deals with &lt;DLE&gt; shielded characters.
     * As a character may require time consuming processing a seperate
     * thread does it to avoid slowing down the fill() loop.
     *
     * @param shielded -  character read after the &lt;DLE&gt; character
     */
     private void handleDLEShield(char shielded){
         //System.err.println("<DLE>0x" + Integer.toHexString(shielded));
         //Expect <DLE>s when the modem detects silence, <DLE>d when dialtone
         handler.setShield(shielded);
     }

    /**
     * Returns the number of bytes available to be read.
     * 
     * @return int - the number of bytes available to be read.
     * @see #free()
     */
    public int available(){
        int result;
        if (readPos <= writePos){
            result = writePos - readPos;
        }else{
            result = BUF_SIZE - readPos + writePos;
        }
        return result;
    }

    /**
     * Returns the number of spaces available to be written in.
     * 
     * @return int - the number of spaces available to be written in.
     * @see #available()
     */
    private int free(){
        int result;
        int comparePos = ((markPos == MARK_INVALID)?readPos:markPos);

        if (comparePos <= writePos){
            result = BUF_SIZE - writePos + comparePos - 1;
        }else{
            result = comparePos - writePos - 1;
        }
        return result;
    }

    /**
     * Read the next byte from the buffer, re-filling if required.
     * 
     * @return int - the next byte read from the buffer
     * @throws IOException
     */
    public int read() throws IOException{
        int result = -1;
        if (in != null && buf != null){
            //if the buffer is nearly empty try to fill it
            if (this.available() < 16){
                fill();
            }
            if (readPos != writePos){
                result = buf[readPos++] & 0xff;
                //If we have read more bytes than readlimit since marking then
                //clear the mark;
                if (markPos != MARK_INVALID){
                    if (--limitCount == -1){
                        markPos = MARK_INVALID;
                    }
                }
                if (readPos >= BUF_SIZE){
                    readPos = 0;
                }
            }
        }
        return result;
    }

    public int read(byte[] data) throws IOException{
        int result = 0;
        int read;
        for (int i=0, len = data.length; i<len; i++){
             if ((read = read()) > -1){
                data[i] = (byte)read;
             }else{
                result = i;
                break;
             }
        }
        return result;
    }

    public int read(byte[] data, int offs, int len) throws IOException{
        int result = 0;
        int read;
        int limit = offs + len;
        if (limit <= data.length){
            for (int i=offs; i<len; i++){
                 if ((read = read()) > -1){
                    data[i] = (byte)read;
                 }else{
                    result = i - offs;
                    break;
                 }
            }
        }
        return result;
    }

    /**
     * Read a line from the underlying input stream (via the buffer).
     * A line is delimited by a '\r' character.
     * 
     * @return String - An entire line from the input stream.
     * @throws IOException
     */
    public String readLine() throws IOException{
        StringBuffer strBuf = new StringBuffer();
        int read;
        while ((read=read()) != -1 && read != '\r'){
            strBuf.append((char)read);
        }
        return strBuf.toString();
    }

    /**
     * Mark the current position in the buffer. No more than half of
     * the total buffer can be remembered; if readlimit is more than this
     * it is silently reduced.
     *
     * @param readlimit - The maximum number of bytes which can be read without losing the mark
     * @see #match()
     */
    public void mark(int readlimit){
        markPos = readPos;
        limit = limitCount = (Math.min(readlimit, BUF_SIZE/2));
    }

    /**
     * @see #mark()
     * @see #match()
     *
     * @return false because although mark (etc.) is implemented, several
     * private methods may move the mark. In other words use mark() with caution.
     */
    public boolean markSupported(){
        return false;
    }

    public void reset() throws IOException{
        if (markPos != MARK_INVALID){
            readPos = markPos;
            limitCount = limit;
        }
    }

    /**
     * Try to skip n bytes of data. Amount skipped is limited by
     * available data, but we will attempt to fill the buffer if
     * neccesary.
     *
     * @todo This hasn't been tested, need to check skip works whether wrapping
     *       or not. Also need to check mark is dealt with correctly.
     * @param n - the number of bytes to try to skip over.
     * @return long - the number of bytes actually skipped
     * @throws IOException
     */
    public long skip(long n) throws IOException{
        if (available() < n){
            fill();
        }

        n = Math.min(n, available()); //side effect: n must now be in int range!
        if (readPos + n < BUF_SIZE){
            readPos += n;
        }else{
            readPos = (int) n - (BUF_SIZE - readPos);
        }

        if (markPos != MARK_INVALID){
            if ((limitCount -= n) < 0){
                markPos = MARK_INVALID;
            }
        }

        return n;
    }

    /**
     * Close down and tidy up.
     * 
     * @throws IOException
     */
    public void close() throws IOException{
        handler.stop();
        in.close();
        out.close();
        port.close();
        in = null;
        out = null;
        port = null;
        buf = null;
    }

    /**
     * Send a line of text to the modem. A return charcter ('\r') is
     * appended to the line.
     * 
     * @param data - The data to be written to the modem.
     */
    public void writeLine(String data){
        out.print(data);
        out.print('\r');
        out.flush();
    }

    public void write(byte[] wrtBuf){
        write(wrtBuf, 0, wrtBuf.length);
    }

    public void write(byte[] wrtBuf, int off, int len){
        out.write(wrtBuf, off, len);
    }

    public void write(int b){
        out.write(b);
    }

    /**
     * Try to match the String goodMatch in the input buffer.
     *
     * The read position in the input buffer is moved to the first character
     * after the matched string.
     *
     * @param timeout - how long we can look for a match (in milliseconds)
     * @param goodMatch - the definition of success. May be a regular expression
     * @param badMatch - the definition of failure. May be a regular expression
     * @return -  TIMEOUT, GOOD_MATCH or BAD_MATCH as appropriate. Use getMatch()
     *            to get the String which caused either of the matches.
     * @throws IOException
     * @see #getMatch()
     */
    public int match(int timeout, String goodMatch, String badMatch)
        throws IOException{
        int result = TIMEOUT;
        long startTime = System.currentTimeMillis();

        Pattern p = Pattern.compile(goodMatch + '|' + badMatch);
        Matcher m = p.matcher("");

        //Loop until we match one of the Strings or we timeout
        boolean matched = false;
        while(matched == false){
            m.reset(readLine());
            matched = m.find();

            //Throw a TimeoutException if we have taken too long
            if (matched == false && (System.currentTimeMillis() - startTime > timeout)){
                //result = TIMEOUT; //default
                break;
            }
        }

        if (matched){
            lastMatch = m.group(0);
            p = Pattern.compile(goodMatch);
            m = p.matcher(lastMatch);
            if (m.find()){
                result = GOOD_MATCH;
            }else{
                result = BAD_MATCH;
            }
        }else{
            lastMatch = "";
        }

        return result;
    }

    /**
     * Returns the last string that was matched by the match(int, String, String) method.
     *
     * @return - String that was matched by match(int, String, String) if no match
     *            then empty string is returned.
     *
     * @see #match()
     */
    public String getMatch(){
        return lastMatch;
    }

    //SerialPortEventListener implementation
    public void serialEvent(SerialPortEvent evt){
        switch (evt.getEventType()) {
            case SerialPortEvent.RI:
                modem.ringing();
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                System.err.println("Data available");
                break;
            default:
                System.err.println("other event: " + evt.getEventType());
        }
    }

///////////////////////////////////////////////////////////////////////////////
    /**
     * A Runnable to deal with &lt;DLE&gt; shielded characters. the run() method
     * waits on <code>lock</code>, calling either stop() or setShield(char) will
     * call notifyAll() on <code>lock</code> which will wake up the run thread
     * to deal with the cause.
     * 
     * This may be an architectural mistake - maybe events would have worked
     * better?
     */
    private class ShieldHandler implements Runnable{
        private Vector shields = new Vector();
        private boolean stop = false;
        private final Object lock = new Object();

        public void run(){
            do{
                synchronized(lock){
                    try{
                        lock.wait();
                    }catch(InterruptedException ex){}
                }
                if ((stop == false) && (shields.size() > 0)){
                    char shield = ((Character)shields.remove(0)).charValue();
                    modem.dleReceived(shield);
                }
            }while (stop == false);
        }

        public void stop(){
            stop = true;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        public void setShield(char shield){
            shields.add(new Character(shield));
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
}