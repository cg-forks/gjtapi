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
 * an AsteriskProvider
 * @author Johannes Boesl - ADITO Software GmbH, 20.06.2005
 */


public class AsteriskProvider implements BasicJtapiTpi, CCTpi
{
  private DefaultAsteriskManager asteriskManager;
  private DefaultManagerConnection managerConnection;
  private Set listeners;
  private Set callSet;
  private String[] addresses;
  private TermData[] terminals;
  private boolean gJtapiSetsStateOnItsOwnOnCallCreate;
  private List addressePrefixes;
  private String conferenceContext;
  private int conferenceCounter = 0;
  private String context;
  private Map confrenceMap;

  private int TIMEOUT = 10000;

  private boolean OUT = false;

  public AsteriskProvider()
  {
	  super();
    listeners = new HashSet();
    callSet = new HashSet();
    confrenceMap = new HashMap();
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
    throw new MethodNotSupportedException("answering callSet is not supported by asterisk");
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
        AsteriskConnectionDetail detail = new AsteriskConnectionDetail
          (uniqueId, chan, address, dest);
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
    throw new MethodNotSupportedException("holding callSet is not supported by asterisk provider");
  }


  public CallId join(CallId call1, CallId call2, String address, String terminal) throws RawStateException, InvalidArgumentException,
      MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException
  {
    if(!(call1 instanceof AsteriskCallId) || !(call2 instanceof AsteriskCallId))
      throw new InvalidArgumentException("at least one callId isn't an AsteriskCallId");
    _o("\t\tjoin called for: " + call1 + " and " + call2);
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


    _o("\t\tmyCallerId: " + myCallerId);
    AsteriskConnectionDetail myDetail1 = getDetailForCallerId(aCId1, myCallerId);
    AsteriskConnectionDetail myDetail2 = getDetailForCallerId(aCId2, myCallerId);
    AsteriskConnectionDetail[] otherDetails1 = getDetailsForNotCallerId(aCId1, myCallerId);
    AsteriskConnectionDetail[] otherDetails2 = getDetailsForNotCallerId(aCId2, myCallerId);


    String myExt = "";
    boolean newConf = false;
    if(aCId1.getConferenceRoom() != null)
      myExt = aCId1.getConferenceRoom();
    else if(aCId2.getConferenceRoom() != null)
      myExt = aCId2.getConferenceRoom();
    else
    {
      myExt = myCallerId + conferenceCounter++%100;
      newConf = true;
    }

    _o(getChannelForUniqueId(myDetail1.uniqueId).toString());

    try
    {
      for(int i = newConf?1:0; i < otherDetails1.length; i++)    // i = newConf?1:0 !
      {
        RedirectAction redAct = new RedirectAction(otherDetails1[i].channel.getName(),
            conferenceContext, myExt, new Integer(1));
        ManagerResponse resp = managerConnection.sendAction(redAct);
        _o("\t\tsecond response " + i + " " + resp.getResponse() + "   " + resp.getMessage());
      }

      {
        RedirectAction redAct = null;
        if(newConf)
          redAct = new RedirectAction(myDetail1.channel.getName(),
              otherDetails1[0].channel.getName(), conferenceContext, myExt, new Integer(1));
        else
          redAct = new RedirectAction(myDetail1.channel.getName(), conferenceContext, myExt, new Integer(1));
        ManagerResponse resp = managerConnection.sendAction(redAct);
        _o("\t\tfirst response " + resp.getResponse() + "   " + resp.getMessage());
      }

      for(int i = 0; i < otherDetails2.length; i++)
      {
        RedirectAction redAct = new RedirectAction(otherDetails2[i].channel.getName(),
            conferenceContext, myExt, new Integer(1));
        ManagerResponse resp = managerConnection.sendAction(redAct);
        _o("\t\tthird response " + i + " " + resp.getResponse() + "   " + resp.getMessage());
      }

      hangUpChannel(myDetail2.channel.getName());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
    }
    return aCId1;
  }

  public void unHold(CallId call, String address, String terminal) throws RawStateException, MethodNotSupportedException,
      PrivilegeViolationException, ResourceUnavailableException
  {
    throw new MethodNotSupportedException("unholding callSet is not supported by asterisk provider");
  }


