package iax.audio.gsm;

import iax.audio.Recorder;
import iax.audio.RecorderException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * GSM audio recorder.
 */
public class GSMRecorder extends Recorder {

	/**
	 * Constructor. Initializes recorder.
	 * @throws RecorderException
	 */
	public GSMRecorder() throws RecorderException {}
}
