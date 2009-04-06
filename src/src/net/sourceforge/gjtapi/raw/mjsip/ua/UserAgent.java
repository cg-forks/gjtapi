package net.sourceforge.gjtapi.raw.mjsip.ua;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import local.media.AudioClipPlayer;
import local.ua.MediaLauncher;
import local.ua.VICLauncher;

import org.zoolu.sdp.AttributeField;
import org.zoolu.sdp.ConnectionField;
import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sdp.TimeField;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.CallListenerAdapter;
import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.call.SdpTools;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;
import org.zoolu.tools.Parser;


/** Simple SIP user agent (UA).
  * It includes audio/video applications.
  * <p>
  * It can use external audio/video tools as media applications.
  * Currently only RAT (Robust Audio Tool) and VIC are supported as external applications.
  */
public class UserAgent extends CallListenerAdapter {
    /** Logger instance. */
    private static final Logger LOGGER =
        Logger.getLogger(UserAgent.class.getName());

   /** Event logger. */
   Log log;

   /** UserAgentProfile */
   protected UserAgentProfile user_profile;

   /** SipProvider */
   protected SipProvider sip_provider;

   /** Call */
   //Call call;
   protected ExtendedCall call;

   /** Call transfer */
   protected ExtendedCall call_transfer;

   /** Audio application */
   protected MediaLauncher audio_app=null;
   /** Video application */
   protected MediaLauncher video_app=null;

   /** Local sdp */
   protected String local_session=null;

   /** UserAgent listener */
   protected UserAgentListener listener=null;

   /** Media file path */
   final String MEDIA_PATH="media/local/ua/";

   /** On wav file */
   final String CLIP_ON=MEDIA_PATH+"on.wav";
   /** Off wav file */
   final String CLIP_OFF=MEDIA_PATH+"off.wav";
   /** Ring wav file */
   final String CLIP_RING=MEDIA_PATH+"ring.wav";

   /** Ring sound */
   AudioClipPlayer clip_ring;
   /** On sound */
   AudioClipPlayer clip_on;
   /** Off sound */
   AudioClipPlayer clip_off;


   // *********************** Startup Configuration ***********************

   /** UA_IDLE=0 */
   static final String UA_IDLE="IDLE";
   /** UA_INCOMING_CALL=1 */
   static final String UA_INCOMING_CALL="INCOMING_CALL";
   /** UA_OUTGOING_CALL=2 */
   static final String UA_OUTGOING_CALL="OUTGOING_CALL";
   /** UA_ONCALL=3 */
   static final String UA_ONCALL="ONCALL";

   /** Call state
     * <P>UA_IDLE=0, <BR>UA_INCOMING_CALL=1, <BR>UA_OUTGOING_CALL=2, <BR>UA_ONCALL=3 */
   String call_state=UA_IDLE;



   // *************************** Basic methods ***************************

   /** Changes the call state */
   protected void changeStatus(String state)
   {  call_state=state;
      //printLog("state: "+call_state,LogLevel.MEDIUM);
   }

   /** Checks the call state */
   protected boolean statusIs(String state)
   {  return call_state.equals(state);
   }

   /** Gets the call state */
   protected String getStatus()
   {  return call_state;
   }

   /** Sets the automatic answer time (default is -1 that means no auto accept mode) */
   public void setAcceptTime(int accept_time)
   {  user_profile.accept_time=accept_time;
   }

   /** Sets the automatic hangup time (default is 0, that corresponds to manual hangup mode) */
   public void setHangupTime(int time)
   {  user_profile.hangup_time=time;
   }

   /** Sets the redirection url (default is null, that is no redircetion) */
   public void setRedirection(String url)
   {  user_profile.redirect_to=url;
   }

   /** Sets the no offer mode for the invite (default is false) */
   public void setNoOfferMode(boolean nooffer)
   {  user_profile.no_offer=nooffer;
   }

   /** Enables audio */
   public void setAudio(boolean enable)
   {  user_profile.audio=enable;
   }

   /** Enables video */
   public void setVideo(boolean enable)
   {  user_profile.video=enable;
   }

   /** Sets the receive only mode */
   public void setReceiveOnlyMode(boolean r_only)
   {  user_profile.recv_only=r_only;
   }

