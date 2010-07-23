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
#include "stdafx.h"
#include "MSTapi3.h"
#include "TAPI3EventNotification.h"
#include "Util.h"

// constructor
MSTapi3::MSTapi3() : tapi(NULL), tapi3EventNotification(NULL), currCallID(0), isDown(false), isUp(false) {
	logger = new Logger("MSTapi3");
	swapOnHold = false;
    handoff = L"";
	extensionPrefix = L"";
}

// destructor
MSTapi3::~MSTapi3() {
	logger->debug("MSTapi3 dtor.");
	ShutdownTapi();
	logger->debug("Deleting logger...");
	delete logger;
	logger = NULL;
}

list<wstring> MSTapi3::getAddressNames() {
	list<wstring> addressNames;
	for (AddressMap::iterator it = addresses.begin(); it != addresses.end(); ++it) {
		addressNames.push_back((*it).first);
	}
    addressNames.sort();
	return addressNames;
}

//////////////////////////////////////////////////////////////
// InitializeTapi
//
// Various tapi initializations
///////////////////////////////////////////////////////////////
HRESULT MSTapi3::InitializeTapi(CallbackNotification aCallback) {
    // cocreate the TAPI object
	logger->debug("InitializeTapi() called.");
    HRESULT hr = CoCreateInstance(
                          CLSID_TAPI,
                          NULL,
                          CLSCTX_INPROC_SERVER,
                          IID_ITTAPI,
                          (LPVOID *)&tapi
                         );
    if (FAILED(hr)) {
        logger->error("CoCreateInstance on TAPI failed: hr=%08X", hr);
        return hr;
    }
	logger->debug("CoCreateInstance on TAPI succeeded. Initializing TAPI...");

	callback = aCallback;
    // call initialize.  this must be called before
    // any other tapi functions are called.
    hr = tapi->Initialize();

    if (FAILED(hr)) {
        logger->error("TAPI failed to initialize");

        tapi->Release();
        tapi = NULL;        
        return hr;
    }
	logger->debug("tapi->Initialize() succeeded.");

    //
    // Create our own event notification object and register it
    //
    tapi3EventNotification = new TAPI3EventNotification(this, callback);
	logger->debug("tapi3EventNotification created.");
    
    hr = RegisterTapiEventInterface();
    if (FAILED(hr)) {
        logger->error("RegisterTapiEventInterface() failed.");
        tapi->Release();
        tapi = NULL;        
        return hr;
    }
	logger->debug("RegisterTapiEventInterface() succeeded.");

    // Set the Event filter to only give us the events we process
    tapi->put_EventFilter(/*TE_ADDRESS | */ TE_CALLNOTIFICATION | TE_CALLSTATE | TE_CALLHUB | TE_CALLINFOCHANGE | TE_DIGITEVENT | TE_GENERATEEVENT);
	// tapi->put_EventFilter(0x2FFFFFF);

    // find all address objects that we will use to listen for calls on
	logger->debug("Calling ListenOnAddresses...");
    hr = ListenOnAddresses();
    if (FAILED(hr)) {
        logger->error("Could not find any addresses to listen on");
        tapi->Release();
        tapi = NULL;
        return hr;
    }
	logger->debug("ListenOnAddresses() succeeded.");
    return S_OK;
}


///////////////////////////////////////////////////////////////
// ShutdownTapi
///////////////////////////////////////////////////////////////
void MSTapi3::ShutdownTapi() {
	if(isDown) {
		logger->warn("TAPI already shut down");
		return;
	}
	isDown = true;
	ReleaseAddresses();
	UnregisterCallNotifications();
	HRESULT hr = UnregisterTapiEventInterface();
	if(FAILED(hr)) {
		logger->error("UnregisterTapiEventInterface() failed: hr=%08X", hr);
	} else {
		logger->debug("UnregisterTapiEventInterface() succeeded");
	}

    // release main object.
    if (NULL != tapi) {
		logger->debug("Shutting down tapi object...");
		try {
			HRESULT hr = tapi->Shutdown();
		} catch(...) {
			logger->error("tapi->Shutdown() failed.");
		}
		logger->debug("Releasing tapi object...");
        tapi->Release();
		tapi = NULL;
		logger->debug("Tapi object shut down and released.");
    } else {
		logger->info("tapi object already NULL.");
	}

}



/////////////////////////////////////////////////////////////////
// MakeTheCall
//
// Sets up and makes a call
/////////////////////////////////////////////////////////////////
HRESULT MSTapi3::MakeTheCall(int callID, wstring& address, wstring& destination, int mode) {
	HRESULT hr = createCall(callID, address, destination);
	if(hr != S_OK) {
		return hr;
	}

	ITBasicCallControl* pCallControl = getCallControl(callID);
	if(pCallControl == NULL) {
		logger->warn("Call is null. Something went wrong...");
		return -1;
	}

	if(mode == TAPICALLCONTROLMODE_NONE)
	{
		// We're now ready to call connect.
		//
		// the VARIANT_TRUE parameter indicates that this call is sychronous - that is, it won't
		// return until the call is in the connected state (or fails to connect).	
		hr = pCallControl->Connect(VARIANT_TRUE);

		if (FAILED(hr)) {
			removeCallControl(callID);
			logger->error("Could not connect the call to %S on %S.", destination.c_str(), address.c_str());
			return hr;
		} else {
			logger->info("Successfully connected to %S on %S.", destination.c_str(), address.c_str());
		}
		hr = DetectDigits(pCallControl);
	}
	
    return S_OK;
}

