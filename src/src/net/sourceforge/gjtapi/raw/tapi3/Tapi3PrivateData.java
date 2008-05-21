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
package net.sourceforge.gjtapi.raw.tapi3;

import java.io.Serializable;

/**
 * A container class for caller and callee information used as private data by the Tapi3Provider 
 * @author Serban Iordache
 */
public class Tapi3PrivateData implements Serializable {
	static final long serialVersionUID = 2360569966156135140L;
	
    /**
     * The caller name
     */
    private final String callerName;
    /**
     * The caller number
     */
    private final String callerNumber;
    /**
     * The called name
     */
    private final String calledName;
    /**
     * The called number
     */
    private final String calledNumber;
    
    /**
     * Initialize container data
     * @param callerName The caller name
     * @param callerNumber The caller number
     * @param calledName The called name
     * @param calledNumber The called number
     */
    public Tapi3PrivateData(String callerName, String callerNumber, String calledName, String calledNumber) {
        this.callerName = (callerName != null) ? callerName.trim() : null;
        this.callerNumber = (callerNumber != null) ? callerNumber.trim() : null;
        this.calledName = (calledName != null) ? calledName.trim() : null;
        this.calledNumber = (calledNumber != null) ? calledNumber.trim() : null;
    }

    /**
     * Return the caller name
     * @return The caller name
     */
    public String getCallerName() {
        return callerName;
    }
    /**
     * Return the caller number
     * @return The caller number
     */
    public String getCallerNumber() {
        return callerNumber;
    }
    /**
     * Return the called name
     * @return The called name
     */
    public String getCalledName() {
        return calledName;
    }
    /**
     * Return the called number
     * @return The called number
     */
    public String getCalledNumber() {
        return calledNumber;
    }

    /**
     * Describe myself.
     * @return A string representation of the Tapi3PrivateData
     */
    public String toString() {
        return "(callerName: " + callerName + 
		        ", callerNumber: " + callerNumber +
		        ", calledName: " + calledName +
		        ", calledNumber: " + calledNumber + ")";
    }
}
