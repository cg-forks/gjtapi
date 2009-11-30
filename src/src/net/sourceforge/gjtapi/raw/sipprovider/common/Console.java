/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */
package net.sourceforge.gjtapi.raw.sipprovider.common;

import java.util.Enumeration;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.1
 */
public class Console
{
    //--------------- LOGGER ENCAPSULATION -----------------------------------
    // ------------------------------------------------------------- Attributes
    private static boolean initialized = false;
    private static String LAYOUT = "%r [%t] %p %c{2} %x - %m%n";
    private Logger logger = null;
    // ------------------------------------------------------------ Constructor
    /**
     * Base constructor
     */
    private Console(Logger logger)
    {
        this.logger = logger;
        if (!initialized) {
            initialize();
        }
    }

    public static Console getConsole(Class clazz)
    {
        return new Console(Logger.getLogger(clazz));
    }

    public static Console getConsole(String name)
    {
        return new Console(Logger.getLogger(name));
    }

    // ---------------------------------------------------------- Implmentation
    private void initialize()
    {
    	Logger root = Logger.getRootLogger();
        Enumeration appenders = root.getAllAppenders();
        if (appenders == null || !appenders.hasMoreElements()) {
            // No config, set some defaults ( consistent with
            // commons-logging patterns ).
            ConsoleAppender app = new ConsoleAppender(new PatternLayout(LAYOUT),
                ConsoleAppender.SYSTEM_OUT);
            app.setName("SIP COMMUNICATOR");
            root.addAppender(app);
            root.setLevel(Level.TRACE);
        }
        initialized = true;
    }

    /**
     * Logs an entry in the calling method
     */
    public void logEntry()
    {
        if (logger.isEnabledFor(Level.TRACE)) {
            StackTraceElement caller = new Throwable().getStackTrace()[1];
            logger.log(Level.TRACE, "[entry] " + caller.getMethodName());
        }
    }

    /**
     * Logs exiting the calling method
     */
    public void logExit()
    {
        if (logger.isEnabledFor(Level.TRACE)) {
            StackTraceElement caller = new Throwable().getStackTrace()[1];
            logger.log(Level.TRACE, "[exit] " + caller.getMethodName());
        }
    }

    /**
     * Log a message to the Log4j Category with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace(Object message)
    {
        logger.log(this.getClass().getName(), Level.TRACE, message, null);
    }

    /**
     * Log an error to the Log4j Category with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace(Object message, Throwable t)
    {
        logger.log(this.getClass().getName(), Level.TRACE, message, t);
    }

    public void debug(Object message, Throwable t)
    {
        logger.log(this.getClass().getName(), Level.DEBUG, message, t);
    }

    public void debug(Object message)
    {
        logger.log(this.getClass().getName(), Level.DEBUG, message, null);
    }

    public void info(Object message, Throwable t)
    {
        logger.log(this.getClass().getName(), Level.INFO, message, t);
    }

    public void info(Object message)
    {
        logger.log(this.getClass().getName(), Level.INFO, message, null);
    }

    public void warn(Object message, Throwable t)
    {
        logger.log(this.getClass().getName(), Level.WARN, message, t);
    }

    public void warn(Object message)
    {
        logger.log(this.getClass().getName(), Level.WARN, message, null);
    }

    public void error(Object message, Throwable t)
    {
        logger.log(this.getClass().getName(), Level.ERROR, message, t);
    }

    public void error(Object message)
    {
        logger.log(this.getClass().getName(), Level.ERROR, message, null);
    }

    public void fatal(Object message, Throwable t)
    {
        logger.log(this.getClass().getName(), Level.FATAL, message, t);
    }

    public void fatal(Object message)
    {
        logger.log(this.getClass().getName(), Level.FATAL, message, null);
    }

    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled()
    {
        return logger.isEnabledFor(Level.WARN);
    }

    //------------------------------- TEST --------------------------------
    public static void main(String[] args)
    {
        Console console = Console.getConsole(Console.class);
        console.debug("Debug");
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException ex) {
        }
        console.trace("Trace");
        console.info("Info");
        console.warn("Warn");
        console.error("Error");
        console.fatal("fatal");
    }
}