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

/**
 * This class helps the Tapi3 provider support transfer and conference.
 * @author richard
 *
 */
public class PrivateTransferConferenceInfo {

	private String consultAddress;
	private String consultTerminal;
	private Tapi3CallID primaryCallId;
	private int consultCallId;
	private boolean transferFlag;
	
	/**
	 * Private constructor, called by static factory methods
	 */
	private PrivateTransferConferenceInfo(Tapi3CallID callId, String address, String terminal, boolean isTransfer) {
		this.primaryCallId = callId;
		this.consultAddress = address;
		this.consultTerminal = terminal;
		this.transferFlag = isTransfer;
	}
	// Factory methods
	/**
	 * Create a private flag to signal a conference
	 * @param primaryId Call id of the primary call
	 * @param consultId Call id of the consultation call
	 * @return a CallControlInfo object suitable for sending to a Call that is to be transferred
	 */
	public static PrivateTransferConferenceInfo createTransferInfo(Tapi3CallID callId, String address, String terminal) {
		return new PrivateTransferConferenceInfo(callId, address, terminal, true);
	}
	/**
	 * Create a private flag to signal a conference
	 * @param primaryId Call id of the primary call
	 * @param consultId Call id of the consultation call
	 * @return a CallControlInfo object suitable for sending to a Call that is to be conferenced
	 */
	public static PrivateTransferConferenceInfo createConferenceInfo(Tapi3CallID callId, String address, String terminal) {
		return new PrivateTransferConferenceInfo(callId, address, terminal, false);
	}

	// Accessors
	protected int getConsultCallId() {
		return consultCallId;
	}
	public void setConsultCallId(int consultId) {
		this.consultCallId = consultId;
	}
	public void setTransferFlag(boolean flag) {
		this.transferFlag = flag;
	}
	protected boolean isTransfer() {
		return this.transferFlag;
	}
	protected boolean isConference() {
		return !this.transferFlag;
	}
	/**
	 * @return Returns the consultAddress.
	 */
	protected String getConsultAddress() {
		return consultAddress;
	}
	/**
	 * @return Returns the consultTerminal.
	 */
	protected String getConsultTerminal() {
		return consultTerminal;
	}
	
	/**
	 * Set or clear the call id.
	 * Clearing the call id is useful in order to remove the mapping using sendPrivateData.
	 * @param id
	 */
	public void setPrimaryCallId(Tapi3CallID id) {
		this.primaryCallId = id;
	}
	
	protected Tapi3CallID getPrimaryCallId() {
		return this.primaryCallId;
	}
}
