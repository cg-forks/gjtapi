package net.sourceforge.gjtapi.util;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/***
 * Extension to the PipedInputStream to give the Java 5 version
 * constructors that have been added to Java 6.
 * This allows for the Java 6 functionality without requiring Java 6
 * for GJTAPI.
 * 
 * Code borrowed from the fix via bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4028462
 */
public class SizedPipedInputStream extends PipedInputStream {

	/**
     * Creates a <code>PipedInputStream</code> so
     * that it is connected to the piped output
     * stream <code>src</code>. Data bytes written
     * to <code>src</code> will then be  available
     * as input from this stream. It configures the
     * buffer size to be equal to the bsize parameter.
     *
     * @param      src   the stream to connect to.
     * @param      bsize the size of the buffer.
     * @throws     IllegalArgumentException if the bsize is < 1.
     * @exception  IOException  if an I/O error occurs.
     */
    public SizedPipedInputStream(PipedOutputStream src, int bsize) throws IOException {
		if(bsize < 1)
		    throw new IllegalArgumentException("bsize must be < 1, was " + bsize);
		buffer = new byte[bsize];
		connect(src);
    }
    
    /**
     * Creates a <code>PipedInputStream</code> so
     * that it is not  yet connected. It must be
     * connected to a <code>PipedOutputStream</code>
     * before being used. It configures the buffer
     * size to be equal to the bsize parameter.
     *
     * @param   bsize the size of the buffer.
     * @throws  IllegalArgumentException if the bsize is < 1.
     * @see     java.io.PipedInputStream#connect(java.io.PipedOutputStream)
     * @see     java.io.PipedOutputStream#connect(java.io.PipedInputStream)
     */
    public SizedPipedInputStream(int bsize) {
		if(bsize < 1)
		    throw new IllegalArgumentException("bsize < 1, was " + bsize);
		buffer = new byte[bsize];
    }

}
