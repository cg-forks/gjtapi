package iax.audio.pcm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import iax.audio.AudioListener;
import iax.audio.Recorder;
import iax.audio.RecorderException;
import iax.protocol.call.Call;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;


/**
 * PCM audio recorder.
 */

public class PCMRecorder extends Recorder {

    AudioListener call;
    boolean recording = true;
    ByteArrayOutputStream out_stream;
    int buffer_size;
    TargetDataLine mic;
    Thread captureThread;

    InputStream inStream;

    /**
     * Constructor. Initializes recorder.
     * @throws RecorderException
     */
    public PCMRecorder(Call call, InputStream inStream) throws
            RecorderException {
        this.call = call;
        this.inStream = inStream;
    }

    public void run() {}

    /**
     * Starts recording.
     * @param al Object that is going to process the recorded audio data.
     */

    public void record(AudioListener al) {
        call = al;
        try {
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.
                                                 PCM_SIGNED, 8000.0F, 16, 1, 2,
                                                 8000.0F, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            mic = (TargetDataLine) AudioSystem.getLine(info);

            //buffer_size = (int)((long)(20 * 8000.0F / 1000 * 2));
            buffer_size = 160;
            buffer_size -= buffer_size % 2;
            //buffer_size = 64;
            mic.open(format);
            mic.start();
            recording = true;
            captureThread = new Thread(new CaptureThread());
            captureThread.start();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
            System.exit( -1);
        }
    }

    /**
     * Stops recording.
     */
    public void stop() {
        recording = false;
    }

    class CaptureThread extends Thread {
        public void run() {
            byte buffer[] = new byte[buffer_size];
            InputStream micData;
            AudioInputStream micPcm, micGsm;

            //System.out.println ("line buffer size is " + mic.getBufferSize());

            //AudioFormat gsmFormat = new AudioFormat (new AudioFormat.Encoding("GSM0610"),
            //		8000.0F, -1, 1, 33, 50.0F, false);
            AudioFormat ulawFormat = new AudioFormat(AudioFormat.Encoding.ULAW,
                    8000.0F, 8, 1, 1, 8000.0F, false);

            try {
                while (recording) {
                    int count;
                    if (inStream == null) //////////////////////////////////Test
                        count = mic.read(buffer, 0, buffer.length);
                    else
                        count = inStream.read(buffer, 0, buffer.length);
                    //System.out.println ("read " + count + " byte");
                    if (count > 0) {
                        micData = new ByteArrayInputStream(buffer, 0, count);
                        micPcm = new AudioInputStream(micData, mic.getFormat(),
                                AudioSystem.NOT_SPECIFIED);
                        micGsm = AudioSystem.getAudioInputStream(ulawFormat,
                                micPcm);
                        count = micGsm.read(buffer, 0, buffer_size);
                        synchronized (buffer) {
                            call.listen(buffer, 0, count);
                        }
                        //System.out.println ("wrote " + count + " byte on call");
                    } else if (count == 0) {
                        Thread.sleep(15);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit( -1);
            }
        }
    }
}