/////////////////////////////////////////////////////////////////////
// Dial a number on the call
// This allows extra digits, such as long distance authorization codes, to
// be dialed on a call.
/////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::Dial(int callID, wstring& destination) {
	logger->debug("Dial(%d, %S)", callID, destination.c_str());

  // get a handle on the call control
	ITBasicCallControl* pCallControl = getCallControl(callID);
	if(pCallControl == NULL) {
    logger->warn("Error: Call is null.");
		return S_FALSE;
	} else {
		logger->debug("Ok: empty entry found for callID=%d.", callID);
	}

  // Now dial the digits on the call
  HRESULT hr = pCallControl->Dial(const_cast<unsigned short*>(destination.c_str()));

  // test for success. Don't remove callcontrol on failure -- we didn't create it.
  if (FAILED(hr)) {
    logger->error("Could not dial the call to %S.", destination.c_str());
    return hr;
  } else {
	  logger->info("Successfully dialed to %S.", destination.c_str());
	}

  return S_OK;
}

/////////////////////////////////////////////////////////////////////
// Answer the call
/////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::AnswerTheCall(int callID) {
	logger->debug("Answering callID=%d...", callID);
	ITBasicCallControl* callControl = getCallControl(callID);
    if (NULL == callControl) {
		logger->error("Cannot answer. CallID %d not found", callID);
        return E_UNEXPECTED;
    }

    // get the address object of this call
    ITCallInfo* pCallInfo;
	HRESULT hr = callControl->QueryInterface( IID_ITCallInfo, (void**)&pCallInfo );
    if(FAILED(hr)) {
		logger->error("Getting callInfo failed. hr=%08X", hr);
		removeCallControl(callID);
        return hr;
    }

    ITAddress* pAddress;
    hr = pCallInfo->get_Address( &pAddress );
    pCallInfo->Release();
    if (FAILED(hr)) {
		removeCallControl(callID);
		logger->error("get_Address() failed. hr=%08X", hr);
        return hr;
    }
	wstring strAddress;
    hr = getAddressName(pAddress, strAddress);
    if(FAILED(hr)) {
        pAddress->Release();
        return hr;
    }

    // Select our terminals on the call; if any of the selections fail, we proceed without that terminal
    hr = SelectTerminalsOnCall(pAddress, callControl);
    pAddress->Release();
    if(FAILED(hr)) {
		logger->error("SelectTerminalsOnCall() failed. hr=%08X. Answering without a terminal.", hr);
    }

    // Now we can actually answer the call
    hr = callControl->Answer();
    if(FAILED(hr)) {
		logger->error("Answer() failed. hr=%08X", hr);
    } else {
		logger->debug("Answer() successfully done.");
        DetectDigits(callControl);

		callback(net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_TERMINAL_CONNECTION_TALKING, callID, strAddress, 
				net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_JNI_CAUSE_NORMAL, NULL);
	}
    return hr;
}

//////////////////////////////////////////////////////////////////////
// HoldTheCall
//
// Holds the call
//////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::HoldTheCall(int callID) {
	logger->debug("Holding callID=%d...", callID);
	ITBasicCallControl* callControl = getCallControl(callID);
    if (NULL != callControl) {
		bool swapIt = swapOnHold;
		HRESULT hr;

		if(!swapIt) {
			hr = callControl->Hold(VARIANT_TRUE);
			logger->debug("Hold() returned %08X.", hr);
			if(hr == TAPI_E_NOTSUPPORTED) {
				logger->debug("Hold not supported. Trying to swap...");
				swapIt = true;
			}
		}
		if(swapIt) {
			bool found = false;
			CallMap::const_iterator it;
			for(it = calls.begin(); it != calls.end(); it++) {
				ITBasicCallControl* curCallControl = (*it).second;
				if(curCallControl == callControl) continue;

				ITCallInfo* pCallInfo;
				HRESULT hr1 = curCallControl->QueryInterface( IID_ITCallInfo, (void**)&pCallInfo );
				if(FAILED(hr1)) {
					logger->error("Getting callInfo failed. hr=%08X", hr1);
					continue;
				}
				CALL_STATE callState;
				hr1 = pCallInfo->get_CallState(&callState);
				pCallInfo->Release();
				if(FAILED(hr1)) {
					logger->error("Cannot retrieve call state: hr=%08X.", hr1);
					continue;
				}
				if(callState == CS_HOLD) {
					break;
				}
			}
			if(it == calls.end()) {
				logger->warn("Cannot swap. No call found in hold.");
			} else {
				logger->debug("Call %d found in hold. Swapping.", (*it).first);
				hr = callControl->SwapHold((*it).second);
			}
		}
        return hr;
    } else {
		logger->warn("Cannot hold. CallID %d not found.", callID);
		return E_FAIL;
	}
}

//////////////////////////////////////////////////////////////////////
// UnHoldTheCall
//
// Unholds the call
//////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::UnHoldTheCall(int callID) {
	logger->debug("Unholding callID=%d...", callID);
	ITBasicCallControl* callControl = getCallControl(callID);
    if (NULL != callControl) {
		bool swapIt = swapOnHold;
		HRESULT hr;
		if(!swapIt) {
			hr = callControl->Hold(VARIANT_FALSE);
			logger->debug("UnHold() returned %08X.", hr);
			if(hr == TAPI_E_NOTSUPPORTED) {
				logger->debug("UnHold not supported. Trying to swap...");
				swapIt = true;
			}
		}
		if(swapIt) {
			bool found = false;
			CallMap::const_iterator it;
			for(it = calls.begin(); it != calls.end(); it++) {
				ITBasicCallControl* curCallControl = (*it).second;
				if(curCallControl == callControl) continue;

				ITCallInfo* pCallInfo;
				HRESULT hr1 = curCallControl->QueryInterface( IID_ITCallInfo, (void**)&pCallInfo );
				if(FAILED(hr1)) {
					logger->error("Getting callInfo failed. hr=%08X", hr1);
					continue;
				}
				CALL_STATE callState;
				hr1 = pCallInfo->get_CallState(&callState);
				pCallInfo->Release();
				if(FAILED(hr1)) {
					logger->error("Cannot retrieve call state: hr=%08X.", hr1);
					continue;
				}
				if(callState == CS_CONNECTED) {
					break;
				}
			}
			if(it == calls.end()) {
				logger->warn("Cannot swap. No connected call found.");
			} else {
				logger->debug("Call %d found connected. Swapping.", (*it).first);
				hr = (*it).second->SwapHold(callControl);
			}
		}
        return hr;
    } else {
		logger->warn("Cannot unhold. CallID %d not found.", callID);
		return E_FAIL;
	}
}

