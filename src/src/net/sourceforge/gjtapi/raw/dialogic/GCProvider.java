package net.sourceforge.gjtapi.raw.dialogic;
import java.util.*;
import net.sourceforge.gjtapi.*;
import javax.telephony.*;
import net.sourceforge.gjtapi.raw.BasicJtapiTpi;
/*
    Copyright (c) 2002 Westhawk Ltd. (www.westhawk.co.uk) 

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
/**
 * GJTAPI Provider for Dialogic GlobalCall 
 * It uses a native library to talk to libgc.
 * Simply translating the events/calls and passing them on.
 * We try to minimize the state that we keep here, since
 * both GC and GJTAPI do this fine, an extra layer would  get in the way.
 * We _depend_ on JDK 1_4 and use dialogic's Signaling mode.
 * There is also code that supports Async polling mode.
 * This was written to run on RedHat 7.2 and Dialogic 5.1
 * and tested on a D/300SCPCI with ctr4 (EuroISDN) on the
 * public network.
 */

public class GCProvider implements BasicJtapiTpi, Runnable {
    
    boolean polling = true;
    boolean done = true;
    int debug_level =0;
    Thread eventThread;
    
    public GCProvider() {
    }
    
    protected String [] addresses;
    protected GCTermData [] terminals;
    protected TelephonyListener nails;
    protected Hashtable calls;
    protected Properties caps;
    
    void debug(int level, String s){
        if (debug_level >= level){
            System.err.println(s);
        }
    }
    
    // GJTAPI methods to fill in
    public String[] getAddresses() throws ResourceUnavailableException {
        return addresses;
    }

    public String[] getAddresses(String terminal) throws InvalidArgumentException {
        return addresses;
    }

    public TermData[] getTerminals() throws ResourceUnavailableException {
        return terminals;
    }
    
    public TermData[] getTerminals(String address) throws InvalidArgumentException {
        return terminals;
    }
    
    public void release(String address, CallId id) throws PrivilegeViolationException, ResourceUnavailableException, MethodNotSupportedException, RawStateException {
        if ((id == null) || !( id instanceof DCall)){
            throw new RawStateException(id,Call.INVALID);
        }
        DCall dc = (DCall) id;
        try {
            int crn = dc.getCrnInt();
            gc_DropCall(crn,true,false);
        } catch (RuntimeException rx){
            throw new RawStateException(id,Call.INVALID);
        }
    }
    
    public void addListener(TelephonyListener ro) {
        nails = ro;
    }
    
    public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException, MethodNotSupportedException, RawStateException {
        /* should do something with the other params, but.... */
        try {
            DCall dc = (DCall) call;
            int crno = dc.getCrnInt();
            this.gc_AnswerCall(crno,0,false);
        } catch (RuntimeException rx) {
            RawStateException rsx = new RawStateException(call,Call.INVALID);
            throw rsx;
        }
    }
    
    public CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException, InvalidPartyException, InvalidArgumentException, RawStateException, MethodNotSupportedException {
        GCTermData gct = this.getTerminal(term);
        if ((id == null) || !( id instanceof DCall)){
            throw new InvalidArgumentException("Call must be from this TPI");
        }
        
        DCall dc = (DCall) id;

        if (gct == null) {
            throw new InvalidArgumentException(term+" not found");
        }

        try {
            int ldev = gct.getLinedev();
            int crn = gc_MakeCall(ldev,dest,0,false);
            dc.setCrn(new Integer(crn));
            dc.setLinedev(new Integer(ldev));
            dc.setDestaddr(dest);
            dc.setOrigaddr(address);
            calls.put(dc.getCrn(),dc); // use crn as the key -
        } catch (RuntimeException rx){
            throw new RawStateException(dc,Call.INVALID);
        }
        return dc;// actually it's ignored 
    }
    
    public Properties getCapabilities() {
        return caps; // defaults....
    }
    
    
    public void initialize(Map props) throws ProviderUnavailableException {
        //extract addresses and dev the properties

        try {
            srlibinit(polling);
            done = false;
            if (polling) {
                Thread t = new Thread(this,"gc_events");
                t.setPriority(Thread.MAX_PRIORITY);
                eventThread = t;
                t.start();
            }
        } catch (RuntimeException x) {
            throw new ProviderUnavailableException(x.getMessage());
        } 
        //extract addresses and dev from the properties

        parseProps(props);
        caps = new Properties();
        calls = new Hashtable();
        try {
            openAll();
        } catch (RuntimeException x) {
            closeAll();
            gc_stop();
            throw new ProviderUnavailableException(x.getMessage());
        }
    }
    
    
    public void releaseCallId(CallId id) {

    }
    
    public void removeListener(TelephonyListener ro) {
        nails = null;
    }
    
    public CallId reserveCallId(String address) throws InvalidArgumentException {
        DCall rcall = new DCall();
        rcall.setOrigaddr(address);
        return rcall;
    }
    
