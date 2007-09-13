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
 * Large portions of this software are based upon public domain software
 * https://sip-communicator.dev.java.net/
 *
 */
package net.sourceforge.gjtapi.raw.sipprovider.media;

import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Processor;

import net.sourceforge.gjtapi.raw.sipprovider.common.Console;

/**
 * <p>
 * Title: SIP COMMUNICATOR
 * </p>
 * <p>
 * Description:JAIN-SIP Audio/Video phone application
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr)
 * </p>
 * <p>
 * Network Research Team (http://www-r2.u-strasbg.fr))
 * </p>
 * <p>
 * Louis Pasteur University - Strasbourg - France
 * </p>
 * <p>
 * Division Chief: Thomas Noel
 * </p>
 * 
 * @author Emil Ivov (http://www.emcho.com)
 * @version 1.1
 * 
 */
class ProcessorUtility implements ControllerListener {
    /** Logger for this class. */
    protected static Console console = Console
            .getConsole(ProcessorUtility.class);

    /** State lock for a wait/notify mechanism. */
    private Integer stateLock = new Integer(0);

    /** <code>true</code> if the waiting failed. */
    private boolean failed = false;

    /**
     * Constructs a new object.
     */
    public ProcessorUtility() {
    }

    Integer getStateLock() {
        return stateLock;
    }

    void setFailed() {
        failed = true;
    }

    public void controllerUpdate(ControllerEvent ce) {
        if (console.isDebugEnabled()) {
            console.debug("received update event " + ce);
        }
        // If there was an error during configure or
        // realize, the processor will be closed
        if (ce instanceof ControllerClosedEvent) {
            setFailed();
        }
        if (ce instanceof ControllerEvent) {
            synchronized (getStateLock()) {
                getStateLock().notifyAll();
            }
        }
    }

    synchronized boolean waitForState(Processor p, int state) {
        if (console.isDebugEnabled()) {
            console.debug("waiting for processor state " + state + "...");
        }

        p.addControllerListener(this);
        failed = false;
        // Call the required method on the processor
        if (p.getState() != Processor.Configured
                && state == Processor.Configured) {
            p.configure();
        } else if (p.getState() != Processor.Realized
                && state == Processor.Realized) {
            p.realize();
        }
        // Wait until we get an event that confirms the
        // success of the method, or a failure event.
        while ((p.getState() != state) && !failed) {
            synchronized (getStateLock()) {
                try {
                    getStateLock().wait();
                } catch (InterruptedException ie) {
                    return false;
                }
            }
        }

        p.removeControllerListener(this);

        if (console.isDebugEnabled()) {
            console.debug("reached processor state " + state);
        }

        return !failed;
    }
}