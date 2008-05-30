package net.sourceforge.gjtapi.raw.mjsip.ua;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamConverter extends OutputStream {

    private OutputStream outputStream = null;

    public OutputStreamConverter() {
        outputStream = null;
    }

    public OutputStreamConverter(OutputStream out) {
        outputStream = out;
    }

    public void setOutputStream(OutputStream out) {
        outputStream = out;
    }

    public void write(int b) throws IOException {
        if (outputStream == null)
            return;

        byte[] buffer = new byte[1];
        buffer[0] = (byte) b;
        write(buffer);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int offset, int length) throws IOException {
        if (outputStream == null)
            return;

        try {
            outputStream.write(b, offset, length);
        } catch (IOException ex) {
            outputStream = null;
        }
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
        if (outputStream == null)
            return;

        try {
            flush();
        } catch (IOException ignored) {
        }

        outputStream.flush();
        outputStream.close();
        outputStream = null;
    }

    public boolean isOpen() {
        if (outputStream == null)
            return false;
        else
            return true;
    }

}

