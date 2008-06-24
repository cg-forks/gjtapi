package iax.audio;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * Audio Player.
 *
 */
public abstract class Player {


	/**
	 * Implements Simple BUFFER
	 */
	public final static int SIMPLE_BUFFER = 0;
	/**
	 * Implements Jitter BUFFER
	 */
	public final static int JITTER_BUFFER = 1;


	/**
	 * Constructor. Initializes the player.
	 * @throws PlayerException
	 */
	public Player(int bufferType) throws PlayerException {}

	/**
	 * Starts playing.
	 */
	 public void play() {}

	 /**
	  * Stops playing.
	  */
	 public void stop() {}

	 /**
	  * Writes audio data in player audio buffer.
	  * @param timestamp Timestamp of the received audio package.
	  * @param data Audio data.
	  * @param absolute if the timestamp absolute or not
	  */
	 public void write(long timestamp, byte[] data, boolean absolute) {}

         //public void run() {}
}