   /** Sets the send only mode */
   public void setSendOnlyMode(boolean s_only)
   {  user_profile.send_only=s_only;
   }

   /** Sets the send tone mode */
   public void setSendToneMode(boolean s_tone)
   {  user_profile.send_tone=s_tone;
   }

   /** Sets the send file */
   public void setSendFile(String file_name)
   {  user_profile.send_file=file_name;
   }

   /** Sets the recv file */
   public void setRecvFile(String file_name)
   {  user_profile.recv_file=file_name;
   }

   /** Sets the send stream */
   public void setSendStream(InputStream stream)
   {  user_profile.send_Stream=stream;
   }

   /** Sets the recv stream */
   public void setRecvStream(OutputStream stream)
   {  user_profile.recv_Stream=stream;
   }


   /** Gets the local SDP */
   public String getSessionDescriptor()
   {  return local_session;
   }

   /** Sets the local SDP */
   public void setSessionDescriptor(String sdp)
   {  local_session=sdp;
   }

   /** Inits the local SDP (no media spec) */
   public void initSessionDescriptor()
   {  SessionDescriptor sdp=new SessionDescriptor(user_profile.from_url,sip_provider.getViaAddress());
      local_session=sdp.toString();
   }

   /** Adds a media to the SDP */
   public void addMediaDescriptor(String media, int port, int avp, String codec, int rate)
   {  if (local_session==null) initSessionDescriptor();
      SessionDescriptor sdp=new SessionDescriptor(local_session);
      String attr_param=String.valueOf(avp);
      if (codec!=null) attr_param+=" "+codec+"/"+rate;
      sdp.addMedia(new MediaField(media,port,0,"RTP/AVP",String.valueOf(avp)),new AttributeField("rtpmap",attr_param));
      local_session=sdp.toString();
   }


   // *************************** Public Methods **************************

   /** Costructs a UA with a default media port */
   public UserAgent(SipProvider sip_provider, UserAgentProfile user_profile, UserAgentListener listener)
   {  this.sip_provider=sip_provider;
      log=sip_provider.getLog();
      this.listener=listener;
      this.user_profile=user_profile;
      // if no contact_url and/or from_url has been set, create it now
      user_profile.initContactAddress(sip_provider);

      // load sounds

      // ################# patch to make audio working with javax.sound.. #################
      // currently AudioSender must be started before any AudioClipPlayer is initialized,
      // since there is a problem with the definition of the audio format
       //TODO Causa erro ao ser executado num PC sem placa de som
      if (!user_profile.use_rat && !user_profile.use_jmf)
      {  if (user_profile.audio && !user_profile.recv_only && user_profile.send_file==null && !user_profile.send_tone) local.media.AudioInput.initAudioLine();
         if (user_profile.audio && !user_profile.send_only && user_profile.recv_file==null) local.media.AudioOutput.initAudioLine();
      }

      // ################# patch to make rat working.. #################
      // in case of rat, do not load and play audio clips
      if (!user_profile.use_rat)
      {  try
         {  String jar_file=user_profile.ua_jar;
          //  clip_on=new AudioClipPlayer(Archive.getAudioInputStream(Archive.getJarURL(jar_file,CLIP_ON)),null);
          //  clip_off=new AudioClipPlayer(Archive.getAudioInputStream(Archive.getJarURL(jar_file,CLIP_OFF)),null);
          //  clip_ring=new AudioClipPlayer(Archive.getAudioInputStream(Archive.getJarURL(jar_file,CLIP_RING)),null);
         }
         catch (Exception e)
         {  printException(e,LogLevel.HIGH);
         }
         //clip_ring=new AudioClipPlayer(CLIP_RING,null);
         //clip_on=new AudioClipPlayer(CLIP_ON,null);
         //clip_off=new AudioClipPlayer(CLIP_OFF,null);
      }

      // set local sdp
      initSessionDescriptor();
      if (user_profile.audio || !user_profile.video) addMediaDescriptor("audio",user_profile.audio_port,user_profile.audio_avp,user_profile.audio_codec,user_profile.audio_sample_rate);
      if (user_profile.video) addMediaDescriptor("video",user_profile.video_port,user_profile.video_avp,null,0);
   }


