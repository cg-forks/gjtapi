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
package net.sourceforge.gjtapi.raw.sipprovider;

import net.sourceforge.gjtapi.raw.sipprovider.sip.Call;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallListener;
import net.sourceforge.gjtapi.raw.sipprovider.sip.event.CallStateEvent;
import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
import net.sourceforge.gjtapi.raw.sipprovider.common.Utils;
import java.applet.*;
//import net.java.sip.communicator.gui.*;

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
public class Interlocutor
    implements 
    CallListener
{
    private static Console console = Console.getConsole(Interlocutor.class);
    private Call call;
 
   
    public Call getCall()
    {
        return call;
    }

    //InterlocutorUI
    public boolean isCaller()
    {
        return call.isIncoming();
    }

    public String getAddress()
    {
        return call.getAddress();
    }

    public String getName()
    {
        return call.getRemoteName();
    }

    public int getID()
    {
        return call.getID();
    }

    public String getCallState()
    {
        return call.getState();
    }


    //CallListener
    public void callStateChanged(CallStateEvent evt)
    {
        try {
            console.logEntry();
            //guiCallback.update(this);
            if (evt.getNewState() == Call.DISCONNECTED) {
                //guiCallback.remove(this);
                //----- Alerts
            }
            if (evt.getNewState() != evt.getOldState()) {
                //file names should be lowercase - reported by Laurent Michel <laurent.michel@geo12.com>
                if (evt.getOldState() == Call.ALERTING) {
                    //guiCallback.stopAlert("alerting.wav");
                }
                else if (evt.getOldState() == Call.RINGING) {
                    //guiCallback.stopAlert("ringing.wav");
                }
                else if (evt.getOldState() == Call.BUSY) {
                    //guiCallback.stopAlert("busy.wav");
                    //Start current alert
                }
                if (evt.getNewState() == Call.ALERTING) {
                    //guiCallback.startAlert("alerting.wav");
                }
                else if (evt.getNewState() == Call.RINGING) {
                    //guiCallback.startAlert("ringing.wav");
                }
                else if (evt.getNewState() == Call.BUSY) {
                    //guiCallback.startAlert("busy.wav");
                }
            }
        }
        finally {
            console.logExit();
        }
    }

    public static void main(String[] args) 
    {
        AudioClip busy = Applet.newAudioClip(Utils.getResource("busy.wav"));
        busy.play();
        busy.stop();
//        Applet.newAudioClip(Utils.getResource("error.wav")).play();
//        Applet.newAudioClip(Utils.getResource("ringing.wav")).play();
    }
}