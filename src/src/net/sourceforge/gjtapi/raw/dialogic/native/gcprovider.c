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
#include <jni.h>
#include <jvmpi.h>
#include <stdio.h>
#include <srllib.h>

/*
 * GlobalCall header(s)
 */
#include <gclib.h>
#include <gcerr.h>
#define MAX_STRING_SIZE 256
/* These 2 are hacks - C treats Structs and Arrays interchangablely
 * which allows us to use structs as arguments to InvokexxxA()
 * avoiding need to keep track of the sequence of args in the array
 * or indeed to allocate the array.
 * 
 */
struct eventData {
    jvalue line;
    jvalue event;
    };
struct callInfo {
    jvalue line;
    jvalue crn;
    jvalue dest;
    jvalue orig;
    };
    
static JavaVM *vm;
static jobject gcprovinstance = 0;

static jmethodID miscEventID = 0;
static jmethodID alertingID = 0;
static jmethodID unblockedID = 0;
static jmethodID blockedID = 0;
static jmethodID offeredID = 0;
static jmethodID openedID = 0;
static jmethodID openFailedID = 0;
static jmethodID acceptID = 0;
static jmethodID taskFailID = 0;
static jmethodID answeredID = 0;
static jmethodID dropCallID = 0;
static jmethodID releaseCallID = 0;
static jmethodID releaseCallFailID = 0;
static jmethodID callStatusID = 0;
static jmethodID connectedID = 0;
static jmethodID disconnectedID = 0;
static jmethodID createCallID = 0;
static jmethodID proceedingID = 0;
  
static JavaVMAttachArgs attachArgs = { JNI_VERSION_1_2, NULL, NULL} ;

int print_error(char *function)  
{  
    int            cclibid;       /* cclib id for gc_ErrorValue( ) */  
    int            gc_error;      /* GlobalCall error code */  
    long           cclib_error;   /* Call Control Library error code */   
    char           *gcmsg;        /* points to the gc error message string */  
    char           *ccmsg;        /* points to the cclib error message string */  
    gc_ErrorValue( &gc_error, &cclibid, &cclib_error);  
    gc_ResultMsg( LIBID_GC, (long) gc_error, &gcmsg);  
    gc_ResultMsg( cclibid, cclib_error , &ccmsg);  
    printf ("%s failed, gc(0x%lx) - %s, cc(0x%lx) - %s\n", function ,
             gc_error, gcmsg, cclib_error, ccmsg);  
    return (gc_error);  
}  
/*  
-- This function is called to print GC_INFO to the system console  
-- Typically it would be called after a call to gc_ErrorInfo( )  
-- or gc_ResultInfo( ) to print the resulting GC_INFO data structure  
*/  
void printGC_INFO(GC_INFO *a_Info)  
{  
    printf("a_Info->gcValue = 0x%x\n", a_Info->gcValue);  
    printf("a_Info->gcMsg = %s\n", a_Info->gcMsg);  
    printf("a_Info->ccLibId = %d\n", a_Info->ccLibId);  
    printf("a_Info->ccLibName = %s\n", a_Info->ccLibName);  
    printf("a_Info->ccValue = 0x%x\n", a_Info->ccValue);  
    printf("a_Info->ccMsg = %s\n", a_Info->ccMsg);  
    printf("a_Info->additionalInfo = %s\n", a_Info->additionalInfo);   
}

/*  
-- This function can be called anytime after a GlobalCall event has occurred  
-- This procedure prints the result information to the console with no other side effects  
*/  
void printResultInfo(METAEVENT *a_metaeventp)  
{  
    int         retCode;  
    GC_INFO     t_Info;  
    retCode = gc_ResultInfo(a_metaeventp, &t_Info);  
    if (retCode == GC_SUCCESS) {  
        printGC_INFO(&t_Info);  
    } else {  
        printf("gc_ResultInfo( ) call failed\n");  
    }  
}  


