package net.sourceforge.gjtapi.raw.mjsip.ua;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;



public class OutputStreamConverter extends OutputStream {

    private OutputStream outputStream = null;
 /*   private AudioInputStream convertedStream;
    private PipedInputStream pis;
    private PipedOutputStream pos;
    //private boolean closed = false;
    private int frameSize = 1;*/


    public OutputStreamConverter() {
        outputStream = null;
    }

    public OutputStreamConverter(AudioFormat from, AudioFormat to) {
        //Create and connect conversion pipes
      /*  pis = new PipedInputStream((int)(to.getChannels() * to.getSampleSizeInBits() / 8 * to.getSampleRate()));
        try {
            pos = new PipedOutputStream(pis);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        convertedStream = AudioSystem.getAudioInputStream(to,
                new AudioInputStream(pis, from, -1));
        frameSize = convertedStream.getFormat().getFrameSize();*/

      outputStream = null;
    }

    public OutputStreamConverter(OutputStream out, AudioFormat from, AudioFormat to) {
        this(from, to);
        outputStream = out;
    }

    public void setOutputStream(OutputStream out) {
       outputStream = out;
   }


    @Override
    public void write(int b) throws IOException {
        if (outputStream == null)
            return;

        byte[] buffer = new byte[1];
        buffer[0] = (byte) b;
        write(buffer);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        if (outputStream == null)
            return;

    /*    pos.write(b, offset, length);
        int available = convertedStream.available();
        byte[] convBuffer = new byte[Math.min(available, b.length * frameSize)];
        int br = convertedStream.read(convBuffer);
        if (br != -1) {
            outputStream.write(convBuffer, 0, br);
        }*/

       try {
           outputStream.write(b, offset, length);
       } catch (IOException ex) {
           outputStream = null;
       }
    }

    @Override
    public void flush() throws IOException {
    /*    int available = convertedStream.available();
        if (available < 1)
            return;
        int br;
        byte[] buffer = new byte[available];
        br = convertedStream.read(buffer);
        if (br != -1 && outputStream != null)
            outputStream.write(buffer, 0, br);*/
    }

    @Override
    public void close() throws IOException {
        if (outputStream == null)
            return;

        try {
            flush();
        } catch (IOException ignored) {
        }

        //pis.close();
        //pis.close();
        //convertedStream.close();
        outputStream.flush();
        outputStream.close();
        outputStream = null;
    }

    public boolean isOpen(){
        if (outputStream == null)
            return false;
        else
            return true;
    }

}

