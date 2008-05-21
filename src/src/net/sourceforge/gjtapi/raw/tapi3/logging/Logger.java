/*
	Copyright (c) 2005 Serban Iordache 
	
	All rights reserved. 
	
	Permission is hereby granted, free of charge, to any person obtaining a 
	copy of this software and associated documentation files (the 
	"Software"), to deal in the Software without restriction, including 
	without limitation the rights to use, copy, modify, merge, publish, 
	distribute, and/or sell copies of the Software, and to permit persons 
	to whom the Software is furnished to do so, provided that the above 
	copyright notice(s) and this permission notice appear in all copies of 
	the Software and that both the above copyright notice(s) and this 
	permission notice appear in supporting documentation. 
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
	OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
	HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
	INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
	FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
	NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
	WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
	
	Except as contained in this notice, the name of a copyright holder 
	shall not be used in advertising or otherwise to promote the sale, use 
	or other dealings in this Software without prior written authorization 
	of the copyright holder.
*/
package net.sourceforge.gjtapi.raw.tapi3.logging;

/**
 * A simple interface for logging  
 * @author Serban Iordache
 */
public interface Logger {
    /**
     * Log a DEBUG message
     * @param message - the message object to log
     */
    public void debug(Object message);

    /**
     * Log a DEBUG message including the stack trace of the Throwable parameter
     * @param message - the message object to log
     */
    public void debug(Object message, Throwable t);

    /**
     * Log an INFO message
     * @param message - the message object to log
     */
    public void info(Object message);

    /**
     * Log an INFO message including the stack trace of the Throwable parameter
     * @param message - the message object to log
     */
    public void info(Object message, Throwable t);

    /**
     * Log a WARN message
     * @param message - the message object to log
     */
    public void warn(Object message);

    /**
     * Log a WARN message including the stack trace of the Throwable parameter
     * @param message - the message object to log
     */
    public void warn(Object message, Throwable t);

    /**
     * Log an ERROR message
     * @param message - the message object to log
     */
    public void error(Object message);

    /**
     * Log an ERROR message including the stack trace of the Throwable parameter
     * @param message - the message object to log
     */
    public void error(Object message, Throwable t);

    /**
     * Log a FATAL message
     * @param message - the message object to log
     */
    public void fatal(Object message);

    /**
     * Log a FATAL message including the stack trace of the Throwable parameter
     * @param message - the message object to log
     */
    public void fatal(Object message, Throwable t);
}
