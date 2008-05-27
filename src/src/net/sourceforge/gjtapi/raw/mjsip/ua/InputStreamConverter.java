package net.sourceforge.gjtapi.raw.mjsip.ua;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.*;
//import java.io.FileOutputStream;
//import java.io.File;
import java.io.*;
//import java.util.Arrays;

public class InputStreamConverter extends InputStream {

//    private AudioInputStream convertedStream;
    private InputStream inputStream;
/*    private AudioFormat fromFormat;
    private AudioFormat toFormat;*/
    private Object closeLock = new Object();
    //private byte silenceSample;


    //private FileOutputStream fos = null;

    public InputStreamConverter() {
        /*try {
            fos = new FileOutputStream(File.createTempFile("mjsip_sent", ".raw",
                    new File(".")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }


    public InputStreamConverter(AudioFormat from, AudioFormat to) {
      /*  fromFormat = from;
        toFormat = to;*/



       /* if ((to.getEncoding() == AudioFormat.Encoding.ALAW) ||
            (to.getEncoding() == AudioFormat.Encoding.ULAW) ||
            (to.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)) {
            silenceSample = 0;
        }
        else {
            silenceSample = (byte) (Math.pow(2, to.getSampleSizeInBits()) / 2);
        }*/
    }

 /*   public InputStreamConverter(InputStream in, AudioFormat from,
                                AudioFormat to) {
        inputStream = in;
        fromFormat = from;
        toFormat = to;
        if (fromFormat == to) {
            convertedStream = new AudioInputStream(inputStream, from, AudioSystem.NOT_SPECIFIED);
        }
        else {
            try {
                convertedStream = AudioSystem.getAudioInputStream(toFormat,
                        new AudioInputStream(inputStream, fromFormat, -1));
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (convertedStream == null) {
                    throw new RuntimeException("null converted stream");
                }
            }
        }
    }*/


    public void setInputStream(InputStream in) {
        inputStream = in;
    /*    if (fromFormat == toFormat) {
            convertedStream = new AudioInputStream(inputStream, fromFormat, AudioSystem.NOT_SPECIFIED);;
        }
        else {
            try {
                convertedStream = AudioSystem.getAudioInputStream(toFormat,
                        new AudioInputStream(inputStream, fromFormat, -1));
            } catch (Exception ex) {
                try {
                    AudioFormat af = new AudioFormat(8000, 16, 1, true, false);
                    convertedStream = AudioSystem.getAudioInputStream(af,
                            new AudioInputStream(in, fromFormat,
                                                 AudioSystem.NOT_SPECIFIED));
                    convertedStream = AudioSystem.getAudioInputStream(toFormat,
                            convertedStream);
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            } finally {
                if (convertedStream == null) {
                    throw new RuntimeException("null converted stream");
                }
            }
        }*/
    }

    @Override
    //TODO verificar o sinal!!
    public int read() throws IOException {
        int a;
        byte[] b = new byte[1];

        a = read(b, 0, 1);

        if (a == -1)
            return a;
        else
            return (int)b[0];
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int br;
        if (inputStream != null) {
            /*try {
                if (inputStream.available() == 0) {
                    return 0;
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                return 0;
            } catch (IOException ex) {
                ex.printStackTrace();
                inputStream = null;
                return 0;
            }*/
            try {
                br = inputStream.read(b, off, len);
                //if (br != -1) fos.write(b, off, len);
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

    @Override
    public void close() throws IOException {
        if (inputStream == null)
            return;

        inputStream.close();
        inputStream = null;
        synchronized (closeLock) {
            closeLock.notifyAll();
        }
    }

    public boolean isOpen(){
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
                    ex.printStackTrace();
                }
            }
        }
        //System.err.println("MJSIP @ play inputstream waitForEnd.... ended");
    }

}
