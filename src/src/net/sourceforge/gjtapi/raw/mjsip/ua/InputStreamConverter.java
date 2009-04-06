package net.sourceforge.gjtapi.raw.mjsip.ua;

import java.io.IOException;
import java.io.InputStream;


public class InputStreamConverter extends InputStream {

    private InputStream inputStream;
    private Object closeLock = new Object();

    public InputStreamConverter() {
    }

    public void setInputStream(InputStream in) {
        inputStream = in;
    }

    //TODO verificar o sinal!!
    public int read() throws IOException {
        int a;
        byte[] b = new byte[1];

        a = read(b, 0, 1);

        if (a == -1)
            return a;
        else
            return (int) b[0];
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int br;
        if (inputStream != null) {
            try {
                br = inputStream.read(b, off, len);
            } catch (Exception ex) {
                ex.printStackTrace();
                inputStream = null;
                return 0;
            }
            return br;
        }
        //Arrays.fill(b, off, off + len - 1, silenceSample);
        //return len;
        return 0;
    }

    public void close() throws IOException {
        if (inputStream == null)
            return;

        inputStream.close();
        inputStream = null;
        synchronized (closeLock) {
            closeLock.notifyAll();
        }
    }

    public boolean isOpen() {
        if (inputStream == null)
            return false;
        else
            return true;
    }

    public void waitForEnd() {
        synchronized (closeLock) {
            while (inputStream != null) {
                try {
                    closeLock.wait(500);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }
    }

}