void print_all_cclibs_status(void)  
{  
    int                  i;  
    char                 str[MAX_STRING_SIZE], str1[MAX_STRING_SIZE];  
    GC_CCLIB_STATUSALL   cclib_status_all;  
    if (gc_CCLibStatusEx("GC_ALL_LIB", &cclib_status_all) != GC_SUCCESS) {  
        /* error handling */  
    }    
    strcpy(str, " Call Control Library Status:\n");  
    for (i = 0; i < GC_TOTAL_CCLIBS; i++) {  
        switch (cclib_status_all.cclib_state[i].state) {  
            case GC_CCLIB_CONFIGURED:  
                sprintf(str1, "%s - configured\n",   
                        cclib_status_all.cclib_state[i].name);  
                break;  
            case GC_CCLIB_AVAILABLE:  
                sprintf(str1, "%s - available\n",   
                        cclib_status_all.cclib_state[i].name);  
                break;  
            case GC_CCLIB_FAILED:  
                sprintf(str1, "%s - is not available for use\n",   
                        cclib_status_all.cclib_state[i].name);  
                break;  
            default:  
                sprintf(str1, "%s - unknown CCLIB status\n",   
                        cclib_status_all.cclib_state[i].name);  
                break;  
        }  
        strcat(str, str1);  
    }  
    printf(str);  
}

void createCall(JNIEnv *env,CRN crn,LINEDEV ldev,char *destaddr,char *origaddr){
    struct callInfo ci;
    jstring jdestaddr;
    jstring jorigaddr;

    jdestaddr =  (*env)->NewStringUTF(env,destaddr);
    jorigaddr =  (*env)->NewStringUTF(env,origaddr);

    ci.line.i = (jint) ldev;
    ci.crn.i = (jint) crn;
    ci.dest.l = jdestaddr;
    ci.orig.l = jorigaddr;
    
    (*env)->CallVoidMethodA(env, gcprovinstance, createCallID,(jvalue *)&ci);
}

void pushCallInfo(JNIEnv *env,int crn){
    char destaddr[50];
    char origaddr[50];

    LINEDEV ldev;
    
    ldev = 0;
    strcpy(destaddr,"unknown");
    strcpy(origaddr,"unknown");
    if(gc_CRN2LineDev(crn, &ldev) != GC_SUCCESS) {
        print_error("crnToLine");
    }   
    if(gc_GetCallInfo(crn, DESTINATION_ADDRESS, destaddr) != GC_SUCCESS) {
        print_error("destination address");
    }
    if(gc_GetCallInfo(crn, ORIGINATION_ADDRESS, origaddr) != GC_SUCCESS) {
        print_error("ORIGINATION address");
    }
    createCall(env,crn,ldev,destaddr,origaddr);
}

jmethodID eventToMethod(METAEVENT *mev_p){
    jmethodID ret = NULL;
    switch (mev_p->evttype) {
        case GCEV_OPENEX: ret = openedID;
            break;
        case GCEV_OPENEX_FAIL: ret = openFailedID;
            printResultInfo(mev_p);
            break;
        case GCEV_UNBLOCKED: ret = unblockedID;
            break;
        case GCEV_BLOCKED: ret = blockedID;
            break;
        case GCEV_ACCEPT: ret = acceptID;
            break;
        case GCEV_TASKFAIL: ret = taskFailID;
            printResultInfo(mev_p);
            break;
        case GCEV_ANSWERED: ret = answeredID;
            break;
        case GCEV_DROPCALL: ret = dropCallID;
            break;
        case GCEV_RELEASECALL: ret = releaseCallID;
            break;
        case GCEV_RELEASECALL_FAIL: ret = releaseCallFailID;
            printResultInfo(mev_p);
            break;
        case GCEV_CALLSTATUS: ret = callStatusID;
            printResultInfo(mev_p);
            break;
        case GCEV_CONNECTED: ret = connectedID;
            break;
        case GCEV_DISCONNECTED: ret = disconnectedID;
            break;
        case GCEV_OFFERED: ret = offeredID;
            break;
        case GCEV_ALERTING: ret = alertingID;
            break;
        case GCEV_PROCEEDING: ret = proceedingID;
            break;
        default: ret = NULL;
    }
    return ret; 
}


