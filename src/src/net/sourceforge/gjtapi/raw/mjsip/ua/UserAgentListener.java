package net.sourceforge.gjtapi.raw.mjsip.ua;

import org.zoolu.sip.address.NameAddress;
import net.sourceforge.gjtapi.raw.mjsip.ua.UserAgent;

/** Listener of UserAgent */
public interface UserAgentListener
{
   /** When a new call is incoming */
   void onUaCallIncoming(UserAgent ua, NameAddress callee, NameAddress caller);

   /** When an incoming call is canceled */
   void onUaCallCancelled(UserAgent ua);

   /** When an outgoing call is remotely ringing */
   void onUaCallRinging(UserAgent ua);

   /** When an outgoing call has been accepted */
   void onUaCallAccepted(UserAgent ua);

   /** When a call has been transferred */
   void onUaCallTrasferred(UserAgent ua);

   /** When an outgoing call has been refused or timeout */
   void onUaCallFailed(UserAgent ua);

   /** When a call has been locally or remotely closed */
   void onUaCallClosed(UserAgent ua);

}
