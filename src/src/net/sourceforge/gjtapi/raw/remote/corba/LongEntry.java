package net.sourceforge.gjtapi.raw.remote.corba;

/**
* com/uforce/jtapi/generic/raw/remote/corba/LongEntry.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaProvider.idl
* Thursday, November 16, 2000 1:38:18 o'clock PM EST
*/

public final class LongEntry implements org.omg.CORBA.portable.IDLEntity
{
  public int key = (int)0;
  public int value = (int)0;

  public LongEntry ()
  {
  } // ctor      
  public LongEntry (int _key, int _value)
  {
	key = _key;
	value = _value;
  } // ctor      
} // class LongEntry