long handleEvent() {
    JNIEnv *env;
    int error;
    struct eventData ev;
    int a =0;
    jint arg;
    jmethodID handleEventID = NULL;
    
    
    METAEVENT         metaevent;  
    if (gc_GetMetaEvent(&metaevent) != GC_SUCCESS) {  
        /* process error return as shown */  
        error = print_error("gc_GetMetaEvent");  
        return(error);  
    }   
    
    handleEventID = eventToMethod(&metaevent);
    
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_2) != JNI_OK) {
        (*vm)->AttachCurrentThread(vm, (void **)&env, &attachArgs);
        a=1;
    }
    /* special case for OFFERED - need to create a 'call' */
    if (metaevent.evttype == GCEV_OFFERED){
        pushCallInfo(env,metaevent.crn);
    }
    /* decide what to pass upwards */
    if (metaevent.crn == 0) {
        arg = (jint) metaevent.linedev;
    } else {
        arg = (jint) metaevent.crn;
    }
    if (handleEventID != NULL) {
        (*env)->CallVoidMethod(env, gcprovinstance, handleEventID,arg);
    } else {
        ev.line.i = (jint) metaevent.linedev;
        ev.event.i = (jint) metaevent.evttype;
        (*env)->CallVoidMethodA(env, gcprovinstance, miscEventID,(jvalue *)&ev);
    }
    
    if (a) 
        (*vm)->DetachCurrentThread(vm);
    return 0;
}

throw(JNIEnv *env, char * which, char *what)
{
    jclass newExcCls;
    char errbuf[128];

   newExcCls = (*env)->FindClass(env, which);
   if (newExcCls != 0)  {
		sprintf(errbuf, what);
		(*env)->ThrowNew(env, newExcCls, errbuf);
   }
   return 0;
}



int gcinit( JNIEnv *env , jobject obj)  
{   
    int            gc_error;         /* GlobalCall error code */  
    long           cc_error;         /* Call Control Library error code */   
    char           *msg;             /* points to the error message string */
    int res;  
    jclass clst;
       
    /* Issue a gc_Start( ) Call */  
    if ( (res = gc_Start( NULL )) != GC_SUCCESS )    {  
        /* process error return as shown */  
        gc_ErrorValue( &gc_error, NULL , &cc_error);  
        gc_ResultMsg( LIBID_GC, (long) gc_error, &msg);  
        printf ("Error in gc_Start ErrorValue: %d - %s\n",   
                 gc_error, msg);  
    } else {
        clst = (*env)->GetObjectClass(env, obj);
        gcprovinstance = (*env)->NewGlobalRef(env, obj);
        if (gcprovinstance == 0) {
            throw(env, "java/lang/RuntimeException",
                    "Instance GCProvider reference error!");
            res = -1;
        }
    }
    if (res != -1){
        miscEventID = (*env)->GetMethodID(env, clst, "miscEvent", "(II)V");
        alertingID = (*env)->GetMethodID(env, clst, "alerting", "(I)V");
        unblockedID = (*env)->GetMethodID(env, clst, "unblocked", "(I)V");
        blockedID = (*env)->GetMethodID(env, clst, "blocked", "(I)V");
        offeredID = (*env)->GetMethodID(env, clst, "offered", "(I)V");
        openedID = (*env)->GetMethodID(env, clst, "opened", "(I)V");
        acceptID = (*env)->GetMethodID(env, clst, "accept", "(I)V");
        taskFailID = (*env)->GetMethodID(env, clst, "taskFail", "(I)V");
        answeredID = (*env)->GetMethodID(env, clst, "answered", "(I)V");
        dropCallID = (*env)->GetMethodID(env, clst, "dropCall", "(I)V");
        releaseCallID = (*env)->GetMethodID(env, clst, "releaseCall", "(I)V");
        releaseCallFailID = (*env)->GetMethodID(env, clst, "releaseCallFail", "(I)V");
        callStatusID = (*env)->GetMethodID(env, clst, "callStatus", "(I)V");
        connectedID = (*env)->GetMethodID(env, clst, "connected", "(I)V");
        disconnectedID = (*env)->GetMethodID(env, clst, "disconnected", "(I)V");
        openFailedID = (*env)->GetMethodID(env, clst, "openFailed", "(I)V");
        alertingID = (*env)->GetMethodID(env, clst, "alerting", "(I)V");
        proceedingID = (*env)->GetMethodID(env, clst, "proceeding", "(I)V");
        createCallID = (*env)->GetMethodID(env, clst, "createDCall", "(IILjava/lang/String;Ljava/lang/String;)V");
    }
    return(res);  
}

