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
  //private String[] callDetails;

  public AsteriskCallId()
  {
    super();
    connections = new HashMap();
  }

  public boolean equals(Object other)
  {
    if(connections.isEmpty() || other == null)
      return false;
    /*if (other instanceof String)
      return connections.containsKey(other);
    else*/
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

  /**
   * content: callingAddress, calledAddress, callingAddressUniqueId
   * @return
   */
  /*public String[] getCallDetails()
  {
    return callDetails;
  }

  public void setCallDetails(String[] details)
  {
    callDetails = details;
  }*/

}