   /** Creates a new session descriptor */
   /*private void newSession(int media_port)
   {  SessionDescriptor local_sdp=new SessionDescriptor(user_profile.from_url,sip_provider.getAddress());
      int audio_port=media_port;
      int video_port=media_port+2;
      //PATCH [040902] if (audio || !video) local_sdp.addMedia(new MediaField("audio",audio_port,0,"RTP/AVP","0"),new AttributeField("rtpmap","0 PCMU/8000"));
      //PATCH [040902] if (video || !(audio || video)) local_sdp.addMedia(new MediaField("video",video_port,0,"RTP/AVP","7"),new AttributeField("rtpmap","17"));
      local_sdp.addMedia(new MediaField("audio",audio_port,0,"RTP/AVP","0"),new AttributeField("rtpmap","0 PCMU/8000"));
      local_session=local_sdp.toString();
   }*/


   /** Makes a new call (acting as UAC). */
   public void call(String target_url) {
      changeStatus(UA_OUTGOING_CALL);
      call = new ExtendedCall(sip_provider, user_profile.from_url,
              user_profile.contact_url, user_profile.username,
              user_profile.realm, user_profile.passwd, this);
      // in case of incomplete url (e.g. only 'user' is present), try to complete it
      target_url=sip_provider.completeNameAddress(target_url).toString();
      if (user_profile.no_offer) {
          call.call(target_url);
      } else {
          call.call(target_url,local_session);
      }
   }


   /** Waits for an incoming call (acting as UAS). */
   public void listen()
   {  changeStatus(UA_IDLE);
      call=new ExtendedCall(sip_provider,user_profile.from_url,user_profile.contact_url,user_profile.username,user_profile.realm,user_profile.passwd,this);
      call.listen();
   }


   /** Closes an ongoing, incoming, or pending call */
   public void hangup()
   {  if (clip_ring!=null) clip_ring.stop();
      closeMediaApplication();
      if (call!=null) call.hangup();
      changeStatus(UA_IDLE);
   }


   /** Closes an ongoing, incoming, or pending call */
   public void accept()
   {  if (clip_ring!=null) clip_ring.stop();
      if (call!=null) call.accept(local_session);
   }


   /** Redirects an incoming call */
   public void redirect(String redirection)
   {  if (clip_ring!=null) clip_ring.stop();
      if (call!=null) call.redirect(redirection);
   }


   public void waitForEndOfMedia() {
       if (audio_app != null) {
           if (audio_app instanceof JAudioLauncher) {
               ((JAudioLauncher)audio_app).waitForEndOfMedia();
           }
       }
       else if (video_app != null) {
       }
   }

   public void waitForEndOfPlay() {
       if (audio_app != null) {
           if (audio_app instanceof JAudioLauncher) {
               ((JAudioLauncher)audio_app).waitForEndOfPlay();
           }
       }
   }

   public void waitForEndOfRecord() {
       if (audio_app != null) {
           if (audio_app instanceof JAudioLauncher) {
               ((JAudioLauncher)audio_app).waitForEndOfRecord();
           }
       }
   }