    public void shutdown() {
        RuntimeException x = null;
        try {
            closeAll();
        } catch (RuntimeException rx) { 
            x = rx;
        }
        gc_stop();
        if (x != null) {
            throw (x);
        }
        done = true;
        if (eventThread != null){
            try {
                eventThread.join(5000);
            } catch (Exception y) {
                y.printStackTrace();
            }
        }
    }
    
 
  // and our 'event' methods - one per GC event type
  
  void alerting(int linedev){
    debug(1,"alerting event on line "+linedev);
    /**@todo: event to deal with */
    /*DCall dc = (DCall) calls.get(new Integer(crn));
    if (dc != null){
        if (dc.getLinedev() != null){
            String te = getTermName(dc.getLinedev().intValue());
            if(nails!=null){
                nails.terminalConnectionRinging(dc, dc.getOrigaddr(),te,CAUSE_NEW_CALL);
            }
        }
    }
    */    
  }
  
  void unblocked(int linedev){
    debug(1,"unblocked event on line "+linedev);    

  }
  
  void blocked(int linedev){
    debug(1,"blocked event on line "+linedev);    
  }
  
  void opened(int linedev){
    debug(1,"opened event on line "+linedev);  
    gc_WaitCall(linedev,0,false);   
  }
  
  void openFailed(int linedev){
    debug(1,"openFailed event on line "+linedev);    
  }
  
  void miscEvent(int linedev, int eventtype){
    debug(1,"got unclaimed event "+eventtype+" on line "+linedev);
  }
  
  /**
   * We handle internally - call Accept immediately
   */
  void offered(int crn){
    debug(1,"offered event on line "+crn);
    gc_AcceptCall(crn,0,false);
  }
  
  void proceeding(int crn){
    debug(1,"proceeding event on call "+crn);
    DCall dc = (DCall) calls.get(new Integer(crn));
    if ((dc != null) && (nails!=null)){
        nails.connectionInProgress(dc, dc.getOrigaddr(),Event.CAUSE_NORMAL);
    }     
    
  }
  
  void accept(int crn){
    debug(1,"accept event on "+crn);
    DCall dc = (DCall) calls.get(new Integer(crn));
    if (dc != null){
        if (dc.getLinedev() != null){
            String te = getTermName(dc.getLinedev().intValue());
            if(nails!=null){
                nails.terminalConnectionCreated(dc,dc.getDestaddr(),"remote",Event.CAUSE_NEW_CALL);
                nails.terminalConnectionRinging(dc, dc.getOrigaddr(),te,Event.CAUSE_NEW_CALL);
            }
        }
    }   
  }
  
  void taskFail(int crn){
    debug(1,"taskFail event on "+crn);    
  }
  
  void answered(int crn){
    debug(1,"answered event on "+crn);
    DCall dc = (DCall) calls.get(new Integer(crn));
    if (dc != null){
        if (dc.getLinedev() != null){
            String te = getTermName(dc.getLinedev().intValue());
            if(nails!=null){
                nails.terminalConnectionTalking(dc, dc.getOrigaddr(),te,Event.CAUSE_NORMAL);
            }
        }
    }  
  }
  
  void dropCall(int crn){
    debug(1,"dropCall event on "+crn);
    DCall dc = (DCall) calls.get(new Integer(crn));
    if (dc != null){
        if (dc.getLinedev() != null){
            String te = getTermName(dc.getLinedev().intValue());
            if(nails!=null){
                nails.terminalConnectionDropped(dc, dc.getOrigaddr(),te,Event.CAUSE_NORMAL);
            }
        }
    } 
    gc_ReleaseCallEx(crn,false);    
  }
  
  void releaseCall(int crn){
    debug(1,"releaseCall event on "+crn);
    DCall dc = (DCall) calls.get(new Integer(crn));
    if ((dc != null) && (nails != null)){
        nails.callInvalid(dc,Event.CAUSE_NORMAL);
    }
    calls.remove(dc.getCrn());
  }  
  
  void releaseCallFail(int crn){
    debug(1,"releaseCallFail event on "+crn);    
  }
  
  void callStatus(int crn){
    debug(1,"callStatus event on "+crn);    
  }
  
  void connected(int crn){
    debug(1,"connected event on "+crn);
    DCall dc = (DCall) calls.get(new Integer(crn));
    if ((dc != null) && (nails != null)){
        if (dc.getLinedev() != null){
            String te = getTermName(dc.getLinedev().intValue());
            nails.terminalConnectionTalking((CallId)dc, dc.getOrigaddr(),te,Event.CAUSE_NORMAL);
        }
    }     
  }
  
  void disconnected(int crn){
    debug(1,"disconnected event on "+crn);
    DCall dc = (DCall) calls.get(new Integer(crn));
    if (dc != null){
        if (dc.getLinedev() != null){
            String te = getTermName(dc.getLinedev().intValue());
            if(nails!=null){
                nails.terminalConnectionDropped(dc, dc.getOrigaddr(),te,Event.CAUSE_NORMAL);
            }
        }
    } 
    this.gc_DropCall(crn,true,false);       
  }
  