//////////////////////////////////////////////////////////////////////
// JoinCalls
//
// Joins two calls
//////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::JoinCalls(int callID1, int callID2, wstring& address, wstring& terminal, int mode) {
	ITBasicCallControl* callControl1 = getCallControl(callID1);
	ITBasicCallControl* callControl2 = getCallControl(callID2);

	HRESULT hr;
	if(mode == TAPICALLCONTROLMODE_SETUPCONFERENCE)
	{
		hr = callControl1->Conference(callControl2, VARIANT_FALSE); //VARIANT_TRUE);
		if(FAILED(hr))
			logger->error("Conference setup failed: hr=%08X.", hr);
		else
			logger->debug("Conference setup succeeded.");
	}
	else if(mode == TAPICALLCONTROLMODE_CONFERENCE)
	{
		hr = callControl2->Finish(FM_ASCONFERENCE);
		if(FAILED(hr))
			logger->error("Finish conference failed: hr=%08X.", hr);
		else
			logger->debug("Finish conference succeeded.");
	}
	else if(mode == TAPICALLCONTROLMODE_SETUPTRANSFER)
	{
		hr = callControl1->Transfer(callControl2, VARIANT_FALSE); //VARIANT_TRUE);
		if(FAILED(hr))
			logger->error("Transfer setup failed: hr=%08X.", hr);
		else
			logger->debug("Transfer setup succeeded.");
	}
	else if(mode == TAPICALLCONTROLMODE_TRANSFER)
	{
		hr = callControl2->Finish(FM_ASTRANSFER);
		if(FAILED(hr))
			logger->error("Finish transfer failed: hr=%08X.", hr);
		else
			logger->debug("Finish transfer succeeded.");
	}
	else
		return E_FAIL;
    return hr;
	
	/*ITBasicCallControl* callControl1 = getCallControl(callID1);
	ITBasicCallControl* callControl2 = NULL;
	if(callID2 != -1)
		callControl2 = getCallControl(callID2);

	HRESULT hr;
	if(mode == 0)
	{
		hr = callControl1->Conference(callControl2, VARIANT_TRUE);
		if(FAILED(hr)) {
			logger->error("Conference() failed: hr=%08X.", hr);
		} 
		else {
			logger->debug("Conference() succeeded.");
			hr = callControl2->Finish(FM_ASCONFERENCE);
		}
	}
	else
	{	
		ITAddress2* pITAddress = (ITAddress2*)getITAddress(address);		
		pITAddress->CreateCall(const_cast<unsigned short*>(terminal.c_str()),
                                LINEADDRESSTYPE_PHONENUMBER, TAPIMEDIATYPE_AUDIO, &callControl2);		

		hr = callControl1->Transfer(callControl2, VARIANT_TRUE);
		if(FAILED(hr)) {
			logger->error("Transfer() failed: hr=%08X.", hr);
		} 
		else {
			logger->debug("Transfer() succeeded.");
			hr = callControl2->Finish(FM_ASTRANSFER);
		}
	}
    return hr;*/
}

/////////////////////////////////////////////////////////////////////
// Transfer the call to another number
// This is a blind transfer, without a consultation call.
/////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::BlindTransfer(int callID, wstring& destination) {
	logger->debug("Blind Transfer(%d, %S)", callID, destination.c_str());

  // get a handle on the call control
	ITBasicCallControl* pCallControl = getCallControl(callID);
	if(pCallControl == NULL) {
    logger->warn("Error: Call is null.");
		return S_FALSE;
	} else {
		logger->debug("Ok: empty entry found for callID=%d.", callID);
	}

  // Now perform the blind transfer
  HRESULT hr = pCallControl->BlindTransfer(const_cast<unsigned short*>(destination.c_str()));

  // test for success. Don't remove callcontrol on failure -- we didn't create it.
  if (FAILED(hr)) {
    logger->error("Could not blind transfer the call to %S.", destination.c_str());
    return hr;
  } else {
	  logger->info("Successfully blind transfered to %S.", destination.c_str());
	}

  return S_OK;
}

/////////////////////////////////////////////////////////////////////
// Start transfering the call to another number
// This is an assisted transfer, with a consultation call.
/////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::ConsultationStart(int callID, wstring& address, wstring& destination) {
	logger->debug("Assisted Transfer Start(%d, %S)", callID, destination.c_str());

  // get a handle on the call control
	ITBasicCallControl* pCallControl = getCallControl(callID);
	if(pCallControl == NULL) {
		logger->warn("Error: Call is null.");
		return -1;
	} else {
		logger->debug("Ok: empty entry found for callID=%d.", callID);
	}

	// create a new transfer call id
	int consultCallID = reserveCall(address);
	// create but don't place the call
	HRESULT hr = createCall(consultCallID, address, destination);
	if (FAILED(hr)) {
		logger->error("Could not create consultation call from %S to %S.", address.c_str(), destination.c_str());
		return -1;
	} else {
		logger->info("Successfully created consultation call from %S to %S.", address.c_str(), destination.c_str());
	}
	ITBasicCallControl* pConsultationCallControl = getCallControl(consultCallID);
	if(pCallControl == NULL) {
		logger->warn("Error: Consultation call is null.");
		return -1;
	}

	// Now initialize the transfer - synchronously
	hr = pCallControl->Transfer(pConsultationCallControl, VARIANT_TRUE);
	// test for success. Remove callcontrol on failure -- we created it.
	if (FAILED(hr)) {
		logger->error("Could not start transfer to %S: hr=%08X.", destination.c_str(), hr);
		ReleaseTheCall(consultCallID);
		return -1;
	} else {
		logger->info("Successfully dialed consultation call for transfer to %S.", destination.c_str());
	}

  return consultCallID;
}

