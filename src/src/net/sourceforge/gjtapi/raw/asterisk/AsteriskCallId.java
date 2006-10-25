package net.sourceforge.gjtapi.raw.asterisk;

import net.sourceforge.gjtapi.CallId;

import java.util.*;

/**
 *
 * this is an identifier-class for calls
 * @author J. Boesl, 21.06.2005
 */


public class AsteriskCallId implements CallId
{

  private Map connections;
  private String callId;
  private String conferenceRoom;


  public AsteriskCallId(String callId)
  {
    super();
    connections = new HashMap();
    this.callId = callId;
    conferenceRoom = null;
  }

  public boolean equals(Object other)
  {
    if(connections.isEmpty() || other == null)
      return false;
    if (other instanceof AsteriskCallId)
    {
      return other.hashCode() == hashCode();
    }
    return false;
  }

  public int hashCode()
  {
    return super.hashCode();
  }

  public String toString()
  {
    return "AsteriskCallId: " + hashCode();
  }

  /**
   * delivers 'connections'-map with uniqueId as identifying key and AsteriskConnectionDetail as value
   * @return the 'connections'-map
   */
  public Map getConnections()
  {
    return connections;
  }

  /**
   * add a 'connection' to this call
   * @param id
   * @param detail
   */
  public void addConnection(String id, AsteriskConnectionDetail detail)
  {
    connections.put(id, detail);
  }

  /**
   * remove a 'connection' from this call
   * @param id
   */
  public void removeConnection(String id)
  {
    connections.remove(id);
  }

  public String getCallId()
  {
    return callId;
  }

  public String getConferenceRoom()
  {
    return conferenceRoom;
  }

  public void setConferenceRoom(String conferenceRoom)
  {
    this.conferenceRoom = conferenceRoom;
  }

}
