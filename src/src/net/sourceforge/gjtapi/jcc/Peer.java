package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2002 Deadman Consulting (www.deadman.ca) 

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
import net.sourceforge.gjtapi.*;
import javax.csapi.cc.jcc.*;
/**
 * <P>
The JcpPeer interface represents a vendor's particular implementation of the JCP API.
<br>
Each JCC implementation vendor must implement this interface. The JcpPeer object returned
by the JcpPeerFactory.getJcpPeer() method determines which Providers are made available to 
the application. 
<br>
Applications use the JcpPeer.getProvider() method on this interface to obtain new Provider
objects. Each implementation may support one or more different "services". A list of available
services can be obtained via the JcpPeer.getServices() method. 
<br>


<H4>Obtaining a Provider</H4> 
Applications use the <CODE>JcpPeer.getProvider()</CODE> method on this 
interface to obtain new Provider objects. Each implementation may support 
one or more different "services" (e.g. for different types of underlying 
network substrate). A list of available services can be obtained via the 
<CODE>JcpPeer.getServices()</CODE> method. <p> Applications may also 
supply optional arguments to the Provider through the 
<CODE>JcpPeer.getProvider()</CODE> method. These arguments are appended to
the <CODE>providerString</CODE> argument passed to the 
<CODE>JcpPeer.getProvider()</CODE> method. The <CODE>providerString</CODE>
argument has the following format: 
<p> &lt service name &gt ; arg1 = val1; arg2 = val2; ... <p> 
Where &lt service name &gt is not optional, and each optional argument pair
which follows is separated by a semi-colon. The keys for these arguments is 
implementation specific, except for two standard-defined keys: <OL> 
<LI>login: provides the login user name to the Provider. 
<LI>passwd: provides a password to the Provider. </OL>
<P>
<B> We can;t simply subclass net.sourceforge.gjtapi.GenericPeer since the JTAPI and Jain Jcc
interfaces conflict with the return value of "getProvider()".
 * Creation date: (2000-10-10 12:18:48)
 * @author: Richard Deadman
 */
public class Peer implements javax.csapi.cc.jcc.JccPeer {
	private GenericJtapiPeer genPeer = null;
/**
 * Peer constructor comment.
 */
public Peer() {
	super();

	this.setGenPeer(new GenericJtapiPeer());
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-10 12:28:10)
 * @return net.sourceforge.gjtapi.GenericJtapiPeer
 */
private net.sourceforge.gjtapi.GenericJtapiPeer getGenPeer() {
	return genPeer;
}
/**
 * getName method comment.
 */
public String getName() {
	return this.getGenPeer().getName();
}
/**
 * getProvider method comment.
 */
public JccProvider getProvider(String providerString) throws ProviderUnavailableException {
	return new Provider((GenericProvider)this.getGenPeer().getProvider(providerString));
}
/**
 * getServices method comment.
 */
public java.lang.String[] getServices() {
	return this.getGenPeer().getServices();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-10 12:28:10)
 * @param newGenPeer net.sourceforge.gjtapi.GenericJtapiPeer
 */
private void setGenPeer(net.sourceforge.gjtapi.GenericJtapiPeer newGenPeer) {
	genPeer = newGenPeer;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jain Jcp Peer for Generic JTAPI Framework Jcc layer";
}
}
