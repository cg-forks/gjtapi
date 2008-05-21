package net.sourceforge.gjtapi.raw.asterisk;

import net.sf.asterisk.manager.Channel;

/**
 * 
 * 
 * @author J. Boesl, 30.06.2005
 */


public class AsteriskConnectionDetail
{

  public String uniqueId;
  public Channel channel;
  public String address;
  public String calledAddress;

  public AsteriskConnectionDetail(String uniqueId, Channel channel, String address, String calledAddress)
  {
    this.uniqueId = uniqueId;
    this.channel = channel;
    this.address = address;
    this.calledAddress = calledAddress;
  }

  public String toString()
  {
    return channel.toString();
  }
}