/////////////////////////////////////////////////////////////////////
// Finish Transferring the call to another number
// This is an assisted transfer, with a consultation call.
/////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::AssistedTransferFinish(int callID) {
	logger->debug("Assisted Transfer Finish(%d, %S)", callID);
	  // get a handle on the call control
	ITBasicCallControl* pCallControl = getCallControl(callID);
	if(pCallControl == NULL) {
		logger->warn("Error: Call is null.");
		return S_FALSE;
	}

	// Now perform the transfer finish
	HRESULT hr = pCallControl->Finish(FM_ASTRANSFER);
	if(FAILED(hr))
		logger->error("Finish transfer failed: hr=%08X.", hr);
	else
		logger->debug("Finish transfer succeeded.");

	return S_OK;
}

/////////////////////////////////////////////////////////////////////
// Finish Conferencing the call to another number
// This is an assisted conference, with a consultation call.
/////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::ConferenceFinish(int callID) {
	logger->debug("Assisted Conference Finish(%d, %S)", callID);
	  // get a handle on the call control
	ITBasicCallControl* pCallControl = getCallControl(callID);
	if(pCallControl == NULL) {
		logger->warn("Error: Call is null.");
		return S_FALSE;
	}

	// Now perform the transfer finish
	HRESULT hr = pCallControl->Finish(FM_ASCONFERENCE);
	if(FAILED(hr))
		logger->error("Finish transfer failed: hr=%08X.", hr);
	else
		logger->debug("Finish transfer succeeded.");

	return S_OK;
}

//////////////////////////////////////////////////////////////////////
// DetectDigits
//
// detects digits
//////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::DetectDigits(ITBasicCallControl* pCallControl) {
	ITLegacyCallMediaControl* pLegacy;
	HRESULT hr = pCallControl->QueryInterface(IID_ITLegacyCallMediaControl, (void **) &pLegacy);
	if(FAILED(hr)) {
		logger->error("Cannot retrieve legacy: hr=%08X.", hr);
	} else {
		hr = pLegacy->DetectDigits(LINEDIGITMODE_DTMF);
		pLegacy->Release();
		if(FAILED(hr)) {
			logger->error("DetectDigits() failed: hr=%08X.", hr);
		} else {
			logger->debug("DetectDigits() succeeded.");
		}
	}
    return hr;
}

HRESULT MSTapi3::SendDigits(wstring terminal, wstring digits) {
	// using terminal as address !!!
	ITAddress* pAddr = getITAddress(terminal);
	if(pAddr == NULL) {
		return TAPI_E_NOITEMS;
	}
	IEnumCall* pCallEnum = NULL;
	HRESULT hr = pAddr->EnumerateCalls(&pCallEnum);
	if(pCallEnum == NULL) {
		logger->error("pCallEnum is NULL !!!");
		return TAPI_E_NOITEMS;
	}
	if(SUCCEEDED(hr)) {
		int count = 0;
		while(true) {
			ITCallInfo* callInfo;
			hr = pCallEnum->Next(1, &callInfo, NULL);
			if(FAILED(hr)) {
				logger->error("pCallEnum->Next() failed: hr=%08X.", hr);
				break;
			}
			if(hr == S_FALSE) {
				logger->debug("pCallEnum done.");
				hr = S_OK;
				break;
			}

			ITBasicCallControl* pCallControl;
			hr = callInfo->QueryInterface(IID_ITBasicCallControl, (void**)&pCallControl);
			if(SUCCEEDED(hr)) {
				if(pCallControl != NULL) {
					CALL_STATE callState;
					callInfo->get_CallState(&callState);
					if(callState == CS_CONNECTED) {
						ITLegacyCallMediaControl* pLegacy;
						hr = pCallControl->QueryInterface(IID_ITLegacyCallMediaControl, (void **) &pLegacy);
						if(FAILED(hr)) {
							logger->error("Cannot retrieve legacy: hr=%08X.", hr);
						} else {
							BSTR bstrDigits = SysAllocString(digits.c_str());
							hr = pLegacy->GenerateDigits(bstrDigits, LINEDIGITMODE_DTMF);
							SysFreeString(bstrDigits);
							pLegacy->Release();
							if(FAILED(hr)) {
								logger->error("GenerateDigits() failed: hr=%08X.", hr);
							} else {
								logger->debug("GenerateDigits() succeeded.");
							}
						}
					}
				} else {
					logger->debug("pCallControl is NULL.");
					hr = TAPI_E_NOITEMS;
				}
			}
			pCallControl->Release();
			callInfo->Release();
		}
	} else {
		logger->error("EnumerateCalls() failed with hr=%08X", hr);
	}
	return hr;
}

//////////////////////////////////////////////////////////////////////
// DisconnectTheCall
//
// Disconnects the call
//////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::DisconnectTheCall(int callID) {
	logger->debug("Disconnecting callID=%d...", callID);
	ITBasicCallControl* callControl = getCallControl(callID);
    if (NULL != callControl) {

        HRESULT hr = callControl->Disconnect(DC_NORMAL);
		logger->debug("Disconnect(DC_NORMAL): hr=%08X", hr);
        // Do not release the call yet, as that would prevent
        // us from receiving the disconnected notification.
        return hr;
    } else {
		logger->warn("Cannot disconnect. CallID %d not found.", callID);
		return E_FAIL;
	}
}

//////////////////////////////////////////////////////////////////////
// ReleaseTheCall
//
// Releases the call
//////////////////////////////////////////////////////////////////////
void MSTapi3::ReleaseTheCall(int callID) {
	logger->debug("Releasing callID=%d...", callID);
	removeCallControl(callID);
}