  /**
   * Called from native code when an incomming call arrives.
   */
  void createDCall(int ldev,int crn,String dest, String orig){
    String line = getTermName(ldev);
    debug(1,"call on "+line+" from "+orig+" to "+dest+"crn ="+crn);
    DCall dc = new DCall();
    dc.setCrn(new Integer(crn));
    dc.setLinedev(new Integer(ldev));
    dc.setDestaddr(dest);
    dc.setOrigaddr(orig);
    calls.put(dc.getCrn(),dc);
    if (nails!=null) {
        nails.callActive(dc,Event.CAUSE_NORMAL);
    }
  }  
    
 /** 
  * look for Properties that start 'Address' or 'Device'
  * Then add them to the requisite tables.
  */
    
  void parseProps(Map p){
    Set all = p.keySet();
    Iterator it = all.iterator();
    Vector addrs = new Vector();
    Vector devs = new Vector();
    
    while (it.hasNext()){
        String key = (String) it.next();
        debug(1,"Property: "+key);
        if (key.startsWith("Address")){
            String addr = (String) p.get(key);
            // could use XXX from the AddressXXX as index into the address
            addrs.add(addr);
        }
        if (key.startsWith("Device")){
            String devname = (String) p.get(key);
            GCTermData gct = new GCTermData(devname,false);
            devs.add(gct);
        }
    }
    String deb =(String) p.get("Debug");
    if(deb != null){
      try {
        debug_level = Integer.parseInt(deb);
      } catch (NumberFormatException nfx){
        ;// drop it.
      }
    }
    String sig = (String) p.get("SigMode");
    if ((sig != null) && (sig.startsWith("t"))){
        this.polling = false;
    }
    addresses = new String[addrs.size()];
    for (int i=0;i<addresses.length;i++){
        addresses[i]= (String) addrs.elementAt(i);
        debug(1,"Address ="+addresses[i]);
    }   
    terminals = new GCTermData [devs.size()];
    for (int i=0;i<terminals.length;i++){
        terminals[i]= (GCTermData) devs.elementAt(i);
        debug(1,"terminal ="+terminals[i].terminal);
    }  
  }
    
  /** 
   * open all the terminals we found in props, bail if _any_ of them
   * fail to open. 
   * Causes async events, which then call waitCall etc.
   */  
  void openAll(){ // implicit throws RuntimeException
    for (int i=0;i<terminals.length;i++){
        GCTermData term = terminals[i];
        String dev = term.terminal;
        int ldev = this.gc_OpenEx(dev,false,(long)i);/**@todo: event to deal with */
        term.setLinedev(ldev);
    }
  }
  /** 
   * close all the terminals 
   */   
  void closeAll(){
    RuntimeException x = null;
    for (int i=0;i<terminals.length;i++){
        GCTermData term = terminals[i];
        int ldev = term.linedev;
        try {
            this.resetLine(ldev);
            this.gc_Close(ldev);
        } catch (RuntimeException rx) {
            x= rx; // we keep trying, but note what failed.
        }
    }
    if (x != null ){
        throw x;
    }
  }
  
  /**
   * Find the terminal that has this linedev
   */
  GCTermData getTerminal(int ldev){
    GCTermData ret = null;
    for (int i=0;i<terminals.length;i++){
        GCTermData term = terminals[i];
        int tdev =term.getLinedev();
        if (ldev == tdev){
            ret = term;
            break;
        }
    }
    return ret;
  }
  
   /**
   * Find the terminal that has this name
   */
  GCTermData getTerminal(String name){
    GCTermData ret = null;
    if (name != null) {
        for (int i=0;i<terminals.length;i++){
            GCTermData term = terminals[i];
            if (name.equals(term.terminal)){
                ret = term;
                break;
            }
        }
    }
    return ret;
  }
  
  /**
   * get the name of the LineDev
   */
  String getTermName(int ldev){
    String name = null;
    GCTermData td = getTerminal(ldev);
    if (td != null){
        name = td.terminal;
    }
    return name;
  }
  public void run() {
        while (!done){
            sr_waitevt(1000);
            debug(5,"tick");
        }
  } 
  
  // now our native methods    
  static {
      System.loadLibrary("gcprovider");
  }

  native int gc_OpenEx(String name, boolean sync, long lineMark);
  native int gc_stop();
  native int sr_waitevt(long delay);
  native int srlibinit(boolean poll);
  native int gc_AcceptCall(int crn, int rings,boolean sync); 
  native int gc_AnswerCall(int crn, int rings,boolean sync);
  native int gc_DropCall(int crn, boolean normalcause,boolean sync);
  native int gc_ReleaseCallEx(int crn, boolean sync);
  native int gc_MakeCall(int linedev, String numberstr, int timeout, boolean sync);
  native int gc_WaitCall(int linedev, int timeout, boolean sync);  
  native int gc_Close(int linedev);
  native int resetLine(int linedev);
   

}