   /** Launches the Media Application (currently, the RAT audio tool) */
   protected void launchMediaApplication()
   {
      // exit if the Media Application is already running
      if (audio_app!=null || video_app!=null) {
         LOGGER.info("DEBUG: media application is already running");
         return;
      }
      SessionDescriptor local_sdp=new SessionDescriptor(call.getLocalSessionDescriptor());
      String local_media_address=(new Parser(local_sdp.getConnection().toString())).skipString().skipString().getString();
      int local_audio_port=0;
      int local_video_port=0;
      // parse local sdp
      for (Enumeration e=local_sdp.getMediaDescriptors().elements(); e.hasMoreElements(); )
      {  MediaField media=((MediaDescriptor)e.nextElement()).getMedia();
         if (media.getMedia().equals("audio"))
            local_audio_port=media.getPort();
         if (media.getMedia().equals("video"))
            local_video_port=media.getPort();
      }
      // parse remote sdp
      SessionDescriptor remote_sdp=new SessionDescriptor(call.getRemoteSessionDescriptor());
      String remote_media_address=(new Parser(remote_sdp.getConnection().toString())).skipString().skipString().getString();
      int remote_audio_port=0;
      int remote_video_port=0;
      for (Enumeration e=remote_sdp.getMediaDescriptors().elements(); e.hasMoreElements(); )
      {  MediaField media=((MediaDescriptor)e.nextElement()).getMedia();
         if (media.getMedia().equals("audio"))
            remote_audio_port=media.getPort();
         if (media.getMedia().equals("video"))
            remote_video_port=media.getPort();
      }

      // select the media direction (send_only, recv_ony, fullduplex)
      int dir=0;
      if (user_profile.recv_only) dir=-1;
      else
      if (user_profile.send_only) dir=1;

      if (user_profile.audio && local_audio_port!=0 && remote_audio_port!=0){  // create an audio_app and start it
         if (audio_app==null && (user_profile.send_Stream==null && user_profile.recv_Stream==null))
         {  // for testing..
            String audio_in=null;
            if (user_profile.send_tone) audio_in=JAudioLauncher.TONE;
            else if (user_profile.send_file!=null) audio_in=user_profile.send_file;
            String audio_out=null;
            //if (user_profile.recv_file!=null) audio_out=user_profile.recv_file;
            //audio_app=new JAudioLauncher(local_audio_port,remote_media_address,remote_audio_port,dir,log);
            //audio_app=new JAudioLauncher(local_audio_port,remote_media_address,remote_audio_port,dir,audio_in,audio_out,user_profile.audio_sample_rate,user_profile.audio_sample_size,user_profile.audio_frame_size,log);
         }
         else{
           audio_app=new JAudioLauncher(local_audio_port,remote_media_address,remote_audio_port,dir,user_profile.send_Stream,user_profile.recv_Stream,user_profile.audio_sample_rate,user_profile.audio_sample_size,user_profile.audio_frame_size,log);
         }
         audio_app.startMedia();
      }
      if (user_profile.video && local_video_port!=0 && remote_video_port!=0)
      {  // create a video_app and start it
         if (user_profile.use_vic)
         {  video_app=new VICLauncher(user_profile.bin_vic,local_video_port,remote_media_address,remote_video_port,log);
         }
         else
         if (user_profile.use_jmf)
         {  // try to use JMF video app
            try
            {  Class myclass=Class.forName("local.ua.JMFVideoLauncher");
               Class[] parameter_types={ java.lang.Integer.TYPE, Class.forName("java.lang.String"), java.lang.Integer.TYPE, java.lang.Integer.TYPE, Class.forName("org.zoolu.tools.Log") };
               Object[] parameters={ new Integer(local_video_port), remote_media_address, new Integer(remote_video_port), new Integer(dir), log };
               java.lang.reflect.Constructor constructor=myclass.getConstructor(parameter_types);
               video_app=(MediaLauncher)constructor.newInstance(parameters);
            }
            catch (Exception e) {
               printException(e,LogLevel.HIGH);
               LOGGER.warning("Error trying to create the JMFVideoLauncher "
                       + e.getMessage());
            }
         }
         // else
         if (video_app==null) {
            LOGGER.info("No external video application nor JMF has been provided: Video not started");
            return;
         }
         video_app.startMedia();
      }
   }


   /** Close the Media Application  */
   protected void closeMediaApplication()
   {  if (audio_app!=null)
      {  audio_app.stopMedia();
         audio_app=null;
      }
      if (video_app!=null)
      {  video_app.stopMedia();
         video_app=null;
      }
   }


   // ********************** Call callback functions **********************

   /** Callback function called when arriving a new INVITE method (incoming call) */
   public void onCallIncoming(Call call, NameAddress callee,
            NameAddress caller, String sdp, Message invite) {
        LOGGER.fine("onCallIncoming()");
        if (call != this.call) {
            LOGGER.fine("NOT the current call");
            return;
        }
        LOGGER.info("INCOMING");
        //System.out.println("DEBUG: inside UserAgent.onCallIncoming(): sdp=\n"+
        // sdp);
        changeStatus(UA_INCOMING_CALL);
        call.ring();
        if (sdp != null) { // Create the new SDP
            SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
            SessionDescriptor local_sdp = new SessionDescriptor(local_session);
            SessionDescriptor new_sdp = new SessionDescriptor(remote_sdp
                    .getOrigin(), remote_sdp.getSessionName(), local_sdp
                    .getConnection(), local_sdp.getTime());
            new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
            new_sdp = SdpTools.sdpMediaProduct(new_sdp, remote_sdp
                    .getMediaDescriptors());
            new_sdp = SdpTools.sdpAttirbuteSelection(new_sdp, "rtpmap");
            local_session = new_sdp.toString();
        }
        // play "ring" sound
        if (clip_ring != null) {
            clip_ring.loop();
        }
        if (listener != null) {
            listener.onUaCallIncoming(this, callee, caller);
        }
    }


