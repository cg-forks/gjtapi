/**
 * 
 */
package net.sourceforge.gjtapi.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * URLConnection for playback URLs.
 * @author Dirk Schnelle-Walka
 *
 */
public final class PlaybackURLConnection extends URLConnection {
    /** The audio format to use. */
    private AudioFormat format;

    /**
     * Constructs a new object.
     * @param url URL that has to be handled.
     */
    public PlaybackURLConnection(final URL url) {
        super(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws IOException {
        final URL url = getURL();
        try {
            format = JavaSoundParser.parse(url);
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() throws IOException {
        final DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                format);
        TargetDataLine line;
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open();
        } catch (LineUnavailableException e) {
            throw new IOException(e.getMessage());
        }
        line.start();
        return new LineInputStream(line);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                format, AudioSystem.NOT_SPECIFIED);
        final SourceDataLine line;
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
        } catch (LineUnavailableException e) {
            throw new IOException(e.getMessage());
        }
        try {
            line.open(format);
        } catch (LineUnavailableException e) {
            throw new IOException(e.getMessage());
        }
        line.start();
        return new LineOutputStream(line);
    }
}
