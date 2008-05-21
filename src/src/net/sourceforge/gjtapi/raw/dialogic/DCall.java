/*
    Copyright (c) 2002 Westhawk Ltd. (www.westhawk.co.uk) 

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
package net.sourceforge.gjtapi.raw.dialogic;

import net.sourceforge.gjtapi.CallId;

public class DCall implements CallId {
    //private static int cno=1; // increments with every call
    public DCall(){
        //pid = cno++; // I'll take the risk that it wraps.
    }
    //private int pid;
    private Integer crn;
    private Integer linedev;
    private String destaddr;
    private String origaddr;
    private String devname;
    
    public Integer getCrn() {
        return crn;
    }
    
    public int getCrnInt(){
        if (crn == null) {
            throw new RuntimeException("No matching call id");
        }
        return crn.intValue();
    }

    public Integer getLinedev() {
        return linedev;
    }

    public String getDestaddr() {
        return destaddr;
    }

    public String getOrigaddr() {
        return origaddr;
    }

    public String getDevname() {
        return devname;
    }
    public void setCrn(Integer newCrn) {
        crn = newCrn;
    }
    public void setDestaddr(String newDestaddr) {
        destaddr = newDestaddr;
    }
    public void setDevname(String newDevname) {
        devname = newDevname;
    }
    public void setLinedev(Integer newLinedev) {
        linedev = newLinedev;
    }
    public void setOrigaddr(String newOrigaddr) {
        origaddr = newOrigaddr;
    }
}