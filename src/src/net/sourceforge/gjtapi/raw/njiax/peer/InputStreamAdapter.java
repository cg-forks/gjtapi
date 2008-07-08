package net.sourceforge.gjtapi.raw.njiax.peer;

import java.io.IOException;
import java.io.InputStream;
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
public class InputStreamAdapter extends InputStream {

    private InputStream inputStream;
    private Object closeLock = new Object();
    private Semaphore acessSemaphore = new Semaphore(1);

    protected InputStreamAdapter() {
    }

    public synchronized void setInputStream(InputStream in) {
        inputStream = in;
    }


    public int available() throws IOException {
        while (true) {
            try {
                acessSemaphore.acquire();
                break;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        int rv = 0;
        if (inputStream != null) {
            rv = inputStream.available();
        }

        acessSemaphore.release();

        return rv;
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
        while (true) {
            try {
                acessSemaphore.acquire();
                break;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        int rv = 0;
        if (inputStream != null) {
            int a = -1;
            try {
                //long startTime = System.currentTimeMillis();
                a = inputStream.read(b, off, len);
                //long endTime = System.currentTimeMillis();
               // System.out.println("NjIAX InStreamAdapter read time: " + (endTime - startTime));
                //fos.write(b, off, len);

              /*  try {
                    Thread.sleep(19);
                } catch (InterruptedException ex1) {
                    ex1.printStackTrace();
                }*/

                if (a != -1) {
                    if ((len - off) != a) {
                        System.err.println("InputStreamAdapter read less bytes ("+a+") than expected "+(len-off));
                       // return read(b, off + a -1, len - a - (off - 1));
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            acessSemaphore.release();

            if (a == -1) {
                close();
                rv = 0;
            }
            else {
                rv = a;
            }
        }
        else {
            acessSemaphore.release();
        }

        return rv;
    }

    public void close() throws IOException {
        if (inputStream == null) {
            return;
        }

        while (true) {
            try {
                acessSemaphore.acquire();
                break;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        inputStream.close();
        inputStream = null;

        acessSemaphore.release();

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
                    closeLock.wait(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