  public void release(String address, CallId call) throws PrivilegeViolationException,
      ResourceUnavailableException, MethodNotSupportedException, RawStateException
  {
    _o("address: " + address + "\t\tcallid: " + call.toString());
    if(callSet.contains(call))
    {
      AsteriskCallId aCId = (AsteriskCallId)call;
      String channel2Hangup = null;
      _o("connections: " + aCId.getConnections().size() + "\t\tmyId: " + aCId.getCallId());
      for(Iterator i = aCId.getConnections().keySet().iterator(); i.hasNext(); )
      {
        AsteriskConnectionDetail detail = getDetail(aCId, i.next().toString());
        if(detail != null && detail.channel != null)
        {
          _o("release debug: " + detail.channel.toString());
          if(aCId.getCallId().equals(detail.address))
          {
            channel2Hangup = detail.channel.getName();
            aCId.removeConnection(detail.uniqueId);
            break;
          }
        }
      }
      if(channel2Hangup != null)
        hangUpChannel(channel2Hangup);
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
    callSet.remove(id);
  }

  public CallId reserveCallId(String address) throws InvalidArgumentException
  {
    CallId call = new AsteriskCallId(address);
    callSet.add(call);
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
    Object confCont = props.get("ConferenceContext");

    managerConnection.setHostname(server==null? null: server.toString());
    managerConnection.setPort(Integer.parseInt(port==null? "5038": port.toString()));
    managerConnection.setUsername(user==null? null: user.toString());
    managerConnection.setPassword(password==null? null: password.toString());
    this.context = context==null? null: context.toString();
    this.addresses = phone==null? null: new String[]{phone.toString()};
    gJtapiSetsStateOnItsOwnOnCallCreate = gJtapiSetsState==null? true: Boolean.valueOf(gJtapiSetsState.toString()).booleanValue();
    conferenceContext = confCont==null? null: confCont.toString();

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

            AsteriskCallId[] calls = getCallIdsForUniqueId(ev.getSrcUniqueId());
            if(calls.length != 0)
            {
              for(int i = 0; i < calls.length; i++)
              {
                AsteriskConnectionDetail callerDetail = getDetail(calls[i], ev.getSrcUniqueId());
                if(callerDetail.calledAddress != null)
                {
                  getChannelForUniqueId(ev.getDestUniqueId()).setCallerId(callerDetail.calledAddress);
                  AsteriskConnectionDetail aDetail = new AsteriskConnectionDetail
                  (
                      ev.getDestUniqueId(),
                      getChannelForUniqueId(ev.getDestUniqueId()),
                      callerDetail.calledAddress,
                      null
                  );
                  calls[i].addConnection(ev.getDestUniqueId(), aDetail);

                  String[] det = adjustCallingCalled(calls[i].getCallId(), callerDetail.address, callerDetail.calledAddress);
                  String callingAddress = det[0];
                  String calledAddress = det[1];

                  _o("called " + calledAddress + "\t\tcalling " + callingAddress + "\t\tthis one: " + calls[i].getCallId());

                  fireTelephonyEvent(AstTeleEventTypes.callActive, calls[i], null, Event.CAUSE_NORMAL);
                  if(getAddressForAsteriskCallerId(calls[i].getCallId()).equals(callingAddress))
                  {
                    fireTelephonyEvent(AstTeleEventTypes.connectionAlerting, calls[i], calledAddress, Event.CAUSE_NORMAL);
                    fireTelephonyEvent(AstTeleEventTypes.terminalConnectionCreated, calls[i], callingAddress, Event.CAUSE_NORMAL);
                    fireTelephonyEvent(AstTeleEventTypes.connectionInProgress, calls[i], callingAddress, Event.CAUSE_NORMAL);
                    fireTelephonyEvent(AstTeleEventTypes.connectionConnected, calls[i], calledAddress, Event.CAUSE_NORMAL);
                    fireTelephonyEvent(AstTeleEventTypes.terminalConnectionCreated, calls[i], calledAddress, Event.CAUSE_NORMAL);
                  }
                  else if(getAddressForAsteriskCallerId(calls[i].getCallId()).equals(calledAddress))
                  {
                    fireTelephonyEvent(AstTeleEventTypes.connectionInProgress, calls[i], callingAddress, Event.CAUSE_NORMAL);
                    fireTelephonyEvent(AstTeleEventTypes.connectionAlerting, calls[i], calledAddress, Event.CAUSE_NORMAL);
                    fireTelephonyEvent(AstTeleEventTypes.terminalConnectionCreated, calls[i], calledAddress, Event.CAUSE_NORMAL);
                    fireTelephonyEvent(AstTeleEventTypes.connectionConnected, calls[i], callingAddress, Event.CAUSE_NORMAL);
                    fireTelephonyEvent(AstTeleEventTypes.terminalConnectionCreated, calls[i], callingAddress, Event.CAUSE_NORMAL);
                  }
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
            _o(ev.getChannel() + "\t\t" + getChannelForUniqueId(ev.getUniqueId()) + "\t\t" + ev.getUniqueId());
            AsteriskCallId[] calls = getCallIdsForUniqueId(ev.getUniqueId());
            if (calls.length != 0)
            {
              for(int i = 0; i < calls.length; i++)
              {
                String address = null;
                AsteriskConnectionDetail aDetail = getDetail(calls[i], ev.getUniqueId());
                if(ev.getChannel().indexOf("<ZOMBIE>") == -1)
                {
                  if(calls[i].getCallId().equals(aDetail.address))
                    address = getAddressForAsteriskCallerId(aDetail.address);
                  calls[i].removeConnection(ev.getUniqueId());
                  _o("connection removed: " + ev.getChannel());
                  if(calls[i].getConnections().size() == 0)
                  {
                    callSet.remove(calls[i]);
                    _o("call " + calls[i].toString() + " removed!");
                  }
                }
                if(address != null)
                {
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionDropped, calls[i], address, Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.connectionDisconnected, calls[i], address, Event.CAUSE_NORMAL);
                }
              }
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
            MeetMeJoinEvent ev = (MeetMeJoinEvent)event;
            _o(ev.getMeetMe() + "\t\t" + ev.getUserNum() + "\t\t" + getChannelForUniqueId(ev.getUniqueId()).toString());
            Channel newChan = getChannelForUniqueId(ev.getUniqueId());

            List confList = (List)confrenceMap.get(ev.getMeetMe());
            if(confList == null)
            {
              confList = new ArrayList();
              confrenceMap.put(ev.getMeetMe(), confList);
            }
            for(Iterator it = callSet.iterator(); it.hasNext(); )
            {
              AsteriskCallId call = (AsteriskCallId)it.next();
              Set keySet = call.getConnections().keySet();
              for(Iterator it2 = keySet.iterator(); it2.hasNext(); )
              {
                AsteriskConnectionDetail aCDi = (AsteriskConnectionDetail)call.getConnections().get(it2.next());
                if(aCDi.channel.getName().indexOf(newChan.getName()) != -1)
                {
                  it2.remove();
                  _o("old chan: " + aCDi.channel);
                  aCDi.channel = newChan;
                  aCDi.uniqueId = ev.getUniqueId();
                  call.setConferenceRoom(ev.getMeetMe());
                  //put every conference join to conferenceMap to be able to put this call to other calls and vice versa
                  if(!confList.contains(aCDi))
                    confList.add(aCDi);
                }
              }
            }

            // don't know whether this is the last event -> do it each time
            for(Iterator it = callSet.iterator(); it.hasNext(); )
            {
              AsteriskCallId call = (AsteriskCallId)it.next();
              if(ev.getMeetMe().equals(call.getConferenceRoom()))
              {
                for(Iterator it2 = confList.iterator(); it2.hasNext(); )
                {
                  AsteriskConnectionDetail aCDi = (AsteriskConnectionDetail)it2.next();
                  call.getConnections().put(aCDi.uniqueId, aCDi);
                }
              }
            }
          }
          else if (event.getClass().equals(MeetMeLeaveEvent.class))
          {
            _o("MeetMeLeaveEvent");
            MeetMeLeaveEvent ev = (MeetMeLeaveEvent)event;
            _o(ev.getMeetMe() + "\t\t" + ev.getUserNum() + "\t\t" + getChannelForUniqueId(ev.getUniqueId()).toString());
            List confList = (List)confrenceMap.get(ev.getMeetMe());
            if(confList != null)
            {
              for(Iterator it = confList.iterator(); it.hasNext(); )
              {
                AsteriskConnectionDetail aCDi = (AsteriskConnectionDetail)it.next();
                if(aCDi.uniqueId.equals(ev.getUniqueId()))
                  it.remove();
              }
            }
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

              AsteriskCallId[] calls = getCallIdsForUniqueId(ev.getUniqueId());
              if(calls.length != 0)
              {
                for(int i = 0; i < calls.length; i++)
                {
                  AsteriskConnectionDetail callerDetail = getCallingDetail(calls[i]);
                  String[] det = adjustCallingCalled(calls[i].getCallId(), callerDetail.address, callerDetail.calledAddress);
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, calls[i], det[0], Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, calls[i], det[1], Event.CAUSE_NORMAL);
                }
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

              String callingAddress = getAsteriskCallerIdForAddress(chan.getCallerId());
              String calledAddress = getAsteriskCallerIdForAddress(ev.getAppData());
              int pipeIndex = (calledAddress.indexOf('|'));
              if(pipeIndex != -1)
                calledAddress = calledAddress.substring(0, pipeIndex);

              AsteriskCallId[] calls = getCallIdsForUniqueId(ev.getUniqueId());
              if(calls.length == 0)
              {
                if(isOneOfMyAddresses(getAddressForAsteriskCallerId(calledAddress)))
                {
                  String calling = callingAddress;
                  if(calledAddress.equals(callingAddress))
                    calling += " (myself)";
                  AsteriskCallId call = new AsteriskCallId(calledAddress);
                  AsteriskConnectionDetail detail = new AsteriskConnectionDetail
                  (
                    ev.getUniqueId(), getChannelForUniqueId(ev.getUniqueId()), calling, calledAddress
                  );
                  call.addConnection(ev.getUniqueId(), detail);
                  callSet.add(call);
                  _o("new Call: " + call.toString() + " !!!");
                }
                if(isOneOfMyAddresses(getAddressForAsteriskCallerId(callingAddress)))
                {
                  String called = calledAddress;
                  if(calledAddress.equals(callingAddress))
                    called += "(myself)";
                  AsteriskCallId call = new AsteriskCallId(callingAddress);
                  AsteriskConnectionDetail detail = new AsteriskConnectionDetail
                  (
                    ev.getUniqueId(), getChannelForUniqueId(ev.getUniqueId()), callingAddress, called
                  );
                  call.addConnection(ev.getUniqueId(), detail);
                  callSet.add(call);
                  _o("new Call: " + call.toString() + " !!!");
                }
              }
              else
              {
                // doesn't need to check for more AsteriskCallIDs because this case can only occur
                // when the call is done by using the jtapi-framework
                AsteriskConnectionDetail aDetail = getDetail(calls[0], ev.getUniqueId());
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

              AsteriskCallId[] calls = getCallIdsForUniqueId(ev.getUniqueId());
              if(calls.length != 0)
              {
                for(int i = 0; i < calls.length; i++)
                {
                  AsteriskConnectionDetail detail = getCallingDetail(calls[i]);
                  String[] det = adjustCallingCalled(calls[i].getCallId(), detail.address, detail.calledAddress);
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionTalking, calls[i], det[1], Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionTalking, calls[i], det[0], Event.CAUSE_NORMAL);
                }
              }
            }
            else if (ev.getState().equals("Ringing"))
            {
              _o("!Ringing");
              _o(getChannelForUniqueId(ev.getUniqueId()).toString());

              AsteriskCallId[] calls = getCallIdsForUniqueId(ev.getUniqueId());
              if(calls.length != 0)
              {
                for(int i = 0; i < calls.length; i++)
                {
                  AsteriskConnectionDetail callerDetail = getCallingDetail(calls[i]);
                  String[] det = adjustCallingCalled(calls[i].getCallId(), callerDetail.address, callerDetail.calledAddress);
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, calls[i], det[0], Event.CAUSE_NORMAL);
                  fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, calls[i], det[1], Event.CAUSE_NORMAL);
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
   * @return the callIds as array
   */
  private AsteriskCallId[] getCallIdsForUniqueId(String uniqueId)
  {
    ArrayList arr = new ArrayList();
    for(Iterator i = callSet.iterator(); i.hasNext(); )
    {
      AsteriskCallId next = (AsteriskCallId)i.next();
      if(next.getConnections().containsKey(uniqueId))
        arr.add(next);
    }
    return (AsteriskCallId[])arr.toArray(new AsteriskCallId[arr.size()]);
  }


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


  /**
   * AsteriskConnectionDetail representing the caller
   * @param call
   * @return
   */
  private AsteriskConnectionDetail getCallingDetail(AsteriskCallId call)
  {
    Collection values =  call.getConnections().values();
    for(Iterator i = values.iterator(); i.hasNext(); )
    {
      AsteriskConnectionDetail aDetail = (AsteriskConnectionDetail)i.next();
      if(aDetail.address != null && aDetail.calledAddress != null)
        return aDetail;
    }
    return null;
  }


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
    }
    return callerId;
  }


  private String getAsteriskCallerIdForAddress(String address)
  {
    int index = address.lastIndexOf('/');
    if(index != -1)
      return address.substring(index+1);
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

  private String[] adjustCallingCalled(String thisOne, String calling, String called)
  {
    if(thisOne.equals(calling))
      return new String[]{getAddressForAsteriskCallerId(calling), called};
    else if(thisOne.equals(called))
      return new String[]{calling, getAddressForAsteriskCallerId(called)};
    return null;
  }

  private void hangUpChannel(String channel)
  {
    HangupAction hangupAction = new HangupAction();
    hangupAction.setChannel(channel);
    try
    {
      managerConnection.sendAction(hangupAction);
      _o("hangup action sent for " + channel);
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
