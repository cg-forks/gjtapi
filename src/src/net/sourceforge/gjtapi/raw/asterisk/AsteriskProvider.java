package net.sourceforge.gjtapi.raw.asterisk;

import net.sourceforge.gjtapi.raw.*;
import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.capabilities.Capabilities;
import net.sf.asterisk.manager.*;
import net.sf.asterisk.manager.event.*;
import net.sf.asterisk.manager.response.*;
import net.sf.asterisk.manager.response.CommandResponse;
import net.sf.asterisk.manager.action.*;

import javax.telephony.*;
import java.util.*;
import java.io.*;

/**
 *
 * a simple Asteriskprovider
 * @author J. Boesl, 20.06.2005
 */


public class AsteriskProvider implements BasicJtapiTpi, CCTpi
{
  private DefaultAsteriskManager asteriskManager;
  private DefaultManagerConnection managerConnection;
  private Set listeners;
  private Set calls;
  private String[] addresses;
  private TermData[] terminals;
  private boolean gJtapiSetsStateOnItsOwnOnCallCreate;
  private List addressePrefixes;
  private String conferenceContext;
  private int conferenceCounter = 0;
  private String context;

  private int TIMEOUT = 10000;

  private boolean OUT = true;

  public AsteriskProvider()
  {
	  super();
    listeners = new HashSet();
    calls = new HashSet();
	}

  public void addListener(TelephonyListener ro)
  {
    listeners.add(ro);
  }

  public void removeListener(TelephonyListener ro)
  {
    listeners.remove(ro);
  }

  /**
   * seems as this is not supported by asterisk
   */
  public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
      MethodNotSupportedException, RawStateException
  {
    throw new MethodNotSupportedException("answering calls is not supported by asterisk");
  }