   /** Callback function called when arriving a new Re-INVITE method (re-inviting/call modify) */
   public void onCallModifying(Call call, String sdp, Message invite) {
      LOGGER.fine("onCallModifying()");
      if (call!=this.call) {  
          LOGGER.fine("NOT the current call");
          return;  
      }
      LOGGER.info("RE-INVITE/MODIFY");
      // to be implemented.
      // currently it simply accepts the session changes (see method onCallModifying() in CallListenerAdapter)
      super.onCallModifying(call,sdp,invite); //original
   }


   /** Callback function that may be overloaded (extended). Called when arriving a 180 Ringing */
   public void onCallRinging(Call call, Message resp) {
      LOGGER.fine("onCallRinging()");
      if (call!=this.call && call!=call_transfer) {  
          LOGGER.fine("NOT the current call"); 
          return;
      }
      LOGGER.info("RINGING");
      // play "on" sound
      if (clip_on!=null) {
          clip_on.replay();
      }
      if (listener!=null) {
          listener.onUaCallRinging(this);
      }
   }


   /** Callback function called when arriving a 2xx (call accepted) */
   public void onCallAccepted(Call call, String sdp, Message resp) {
      LOGGER.fine("onCallAccepted()");
      if (call!=this.call && call!=call_transfer) {  
          LOGGER.fine("NOT the current call");  
          return;  
      }
      LOGGER.info("ACCEPTED/CALL");
      changeStatus(UA_ONCALL);
      if (user_profile.no_offer)
      {  // Create the new SDP
         SessionDescriptor remote_sdp=new SessionDescriptor(sdp);
         SessionDescriptor local_sdp=new SessionDescriptor(local_session);
         SessionDescriptor new_sdp=new SessionDescriptor(remote_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),local_sdp.getTime());
         new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
         new_sdp=SdpTools.sdpMediaProduct(new_sdp,remote_sdp.getMediaDescriptors());
         new_sdp=SdpTools.sdpAttirbuteSelection(new_sdp,"rtpmap");


         // update the local SDP
         local_session=new_sdp.toString();
         // answer with the local sdp
         call.ackWithAnswer(local_session);
      }
      // play "on" sound
      if (clip_on!=null) clip_on.replay();
      if (listener!=null) listener.onUaCallAccepted(this);

      //TODO
      launchMediaApplication();

