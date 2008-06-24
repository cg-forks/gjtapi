package iax.audio.gsm;

import iax.audio.Player;
import iax.audio.PlayerException;

/**
 * GSM audio player.
 */
public class GSMPlayer extends Player {

	/**
	 * Constructor. Initializes player.
	 * @throws PlayerException
	 */
	public GSMPlayer() throws PlayerException {
		super(Player.JITTER_BUFFER);
	}
}