/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_OpenEx
 * Signature: (Ljava/lang/String;ZJ)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1OpenEx
  (JNIEnv *env, jobject obj, jstring Sline, jboolean sync, jlong key)
{
    int ret;
    int mode = EV_ASYNC;
    char *name;
    LINEDEV line = -1;
    
    name =(char *) (*env)->GetStringUTFChars(env, Sline, 0);
    if(sync) {
      mode = EV_SYNC;
    }
    sr_hold();
    ret = gc_OpenEx(&line, name, mode, (void *) 0);
    if (ret != GC_SUCCESS){
        print_error("OpenEx");
        throw(env, "java/lang/RuntimeException", "GC OpenEX error");
    }
    sr_release();
    (*env)->ReleaseStringUTFChars(env, Sline, name);

    return line;

}




/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    srlibinit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_srlibinit
  (JNIEnv *env , jobject obj, jboolean poll)
{
    int gotvm;
    int par,ret ;

	ret = 0;
#ifndef WIN32
    /* Reference symbols to get them loaded  - pulls in LiS */
    if (0) {
        getpmsg(0);
        putpmsg(0);
    }
#endif
    
    gotvm = (*env)->GetJavaVM(env, &vm);
    if (gotvm < 0) {
        throw(env, "java/lang/RuntimeException",
                    "VM reference error!");
        return -1;
    }  else {
      printf("gotvm \n", ret);
    }
    if (GC_SUCCESS != (ret = gcinit(env,obj))) {
        printf("gc_Start returned %d \n", ret);
        print_error("gc_Start");
        print_all_cclibs_status();
        throw(env, "java/lang/RuntimeException", "GClib start error");
        return -1;
    }
    if (poll) {
        par = SR_POLLMODE;
    } else {
        par = SR_SIGMODE;
    }
    if( sr_setparm( SRL_DEVICE, SR_MODEID, &par ) == -1 ) {
        throw(env, "java/lang/RuntimeException", "SRL MODE init error");
        return -1;
    }

    if (sr_enbhdlr(EV_ANYDEV, EV_ANYEVT, handleEvent ) == -1 ) {
        throw(env, "java/lang/RuntimeException", "SRL handler registration error");
        return -1;
    }

    return 0;
}

/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    sr_waitevt
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_sr_1waitevt
  (JNIEnv *env, jobject obj, jlong delay){

  int res = 0;
  res = sr_waitevt(delay);
  return res;
}


/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_stop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1stop
  (JNIEnv *env, jobject obj){

  int res = 0;
  res = gc_Stop();
  return res;
}

/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_AcceptCall
 * Signature: (IIZ)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1AcceptCall
  (JNIEnv *env, jobject obj, jint crn, jint rings, jboolean sync){
    
    int ret;
    int mode = EV_ASYNC;
    if(sync) {
      mode = EV_SYNC;
    }
    sr_hold();
    ret = gc_AcceptCall(crn, rings, mode);
    if (ret != GC_SUCCESS){
        print_error("AcceptCall");
        throw(env, "java/lang/RuntimeException", "AcceptCall error");
    }
    sr_release();
    return ret;  
}