//////////////////////////////////////////////////////////////////////
// SendLineDevSpecific
// 
// Sends a LineDevSpecific action
// Note: Available since Tapi 3.1 -> WindowsXP or newer required
//////////////////////////////////////////////////////////////////////
long MSTapi3::SendLineDevSpecific(int callID, wstring& address, BYTE* bytes, DWORD bytesSize){
	ITAddress2* pITAddress = (ITAddress2*)getITAddress(address);
	ITCallInfo* pCallInfo = NULL;
	
	/*ITBasicCallControl* callControl = getCallControl(callID);
	
	if(callControl == NULL)
		pITAddress->CreateCall
		(NULL, LINEADDRESSTYPE_PHONENUMBER, TAPIMEDIATYPE_AUDIO, &callControl);

	if(callControl != NULL){
		HRESULT hr = callControl->QueryInterface( IID_ITCallInfo, (void**)&pCallInfo);
		if(FAILED(hr)){
			logger->error("Getting callInfo failed. hr=%08X", hr);
			pCallInfo = NULL;
		}
	}
	int result = pITAddress->DeviceSpecific(pCallInfo, bytes, bytesSize);
	if(callID == -1)
		callControl->Release();*/

	if(callID != -1){
		ITBasicCallControl* callControl = getCallControl(callID);
		if(callControl != NULL){
			HRESULT hr = callControl->QueryInterface( IID_ITCallInfo, (void**)&pCallInfo );
			if(FAILED(hr)){
				logger->error("Getting callInfo failed. hr=%08X", hr);
				pCallInfo = NULL;
			}
		}
	}

	HRESULT hr = pITAddress->DeviceSpecific(pCallInfo, bytes, bytesSize);
	return hr;
}






/////////////////////////////////////
// PRIVATE METHODS
/////////////////////////////////////

void MSTapi3::ReleaseCalls(ITAddress* pAddr) {
	IEnumCall* pCallEnum = NULL;
	if(pAddr == NULL) {
		logger->error("pAddr is NULL !!!");
		return;
	}
	HRESULT hr = pAddr->EnumerateCalls(&pCallEnum);
	if(pCallEnum == NULL) {
		logger->error("pCallEnum is NULL !!!");
		return;
	}
	if(SUCCEEDED(hr)) {
		while(true) {
			ITCallInfo* callInfo;
			hr = pCallEnum->Next(1, &callInfo, NULL);
			if(FAILED(hr)) {
				logger->error("pCallEnum->Next() failed: hr=%08X.", hr);
				break;
			}
			if(hr == S_FALSE) {
				logger->debug("pCallEnum done.");
				break;
			}
			ITBasicCallControl* pCallControl;
			hr = callInfo->QueryInterface(IID_ITBasicCallControl, (void**)&pCallControl);
			if(SUCCEEDED(hr)) {
				if(pCallControl != NULL) {
					int callID = getCallID(pCallControl);
					if(callID >= 0) {
						calls.erase(callID);
					}
					logger->debug("Disconnecting pCallControl...");
					//hr = pCallControl->Disconnect( DC_NORMAL );
					//logger->debug("Disconnected pCallControl.");
					logger->debug("Releasing pCallControl...");
					pCallControl->Release();
					logger->debug("pCallControl released.");
				} else {
					logger->debug("pCallControl is NULL.");
				}
			}
			logger->debug("Releasing callInfo...");
			callInfo->Release();
			logger->debug("callInfo released.");
		}
	} else {
		logger->error("EnumerateCalls() failed with hr=%08X", hr);
	}
}

void MSTapi3::ReleaseAddresses() {
	for (AddressMap::iterator it = addresses.begin(); it != addresses.end(); ++it) {
		ITAddress* address = (*it).second;
		if(address != NULL) {
			logger->info("Releasing calls on %S", (*it).first.c_str());
			ReleaseCalls(address);
			address->Release();
		} else {
			logger->warn("ITAddress is NULL for %S.", (*it).first.c_str());
		}
	}
	addresses.clear();
}

void MSTapi3::UnregisterCallNotifications() {
	for (list<long>::iterator it = callNotificationRegisters.begin(); it != callNotificationRegisters.end(); ++it) {
		HRESULT hr = tapi->UnregisterNotifications(*it);
		if(FAILED(hr)) {
			logger->error("UnregisterNotifications(%ld) failed: hr=%08X", *it, hr);
		} else {
			logger->debug("UnregisterNotifications(%ld) succeeded", *it);
		}
	}
	callNotificationRegisters.clear();

}


///////////////////////////////////////////////////////////////////////////
// RegisterTapiEventInterface()
///////////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::RegisterTapiEventInterface() {
    HRESULT                       hr = S_OK;
    IConnectionPointContainer   * pCPC;
    IConnectionPoint            * pCP;
    
    hr = tapi->QueryInterface(IID_IConnectionPointContainer, (void **)&pCPC);

    if ( FAILED(hr) ) {
        return hr;
    }

    hr = pCPC->FindConnectionPoint(IID_ITTAPIEventNotification, &pCP);
    pCPC->Release();
        
    if (FAILED(hr)) {
        return hr;
    }
    hr = pCP->Advise(tapi3EventNotification, &gulAdvise);
    pCP->Release();
    return hr;

}

///////////////////////////////////////////////////////////////////////////
// UnregisterTapiEventInterface()
///////////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::UnregisterTapiEventInterface() {
    HRESULT                       hr = S_OK;
    IConnectionPointContainer   * pCPC;
    IConnectionPoint            * pCP;
    

    hr = tapi->QueryInterface(IID_IConnectionPointContainer, (void **)&pCPC);

    if ( FAILED(hr) ) {
        return hr;
    }

    hr = pCPC->FindConnectionPoint(IID_ITTAPIEventNotification, &pCP);
    pCPC->Release();
        
    if (FAILED(hr)) {
        return hr;
    }
    hr = pCP->Unadvise(gulAdvise);
    pCP->Release();
    return hr;

}


