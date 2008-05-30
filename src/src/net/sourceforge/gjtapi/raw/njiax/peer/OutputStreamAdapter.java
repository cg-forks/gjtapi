package net.sourceforge.gjtapi.raw.njiax.peer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: L2F | INESC-ID</p>
 *
 * @author Dário Marcelino
 * @version 1.0
 */
public class OutputStreamAdapter extends OutputStream {

    private OutputStream outputStream = null;
    private Object closeLock = new Object();
    private Semaphore accessSemaphore;


    public OutputStreamAdapter() {
        accessSemaphore = new Semaphore(1, true);
    }

    public void setOutputStream(OutputStream out) {
        outputStream = out;
    }

    public void write(int b) throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte)b;
        write(buffer, 0, 1);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        while (true) {
            try {
                accessSemaphore.acquire(1);
                break;
            } catch (InterruptedException ex1) {
                ex1.printStackTrace();
            }
        }

        if (outputStream != null) {
            try {
                outputStream.write(b, off, len);
            } catch (IOException ex) {
                outputStream = null;
            }
        }
        accessSemaphore.release(1);
    }

    public void close() throws IOException {
        while (true) {
            try {
                accessSemaphore.acquire(1);
                break;
            } catch (InterruptedException ex1) {
                ex1.printStackTrace();
            }
        }

        if (outputStream != null){

            outputStream.flush();
            outputStream.close();
            outputStream = null;
        }

        accessSemaphore.release(1);

        synchronized (closeLock) {
            closeLock.notifyAll();
        }
    }

    public boolean isOpen() {
        if (outputStream == null)
            return false;
        else
            return true;
    }

    public void waitForEnd() {
        synchronized (closeLock) {
            while (outputStream != null) {
                try {
                    closeLock.wait(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


}

