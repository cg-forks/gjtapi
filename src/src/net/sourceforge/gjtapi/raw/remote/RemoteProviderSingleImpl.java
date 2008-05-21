package net.sourceforge.gjtapi.raw.remote;

/*
	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 

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
import javax.telephony.*;
/**
 * This is a type of RemoteProvider that assumes that only one client will be using it.
 * Therefore Call monitoring stop events are not eaten.
 * Creation date: (2000-06-28 1:19:48)
 * @author: Richard Deadman
 */
public class RemoteProviderSingleImpl extends RemoteProviderImpl {
	static final long serialVersionUID = 6085374214584723631L;
	
/**
 * RemoteProviderSingleImpl constructor comment.
 * @param rp net.sourceforge.gjtapi.TelephonyProvider
 * @exception java.rmi.RemoteException The exception description.
 */
public RemoteProviderSingleImpl(net.sourceforge.gjtapi.TelephonyProvider rp) throws java.rmi.RemoteException {
	super(rp);
}
/**
 * Forward the request on to the local RawProvider
 */
public void reportCallsOnAddress(String address, boolean flag)
throws InvalidArgumentException, ResourceUnavailableException {
	this.getDelegate().reportCallsOnAddress(address, flag);
}
/**
 * Forward the request on to the local RawProvider
 */
public void reportCallsOnTerminal(String terminal, boolean flag)
throws InvalidArgumentException, ResourceUnavailableException {
	this.getDelegate().reportCallsOnTerminal(terminal, flag);
}
/**
 * Forward request to the local TelephonyProvider
 * <P>Since we assume only one Framework is using us, it is safe to stop reporting on this call.
 */
public boolean stopReportingCall(SerializableCallId call)  {
	return this.getDelegate().stopReportingCall(call);
}
}
