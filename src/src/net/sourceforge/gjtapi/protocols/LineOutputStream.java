/**
 * 
 */
package net.sourceforge.gjtapi.protocols;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.SourceDataLine;

/**
 * An {@link OutputStream} that writes to a {@link SourceDataLine}.
 * @author Dirk Schnelle-Walka
 *
 */
public class LineOutputStream extends OutputStream
    implements Closeable {
    /** The source data line. */
    private SourceDataLine line;

    /**
     * Constructs a new object.
     * @param source the line to write to.
     */
    public LineOutputStream(SourceDataLine source) {
        line = source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        byte[] bytes = new byte[1];
        write(bytes, 0, bytes.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        line.write(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        line.drain();
        super.flush();
    }

}