////////////////////////////////////////////////////////////////////////
// ListenOnAddresses
//
// This procedure will find all addresses that support audioin and audioout
// and will call ListenOnThisAddress to start listening on it.
////////////////////////////////////////////////////////////////////////
HRESULT MSTapi3::ListenOnAddresses()
{
    HRESULT hr = S_OK;
    IEnumAddress* pEnumAddress;
    ITAddress* pAddress;

    // enumerate the addresses
    hr = tapi->EnumerateAddresses( &pEnumAddress );
    if (FAILED(hr)) {
		logger->error("EnumerateAddresses() failed: hr=%08X.", hr);
        return hr;
    }

    typedef map<wstring, list<ITAddress*> > TempAddressMap;
    TempAddressMap tempAddressMap;

    while (TRUE) {
        hr = pEnumAddress->Next( 1, &pAddress, NULL );
        if(FAILED(hr)) {
			logger->error("pEnumAddress->Next() failed: hr=%08X.", hr);
            break;
        }
		if(hr == S_FALSE) {
			logger->info("pEnumAddress done.");
			break;
		}

		BSTR bstrAddrName;
		hr = pAddress->get_AddressName(&bstrAddrName);
		if(FAILED(hr)) {
			logger->error("Cannot retrieve address name: hr=%08X.", hr);
			continue;
		}

		// See if the address name bstrAddrName matches the allowed names
		if(extensionPrefix.length() > 0) {
			std::wstring addrName(bstrAddrName);
			string::size_type location = addrName.find(extensionPrefix);
			if(location != 0) {		// == string::npos) {
				logger->debug("Address name %s did not match prefix %s", addrName, extensionPrefix);
				continue;
			}
		}

        tempAddressMap[bstrAddrName].push_back(pAddress);

		char addressName[256];
		sprintf(addressName, "%S", bstrAddrName);
		logger->info("Address %s added.", addressName);
		SysFreeString(bstrAddrName);

		// Is this line on our list of lines to be listened to?
		if(strstr(addressName, "EXT") != NULL) {
			logger->info("We should listen on this address.");
		}
        // does the address support audio?
        if (AddressSupportsMediaType(pAddress, TAPIMEDIATYPE_AUDIO)) {
            // If it does then we'll listen.
			long lRegister;
            hr = ListenOnThisAddress(pAddress, &lRegister);
            if (FAILED(hr)) {
                logger->error("Listen failed on address %s.", addressName);
            } else {
				callNotificationRegisters.push_back(lRegister);
                logger->info("Listener registered on address %s with register %ld.", addressName, lRegister);
			}
        } else {
			logger->info("Not listening on address %s...", addressName);
		}

		pAddress->Release();
    }
    pEnumAddress->Release();

    addresses.clear();
	for (TempAddressMap::iterator it = tempAddressMap.begin(); it != tempAddressMap.end(); ++it) {
        wstring addrName = (*it).first;
        list<ITAddress*> addrList = (*it).second;
        if(addrList.size() == 1) {
            addresses[addrName] = addrList.front();
        } else {
            int i=0;
            for(list<ITAddress*>::iterator addrIt = addrList.begin(); addrIt != addrList.end(); ++addrIt) {
                i++;
                char sIndex[8];
                wchar_t wIndex[8];
                sprintf(sIndex, "-#%d", i);
                mbstowcs(wIndex, sIndex, strlen(sIndex) + 1);
                addresses[addrName + wIndex] = *addrIt;
            }
        }
	}

	isUp = true;	// Now okay to start handling address events
    return S_OK;
}


///////////////////////////////////////////////////////////////////
// ListenOnThisAddress
//
// We call RegisterCallNotifications to inform TAPI that we want
// notifications of calls on this address. We already resistered
// our notification interface with TAPI, so now we are just telling
// TAPI that we want calls from this address to trigger events on
// our existing notification interface.
//    
///////////////////////////////////////////////////////////////////
HRESULT MSTapi3::ListenOnThisAddress(ITAddress* pAddress, long* pRegister) {
    //
    // RegisterCallNotifications takes a media type bitmap indicating
    // the set of media types we are interested in. We know the
    // address supports audio, but let's add in video as well
    // if the address supports it.
    //
    long lMediaTypes = TAPIMEDIATYPE_AUDIO;

    if(AddressSupportsMediaType(pAddress, TAPIMEDIATYPE_VIDEO)) {
        lMediaTypes |= TAPIMEDIATYPE_VIDEO;
    }

    HRESULT  hr;
    hr = tapi->RegisterCallNotifications(
                                           pAddress,
                                           VARIANT_TRUE,
                                           VARIANT_TRUE,
                                           lMediaTypes,
                                           gulAdvise,
                                           pRegister
                                          );
    return hr;
}

/////////////////////////////////////////////////////////////////
// IsVideoCaptureStream
//
// Returns true if the stream is for video capture
/////////////////////////////////////////////////////////////////
BOOL MSTapi3::IsVideoCaptureStream(ITStream * pStream) {
    TERMINAL_DIRECTION tdStreamDirection;
    long               lStreamMediaType;

    if ( FAILED( pStream  ->get_Direction(&tdStreamDirection)   ) ) { return FALSE; }
    if ( FAILED( pStream  ->get_MediaType(&lStreamMediaType)    ) ) { return FALSE; }

    return (tdStreamDirection == TD_CAPTURE) &&
           (lStreamMediaType  == TAPIMEDIATYPE_VIDEO);
}