      if (call==call_transfer)
      {  StatusLine status_line=resp.getStatusLine();
         int code=status_line.getCode();
         String reason=status_line.getReason();
         this.call.notify(code,reason);
      }
   }


   /** Callback function called when arriving an ACK method (call confirmed) */
   public void onCallConfirmed(Call call, String sdp, Message ack) {
      LOGGER.fine("onCallConfirmed()");
      if (call!=this.call) {  
          LOGGER.fine("NOT the current call");
          return;
      }
      LOGGER.info("CONFIRMED/CALL");
      changeStatus(UA_ONCALL);
      // play "on" sound
      if (clip_on!=null) clip_on.replay();
      if (listener!=null) listener.onUaCallAccepted(this);
      //TODO
      launchMediaApplication();
      if (user_profile.hangup_time>0) this.automaticHangup(user_profile.hangup_time);
   }


   /** Callback function called when arriving a 2xx (re-invite/modify accepted) */
   public void onCallReInviteAccepted(Call call, String sdp, Message resp) {
      LOGGER.fine("onCallReInviteAccepted()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");  
          return;
      }
      LOGGER.info("RE-INVITE-ACCEPTED/CALL");
   }


   /** Callback function called when arriving a 4xx (re-invite/modify failure) */
   public void onCallReInviteRefused(Call call, String reason, Message resp) {
      LOGGER.fine("onCallReInviteRefused()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");
          return;  
      }
      LOGGER.info("RE-INVITE-REFUSED ("+reason+")/CALL");
      if (listener!=null) {
          listener.onUaCallFailed(this);
      }
   }


   /** Callback function called when arriving a 4xx (call failure) */
   public void onCallRefused(Call call, String reason, Message resp) {
      LOGGER.fine("onCallRefused()");
      if (call!=this.call) { 
          LOGGER.fine("NOT the current call");
          return;  
      }
      LOGGER.info("REFUSED ("+reason+")");
      changeStatus(UA_IDLE);
      if (call==call_transfer)
      {  StatusLine status_line=resp.getStatusLine();
         int code=status_line.getCode();
         //String reason=status_line.getReason();
         this.call.notify(code,reason);
         call_transfer=null;
      }
      // play "off" sound
      if (clip_off!=null) clip_off.replay();
      if (listener!=null) listener.onUaCallFailed(this);
   }


   /** Callback function called when arriving a 3xx (call redirection) */
   public void onCallRedirection(Call call, String reason, Vector contact_list, Message resp) {
      LOGGER.fine("onCallRedirection()");
      if (call!=this.call) {  
          LOGGER.fine("NOT the current call");  
          return;  
      }
      LOGGER.info("REDIRECTION ("+reason+")");
      call.call(((String)contact_list.elementAt(0)));
   }


   /** Callback function that may be overloaded (extended). Called when arriving a CANCEL request */
   public void onCallCanceling(Call call, Message cancel) {
      LOGGER.fine("onCallCanceling()");
      if (call!=this.call) { 
          LOGGER.fine("NOT the current call");  
          return;  
      }
      LOGGER.info("CANCEL");
      changeStatus(UA_IDLE);
      // stop ringing
      if (clip_ring!=null) clip_ring.stop();
      // play "off" sound
      if (clip_off!=null) clip_off.replay();
      if (listener!=null) listener.onUaCallCancelled(this);
   }


   /** Callback function called when arriving a BYE request */
   public void onCallClosing(Call call, Message bye) {
      LOGGER.fine("onCallClosing()");
      if (call!=this.call && call!=call_transfer) {
          LOGGER.fine("NOT the current call");  
          return;  
      }
      if (call!=call_transfer && call_transfer!=null) {
         LOGGER.info("CLOSE PREVIOUS CALL");
         this.call=call_transfer;
         call_transfer=null;
         return;
      }
      // else
      LOGGER.info("CLOSE");
      closeMediaApplication();
      // play "off" sound
      if (clip_off!=null) clip_off.replay();
      if (listener!=null) listener.onUaCallClosed(this);
      changeStatus(UA_IDLE);
   }


   /** Callback function called when arriving a response after a BYE request (call closed) */
   public void onCallClosed(Call call, Message resp) {
      LOGGER.fine("onCallClosed()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");
          return;  
      }
      LOGGER.info("CLOSE/OK");
      if (listener!=null) listener.onUaCallClosed(this);
      changeStatus(UA_IDLE);
   }

   /** Callback function called when the invite expires */
   public void onCallTimeout(Call call) {
      LOGGER.fine("onCallTimeout()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");
          return;
      }
      LOGGER.info("NOT FOUND/TIMEOUT");
      changeStatus(UA_IDLE);
      if (call==call_transfer)
      {  int code=408;
         String reason="Request Timeout";
         this.call.notify(code,reason);
         call_transfer=null;
      }
      // play "off" sound
      if (clip_off!=null) clip_off.replay();
      if (listener!=null) listener.onUaCallFailed(this);
   }



   // ****************** ExtendedCall callback functions ******************

   /** Callback function called when arriving a new REFER method (transfer request) */
   public void onCallTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, Message refer) {
      LOGGER.fine("onCallTransfer()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");
          return;
      }
      LOGGER.info("Transfer to "+refer_to.toString());
      call.acceptTransfer();
      call_transfer=new ExtendedCall(sip_provider,user_profile.from_url,user_profile.contact_url,this);
      call_transfer.call(refer_to.toString(),local_session);
   }

   /** Callback function called when a call transfer is accepted. */
   public void onCallTransferAccepted(ExtendedCall call, Message resp) {
      LOGGER.fine("onCallTransferAccepted()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");
          return;
      }
      LOGGER.info("Transfer accepted");
   }

   /** Callback function called when a call transfer is refused. */
   public void onCallTransferRefused(ExtendedCall call, String reason, Message resp) {
      LOGGER.fine("onCallTransferRefused()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");
          return;
      }
      LOGGER.info("Transfer refused");
   }

   /** Callback function called when a call transfer is successfully completed */
   public void onCallTransferSuccess(ExtendedCall call, Message notify) {
      LOGGER.fine("onCallTransferSuccess()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");
          return;
      }
      LOGGER.info("Transfer successed");
      call.hangup();
      if (listener!=null) listener.onUaCallTrasferred(this);
   }

   /** Callback function called when a call transfer is NOT sucessfully completed */
   public void onCallTransferFailure(ExtendedCall call, String reason, Message notify) {
      LOGGER.fine("onCallTransferFailure()");
      if (call!=this.call) {
          LOGGER.fine("NOT the current call");
          return;
      }
      LOGGER.info("Transfer failed");
   }


   // ************************* Schedule events ***********************

   /** Schedules a re-inviting event after <i>delay_time</i> secs. */
   void reInvite(final String contact_url, final int delay_time)
   {  SessionDescriptor sdp=new SessionDescriptor(local_session);
      final SessionDescriptor new_sdp=new SessionDescriptor(sdp.getOrigin(),sdp.getSessionName(),new ConnectionField("IP4","0.0.0.0"),new TimeField());
      new_sdp.addMediaDescriptors(sdp.getMediaDescriptors());
      (new Thread() {  public void run() {  runReInvite(contact_url,new_sdp.toString(),delay_time);  }  }).start();
   }

   /** Re-invite. */
   private void runReInvite(String contact, String body, int delay_time) {
      try {
         if (delay_time>0) {
             Thread.sleep(delay_time*1000);
         }
         LOGGER.info("RE-INVITING/MODIFING");
         if (call!=null && call.isOnCall()) {
            LOGGER.info("REFER/TRANSFER");
            call.modify(contact,body);
         }
      }
      catch (Exception e) { e.printStackTrace(); }
   }


   /** Schedules a call-transfer event after <i>delay_time</i> secs. */
   void callTransfer(final String transfer_to, final int delay_time)
   {  (new Thread() {  public void run() {  runCallTransfer(transfer_to,delay_time);  }  }).start();
   }

   /** Call-transfer. */
   private void runCallTransfer(String transfer_to, int delay_time)
   {  try
      {  if (delay_time>0) Thread.sleep(delay_time*1000);
         if (call!=null && call.isOnCall()) {
            LOGGER.info("REFER/TRANSFER");
            call.transfer(transfer_to);
         }
      }
      catch (Exception e) { e.printStackTrace(); }
   }


   /** Schedules an automatic answer event after <i>delay_time</i> secs. */
   void automaticAccept(final int delay_time)
   {  (new Thread() {  public void run() {  runAutomaticAccept(delay_time);  }  }).start();
   }

   /** Automatic answer. */
   private void runAutomaticAccept(int delay_time)
   {  try
      {  if (delay_time>0) Thread.sleep(delay_time*1000);
         if (call!=null) {
            LOGGER.info("AUTOMATIC-ANSWER");
            accept();
         }
      }
      catch (Exception e) { e.printStackTrace(); }
   }


   /** Schedules an automatic hangup event after <i>delay_time</i> secs. */
   void automaticHangup(final int delay_time)
   {  (new Thread() {  public void run() {  runAutomaticHangup(delay_time);  }  }).start();
   }

   /** Automatic hangup. */
   private void runAutomaticHangup(int delay_time)
   {  try
      {  if (delay_time>0) Thread.sleep(delay_time*1000);
         if (call!=null && call.isOnCall()) {
            LOGGER.info("AUTOMATIC-HANGUP");
            hangup();
            listen();
         }
      }
      catch (Exception e) { e.printStackTrace(); }
   }


   // ****************************** Logs *****************************

   /** Adds the Exception message to the default Log */
   void printException(Exception e,int level)
   {  if (log!=null) log.printException(e,level+SipStack.LOG_LEVEL_UA);
   }

}
