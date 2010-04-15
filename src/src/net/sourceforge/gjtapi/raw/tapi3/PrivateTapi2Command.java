/*
	Copyright (c) 2009 Richard Deadman 
	
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
 * @author rdeadman
 *
 * This is a simple command class that allows a JTAPI application
 * to request that the TAPI2 overload commands for transfer/conference be used.
 */
public class PrivateTapi2Command extends PrivateJoinCommand implements Serializable {

	/**
	 * Required serializable id
	 */
	private static final long serialVersionUID = 1L;
	// Variables
	private boolean complete = false;
	private boolean transfer = true;
	private int consultationCallId = -1;
	
	/**
	 * Create a command for setting up an assisted transfer.
	 */
	public PrivateTapi2Command(String controller, String theNumberToDial, boolean isTransfer) {
		super(controller, theNumberToDial);
		
		this.transfer = isTransfer;
	}
		
	public void setComplete(boolean shouldComplete) {
		this.complete = shouldComplete;
	}
	
	public boolean isComplete() {
		return this.complete;
	}

	public void setTransfer(boolean isTransfer) {
		this.transfer = isTransfer;
	}
	
	public boolean isTransfer() {
		return this.transfer;
	}

	public boolean isConference() {
		return !this.transfer;
	}

	@Override
	public int perform(Tapi3Native nativeHandle, Tapi3CallID call) {
		if(!this.isComplete()) {
			// set up the transfer consultation call
			return nativeHandle.tapi2ConsultationStart(this.getControllerNumber(), this.getNumberToDial());
		} else {
			// finish the transfer/conference
			return nativeHandle.tapi2TransferFinish(this.getControllerNumber(), this.isTransfer());
		}
	}

//	public void testConsult(String from, String to) {
//		Tapi3Native impl = Tapi3NativeImpl.getInstance();
//		impl.tapi2ConsultationStart(from, to);
//	}
	/**
	 * @param consultationCallId The consultationCallId to set.
	 */
	public void setConsultationCallId(int consultationCallId) {
		this.consultationCallId = consultationCallId;
	}

	/**
	 * @return Returns the consultationCallId.
	 */
	public int getConsultationCallId() {
		return consultationCallId;
	}

}