/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_AnswerCall
 * Signature: (IIZ)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1AnswerCall
  (JNIEnv *env, jobject obj, jint crn, jint rings, jboolean sync){
      
    int ret;
    int mode = EV_ASYNC;
    if(sync) {
      mode = EV_SYNC;
    }
    sr_hold();
    ret = gc_AnswerCall(crn, rings, mode);
    if (ret != GC_SUCCESS){
        print_error("AnswerCall");
        throw(env, "java/lang/RuntimeException", "AnswerCall error");
    }
    sr_release();
    return ret;  
}
/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_DropCall
 * Signature: (IZZ)I
 *  static native int gc_DropCall(int crn, boolean normalcause,boolean sync);
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1DropCall
  (JNIEnv *env, jobject obj, jint crn , jboolean normalcause , jboolean sync){
      
    int ret;
    int cause = GC_NORMAL_CLEARING;
    int mode = EV_ASYNC;
    if(sync) {
      mode = EV_SYNC;
    }
    if(!normalcause) {
        cause = GC_DEST_OUT_OF_ORDER; /* just pick a random - non-normal cause */
    }
    sr_hold();
    ret = gc_DropCall(crn, cause, mode);
    if (ret != GC_SUCCESS){
        print_error("DropCall");
        throw(env, "java/lang/RuntimeException", "DropCall error");
    }
    sr_release();
    return ret;  
}

/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_ReleaseCallEx
 * Signature: (IZ)I
 * static native int gc_ReleaseCallEx(int crn, boolean sync);
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1ReleaseCallEx
  (JNIEnv *env, jobject obj, jint crn, jboolean sync){
      
    int ret;
    int mode = EV_ASYNC;
    if(sync) {
      mode = EV_SYNC;
    }
    sr_hold();
    ret = gc_ReleaseCallEx(crn, mode);
    if (ret != GC_SUCCESS){
        print_error("ReleaseCallEx");
        throw(env, "java/lang/RuntimeException", "ReleaseCallEx error");
    }
    sr_release();
    return ret;  
}
/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_MakeCall
 * Signature: (ILjava/lang/String;IZ)I
 * static native int gc_MakeCall(int linedev, String numberstr, int timeout, boolean sync);    
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1MakeCall
  (JNIEnv *env, jobject obj, jint line, jstring numberstr, jint timeout, jboolean sync){
    int ret;
    int mode = EV_ASYNC;
    CRN crn;
    char *num;
    
    num =(char *) (*env)->GetStringUTFChars(env, numberstr, 0);
    if(sync) {
      mode = EV_SYNC;
    }
    sr_hold();
    /* (linedev, crnp, numberstr, makecallp, timeout, mode) */
    ret = gc_MakeCall((LINEDEV)line,&crn,num,NULL,(int)timeout,mode);
    if (ret != GC_SUCCESS){
        print_error("MakeCall");
        throw(env, "java/lang/RuntimeException", "MakeCall error");
    }
    sr_release();

    (*env)->ReleaseStringUTFChars(env, numberstr, num);

    return crn;
}
/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_WaitCall
 * Signature: (IIZ)I
 *   static native int gc_WaitCall(int linedev, int timeout, boolean sync);     
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1WaitCall
  (JNIEnv *env, jobject obj, jint line, jint timeout, jboolean sync){
    int ret;
    int mode = EV_ASYNC;
    CRN *crnp = NULL;
    CRN crn;
    
    if(sync) {
      mode = EV_SYNC;
      crnp = &crn;
    }
    sr_hold();
    ret = gc_WaitCall((LINEDEV)line,crnp,NULL,(int)timeout,mode);
    if (ret != GC_SUCCESS){
        print_error("WaitCall");
        throw(env, "java/lang/RuntimeException", "MakeCall error");
    }
    sr_release();
    return ret;
}
/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    resetLine
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_resetLine
  (JNIEnv *env, jobject obj, jint line){
    int ret;
    int mode = EV_SYNC;
    
    sr_hold();
    ret = gc_ResetLineDev(line, mode) ;
    if (ret != GC_SUCCESS){
        print_error("ResetLineDev");
        throw(env, "java/lang/RuntimeException", "ResetLineDev error");
    }
    sr_release();
    return ret;
}
/*
 * Class:     net_sourceforge_gjtapi_raw_dialogic_GCProvider
 * Method:    gc_Close
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_dialogic_GCProvider_gc_1Close
  (JNIEnv *env, jobject obj, jint line){
  
    int ret;
    sr_hold();
    ret = gc_Close((LINEDEV)line);
    if (ret != GC_SUCCESS){
        print_error("Close");
        throw(env, "java/lang/RuntimeException", "Close error");
    }
    sr_release();
    return ret;
}


