/*
	Copyright (c) 2005 Serban Iordache 
	
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
// Tapi3Provider.cpp : Defines the entry point for the DLL application.

#include "stdafx.h"
#include "net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl.h"
#include "MSTapi3.h"
#include "MSTapi2.h"
#include "TAPI3EventNotification.h"
#include "Logger.h"

Logger* logger = new Logger("Tapi3Provider");
HINSTANCE g_hinstDLL;
MSTapi3* g_msTapi3 = NULL;
MSTapi2* g_msTapi2 = NULL;

JavaVM* g_javaVM = NULL;
jobject g_thisObj = NULL;

void CALLBACK callback(MethodID methodID, int callID, wstring& address, Cause cause, wstring* callInfo);

BOOL APIENTRY DllMain(HANDLE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved) {
	//Logger::setLogFile("./tapi3dll.log");
	try {
		if(DLL_PROCESS_ATTACH == ul_reason_for_call) {
			logger->debug("DllMain: DLL_PROCESS_ATTACH");
			g_hinstDLL = (HINSTANCE)hModule;
			CoInitializeEx(NULL, COINIT_MULTITHREADED);
			g_msTapi3 = new MSTapi3();
			g_msTapi2 = new MSTapi2();
		}
	} catch(...) {
		logger->fatal("DLL_PROCESS_ATTACH failed");
		return FALSE;
	}

	try {
		if(DLL_PROCESS_DETACH == ul_reason_for_call) {
			logger->debug("DllMain: Detaching...");
			if(g_msTapi3 != NULL) {
				g_msTapi3->ShutdownTapi();
				g_msTapi3 = NULL;
				delete g_msTapi2;
				g_msTapi2 = NULL;	// garbage collector will clean up
			}
			CoUninitialize();
			logger->debug("DllMain: DLL_PROCESS_DETACH");
		}
	} catch(...) {
		logger->fatal("DLL_PROCESS_DETACH failed");
		return FALSE;
	}

    return TRUE;
}

bool getProperty(JNIEnv* pEnv, jobject objMap, const char* key, string& value) {
	try {
		jclass mapCls = pEnv->FindClass("java/util/Map");
		jmethodID mapGetMethodID = pEnv->GetMethodID(mapCls, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
		if(mapGetMethodID == NULL) {
			logger->fatal("Failed to retrieve Map.get() method.");
			return false;
		} else {
			jstring jKey = pEnv->NewStringUTF(key);
			jstring jValue = (jstring)pEnv->CallObjectMethod(objMap, mapGetMethodID, jKey);
			if(jValue == NULL) {
				logger->debug("Property '%s' not available.", key);
				return false;
			}
			jboolean isCopyAddress;
			const char* sValue = pEnv->GetStringUTFChars(jValue, &isCopyAddress);
			value = sValue;
			if(JNI_TRUE == isCopyAddress) {
				pEnv->ReleaseStringUTFChars(jValue, sValue);
			}
			logger->debug("Returning value '%s' for property '%s'", value.c_str(), key);
			return true;
		}
	} catch(...) {
		logger->fatal("getProperty(%s) failed.", key);
		return false;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3Init
 * Signature: (Ljava/util/Map;)I
 */
