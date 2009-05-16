package net.sourceforge.gjtapi.raw.mjsip.ua;

import java.io.IOException;
import java.io.InputStream;


public class InputStreamConverter extends InputStream {

    private InputStream inputStream;
    private final Object closeLock = new Object();

    public InputStreamConverter() {
    }

    public void setInputStream(InputStream in) {
        inputStream = in;
    }

    //TODO verificar o sinal!!
    @Override
    public int read() throws IOException {
        int a;
        byte[] b = new byte[1];

        a = read(b, 0, 1);

        if (a == -1)
            return a;
        else
            return b[0];
    }

    @Override
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
            if (br < 0) {
                close();
            }
            return br;
        }
        //Arrays.fill(b, off, off + len - 1, silenceSample);
        //return len;
        return 0;
    }

    @Override
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

