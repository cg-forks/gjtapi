package iax.audio;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioPermission;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * Audio recorder.
 */
public abstract class Recorder {

	/**
	 * Constructor. Initializes the recorder.
	 * @throws RecorderException
	 */
	public Recorder() throws RecorderException {}

	/**
	 * Starts recording.
	 * @param al Object that is going to process the recorded audio data.
	 */
	public void record(AudioListener al) {}

	/**
	 * Stops recording.
	 */
	public void stop()	{}

	//public void run() {}
}
