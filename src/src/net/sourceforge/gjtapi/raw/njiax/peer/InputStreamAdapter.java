package net.sourceforge.gjtapi.raw.njiax.peer;

import java.io.IOException;
import java.io.InputStream;

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
public class InputStreamAdapter extends InputStream {

    private InputStream inputStream;
    private Object closeLock = new Object();

    protected InputStreamAdapter() {
    }

    public void setInputStream(InputStream in) {
        inputStream = in;
    }

    public int read() throws IOException {
        byte[] b = new byte[1];
        int rv = read(b, 0, 1);
        if (rv != -1) {
            rv = b[0];
        }

        return rv;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (inputStream != null) {
            int a = -1;
            try {
                a = inputStream.read(b, off, len);
                //fos.write(b, off, len);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (a == -1) {
                close();
                return 0;
            } else
                return a;
        } else
            return 0;
    }

    public void close() throws IOException {
        if (inputStream == null) {
            return;
        }

        inputStream.close();
        inputStream = null;
        synchronized (closeLock) {
            closeLock.notifyAll();
        }
    }

    public boolean isOpen() {
        if (inputStream == null) {
            return false;
        } else {
            return true;
        }
    }

    public void waitForEnd() {
        synchronized (closeLock) {
            while (inputStream != null) {
                try {
                    closeLock.wait(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