/////////////////////////////////////////////////////////
// GetVideoRenderTerminal
//
// Creates a dynamic terminal for the Video Render mediatype / direction
//
/////////////////////////////////////////////////////////
HRESULT MSTapi3::GetVideoRenderTerminal(
                   ITAddress   * pAddress,
                   ITTerminal ** ppTerminal
                  )
{
    //
    // Construct a BSTR for the correct IID.
    //
    LPOLESTR            lpTerminalClass;
    HRESULT             hr;

    hr = StringFromIID(CLSID_VideoWindowTerm, &lpTerminalClass);
    if (SUCCEEDED(hr)) {
        BSTR bstrTerminalClass;

        bstrTerminalClass = SysAllocString ( lpTerminalClass );

        CoTaskMemFree( lpTerminalClass );

        if ( bstrTerminalClass == NULL ) {
            hr = E_OUTOFMEMORY;
        }
        else {
            // Get the terminal support interface
            ITTerminalSupport * pTerminalSupport;
            hr = pAddress->QueryInterface(IID_ITTerminalSupport, (void **)&pTerminalSupport);
            if (SUCCEEDED(hr)) {
                // Create the video render terminal.
                hr = pTerminalSupport->CreateTerminal(bstrTerminalClass,
                                                      TAPIMEDIATYPE_VIDEO,
                                                      TD_RENDER,
                                                      ppTerminal);
                pTerminalSupport->Release();
            }
            SysFreeString( bstrTerminalClass );
        }
    }
    return hr;
}

/////////////////////////////////////////////////////////////////
// EnablePreview
//
// Selects a video render terminal on a video capture stream,
// thereby enabling video preview.
/////////////////////////////////////////////////////////////////
HRESULT MSTapi3::EnablePreview(ITAddress* pAddress, ITStream* pStream) {
    ITTerminal* pTerminal;
    HRESULT hr = GetVideoRenderTerminal(pAddress, &pTerminal);
    if(FAILED(hr)) {
		logger->error("GetVideoRenderTerminal() failed: hr=%08X.", hr);
	} else {
        hr = pStream->SelectTerminal(pTerminal);
        pTerminal->Release();
    }
    return hr;
}

/////////////////////////////////////////////////////////
// GetTerminal
//
// Creates the default terminal for the passed-in stream.
/////////////////////////////////////////////////////////
HRESULT MSTapi3::GetTerminal(ITAddress* pAddress, ITStream* pStream, ITTerminal** ppTerminal) {
    // Determine the media type and direction of this stream.
    
    HRESULT hr;
    long lMediaType;
    TERMINAL_DIRECTION dir;

    hr = pStream->get_MediaType( &lMediaType );
    if ( FAILED(hr) ) return hr;

    hr = pStream->get_Direction( &dir );
    if ( FAILED(hr) ) return hr;

    // Since video render is a dynamic terminal, the procedure for creating it is different.    
    if (( lMediaType == TAPIMEDIATYPE_VIDEO) && (dir == TD_RENDER)) {
        return GetVideoRenderTerminal(pAddress, ppTerminal);
    }

    // For all other terminals we use GetDefaultStaticTerminal.
    // First, get the terminal support interface.
    ITTerminalSupport * pTerminalSupport;
    hr = pAddress->QueryInterface( IID_ITTerminalSupport, (void **)&pTerminalSupport);
    if (SUCCEEDED(hr)) {
        // get the default terminal for this MediaType and direction
        hr = pTerminalSupport->GetDefaultStaticTerminal(lMediaType, dir, ppTerminal);
        pTerminalSupport->Release();
    }
    return hr;
}

/////////////////////////////////////////////////////////////////
// SelectTerminalsOnCall
//
// Selects a given terminal on the first compatible stream that exists on the given call.
/////////////////////////////////////////////////////////////////
HRESULT MSTapi3::SelectTerminalsOnCall(ITAddress* pAddress, ITBasicCallControl* pCall) {
    // get the ITStreamControl interface for this call
    ITStreamControl* pStreamControl;
    HRESULT hr = pCall->QueryInterface(IID_ITStreamControl, (void **) &pStreamControl);
    if (FAILED(hr)) {
		logger->error("Interface ITStreamControl not supported: hr=%08X", hr);
	} else {
        // enumerate the streams
        IEnumStream * pEnumStreams;
        hr = pStreamControl->EnumerateStreams(&pEnumStreams);
        pStreamControl->Release();
        if(FAILED(hr)){
			logger->error("EnumerateStreams() failed: hr=%08X", hr);
		} else {
            // for each stream
            ITStream* pStream;
            while (S_OK == pEnumStreams->Next(1, &pStream, NULL)) {
                ITTerminal* pTerminal;
                // Find out the media type and direction of this stream, and
                // create the default terminal for this media type and direction.
                hr = GetTerminal(pAddress, pStream, &pTerminal);
                if(FAILED(hr)) {
					logger->error("GetTerminal() failed: hr=%08X", hr);
				} else {

		            BSTR bstrTermName;
                    pTerminal->get_Name(&bstrTermName);
		            logger->debug("Selecting terminal %S...", bstrTermName);
		            SysFreeString(bstrTermName);

                    // Select the terminal on the stream.
                    hr = pStream->SelectTerminal(pTerminal);
                    if (FAILED(hr)) {
						logger->error("SelectTerminal() failed: hr=%08X", hr);
					} else {
                        // Also enable preview on the video capture stream.
                        if (IsVideoCaptureStream( pStream )) {
                            EnablePreview(pAddress, pStream);
                        }
                    }
                    pTerminal->Release();
                }
                pStream->Release();
            }
            pEnumStreams->Release();
        }
    }
    return hr;
}
//////////////////////////////////////////////////////////////
// AddressSupportsMediaType
//
// Finds out if the given address supports the given media
// type, and returns TRUE if it does.
//////////////////////////////////////////////////////////////
BOOL MSTapi3::AddressSupportsMediaType(ITAddress* pAddress, long lMediaType)
{
    VARIANT_BOOL     bSupport = VARIANT_FALSE;
    ITMediaSupport * pMediaSupport;
    
    if(SUCCEEDED(pAddress->QueryInterface(IID_ITMediaSupport, (void **)&pMediaSupport))) {
        // does it support this media type?
        pMediaSupport->QueryMediaType(lMediaType, &bSupport);    
        pMediaSupport->Release();
    }
    return (bSupport == VARIANT_TRUE);
}

int MSTapi3::reserveCall(wstring& address) {
	logger->debug("reserveCall() called for %S", address.c_str());
	currCallID++;
	calls[currCallID] = NULL;
	logger->debug("reserveCall() returning callID=%d for %S", currCallID, address.c_str());
	return currCallID;
}

