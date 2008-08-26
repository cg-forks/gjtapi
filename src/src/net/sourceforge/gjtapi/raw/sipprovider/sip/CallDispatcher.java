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
package net.sourceforge.gjtapi.raw.sipprovider.sip;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sip.Dialog;
import javax.sip.message.Request;

import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallListener;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallStateEvent;

/**
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov (http://www.emcho.com)
 * @version 1.1
 *
 */
class CallDispatcher
    implements CallListener
{
    private static final Console console =
        Console.getConsole(CallDispatcher.class);

    /**
     * All currently active calls.
     */
    Hashtable calls = new Hashtable();


    Call createCall(Dialog dialog,
                    Request initialRequest
                    )
    {
        try {
            console.logEntry();
            Call call = null;
            if (dialog.getDialogId() != null) {
                call = findCall(dialog);
            }
            if (call == null) {
                call = new Call();
            }
            call.setDialog(dialog);
            call.setInitialRequest(initialRequest);
//            call.setState(Call.DIALING);
            calls.put(new Integer(call.hashCode()), call);
            if (console.isDebugEnabled()) {
                console.debug("created call" + call);
            }
            call.addStateChangeListener(this);
            return call;
        }
        finally {
            console.logExit();
        }
    }

    Call getCall(int id)
    {
        return (Call) calls.get(new Integer(id));
    }


    /**
     * Find the call that contains the specified dialog.
     * @param dialog the dialog whose containing call is to be found
     * @return the call that contains the specified dialog,
     * <code>null</code> if the call could not be found.
     */
    Call findCall(Dialog dialog)
    {
        try {
            console.logEntry();
            if (dialog == null) {
                return null;
            }
            synchronized (calls) {
                Iterator iterator = calls.values().iterator();
                while (iterator.hasNext()) {
                    Call item = (Call) iterator.next();
                    if (item.getDialog().getCallId().equals(dialog.getCallId())) {
                        return item;
                    }
                }
            }
            return null;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Find the call with the specified CallID header value.
     * @param callID the CallID header value of the searched call.
     * @return the call with the specified CallID header value.
     */
    Call findCall(String callId)
    {
        try {
            console.logEntry();
            if (callId == null) {
                return null;
            }
            synchronized (calls) {
                Enumeration callEnumerator = calls.elements();
                while (callEnumerator.hasMoreElements()) {
                    Call item = (Call) callEnumerator.nextElement();
                    if (item.getDialog().getCallId().equals(callId)) {
                        return item;
                    }
                }
            }
            return null;
        }
        finally {
            console.logExit();
        }
    }


    public Object[] getAllCalls()
    {
        return calls.keySet().toArray();
    }

    private void removeCall(Call call)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("removing call" + call);
            }
            calls.remove(new Integer(call.getID()));
        }
        finally {
            console.logExit();
        }
    }

//================================ DialogListener =================
    public void callStateChanged(CallStateEvent evt)
    {
        if (evt.getNewState().equals(Call.DISCONNECTED)) {
            removeCall(evt.getSourceCall());
        }
    }
}