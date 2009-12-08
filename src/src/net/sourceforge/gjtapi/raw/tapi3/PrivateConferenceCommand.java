package net.sourceforge.gjtapi.raw.tapi3;

public class PrivateConferenceCommand extends PrivateJoinCommand {

	// Variables
	private boolean complete = false;
	private int consultationCallId = -1;

	/**
	 * Create a command for setting up a conference call
	 */
	public PrivateConferenceCommand(String controller, String theNumberToDial) {
		super(controller, theNumberToDial);
	}

	public void setComplete(boolean shouldComplete) {
		this.complete = shouldComplete;
	}
	
	public boolean isComplete() {
		return this.complete;
	}

	@Override
	public int perform(Tapi3Native nativeHandle, Tapi3CallID call) {
		if(!this.isComplete()) {
			// set up the conference consultation call
			setConsultationCallId(nativeHandle.tapi3ConsultationStart(call.getCallID(), this.getControllerNumber(), this.getNumberToDial()));
			return getConsultationCallId();
		} else {
			// finish the transfer
			return nativeHandle.tapi3ConferenceFinish(getConsultationCallId());
		}
	}

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