// returns the callID
int MSTapi3::putCall(ITBasicCallControl* pCallControl) {
	currCallID++;
	calls[currCallID] = pCallControl;
	logger->debug("putCall(%d, %p).", currCallID, pCallControl);
	return currCallID;
}

ITBasicCallControl* MSTapi3::getCallControl(int callID) {
	CallMap::iterator it = calls.find(callID);
	if(it != calls.end()) {
		return (*it).second;
	}
	logger->warn("getCallControl(): No call found for callID=%d", callID);
	return NULL;
}

void MSTapi3::removeCallControl(int callID) {
	CallMap::iterator it = calls.find(callID);
	if(it != calls.end()) {
		ITBasicCallControl* pCallControl = (*it).second;
		calls.erase(it);
		if(pCallControl != NULL) {
			pCallControl->Release();
			logger->debug("CallControl released for callID=%d.", callID);
		} else {
			logger->debug("Call reserved but not initialized for callID=%d", callID);
		}
	} else {
		logger->warn("removeCallControl(): No call found for callID=%d", callID);
	}
}

int MSTapi3::getCallID(ITBasicCallControl* pCallControl) {
	for(CallMap::reverse_iterator it = calls.rbegin(); it != calls.rend(); ++it) {
		if((*it).second == pCallControl) {
			int callID = (*it).first;
			logger->debug("getCallID() found callID=%d", callID);
			return callID;
		}
	}
	logger->debug("getCallID(): callID not found.");
	return -1;
}

// Returns -callID if this is a new call
int MSTapi3::getOrCreateCallID(ITBasicCallControl* pCallControl) {
	for(CallMap::reverse_iterator it = calls.rbegin(); it != calls.rend(); ++it) {
		if((*it).second == pCallControl) {
			int callID = (*it).first;
			logger->debug("getOrCreateCallID() found callID=%d", callID);
			return callID;
		}
	}
	int callID = putCall(pCallControl);
	logger->debug("getOrCreateCallID(): call not found. Created new callID=%d.", callID);
	return -callID;
}

ITAddress* MSTapi3::getITAddress(wstring& address) {
	AddressMap::iterator it = addresses.find(address);
	if(it == addresses.end()) {
		logger->warn("Address %S not found", address);
		return NULL;
	}
	ITAddress* pITAddress = (*it).second;
	return pITAddress;
}

HRESULT MSTapi3::getAddressName(ITAddress* pITAddress, wstring& strAddress) {
    HRESULT hr = 0;
    strAddress = L"";
    for (AddressMap::iterator it = addresses.begin(); it != addresses.end(); ++it) {
        if(pITAddress == (*it).second) {
            strAddress = (*it).first;
            break;
        }
	}
	if(strAddress.length() == 0) {
	    BSTR bstrAddrName;
	    hr = pITAddress->get_AddressName(&bstrAddrName);
	    if(FAILED(hr)) {
		    logger->error("get_AddressName() failed: hr=%08X", hr);
        } else {
	        strAddress = bstrAddrName;
	        SysFreeString(bstrAddrName);
            logger->error("Address name alias not found in address map. Using real name '%S'", strAddress.c_str());
        }
    }
	return hr;
}

/**
 * Utility function for creating a call
 **/
HRESULT MSTapi3::createCall(int callID, wstring& address, wstring& destination) {
	ITAddress* pITAddress = getITAddress(address);
	if(pITAddress == NULL) {
		logger->error("Address not found: %S", address.c_str());
		return -1;
	}

    // find out which media types this address supports
    long lMediaTypes = 0;
    if ( AddressSupportsMediaType(pITAddress, TAPIMEDIATYPE_AUDIO) ) {
        lMediaTypes |= TAPIMEDIATYPE_AUDIO; // we will use audio
    }

    if ( AddressSupportsMediaType(pITAddress, TAPIMEDIATYPE_VIDEO) ) {
        lMediaTypes |= TAPIMEDIATYPE_VIDEO; // we will use video
    }

	logger->debug("createCall(%d, %S, %S)", callID, address.c_str(), destination.c_str());

	ITBasicCallControl* pCallControl = getCallControl(callID);
	if(pCallControl != NULL) {
		logger->warn("Call not null. Releasing old call...");
		removeCallControl(callID);
	} else {
		logger->debug("Ok: empty entry found for callID=%d.", callID);
	}
	
    // Create the call.
    HRESULT hr = pITAddress->CreateCall(const_cast<unsigned short*>(destination.c_str()),
                                LINEADDRESSTYPE_PHONENUMBER, lMediaTypes, &pCallControl);
    if (FAILED(hr)) {
        logger->error("Could not create a call to %S on %S.", destination, address);
        return hr;
    }
	calls[callID] = pCallControl;
	logger->debug("Call successfully created.");

    // Select our terminals on the call; if any of the selections fail, we proceed without that terminal.
    hr = SelectTerminalsOnCall(pITAddress, pCallControl);
	if(FAILED(hr)) {
		logger->warn("SelectTerminalsOnCall() failed: hr=%08X. Trying to make the call without a terminal.", hr);
	}

    ITCallInfo* pCallInfo;
	hr = pCallControl->QueryInterface( IID_ITCallInfo, (void**)&pCallInfo );
    if(FAILED(hr)) {
		logger->error("Getting callInfo failed. hr=%08X. Cannot set CIS_CALLEDIDNUMBER", hr);
    } else {
		BSTR bstrDestination = SysAllocString(destination.c_str());
		hr = pCallInfo->put_CallInfoString(CIS_CALLEDIDNUMBER, bstrDestination);
		if(FAILED(hr)) {
			logger->error("Cannot set CIS_CALLEDIDNUMBER. hr=%08X", hr);
		}
		pCallInfo->Release();
		SysFreeString(bstrDestination);
	}

	return S_OK;
}