JNIEXPORT jobjectArray JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3Init(JNIEnv* pEnv, jobject oObj, jobject objMap) {
	try{
		g_thisObj = pEnv->NewGlobalRef(oObj);
		pEnv->GetJavaVM(&g_javaVM);

		string logFilePath;
		if(getProperty(pEnv, objMap, "tapi3.native.log.out", logFilePath)) {
			logger->debug("Setting logFile to %s", logFilePath.c_str());
			Logger::setLogFile(logFilePath.c_str());
		}

		jobjectArray objAddresses = NULL;	// String array of address names

		// test initialization of TAPI 2
		g_msTapi2->InitializeTapi2();

		HRESULT hr = g_msTapi3->InitializeTapi(callback);
		if(SUCCEEDED(hr)) {
			jclass oCls = pEnv->FindClass("java/lang/String");
			list<wstring> addressNames = g_msTapi3->getAddressNames();
			objAddresses = pEnv->NewObjectArray(addressNames.size(), oCls, NULL);
			int i=0;
			for (list<wstring>::iterator it = addressNames.begin(); it != addressNames.end(); ++it, ++i) {
				jstring objAddress = pEnv->NewString((*it).c_str(), (*it).length());
				pEnv->SetObjectArrayElement(objAddresses, i, objAddress);
			}
		}

		string swapOnHold;
		if(getProperty(pEnv, objMap, "tapi3.native.swapOnHold", swapOnHold)) {
			if(!stricmp("true", swapOnHold.c_str())) {
				g_msTapi3->setSwapOnHold(true);
				logger->debug("SwapOnHold activated.");
			}
		}

        string handoff;
		if(getProperty(pEnv, objMap, "tapi3.native.handoff", handoff)) {
			if(handoff.length() > 0) {
                wchar_t wHandoff[256];
                mbstowcs(wHandoff, handoff.c_str(), handoff.length() + 1);
                wstring wsHandoff = wHandoff;
				g_msTapi3->setHandoff(wsHandoff);                
				logger->debug("Handoff activated.");
			}
		}


		return objAddresses;
	} catch(...){
		logger->fatal("tapi3Init failed.");
		return NULL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    answerCall
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3AnswerCall(JNIEnv* pEnv, jobject oObj, jint callID) {
	try{
		logger->debug("answerCall() called for callID=%d", callID);
		HRESULT hr = g_msTapi3->AnswerTheCall(callID);
		logger->debug("answerCall() done for callID=%d", callID);
		return hr;
	} catch(...){
		logger->fatal("answerCall failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3DisconnectCall
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3DisconnectCall(JNIEnv* pEnv, jobject oObj, jint callID) {
	try{
		logger->debug("disconnectCall() called for callID=%d", callID);
		HRESULT hr = g_msTapi3->DisconnectTheCall(callID);
		logger->debug("disconnectCall() done callID=%d", callID);
		return hr;
	} catch(...){
		logger->fatal("disconnectCall failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3ReleaseCall
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3ReleaseCall(JNIEnv* pEnv, jobject oObj, jint callID) {
	try{
		logger->debug("releaseCall() called for callID=%d", callID);
		g_msTapi3->ReleaseTheCall(callID);
		logger->debug("releaseCall() done callID=%d", callID);
		return 0;
	} catch(...){
		logger->fatal("releaseCall failed.");
		return -1;
	}
}


/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3ReserveCallId
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3ReserveCallId(JNIEnv* pEnv, jobject oObj, jstring jAddress) {
	try{
		jboolean isCopyAddress;
		const unsigned short* wsAddress = pEnv->GetStringChars(jAddress, &isCopyAddress);
		logger->debug("reserveCall() called for %S", wsAddress);
		wstring address = wsAddress;
		if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseStringChars(jAddress, wsAddress);
		}
		int callID = g_msTapi3->reserveCall(address);
		logger->debug("reserveCall() done for %S: callID=%d", address.c_str(), callID);
		return callID;
	} catch(...){
		logger->fatal("reserveCall failed.");
		return -1;
	}
}


/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3CreateCall
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3CreateCall(
					JNIEnv* pEnv, jobject oObj, jint callID, jstring jAddress, jstring jDestination, jint mode) {
	try{
		logger->debug("createCall() called for callID=%d", callID);
		jboolean isCopyAddress;
		const unsigned short* wsAddress = pEnv->GetStringChars(jAddress, &isCopyAddress);
		wstring address = wsAddress;
		if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseStringChars(jAddress, wsAddress);
		}

		jboolean isCopyDestination;
		const unsigned short* wsDestination = pEnv->GetStringChars(jDestination, &isCopyDestination);
		wstring destination = wsDestination;
		if(JNI_TRUE == isCopyDestination) {
			pEnv->ReleaseStringChars(jDestination, wsDestination);
		}

		logger->debug("Creating call with callID=%d to %S on %S", callID, destination.c_str(), address.c_str());
		HRESULT hr = g_msTapi3->MakeTheCall(callID, address, destination, mode);
		if(SUCCEEDED(hr)) {
			logger->debug("createCall() done for callID=%d to %S on %S", callID, destination.c_str(), address.c_str());
		} else {
			logger->error("createCall() failed for callID=%d to %S on %S", callID, destination.c_str(), address.c_str());
			callback(net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_CONNECTION_DISCONNECTED, callID, address, 
				net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_JNI_CAUSE_DEST_NOT_OBTAINABLE, NULL);
		}
		return hr;
	} catch(...){
		logger->fatal("createCall failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3Dial
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3Dial(
					JNIEnv* pEnv, jobject oObj, jint callID, jstring jNumberToDial) {
	try{
		logger->debug("Dial() called for callID=%d", callID);
		jboolean isCopyDestination;
		const unsigned short* wsDestination = pEnv->GetStringChars(jNumberToDial, &isCopyDestination);
		wstring destination = wsDestination;
		if(JNI_TRUE == isCopyDestination) {
			pEnv->ReleaseStringChars(jNumberToDial, wsDestination);
		}

		logger->debug("Dialing with callID=%d to %S", callID, destination.c_str());
    HRESULT hr = g_msTapi3->Dial(callID, destination);
		if(SUCCEEDED(hr)) {
			logger->debug("Dial() done for callID=%d to %S", callID, destination.c_str());
		} else {
			logger->error("Dial() failed for callID=%d to %S", callID, destination.c_str());
		}
		return hr;
	} catch(...){
		logger->fatal("Dial() failed.");
		return E_FAIL;
	}
}
/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3Hold
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3Hold(JNIEnv* pEnv, jobject oObj, jint callID) {
	try{
		logger->debug("hold() called for callID=%d", callID);
		HRESULT hr = g_msTapi3->HoldTheCall(callID);
		logger->debug("hold() done for callID=%d.", callID);
		return hr;
	} catch(...){
		logger->fatal("hold failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3UnHold
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3UnHold(JNIEnv* pEnv, jobject oObj, jint callID) {
	try{
		logger->debug("unHold() called for callID=%d", callID);
		HRESULT hr = g_msTapi3->UnHoldTheCall(callID);
		logger->debug("unHold() done for callID=%d.", callID);
		return hr;
	} catch(...){
		logger->fatal("unHold failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3Join
 * Signature: (IILjava/lang/String;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3Join(JNIEnv* pEnv, jobject oObj, jint callID1, jint callID2, jstring jAddress, jstring jTerminal, jint mode) {
	try{
		char* modeName;
		switch(mode)
		{
			default:
			case 0:
				modeName = "NONE";
				break;
			case 1:
				modeName = "SETUP_TRANSFER";
				break;
			case 2:
				modeName = "TRANSFER";
				break;
			case 3:
				modeName = "SETUP_CONFERENCE";
				break;
			case 4:
				modeName = "CONFERENCE";
				break;
		}
		logger->debug("join() called for callID1=%d and callID2=%d with mode=%s", callID1, callID2, modeName);
		jboolean isCopyAddress;
		const unsigned short* wsAddress = pEnv->GetStringChars(jAddress, &isCopyAddress);
		wstring address = wsAddress;
		if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseStringChars(jAddress, wsAddress);
		}
		const unsigned short* wsTerminal = pEnv->GetStringChars(jTerminal, &isCopyAddress);
		wstring terminal = wsTerminal;
		if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseStringChars(jTerminal, wsTerminal);
		}
		HRESULT hr = g_msTapi3->JoinCalls(callID1, callID2, address, terminal, mode);
		logger->debug("join() done for callID1=%d and callID2=%d with mode=%s", callID1, callID2, modeName);
        if(!FAILED(hr)) {
            hr = 0;
        }
		return hr;
	} catch(...){
		logger->fatal("join failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3BlindTransfer
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3BlindTransfer(
					JNIEnv* pEnv, jobject oObj, jint callID, jstring jNumberToDial) {
	try{
		logger->debug("BlindTransfer() called for callID=%d", callID);
		jboolean isCopyDestination;
		const unsigned short* wsDestination = pEnv->GetStringChars(jNumberToDial, &isCopyDestination);
		wstring destination = wsDestination;
		if(JNI_TRUE == isCopyDestination) {
			pEnv->ReleaseStringChars(jNumberToDial, wsDestination);
		}

		logger->debug("Blind Transfer with callID=%d to %S", callID, destination.c_str());
    HRESULT hr = g_msTapi3->BlindTransfer(callID, destination);
		if(SUCCEEDED(hr)) {
			logger->debug("BlindTransfer() done for callID=%d to %S", callID, destination.c_str());
		} else {
			logger->error("BlindTransfer() failed for callID=%d to %S", callID, destination.c_str());
		}
		return hr;
	} catch(...){
		logger->fatal("BlindTransfer() failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3ConsultationStartStart
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3ConsultationStart(
					JNIEnv* pEnv, jobject oObj, jint callID, jstring jAddress, jstring jNumberToDial) {
	try{
		logger->debug("ConsultationStart() called for callID=%d", callID);
		jboolean isCopyAddress;
		const unsigned short* wsAddress = pEnv->GetStringChars(jAddress, &isCopyAddress);
		wstring address = wsAddress;
		if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseStringChars(jAddress, wsAddress);
		}

		jboolean isCopyDestination;
		const unsigned short* wsDestination = pEnv->GetStringChars(jNumberToDial, &isCopyDestination);
		wstring destination = wsDestination;
		if(JNI_TRUE == isCopyDestination) {
			pEnv->ReleaseStringChars(jNumberToDial, wsDestination);
		}

		logger->debug("Starting consultation call with callID=%d from %S to %S", callID, address.c_str(), destination.c_str());
		HRESULT hr = g_msTapi3->ConsultationStart(callID, address, destination);
		if(SUCCEEDED(hr)) {
			logger->debug("ConsultationStart() done for callID=%d from %S to %S", callID, address.c_str(), destination.c_str());
		} else {
			logger->error("ConsultationStart() failed for callID=%d from %S to %S", callID, address.c_str(), destination.c_str());
		}
		return hr;	// this will be the callId of the consultation call
	} catch(...){
		logger->fatal("ConsultationStart() failed.");
		return -1;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi2ConsultationStartStart
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi2ConsultationStart(
					JNIEnv* pEnv, jobject oObj, jstring jAddress, jstring jNumberToDial) {
	try{
		logger->debug("TAPI2 ConsultationStart() called for address=%s", jAddress);
		jboolean isCopyAddress;
		const unsigned short* wsAddress = pEnv->GetStringChars(jAddress, &isCopyAddress);
		wstring address = wsAddress;
		if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseStringChars(jAddress, wsAddress);
		}

		jboolean isCopyDestination;
		const unsigned short* wsDestination = pEnv->GetStringChars(jNumberToDial, &isCopyDestination);
		wstring destination = wsDestination;
		if(JNI_TRUE == isCopyDestination) {
			pEnv->ReleaseStringChars(jNumberToDial, wsDestination);
		}

		logger->debug("Starting consultation call from %S to %S", address.c_str(), destination.c_str());
		HRESULT hr = g_msTapi2->ConsultationStart(address, destination);
		if(SUCCEEDED(hr)) {
			logger->debug("Tapi2 ConsultationStart() done from %S to %S", address.c_str(), destination.c_str());
		} else {
			logger->error("Tapi2 ConsultationStart() failed from %S to %S", address.c_str(), destination.c_str());
		}
		return hr;	// this will be the callId of the consultation call
	} catch(...){
		logger->fatal("Tapi2 ConsultationStart() failed.");
		return -1;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi2ConsultationStartStart
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi2TransferFinish(
	JNIEnv* pEnv, jobject oObj, jstring jAddress, jboolean jTransferFlag) {
	try{
		logger->debug("TAPI2 Transfer finish called for address=%s", jAddress);
		jboolean isCopyAddress;
		const unsigned short* wsAddress = pEnv->GetStringChars(jAddress, &isCopyAddress);
		wstring address = wsAddress;
		if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseStringChars(jAddress, wsAddress);
		}

		//jTransferFlag.

		logger->debug("Starting consultation finish from %S", address.c_str());
		HRESULT hr = g_msTapi2->ConsultationFinish(address, (jTransferFlag != 0));
		if(SUCCEEDED(hr)) {
			logger->debug("Tapi2 ConsultationFinish() done from %S", address.c_str());
		} else {
			logger->error("Tapi2 ConsultationFinish() failed from %S", address.c_str());
		}
		return hr;	// this will be the callId of the consultation call
	} catch(...){
		logger->fatal("Tapi2 TransferFisish() failed.");
		return -1;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3AssistedTransferFinish
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3AssistedTransferFinish(
					JNIEnv* pEnv, jobject oObj, jint callID) {
	try{
		logger->debug("AssistedTransferFinish() called for callID=%d", callID);

		HRESULT hr = g_msTapi3->AssistedTransferFinish(callID);
		if(SUCCEEDED(hr)) {
			logger->debug("AssistedTransferFinish() done for callID=%d", callID);
		} else {
			logger->error("AssistedTransferFinish() failed for callID=%d", callID);
		}
		return hr;
	} catch(...){
		logger->fatal("AssistedTransferFinish() failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3ConferenceFinish
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3ConferenceFinish(
					JNIEnv* pEnv, jobject oObj, jint callID) {
	try{
		logger->debug("ConferenceFinish() called for callID=%d", callID);

		HRESULT hr = g_msTapi3->ConferenceFinish(callID);
		if(SUCCEEDED(hr)) {
			logger->debug("ConferenceFinish() done for callID=%d", callID);
		} else {
			logger->error("ConferenceFinish() failed for callID=%d", callID);
		}
		return hr;
	} catch(...){
		logger->fatal("ConferenceFinish() failed.");
		return E_FAIL;
	}
}

/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl
 * Method:    tapi3SendSignals
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3SendSignals(JNIEnv *pEnv, jobject oObj, jstring jTerminal, jstring jDigits) {
	try {
		jboolean isCopyTerminal;
		const unsigned short* wsTerminal = pEnv->GetStringChars(jTerminal, &isCopyTerminal);
		wstring terminal = wsTerminal;
		if(JNI_TRUE == isCopyTerminal) {
			pEnv->ReleaseStringChars(jTerminal, wsTerminal);
		}
		jboolean isCopyDigits;
		const unsigned short* wsDigits = pEnv->GetStringChars(jDigits, &isCopyDigits);
		wstring digits = wsDigits;
		if(JNI_TRUE == isCopyDigits) {
			pEnv->ReleaseStringChars(jDigits, wsDigits);
		}
		HRESULT hr = g_msTapi3->SendDigits(terminal, digits);
		logger->debug("sendSignals() done for terminal=%S: hr=%08X", terminal.c_str(), hr);
		return hr;
	} catch(...){
		logger->fatal("sendSignals failed.");
		return E_FAIL;
	}
}


/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3Shutdown
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3Shutdown(JNIEnv *, jobject) {
	try{
		g_msTapi3->ShutdownTapi();
		g_msTapi3 = 0;
		return 0;
	} catch(...){
		logger->fatal("tapi3Shutdown failed.");
		return -1;
	}
}


/*
 * Class:     net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider
 * Method:    tapi3LineDevSpecific
 * Signature: (ILjava/lang/String;[B)J
 */
JNIEXPORT jlong JNICALL Java_net_sourceforge_gjtapi_raw_tapi3_Tapi3NativeImpl_tapi3LineDevSpecific
(JNIEnv* pEnv, jobject oObj, jint callID, jstring jAddress, jbyteArray jByteArray) {
	try{
		jboolean isCopyAddress;
		const unsigned short* wsAddress = pEnv->GetStringChars(jAddress, &isCopyAddress);
		logger->debug("SendLineDevSpecific() called for %S and callID=%d", wsAddress, callID);
		wstring address = wsAddress;
		if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseStringChars(jAddress, wsAddress);
		}
		int size = pEnv->GetArrayLength(jByteArray);
		jbyte* jBytes = new jbyte[size];
		pEnv->GetByteArrayRegion(jByteArray, 0, size, jBytes);
		//jbyte* jBytes = pEnv->GetByteArrayElements(jByteArray, &isCopyAddress);
		/*if(JNI_TRUE == isCopyAddress) {
			pEnv->ReleaseByteArrayElements(jByteArray, jBytes, 0);
		}*/
		logger->debug("SendLineDevSpecific() data has %d bytes and has content:", size);
		for(int i = 0; i < size; i++){
			logger->debug("%x", (BYTE)jBytes[i]);	
		}
		int result = g_msTapi3->SendLineDevSpecific(callID, address, (BYTE*)jBytes, size);
		
		logger->debug("SendLineDevSpecific() done for %S: callID=%d - result=%d", address.c_str(), callID, result);
		return result;
	} catch(...){
		logger->fatal("SendLineDevSpecific failed.");
		return -1;
	}
}


void CALLBACK callback(MethodID methodID, int callID, wstring& address, Cause cause, wstring* callInfo) {
    static HANDLE hMutex = CreateMutex(NULL, FALSE, "callbackMutex");
    DWORD dwWaitResult = WaitForSingleObject(hMutex, 5000L);
	logger->debug("Entering callback method %d: callID=%d, address=%S...", methodID, callID, address.c_str());
    if(dwWaitResult == WAIT_OBJECT_0) {
	    try{
		    JNIEnv *localEnv = NULL;
		    g_javaVM->AttachCurrentThread((void**)&localEnv, NULL);
		    jclass cls = localEnv->GetObjectClass(g_thisObj);

            jmethodID callbackID = NULL;
            for(int retry=0; callbackID == NULL && retry < 5; retry++) {
		        callbackID = localEnv->GetMethodID(cls, "callback", "(IILjava/lang/String;I[Ljava/lang/String;)V");
				// if we have to try again, delay for 250 ms
				if(callbackID == NULL && retry < 5) {
					Sleep(250);
				}
            }           
            logger->debug("callback methodID: %p, retry=%d", callbackID, retry);

		    if (callbackID == NULL) {
			    logger->fatal("Callback method not available.");
		    } else {
			    jint jMethodID = methodID;
			    jint jCallID = callID;
			    jstring jAddress = localEnv->NewString(address.c_str(), address.length());
			    jint jCause = cause;

			    jobjectArray objCallInfo = NULL;
			    if(callInfo != NULL) {
				    logger->debug("Setting callInfo...");
				    jclass oCls = localEnv->FindClass("java/lang/String");
				    objCallInfo = localEnv->NewObjectArray(4, oCls, NULL);
				    for (int i=0; i<4; i++) {
					    jstring jInfo = localEnv->NewString(callInfo[i].c_str(), callInfo[i].length());
					    localEnv->SetObjectArrayElement(objCallInfo, i, jInfo);
				    }
			    } else {
				    logger->debug("No callInfo for this callback.");
			    }
			    localEnv->CallVoidMethod(g_thisObj, callbackID, jMethodID, jCallID, jAddress, jCause, objCallInfo);
				g_javaVM->DetachCurrentThread();
			    logger->debug("Java callback successfully called.");
		    }
	    } catch(...){
		    logger->fatal("Callback failed.");
		}
        ReleaseMutex(hMutex);
    } else {
		logger->fatal("Cannot obtain callback mutex: dwWaitResult=%08X.", dwWaitResult);
    }
}