  public CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException,
      InvalidPartyException, InvalidArgumentException, RawStateException,
      MethodNotSupportedException
  {
    final OriginateAction originateAction;
    originateAction = new OriginateAction();
    originateAction.setChannel(address);
    originateAction.setContext(context);
    originateAction.setCallerId(getAsteriskCallerIdForAddress(address));
    originateAction.setAsync(Boolean.TRUE);
    originateAction.setExten(dest);

    originateAction.setPriority(new Integer(1));
    originateAction.setTimeout(new Long(TIMEOUT));

    try
    {
      ManagerResponse response = managerConnection.sendAction(originateAction);
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      _o(response.toString());
      String uniqueId = response.getUniqueId();
      Channel chan = getChannelForUniqueId(uniqueId);

      if(gJtapiSetsStateOnItsOwnOnCallCreate)
      {
        String channel = chan.getName();
        AsteriskConnectionDetail detail = new AsteriskConnectionDetail
          (uniqueId, channel, address, dest);
        ((AsteriskCallId)id).addConnection(response.getUniqueId(), detail);
        _o("set the ID: " + response.getUniqueId() + "\t\t" + response.getResponse());
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
    }
    return null;
  }


  public void hold(CallId call, String address, String terminal) throws RawStateException, MethodNotSupportedException,
      PrivilegeViolationException, ResourceUnavailableException
  {
    throw new MethodNotSupportedException("holding calls is not supported by asterisk provider");
  }


  // todo NOT WORKING, YET!
  public CallId join(CallId call1, CallId call2, String address, String terminal) throws RawStateException, InvalidArgumentException,
      MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException
  {
    if(!(call1 instanceof AsteriskCallId) || !(call2 instanceof AsteriskCallId))
      throw new InvalidArgumentException("at least one callId isn't an AsteriskCallId");
    _o("join called for: " + call1 + " and " + call2);
    AsteriskCallId aCId1 = (AsteriskCallId)call1;
    AsteriskCallId aCId2 = (AsteriskCallId)call2;

    AsteriskConnectionDetail aCDi1 = getCallingDetail(aCId1);
    AsteriskConnectionDetail aCDi2 = getCallingDetail(aCId2);

    String myCallerId = "";
    if(aCDi1.address.equals(aCDi2.address) &&
        isOneOfMyAddresses(getAddressForAsteriskCallerId(aCDi1.address)))
      myCallerId = aCDi1.address;
    else if(aCDi1.address.equals(aCDi2.calledAddress) &&
        isOneOfMyAddresses(getAddressForAsteriskCallerId(aCDi1.address)))
      myCallerId = aCDi1.address;
    else if(aCDi1.calledAddress.equals(aCDi2.address) &&
        isOneOfMyAddresses(getAddressForAsteriskCallerId(aCDi1.calledAddress)))
      myCallerId = aCDi1.calledAddress;
    else if(aCDi1.calledAddress.equals(aCDi2.calledAddress) &&
        isOneOfMyAddresses(getAddressForAsteriskCallerId(aCDi1.calledAddress)))
      myCallerId = aCDi1.calledAddress;

    String myExt = myCallerId + ++conferenceCounter%100;
    _o("myCallerId: " + myCallerId);
    AsteriskConnectionDetail myDetail1 = getDetailForCallerId(aCId1, myCallerId);
    AsteriskConnectionDetail myDetail2 = getDetailForCallerId(aCId2, myCallerId);
    _o(myDetail1 + "  " + myDetail2);
    AsteriskConnectionDetail[] otherDetails1 = getDetailsForNotCallerId(aCId1, myCallerId);
    AsteriskConnectionDetail[] otherDetails2 = getDetailsForNotCallerId(aCId2, myCallerId);
    AsteriskCallId aCallId = new AsteriskCallId();

    try
    {


      for(int i = 0; i < otherDetails1.length; i++)
      {
        RedirectAction redAct = new RedirectAction(otherDetails1[i].channel, conferenceContext, myExt, new Integer(1));
        ManagerResponse resp = managerConnection.sendAction(redAct);
        _o("second response " + i + " " + resp.getResponse() + "   " + resp.getMessage());
        aCallId.addConnection(otherDetails1[i].uniqueId, otherDetails1[i]);
      }
      for(int i = 0; i < otherDetails2.length; i++)
      {
        RedirectAction redAct = new RedirectAction(otherDetails2[i].channel, conferenceContext, myExt, new Integer(1));
        ManagerResponse resp = managerConnection.sendAction(redAct);
        aCallId.addConnection(otherDetails2[i].uniqueId, otherDetails2[i]);
        _o("third response " + i + " " + resp.getResponse() + "   " + resp.getMessage());
      }

      RedirectAction redirAction1 = new RedirectAction(myDetail1.channel, conferenceContext, myExt, new Integer(1));
      //RedirectAction redirAction1 = new RedirectAction(myDetail1.channel, "from-internal", "270", new Integer(1));
      ManagerResponse response = managerConnection.sendAction(redirAction1);
      aCallId.addConnection(myDetail1.uniqueId, myDetail1);
      _o("first response " + response.getResponse() + "   " + response.getMessage());
      /*for(Iterator i = asteriskManager.getChannels().values().iterator(); i.hasNext(); )
        _o(i.next().toString());*/
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
    }


    return aCallId;
  }

  public void unHold(CallId call, String address, String terminal) throws RawStateException, MethodNotSupportedException,
      PrivilegeViolationException, ResourceUnavailableException
  {
    throw new MethodNotSupportedException("unholding calls is not supported by asterisk provider");
  }


  public void release(String address, CallId call) throws PrivilegeViolationException,
      ResourceUnavailableException, MethodNotSupportedException, RawStateException
  {
    if(calls.contains(call))
    {
      String channel2Hangup = null;
      for(Iterator i = ((AsteriskCallId)call).getConnections().keySet().iterator(); i.hasNext(); )
      {
        AsteriskConnectionDetail detail = getDetail((AsteriskCallId)call, i.next().toString());
        if(detail != null && detail.channel != null)
        {
          String channelAddress = getAddressForAsteriskCallerId(detail.address);
          if(address != null && address.equals(channelAddress))
          {
            channel2Hangup = detail.channel;
            ((AsteriskCallId)call).removeConnection(detail.uniqueId);
            break;
          }
        }
      }
      if(channel2Hangup != null)
      {
        HangupAction hangupAction = new HangupAction();
        hangupAction.setChannel(channel2Hangup);
        try
        {
          managerConnection.sendAction(hangupAction);
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
        catch (TimeoutException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  public Properties getCapabilities()
  {
    Properties capabilities = new Properties();
		capabilities.put(Capabilities.THROTTLE, "f");
		capabilities.put(Capabilities.MEDIA, "f");
		capabilities.put(Capabilities.ALL_MEDIA_TERMINALS, "f");
		capabilities.put(Capabilities.ALLOCATE_MEDIA, "f");
		return capabilities;
  }

  public void releaseCallId(CallId id)
  {
    calls.remove(id);
  }

  public CallId reserveCallId(String address) throws InvalidArgumentException
  {
    //getAddresses(address);
    CallId call = new AsteriskCallId();
    calls.add(call);
    return call;
  }

  public void shutdown()
  {
    try
    {
      managerConnection.logoff();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * lists only sip-addresses
   * @return
   * @throws ResourceUnavailableException
   */
  public String[] getAddresses() throws ResourceUnavailableException
  {
    if(addresses == null)
    {
      CommandAction command = new CommandAction();
      command.setCommand("sip show users");

      CommandResponse cr = null;
      try
      {
        cr = (CommandResponse)managerConnection.sendAction(command);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      catch (TimeoutException e)
      {
        e.printStackTrace();
      }
      Object[] result = cr.getResult().toArray();
      addresses = new String[result.length-1];
      for(int i = 1; i < result.length; i++)
        addresses[i-1] = "SIP/" + result[i].toString().split(" ")[0];
    }
    return addresses;
  }

  public String[] getAddresses(String terminal) throws InvalidArgumentException
  {
    try
    {
      for(int i = 0; i < getAddresses().length; i++)
      {
        if(getAddresses()[i].equals(terminal))
          return new String[]{terminal};
      }
    }
    catch (ResourceUnavailableException e)
    {
    }
    throw new InvalidArgumentException("no terminal " + terminal);
  }

  public TermData[] getTerminals() throws ResourceUnavailableException
  {
    if(terminals == null)
    {
      terminals = new TermData[getAddresses().length];
      for(int i = 0; i < getAddresses().length; i++)
      {
        terminals[i] = new TermData(getAddresses()[i], false);
      }
    }
    return terminals;
  }

  public TermData[] getTerminals(String address) throws InvalidArgumentException
  {
    return new TermData[]{new TermData(getAddresses(address)[0], false)};
  }


  public void initialize(Map props) throws ProviderUnavailableException
  {
    managerConnection = new DefaultManagerConnection();
    asteriskManager = new DefaultAsteriskManager();

    Object server = props.get("Server");
    Object port = props.get("Port");
    Object user = props.get("User");
    Object password = props.get("Password");
    Object context = props.get("Context");
    Object phone = props.get("Phone");
    Object gJtapiSetsState = props.get("GJtapiSetsState");
    Object confCont = props.get("ConferenceExtension");
    //Object reconWait = props.get("ReconnectWaitTime");

    managerConnection.setHostname(server==null? null: server.toString());
    managerConnection.setPort(Integer.parseInt(port==null? "5038": port.toString()));
    managerConnection.setUsername(user==null? null: user.toString());
    managerConnection.setPassword(password==null? null: password.toString());
    this.context = context==null? null: context.toString();
    this.addresses = phone==null? null: new String[]{phone.toString()};
    gJtapiSetsStateOnItsOwnOnCallCreate = gJtapiSetsState==null? true: Boolean.valueOf(gJtapiSetsState.toString()).booleanValue();
    conferenceContext = confCont==null? null: confCont.toString();
    //reconWaitTime = reconWait==null? 7500: Integer.parseInt(reconWait.toString());

    asteriskManager.setManagerConnection(managerConnection);

    try
    {
      asteriskManager.initialize();
    }
    catch (IOException e)
    {
      throw new ProviderUnavailableException(e.toString());
    }
    catch (AuthenticationFailedException e)
    {
      throw new ProviderUnavailableException(e.toString());
    }
    catch (TimeoutException e)
    {
      throw new ProviderUnavailableException(e.toString());
    }
    managerConnection.addEventHandler(getManagerEventHandler());
  }


  /**
   * these are the events got by AsteriskJava
   */
  private ManagerEventHandler getManagerEventHandler()
  {
    return new ManagerEventHandler()
    {
      public void handleEvent(ManagerEvent event)
      {
        try
        {
          if (event.getClass().equals(AgentCallbackLoginEvent.class))
          {
            _o("AgentCallbackLoginEvent");
          }
          else if (event.getClass().equals(AgentCallbackLogoffEvent.class))
          {
            _o("AgentCallbackLogoffEvent");
          }
          else if (event.getClass().equals(AgentCalledEvent.class))
          {
            _o("AgentCalledEvent");
          }
          else if (event.getClass().equals(AgentLoginEvent.class))
          {
            _o("AgentLoginEvent");
          }
          else if (event.getClass().equals(AgentLogoffEvent.class))
          {
            _o("AgentLogoffEvent");
          }
          else if (event.getClass().equals(AlarmClearEvent.class))
          {
            _o("AlarmClearEvent");
          }
          else if (event.getClass().equals(AlarmEvent.class))
          {
            _o("AlarmEvent");
          }
          else if (event.getClass().equals(CdrEvent.class))
          {
            _o("CdrEvent");
          }
          else if (event.getClass().equals(ChannelEvent.class))
          {
            _o("ChannelEvent");
          }
          else if (event.getClass().equals(ConnectEvent.class))
          {
            _o("ConnectEvent");
          }
          else if (event.getClass().equals(DialEvent.class))
          {
            _o("DialEvent");
            DialEvent ev = (DialEvent)event;

            AsteriskCallId call = getCallIdForUniqueId(ev.getSrcUniqueId());
            if(call != null)
            {
              AsteriskConnectionDetail callerDetail = getDetail(call, ev.getSrcUniqueId());
              if(callerDetail.calledAddress != null)
              {
                getChannelForUniqueId(ev.getDestUniqueId()).setCallerId(callerDetail.calledAddress);
                AsteriskConnectionDetail aDetail = new AsteriskConnectionDetail
                (
                    ev.getDestUniqueId(),
                    getChannelForUniqueId(ev.getDestUniqueId()).toString(),
                    callerDetail.calledAddress,
                    null
                );
                call.addConnection(ev.getDestUniqueId(), aDetail);

                String callingAddress = getAddressForAsteriskCallerId(callerDetail.address);
                String calledAddress = callerDetail.calledAddress;
                if(!isOneOfMyAddresses(callingAddress))
                  calledAddress = getAddressForAsteriskCallerId(calledAddress);
                _o("called " + calledAddress + "\t\tcalling " + callingAddress);

                fireTelephonyEvent(AstTeleEventTypes.callActive, call, null, Event.CAUSE_NORMAL);
                if(isOneOfMyAddresses(callingAddress))
                {
                  fireTelephonyEvent(AstTeleEventTypes.connectionAlerting, call, calledAddress, Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionCreated, call, callingAddress, Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.connectionInProgress, call, callingAddress, Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.connectionConnected, call, calledAddress, Event.CAUSE_NORMAL);
                }
                else
                {
                  fireTelephonyEvent(AstTeleEventTypes.connectionInProgress, call, callingAddress, Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.connectionAlerting, call, calledAddress, Event.CAUSE_NORMAL);
                  //fireTelephonyEvent(AstTeleEventTypes.terminalConnectionCreated, call, calledAddress, Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.connectionConnected, call, callingAddress, Event.CAUSE_NORMAL);
                }
              }

              _o(ev.getCallerId() + "\t\t" + ev.getCallerIdName() + "\t\t" + ev.getDestination() + "\t\t" +
                ev.getDestUniqueId() + "\t\t" + ev.getSrc() + "\t\t" + ev.getSrcUniqueId());
              _o(getChannelForUniqueId(ev.getDestUniqueId()).toString());
              _o(getChannelForUniqueId(ev.getSrcUniqueId()).toString());
            }

          }
          else if (event.getClass().equals(DisconnectEvent.class))
          {
            _o("DisconnectEvent");
          }
          else if (event.getClass().equals(HangupEvent.class))
          {
            _o("HangupEvent");
            HangupEvent ev = (HangupEvent)event;
            AsteriskCallId call;
            call = getCallIdForUniqueId(ev.getUniqueId());
            String address = null;
            if (call != null)
            {
              AsteriskConnectionDetail aDetail = getDetail(call, ev.getUniqueId());
              if(isOneOfMyAddresses(getAddressForAsteriskCallerId(aDetail.address)))
                address = getAddressForAsteriskCallerId(aDetail.address);
              else if((isOneOfMyAddresses(getAddressForAsteriskCallerId(aDetail.calledAddress))))
                address = getAddressForAsteriskCallerId(aDetail.calledAddress);
              call.removeConnection(ev.getUniqueId());
            }
            if(address != null)
            {
              fireTelephonyEvent(AstTeleEventTypes.terminalConnectionDropped, call, address, Event.CAUSE_NORMAL);
              fireTelephonyEvent(AstTeleEventTypes.connectionDisconnected, call, address, Event.CAUSE_NORMAL);
              calls.remove(call);
              _o("call " + call.toString() + " removed!");
            }
          }
          else if (event.getClass().equals(HoldedCallEvent.class))
          {
            _o("HoldedCallEvent");
          }
          else if (event.getClass().equals(JoinEvent.class))
          {
            _o("JoinEvent");
          }
          else if (event.getClass().equals(LeaveEvent.class))
          {
            _o("LeaveEvent");
          }
          else if (event.getClass().equals(LinkageEvent.class))
          {
            _o("LinkageEvent");
          }
          else if (event.getClass().equals(LinkEvent.class))
          {
            _o("LinkEvent");
          }
          else if(event .getClass().equals(ManagerEvent.class))
          {
            _o("ManagerEvent");
          }
          else if (event.getClass().equals(MeetMeEvent.class))
          {
            _o("MeetMeEvent");
          }
          else if (event.getClass().equals(MeetMeJoinEvent.class))
          {
            _o("MeetMeJoinEvent");
          }
          else if (event.getClass().equals(MeetMeLeaveEvent.class))
          {
            _o("MeetMeLeaveEvent");
          }
          else if (event.getClass().equals(MessageWaitingEvent.class))
          {
            _o("MessageWaitingEvent");
          }
          else if (event.getClass().equals(NewCallerIdEvent.class))
          {
            _o("NewCallerIdEvent");
          }
          else if (event.getClass().equals(NewChannelEvent.class))
          {
            _o("NewChannelEvent");
            NewChannelEvent ev = (NewChannelEvent) event;

            if (ev.getState().equals("Ringing"))
            {
              _o("!Ringing");
              _o(getChannelForUniqueId(ev.getUniqueId()).toString());

              AsteriskCallId call = getCallIdForUniqueId(ev.getUniqueId());
              if(call != null)
              {
                AsteriskConnectionDetail callerDetail = getCallingDetail(call);
                fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, call,
                    getAddressForAsteriskCallerId(callerDetail.address), Event.CAUSE_NORMAL);
                fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, call,
                    getAddressForAsteriskCallerId(callerDetail.calledAddress), Event.CAUSE_NORMAL);
              }
            }
          }
          else if (event.getClass().equals(NewExtenEvent.class))
          {
            _o("NewExtenEvent");
            NewExtenEvent ev = (NewExtenEvent) event;

            if (ev.getApplication().equals("Dial"))
            {
              _o("!Dial");
              _o(ev.getAppData() + "\t\t" + ev.getApplication() + "\t\t" + ev.getChannel() + "\t\t" + ev.getExtension());
              Channel chan = getChannelForUniqueId(ev.getUniqueId());
              _o(chan.toString());

              String callingAddress = chan.getCallerId();
              String calledAddress = ev.getAppData();
              int pipeIndex = (calledAddress.indexOf('|'));
              if(pipeIndex != -1)
                calledAddress = calledAddress.substring(0, pipeIndex);
              pipeIndex = calledAddress.lastIndexOf('/');
              if(pipeIndex != -1)
                calledAddress = calledAddress.substring(pipeIndex+1);

              AsteriskCallId call = getCallIdForUniqueId(ev.getUniqueId());
              if(call == null)
              {
                if(!isOneOfMyAddresses(getAddressForAsteriskCallerId(calledAddress)) &!
                    isOneOfMyAddresses(getAddressForAsteriskCallerId(callingAddress)))
                  return ;

                call = new AsteriskCallId();
                AsteriskConnectionDetail detail = new AsteriskConnectionDetail
                (
                  ev.getUniqueId(), ev.getChannel(), callingAddress, calledAddress
                );
                call.addConnection(ev.getUniqueId(), detail);
                calls.add(call);
              }
              else
              {
                AsteriskConnectionDetail aDetail = getDetail(call, ev.getUniqueId());
                if(aDetail != null)
                  aDetail.calledAddress = calledAddress;
              }
              _o("Calling: " + callingAddress + "\t\tCalled: " + calledAddress);
            }
          }
          else if (event.getClass().equals(NewStateEvent.class))
          {
            _o("NewStateEvent");
            NewStateEvent ev = (NewStateEvent) event;

            if (ev.getState().equals("Up"))
            {
              _o("!Up");
              _o(ev.getCallerId() + "\t\t" + ev.getCallerIdName() + "\t\t" + ev.getChannel());
              _o(getChannelForUniqueId(ev.getUniqueId()).toString());

              AsteriskCallId call = getCallIdForUniqueId(ev.getUniqueId());
              if(call!= null)
              {
                AsteriskConnectionDetail detail = null;
                detail = getCallingDetail(call);
                if(detail == null)
                  detail = getDetail(call, ev.getUniqueId());
                String address = getAddressForAsteriskCallerId(detail.address);
                String destAddress = getAddressForAsteriskCallerId(detail.calledAddress);
                if(destAddress != null)
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionTalking, call, destAddress, Event.CAUSE_NORMAL);
                if(address != null)
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionTalking, call, address, Event.CAUSE_NORMAL);
              }
            }
            else if (ev.getState().equals("Ringing"))
            {
              _o("!Ringing");
              _o(getChannelForUniqueId(ev.getUniqueId()).toString());

              AsteriskCallId call = getCallIdForUniqueId(ev.getUniqueId());
              if(call != null)
              {
                AsteriskConnectionDetail callerDetail = getCallingDetail(call);
                if(callerDetail != null)
                {
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, call,
                      getAddressForAsteriskCallerId(callerDetail.address), Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, call,
                      getAddressForAsteriskCallerId(callerDetail.calledAddress), Event.CAUSE_NORMAL);
                }
              }
            }
          }
          else if (event.getClass().equals(OriginateEvent.class))
          {
            _o("OriginateEvent");
          }
          else if (event.getClass().equals(OriginateFailureEvent.class))
          {
            _o("OriginateFailureEvent");
          }
          else if (event.getClass().equals(OriginateSuccessEvent.class))
          {
            _o("OriginateSuccessEvent");
          }
          else if (event.getClass().equals(ParkedCallEvent.class))
          {
            _o("ParkedCallEvent");
          }
          else if (event.getClass().equals(ParkedCallsCompleteEvent.class))
          {
            _o("ParkedCallsCompleteEvent");
          }
          else if (event.getClass().equals(PeerStatusEvent.class))
          {
            _o("PeerStatusEvent");
          }
          else if (event.getClass().equals(QueueEntryEvent.class))
          {
            _o("QueueEntryEvent");
          }
          else if (event.getClass().equals(QueueEvent.class))
          {
            _o("QueueEvent");
          }
          else if (event.getClass().equals(QueueMemberEvent.class))
          {
            _o("QueueMemberEvent");
          }
          else if (event.getClass().equals(QueueMemberStatusEvent.class))
          {
            _o("QueueMemberStatusEvent");
          }
          else if (event.getClass().equals(QueueParamsEvent.class))
          {
            _o("QueueParamsEvent");
          }
          else if (event.getClass().equals(RegistryEvent.class))
          {
            _o("RegistryEvent");
          }
          else if (event.getClass().equals(ReloadEvent.class))
          {
            _o("ReloadEvent");
          }
          else if (event.getClass().equals(RenameEvent.class))
          {
            _o("RenameEvent");
          }
          else if (event.getClass().equals(ResponseEvent.class))
          {
            _o("ResponseEvent");
          }
          else if (event.getClass().equals(ShutdownEvent.class))
          {
            _o("ShutdownEvent");
          }
          else if (event.getClass().equals(StatusCompleteEvent.class))
          {
            _o("StatusCompleteEvent");
          }
          else if (event.getClass().equals(StatusEvent.class))
          {
            _o("StatusEvent");
          }
          else if (event.getClass().equals(UnlinkEvent.class))
          {
            _o("UnlinkEvent");
          }
          else if (event.getClass().equals(UserEvent.class))
          {
            _o("UserEvent");
          }
          else if (event.getClass().equals(ZapShowChannelsCompleteEvent.class))
          {
            _o("ZapShowChannelsCompleteEvent");
          }
          else if (event.getClass().equals(ZapShowChannelsEvent.class))
          {
            _o("ZapShowChannelsEvent");
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    };
  }

  /**
   * fires the asterisk events to the GJtapi-Framework
   * @param eventType defined in AstTeleEventTypes
   * @param call the CallId the event might need
   * @param address defines the address and the terminal for the event
   * @param cause the cause
   */
  protected void fireTelephonyEvent(int eventType, CallId call, String address, int cause)
	{
    for(Iterator i = listeners.iterator(); i.hasNext(); )
    {
      TelephonyListener tL = (TelephonyListener)i.next();

      switch (eventType)
      {
        /*case AstTeleEventTypes.addressPrivateData:
        tL.addressPrivateData(String address, Serializable data, int cause);
          break;*/
        case AstTeleEventTypes.callActive:
          tL.callActive(call, cause);
          break;
        case AstTeleEventTypes.callInvalid:
          tL.callInvalid(call, cause);
          break;
        /*case AstTeleEventTypes.callOverloadCeased:
          tL.callOverloadCeased(String address);callOverloadEncountered(String address);
          break;
        case AstTeleEventTypes.callPrivateData:
          tL.callPrivateData(CallId call, Serializable data, int cause);
          break;
        case AstTeleEventTypes.connectionAddressAnalyse:
          tL.connectionAddressAnalyse(CallId id, String address, int cause);
          break;
        case AstTeleEventTypes.connectionAddressCollect:
          tL.connectionAddressCollect(CallId id, String address, int cause);
          break;*/
        case AstTeleEventTypes.connectionAlerting:
          tL.connectionAlerting(call, address, cause);
          break;
       /* case AstTeleEventTypes.connectionAuthorizeCallAttempt:
          tL.connectionAuthorizeCallAttempt(CallId id, String address, int cause);
          break;
        case AstTeleEventTypes.connectionCallDelivery:
          tL.connectionCallDelivery(CallId id, String address, int cause);
          break;*/
        case AstTeleEventTypes.connectionConnected:
          tL.connectionConnected(call, address, cause);
          break;
        case AstTeleEventTypes.connectionDisconnected:
          tL.connectionDisconnected(call, address, cause);
          break;
        case AstTeleEventTypes.connectionFailed:
          tL.connectionFailed(call, address, cause);
          break;
        case AstTeleEventTypes.connectionInProgress:
          tL.connectionInProgress(call, address, cause);
          break;
        /*case AstTeleEventTypes.connectionSuspended:
          //tL.connectionSuspended(CallId id, String address, int cause);
          break;
        case AstTeleEventTypes.mediaPlayPause:
          //tL.mediaPlayPause(String terminal, int index, int offset, Symbol trigger);
          break;
        case AstTeleEventTypes.mediaPlayResume:
          //tL.mediaPlayResume(String terminal, Symbol trigger);
          break;
        case AstTeleEventTypes.mediaRecorderPause:
          //tL.mediaRecorderPause(String terminal, int duration, Symbol trigger);
          break;
        case AstTeleEventTypes.mediaRecorderResume:
          //tL.mediaRecorderResume(String terminal, Symbol trigger);
          break;
        case AstTeleEventTypes.mediaSignalDetectorDetected:
          //tL.mediaSignalDetectorDetected(String terminal, Symbol[] sigs);
          break;
        case AstTeleEventTypes.mediaSignalDetectorOverflow:
          //tL.mediaSignalDetectorOverflow(String terminal, Symbol[] sigs);
          break;
        case AstTeleEventTypes.mediaSignalDetectorPatternMatched:
          //tL.mediaSignalDetectorPatternMatched(String terminal, Symbol[] sigs, int index);
          break;
        case AstTeleEventTypes.providerPrivateData:
          //tL.providerPrivateData(Serializable data, int cause);
          break; */
        case AstTeleEventTypes.terminalConnectionCreated:
          tL.terminalConnectionCreated(call, address, address, cause);
          break;
        case AstTeleEventTypes.terminalConnectionDropped:
          tL.terminalConnectionDropped(call, address, address, cause);
          break;
        case AstTeleEventTypes.terminalConnectionHeld:
          tL.terminalConnectionHeld(call, address, address, cause);
          break;
        case AstTeleEventTypes.terminalConnectionRinging:
          tL.terminalConnectionRinging(call, address, address, cause);
          break;
        case AstTeleEventTypes.terminalConnectionTalking:
          tL.terminalConnectionTalking(call, address, address, cause);
          break;
        /*case AstTeleEventTypes.terminalPrivateData:
          tL.terminalPrivateData(String terminal, Serializable data, int cause);*/
        default:
          break;
      }
    }
	}

  /**
   * returns a 'connection' alias 'channel' from an existing call
   * @param call
   * @param uniqueId
   * @return
   *//*
  private Channel getChannel(AsteriskCallId call, String uniqueId)
  {
    if(call!= null && call.getConnections().containsKey(uniqueId))
      return getChannelForUniqueId(uniqueId);
    return null;
  }*/

  /**
   * returns the Channel for that uniqueId regardless of a CallId
   * @param uniqueId
   * @return
   */
  private Channel getChannelForUniqueId(String uniqueId)
  {
    return (Channel)asteriskManager.getChannels().get(uniqueId);
  }

  /**
   * Searches all callIds for this uniqueId.
   * @param uniqueId
   * @return the callId when found, null otherwise
   */
  private AsteriskCallId getCallIdForUniqueId(String uniqueId)
  {
    for(Iterator i = calls.iterator(); i.hasNext(); )
    {
      AsteriskCallId next = (AsteriskCallId)i.next();
      if(next.getConnections().containsKey(uniqueId))
        return next;
    }
    return null;
  }

  /**
   * return the fitting address for the uniqueId
   * @param call
   * @param uniqueId
   * @return
   *//*
  private String getAddress(AsteriskCallId call, String uniqueId)
  {
    Object detail = call.getConnections().get(uniqueId);
    if(detail!=null)
      return ((AsteriskConnectionDetail)detail).address;
    return null;
  }*/

  /**
   * I don't have another solution for this, yet. Works well for sip-connections.
   * @param chan
   * @return
   */
  /*private String getAddressForChannel(String chan)
  {
    if(chan != null)
    {
      if(chan.startsWith("SIP/"))
        return chan.substring(0, chan.indexOf("-"));
      return chan;
    }
    return null;
  }*/

  /**
   * a simple debugging method
   * @param out is put to System.out
   */
  private void _o(String out)
  {
    if(OUT)
      System.out.println(out);
  }

  private AsteriskConnectionDetail getDetail(AsteriskCallId call, String uniqueId)
  {
    return (AsteriskConnectionDetail)call.getConnections().get(uniqueId);
  }


  private AsteriskConnectionDetail getCallingDetail(AsteriskCallId call)
  {
    Collection values =  call.getConnections().values();
    for(Iterator i = values.iterator(); i.hasNext(); )
    {
      AsteriskConnectionDetail aDetail = (AsteriskConnectionDetail)i.next();
      //if(callerId.equals(aDetail.calledAddress))
      if(aDetail.address != null && aDetail.calledAddress != null)
        return aDetail;
    }
    return null;
  }

  /*// not safe
  private String getUniqueIdThroughCallerId(AsteriskCallId call, String callerId)
  {
    for( Iterator i= call.getConnections().entrySet().iterator(); i.hasNext(); )
    {
      AsteriskConnectionDetail detail = (AsteriskConnectionDetail)((Map.Entry) i.next()).getValue();
      if(detail.address.equals(callerId))
        return detail.uniqueId;
    }
    return null;
  }*/


  private String getAddressForAsteriskCallerId(String callerId)
  {
    if(addressePrefixes == null)
    {
      addressePrefixes = new ArrayList();
      addressePrefixes.add("");
      for(int i = 0; i < addresses.length; i++)
      {
        String[] splitted = addresses[i].split("/");
        if(splitted.length > 1)
          if(!addressePrefixes.contains(splitted[0]+"/"))
            addressePrefixes.add(splitted[0]+"/");
      }
    }
    for(int i = 0; i < addresses.length; i++)
    {
      for(Iterator j = addressePrefixes.iterator(); j.hasNext(); )
      {
        if(addresses[i].equals(j.next().toString() + callerId))
          return addresses[i];
      }
      /*if(addresses[i].equals("SIP/" + callerId) || addresses[i].equals("ZAP/" + callerId) ||
          addresses[i].equals("IAX/" + callerId) || addresses[i].equals("IAX2/" + callerId) ||
          addresses[i].equals(callerId))
        return addresses[i];*/
    }
    return callerId;
  }


  private String getAsteriskCallerIdForAddress(String address)
  {
    String[] addrSplit = address.split("/");
      if(addrSplit.length > 1)
        return addrSplit[1];
    return address;
  }

  private boolean isOneOfMyAddresses(String address)
  {
    for(int i = 0; i < addresses.length; i++)
    {
      if(addresses[i].equals(address))
        return true;
    }
    return false;
  }


  private AsteriskConnectionDetail getDetailForCallerId(AsteriskCallId call, String callerId)
  {
    Collection values = call.getConnections().values();
    for(Iterator i = values.iterator(); i.hasNext(); )
    {
      AsteriskConnectionDetail aDetail = (AsteriskConnectionDetail)i.next();
      if(aDetail.address.equals(callerId))
        return aDetail;
    }
    return null;
  }

  private AsteriskConnectionDetail[] getDetailsForNotCallerId(AsteriskCallId call, String callerId)
  {
    List aList = new ArrayList();
    Collection values = call.getConnections().values();
    for(Iterator i = values.iterator(); i.hasNext(); )
    {
      AsteriskConnectionDetail aDetail = (AsteriskConnectionDetail)i.next();
      if(!aDetail.address.equals(callerId))
        aList.add(aDetail);
    }
    AsteriskConnectionDetail[] arr = new AsteriskConnectionDetail[aList.size()];
    aList.toArray(arr);
    return arr;
  }
}
