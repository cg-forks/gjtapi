package iax.audio.ulaw;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import iax.audio.AudioListener;
import iax.audio.Recorder;
import iax.audio.RecorderException;
import iax.protocol.call.Call;


/**
 * ULAW audio recorder.
 * Reads audio from inStream and sends it to a class implementing AudioListener.
 * Analog to PCMRecorder
 */

public class ULAWRecorder extends Recorder {
    private static final int BUFFER_SIZE = 128000;

    AudioListener call;
    boolean recording = true;
    ByteArrayOutputStream out_stream;
    int buffer_size;
    Thread captureThread;

    InputStream inStream;

    /**
     * Constructor. Initializes recorder.
     * @throws RecorderException
     */
    public ULAWRecorder(Call call, InputStream inStream) throws
            RecorderException {
        this.call = call;
        if (inStream == null)
            throw new RecorderException("inStream null");
        this.inStream = inStream;
    }

    public void run() {}

    /**
     * Starts recording.
     * @param al Object that is going to process the recorded audio data.
     */

    public void record(AudioListener al) {
        call = al;
        buffer_size = 160;
        buffer_size -= buffer_size % 2;

        recording = true;
        captureThread = new Thread(new CaptureThread());
        captureThread.start();
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

            try {
                while (recording) {
                    int count;
                    count = inStream.read(buffer, 0, buffer.length);

                    if (count > 0) {
                        synchronized (buffer) {
                            call.listen(buffer, 0, count);
                        }
                    } else if (count == 0) {
                        //System.out.println("nothing to write");
                        Thread.sleep(15);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
