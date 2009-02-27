/**
 * 
 */
package net.sourceforge.gjtapi.protocols;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.TargetDataLine;

/**
 * An {@link InputStream} that reads from a {@link TargetDataLine}.
 * @author Dirk Schnelle-Walka
 *
 */
public final class LineInputStream extends InputStream
    implements Closeable {
    /** The line to read from. */
    private final TargetDataLine line;

    /**
     * Constructs a new object.
     * @param target the line to read from.
     */
    public LineInputStream(final TargetDataLine target) {
        line = target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        byte[] bytes = new byte[1];
        return read(bytes, 0, bytes.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final byte[] b, final int off, final int len)
        throws IOException {
        return line.read(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        line.drain();
        line.stop();
        line.close();
        super.close();
    }

}
