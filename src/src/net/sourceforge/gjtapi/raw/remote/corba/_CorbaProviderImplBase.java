package net.sourceforge.gjtapi.raw.remote.corba;

/**
* com/uforce/jtapi/generic/raw/remote/corba/_CorbaProviderImplBase.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaProvider.idl
* Thursday, November 16, 2000 1:38:18 o'clock PM EST
*/

public abstract class _CorbaProviderImplBase extends org.omg.CORBA.portable.ObjectImpl implements CorbaProvider, org.omg.CORBA.portable.InvokeHandler {
  private static java.util.Hashtable _methods = new java.util.Hashtable ();

  static
  {
	_methods.put ("addListener", new java.lang.Integer (0));
	_methods.put ("allocateMedia", new java.lang.Integer (1));
	_methods.put ("answerCall", new java.lang.Integer (2));
	_methods.put ("attachMedia", new java.lang.Integer (3));
	_methods.put ("beep", new java.lang.Integer (4));
	_methods.put ("createCall", new java.lang.Integer (5));
	_methods.put ("freeMedia", new java.lang.Integer (6));
	_methods.put ("getAddresses", new java.lang.Integer (7));
	_methods.put ("getAddressesForTerm", new java.lang.Integer (8));
	_methods.put ("getAddressType", new java.lang.Integer (9));
	_methods.put ("getCall", new java.lang.Integer (10));
	_methods.put ("getCallsOnAddress", new java.lang.Integer (11));
	_methods.put ("getCallsOnTerminal", new java.lang.Integer (12));
	_methods.put ("getCapabilities", new java.lang.Integer (13));
	_methods.put ("getDialledDigits", new java.lang.Integer (14));
	_methods.put ("getPrivateData", new java.lang.Integer (15));
	_methods.put ("getTerminals", new java.lang.Integer (16));
	_methods.put ("getTerminalsForAddr", new java.lang.Integer (17));
	_methods.put ("hold", new java.lang.Integer (18));
	_methods.put ("initialize", new java.lang.Integer (19));
	_methods.put ("isMediaTerminal", new java.lang.Integer (20));
	_methods.put ("join", new java.lang.Integer (21));
	_methods.put ("play", new java.lang.Integer (22));
	_methods.put ("record", new java.lang.Integer (23));
	_methods.put ("release", new java.lang.Integer (24));
	_methods.put ("releaseCallId", new java.lang.Integer (25));
	_methods.put ("removeListener", new java.lang.Integer (26));
	_methods.put ("reportCallsOnAddress", new java.lang.Integer (27));
	_methods.put ("reportCallsOnTerminal", new java.lang.Integer (28));
	_methods.put ("reserveCallId", new java.lang.Integer (29));
	_methods.put ("retrieveSignals", new java.lang.Integer (30));
	_methods.put ("sendPrivateData", new java.lang.Integer (31));
	_methods.put ("sendSignals", new java.lang.Integer (32));
	_methods.put ("setLoadControl", new java.lang.Integer (33));
	_methods.put ("setPrivateData", new java.lang.Integer (34));
	_methods.put ("shutdown", new java.lang.Integer (35));
	_methods.put ("stop", new java.lang.Integer (36));
	_methods.put ("stopReportingCall", new java.lang.Integer (37));
	_methods.put ("triggerRTC", new java.lang.Integer (38));
	_methods.put ("unHold", new java.lang.Integer (39));
  }
  // Type-specific CORBA::Object operations
  private static String[] __ids = {
	"IDL:com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider:1.0"};
  // Constructors
  public _CorbaProviderImplBase ()
  {
  }      
  public String[] _ids ()
  {
	return __ids;
  }      
  public org.omg.CORBA.portable.OutputStream _invoke (String method,
								org.omg.CORBA.portable.InputStream in,
								org.omg.CORBA.portable.ResponseHandler rh)
  {
	org.omg.CORBA.portable.OutputStream out = null;
	java.lang.Integer __method = (java.lang.Integer)_methods.get (method);
	if (__method == null)
	  throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

	switch (__method.intValue ())
	{
	   case 0:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/addListener
	   {
		 net.sourceforge.gjtapi.raw.remote.corba.CorbaListener cl = net.sourceforge.gjtapi.raw.remote.corba.CorbaListenerHelper.read (in);
		 this.addListener (cl);
		 out = rh.createReply();
		 break;
	   }

	   case 1:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/allocateMedia
	   {
		 String term = in.read_string ();
		 int type = in.read_long ();
		 net.sourceforge.gjtapi.raw.remote.corba.LongEntry parameters[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		 boolean __result = false;
		 __result = this.allocateMedia (term, type, parameters);
		 out = rh.createReply();
		 out.write_boolean (__result);
		 break;
	   }

	   case 2:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/answerCall
	   {
		 try {
		   int callId = in.read_long ();
		   String address = in.read_string ();
		   String terminal = in.read_string ();
		   this.answerCall (callId, address, terminal);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.RawStateEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.RawStateExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 3:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/attachMedia
	   {
		 int call = in.read_long ();
		 String address = in.read_string ();
		 boolean onFlag = in.read_boolean ();
		 boolean __result = false;
		 __result = this.attachMedia (call, address, onFlag);
		 out = rh.createReply();
		 out.write_boolean (__result);
		 break;
	   }

	   case 4:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/beep
	   {
		 int call = in.read_long ();
		 this.beep (call);
		 out = rh.createReply();
		 break;
	   }

	   case 5:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/createCall
	   {
		 try {
		   int callId = in.read_long ();
		   String address = in.read_string ();
		   String terminal = in.read_string ();
		   String destination = in.read_string ();
		   int __result = (int)0;
		   __result = this.createCall (callId, address, terminal, destination);
		   out = rh.createReply();
		   out.write_long (__result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.RawStateEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.RawStateExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.InvalidPartyEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.InvalidPartyExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 6:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/freeMedia
	   {
		 String terminal = in.read_string ();
		 int type = in.read_long ();
		 boolean __result = false;
		 __result = this.freeMedia (terminal, type);
		 out = rh.createReply();
		 out.write_boolean (__result);
		 break;
	   }

	   case 7:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getAddresses
	   {
		 try {
		   String __result[] = null;
		   __result = this.getAddresses ();
		   out = rh.createReply();
		   net.sourceforge.gjtapi.raw.remote.corba.StringArrayHelper.write (out, __result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 8:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getAddressesForTerm
	   {
		 try {
		   String terminal = in.read_string ();
		   String __result[] = null;
		   __result = this.getAddressesForTerm (terminal);
		   out = rh.createReply();
		   net.sourceforge.gjtapi.raw.remote.corba.StringArrayHelper.write (out, __result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 9:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getAddressType
	   {
		 String name = in.read_string ();
		 int __result = -1;
		 __result = this.getAddressType (name);
		 out = rh.createReply();
		 out.write_long(__result);
		 break;
	   }

	   case 10:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getCall
	   {
		 int callId = in.read_long ();
		 net.sourceforge.gjtapi.raw.remote.corba.CallData __result = null;
		 __result = this.getCall (callId);
		 out = rh.createReply();
		 net.sourceforge.gjtapi.raw.remote.corba.CallDataHelper.write (out, __result);
		 break;
	   }

	   case 11:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getCallsOnAddress
	   {
		 String address = in.read_string ();
		 net.sourceforge.gjtapi.raw.remote.corba.CallData __result[] = null;
		 __result = this.getCallsOnAddress (address);
		 out = rh.createReply();
		 net.sourceforge.gjtapi.raw.remote.corba.CallArrayHelper.write (out, __result);
		 break;
	   }

	   case 12:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getCallsOnTerminal
	   {
		 String terminal = in.read_string ();
		 net.sourceforge.gjtapi.raw.remote.corba.CallData __result[] = null;
		 __result = this.getCallsOnTerminal (terminal);
		 out = rh.createReply();
		 net.sourceforge.gjtapi.raw.remote.corba.CallArrayHelper.write (out, __result);
		 break;
	   }

	   case 13:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getCapabilities
	   {
		 net.sourceforge.gjtapi.raw.remote.corba.StringEntry __result[] = null;
		 __result = this.getCapabilities ();
		 out = rh.createReply();
		 net.sourceforge.gjtapi.raw.remote.corba.StringDictionaryHelper.write (out, __result);
		 break;
	   }

	   case 14:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getDialledDigits
	   {
		 int id = in.read_long ();
		 String address = in.read_string ();
		 String __result = null;
		 __result = this.getDialledDigits (id, address);
		 out = rh.createReply();
		 out.write_string (__result);
		 break;
	   }

	   case 15:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getPrivateData
	   {
		 try {
		   int callId = in.read_long ();
		   String address = in.read_string ();
		   String terminal = in.read_string ();
		   org.omg.CORBA.Any __result = null;
		   __result = this.getPrivateData (callId, address, terminal);
		   out = rh.createReply();
		   out.write_any (__result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.NotSerializableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.NotSerializableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 16:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getTerminals
	   {
		 try {
		   net.sourceforge.gjtapi.raw.remote.corba.TermData __result[] = null;
		   __result = this.getTerminals ();
		   out = rh.createReply();
		   net.sourceforge.gjtapi.raw.remote.corba.TermArrayHelper.write (out, __result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 17:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/getTerminalsForAddr
	   {
		 try {
		   String terminal = in.read_string ();
		   net.sourceforge.gjtapi.raw.remote.corba.TermData __result[] = null;
		   __result = this.getTerminalsForAddr (terminal);
		   out = rh.createReply();
		   net.sourceforge.gjtapi.raw.remote.corba.TermArrayHelper.write (out, __result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 18:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/hold
	   {
		 try {
		   int callId = in.read_long ();
		   String address = in.read_string ();
		   String terminal = in.read_string ();
		   this.hold (callId, address, terminal);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.RawStateEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.RawStateExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 19:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/initialize
	   {
		 try {
		   net.sourceforge.gjtapi.raw.remote.corba.StringEntry props[] = net.sourceforge.gjtapi.raw.remote.corba.StringDictionaryHelper.read (in);
		   this.initialize (props);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ProviderUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ProviderUnavailableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 20:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/isMediaTerminal
	   {
		 String terminal = in.read_string ();
		 boolean __result = false;
		 __result = this.isMediaTerminal (terminal);
		 out = rh.createReply();
		 out.write_boolean (__result);
		 break;
	   }

	   case 21:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/join
	   {
		 try {
		   int callId1 = in.read_long ();
		   int callId2 = in.read_long ();
		   String address = in.read_string ();
		   String terminal = in.read_string ();
		   int __result = (int)0;
		   __result = this.join (callId1, callId2, address, terminal);
		   out = rh.createReply();
		   out.write_long (__result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.RawStateEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.RawStateExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 22:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/play
	   {
		 try {
		   String terminal = in.read_string ();
		   String streamIds[] = net.sourceforge.gjtapi.raw.remote.corba.StringArrayHelper.read (in);
		   int offset = in.read_long ();
		   net.sourceforge.gjtapi.raw.remote.corba.LongEntry rtcs[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		   net.sourceforge.gjtapi.raw.remote.corba.LongEntry optArgs[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		   this.play (terminal, streamIds, offset, rtcs, optArgs);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MediaResourceEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MediaResourceExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 23:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/record
	   {
		 try {
		   String terminal = in.read_string ();
		   String streamId = in.read_string ();
		   net.sourceforge.gjtapi.raw.remote.corba.LongEntry rtcs[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		   net.sourceforge.gjtapi.raw.remote.corba.LongEntry optArgs[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		   this.record (terminal, streamId, rtcs, optArgs);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MediaResourceEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MediaResourceExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 24:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/release
	   {
		 try {
		   String address = in.read_string ();
		   int callId = in.read_long ();
		   this.release (address, callId);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.RawStateEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.RawStateExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 25:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/releaseCallId
	   {
		 int callId = in.read_long ();
		 this.releaseCallId (callId);
		 out = rh.createReply();
		 break;
	   }

	   case 26:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/removeListener
	   {
		 net.sourceforge.gjtapi.raw.remote.corba.CorbaListener cl = net.sourceforge.gjtapi.raw.remote.corba.CorbaListenerHelper.read (in);
		 this.removeListener (cl);
		 out = rh.createReply();
		 break;
	   }

	   case 27:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/reportCallsOnAddress
	   {
		 try {
		   String address = in.read_string ();
		   boolean flag = in.read_boolean ();
		   this.reportCallsOnAddress (address, flag);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 28:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/reportCallsOnTerminal
	   {
		 try {
		   String terminal = in.read_string ();
		   boolean flag = in.read_boolean ();
		   this.reportCallsOnTerminal (terminal, flag);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 29:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/reserveCallId
	   {
		 try {
		   String address = in.read_string ();
		   int __result = (int)0;
		   __result = this.reserveCallId (address);
		   out = rh.createReply();
		   out.write_long (__result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 30:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/retrieveSignals
	   {
		 try {
		   String terminal = in.read_string ();
		   int num = in.read_long ();
		   int patterns[] = net.sourceforge.gjtapi.raw.remote.corba.LongArrayHelper.read (in);
		   net.sourceforge.gjtapi.raw.remote.corba.LongEntry rtcs[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		   net.sourceforge.gjtapi.raw.remote.corba.LongEntry optArgs[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		   net.sourceforge.gjtapi.raw.remote.corba.DetectEvent __result = null;
		   __result = this.retrieveSignals (terminal, num, patterns, rtcs, optArgs);
		   out = rh.createReply();
		   net.sourceforge.gjtapi.raw.remote.corba.DetectEventHelper.write (out, __result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MediaResourceEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MediaResourceExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 31:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/sendPrivateData
	   {
		 try {
		   int callId = in.read_long ();
		   String address = in.read_string ();
		   String terminal = in.read_string ();
		   org.omg.CORBA.Any data = in.read_any ();
		   org.omg.CORBA.Any __result = null;
		   __result = this.sendPrivateData (callId, address, terminal, data);
		   out = rh.createReply();
		   out.write_any (__result);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.NotSerializableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.NotSerializableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 32:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/sendSignals
	   {
		 try {
		   String terminal = in.read_string ();
		   int syms[] = net.sourceforge.gjtapi.raw.remote.corba.LongArrayHelper.read (in);
		   net.sourceforge.gjtapi.raw.remote.corba.LongEntry rtcs[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		   net.sourceforge.gjtapi.raw.remote.corba.LongEntry optArgs[] = net.sourceforge.gjtapi.raw.remote.corba.LongDictionaryHelper.read (in);
		   this.sendSignals (terminal, syms, rtcs, optArgs);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MediaResourceEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MediaResourceExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 33:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/setLoadControl
	   {
		 try {
		   String startAddr = in.read_string ();
		   String endAddr = in.read_string ();
		   double duration = in.read_double ();
		   double admissionRate = in.read_double ();
		   double interval = in.read_double ();
		   int treatment[] = net.sourceforge.gjtapi.raw.remote.corba.LongArrayHelper.read (in);
		   this.setLoadControl (startAddr, endAddr, duration, admissionRate, interval, treatment);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedExHelper.write (out, __ex);
		 }
		 break;
	   }

	   case 34:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/setPrivateData
	   {
		 int callId = in.read_long ();
		 String address = in.read_string ();
		 String terminal = in.read_string ();
		 org.omg.CORBA.Any data = in.read_any ();
		 this.setPrivateData (callId, address, terminal, data);
		 out = rh.createReply();
		 break;
	   }

	   case 35:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/shutdown
	   {
		 this.shutdown ();
		 out = rh.createReply();
		 break;
	   }

	   case 36:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/stop
	   {
		 String terminal = in.read_string ();
		 this.stop (terminal);
		 out = rh.createReply();
		 break;
	   }

	   case 37:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/stopReportingCall
	   {
		 int callId = in.read_long ();
		 boolean __result = false;
		 __result = this.stopReportingCall (callId);
		 out = rh.createReply();
		 out.write_boolean (__result);
		 break;
	   }

	   case 38:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/triggerRTC
	   {
		 String terminal = in.read_string ();
		 int action = in.read_long ();
		 this.triggerRTC (terminal, action);
		 out = rh.createReply();
		 break;
	   }

	   case 39:  // com/uforce/jtapi/generic/raw/remote/corba/CorbaProvider/unHold
	   {
		 try {
		   int callId = in.read_long ();
		   String address = in.read_string ();
		   String term = in.read_string ();
		   this.unHold (callId, address, term);
		   out = rh.createReply();
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.RawStateEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.RawStateExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationExHelper.write (out, __ex);
		 } catch (net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx __ex) {
		   out = rh.createExceptionReply ();
		   net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableExHelper.write (out, __ex);
		 }
		 break;
	   }

	   default:
		 throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	return out;
  } // _invoke          
} // class _CorbaProviderImplBase