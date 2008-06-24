package iax.audio.pcm;

import iax.audio.Player;
import iax.audio.PlayerException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineUnavailableException;

/**
 * Audio Player.
 *
 */
public class PCMPlayer extends Player {

    /**
     * Implements Jitter BUFFER
     */

    public final static int JITTER_BUFFER = 0;

    AudioInputStream netStream;
    SourceDataLine headphone;

    AudioFormat gsmFormat;
    AudioFormat ulawFormat;
    AudioFormat pcmFormat;

    boolean first;

    OutputStream outStream;


    /**
     * Constructor. Initializes the player.
     * @throws PlayerException
     */
    public PCMPlayer(int bufferType) throws PlayerException {
        super(bufferType);

        //gsmFormat = new AudioFormat(Encodings.getEncoding("GSM0610"), 8000.0F, -1, 1, 33, 50.0F, false);
        ulawFormat = new AudioFormat(AudioFormat.Encoding.ULAW, 8000.0F, 8, 1,
                                     1, 8000.0F, false);
        pcmFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F,
                                    16, 1, 2, 8000.0F, false);
        openHeadphone();
    }

    private void openHeadphone() {
        try {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,
                    pcmFormat);
            headphone = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            headphone.open(pcmFormat);
            headphone.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public PCMPlayer(OutputStream outStream) throws PlayerException {
        this(JITTER_BUFFER);
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
            //System.out.println("aux audio stream created");
            netStream = realNetStream;

            synchronized (netStream) {

                netStream = AudioSystem.getAudioInputStream(pcmFormat,
                        realNetStream);
                //netStream = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, realNetStream);

                int len = 128000;
                int cnt;
                byte tempBuffer[] = new byte[len];

                while ((cnt = netStream.read(tempBuffer,
                                             0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        /* Write data to the internal buffer of the data line where
                           it will be delivered
                           to the speaker. */
                        if (outStream == null) /////////////////////////////////////Tests
                            headphone.write(tempBuffer, 0, cnt);
                        else
                            outStream.write(tempBuffer, 0, cnt);
                        //System.out.println("wrote to headphone " + cnt + " bytes");

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
