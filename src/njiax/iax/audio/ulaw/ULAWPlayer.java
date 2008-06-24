package iax.audio.ulaw;

import iax.audio.Player;
import iax.audio.PlayerException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Audio Player.
 *
 */
public class ULAWPlayer extends Player {
    /**
     * Implements Jitter BUFFER
     */

  //  public final static int JITTER_BUFFER = 0;

    AudioFormat ulawFormat;

    boolean first;

    //Testing Purposes
    //FileOutputStream  fos;

    OutputStream outStream;


    /**
     * Constructor. Initializes the player.
     * @throws PlayerException
     */

    public ULAWPlayer(int bufferType) throws PlayerException {
        super(bufferType);
        ulawFormat = new AudioFormat(AudioFormat.Encoding.ULAW, 8000.0F, 8, 1,
                                     1, 8000.0F, false);

        //playThread = new Thread(new PlayThread());

        //Testing Purposes
        /*try {
         fos = new FileOutputStream("njiaxExt/in.raw");
           } catch (FileNotFoundException e) {
         e.printStackTrace();
           }*/
    }


    public ULAWPlayer(OutputStream outStream) throws PlayerException {
        this(JITTER_BUFFER);
        if (outStream == null)
            throw new PlayerException("OutputStream null");
        this.outStream = outStream;
    }

    /**
     * Starts playing.
     */

    public void play() {
        //playThread.start();
    }

    /**
     * Stops playing.
     */

    public void stop() {
        //playThread.interrupt();
    }

    /**
     * Writes audio data in player audio buffer.
     * @param timestamp Timestamp of the received audio package.
     * @param data Audio data.
     * @param absolute if the timestamp absolute or not
     */

    public void write(long timestamp, byte[] audioData, boolean absolute) {
        try {

            InputStream byteArrayInputStream = new ByteArrayInputStream(
                    audioData);
            AudioInputStream realNetStream = new AudioInputStream(
                    byteArrayInputStream, ulawFormat, AudioSystem.NOT_SPECIFIED);

            synchronized (realNetStream) {
                //int len = 128000;
                int len = 160;
                int cnt;
                byte tempBuffer[] = new byte[len];

                while ((cnt = realNetStream.read(tempBuffer,
                                                 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        /* Write data to the internal buffer of the data line where
                           it will be delivered
                           to the speaker. */
                        outStream.write(tempBuffer, 0, cnt);
                        //System.out.println("wrote to phone " + cnt + " bytes");
                        //System.out.print(". ");

                        //Testing Purposes
                        //fos.write(tempBuffer, 0, cnt);
                    } else if (cnt == 0) {
                        //System.out.println("nothing to write");
                        Thread.sleep(15);
                    } else
                        break;
                }
            }

            /* Create a thread to play back the data and start it running.
               It will run until all the data has been played back.*/

            //playThread = new Thread(new PlayThread());
            //playThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
