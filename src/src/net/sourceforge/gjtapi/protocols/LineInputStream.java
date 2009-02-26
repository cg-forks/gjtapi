/**
 * 
 */
package net.sourceforge.gjtapi.protocols;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.Line;
import javax.sound.sampled.TargetDataLine;

/**
 * @author Piri
 *
 */
public final class LineInputStream extends InputStream {
    /** The line to read from. */
    private final TargetDataLine line;

    /**
     * Constructs a new object.
     * @param source the line to read from.
     */
    public LineInputStream(final TargetDataLine source) {
        line = source;
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

}
