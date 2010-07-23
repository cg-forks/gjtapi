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
#include <tapi3.h>
#include <control.h>
#include <strmif.h>

#include "TAPI3EventNotification.h"
#include "MSTapi3.h"
#include "Util.h"

TAPI3EventNotification::TAPI3EventNotification(MSTapi3* aMSTapi3, CallbackNotification aCallback) : 
		pTapi3(aMSTapi3), callback(aCallback) {
	logger = new Logger("TAPI3EventNotification");
}
// destructor
TAPI3EventNotification::~TAPI3EventNotification() {
	delete logger;
}
                
///////////////////////////////////////////////////////////////////
// CallEventNotification
//
// The only method in the ITCallEventNotification interface.  This gets
// called by TAPI 3.0 when there is a call event to report. This just
// posts the message to our UI thread, so that we do as little as
// possible on TAPI's callback thread.
//
///////////////////////////////////////////////////////////////////
HRESULT STDMETHODCALLTYPE TAPI3EventNotification::Event(TAPI_EVENT tapiEvent, IDispatch* pEvent) {
	try {
		if(!pTapi3->isUp) {
			logger->warn("TAPI3EventNotification: TAPI not fully initialized.");
			return S_FALSE;
		}
		if(pTapi3->isDown) {
			logger->warn("TAPI3EventNotification: TAPI already shut down.");
			return S_FALSE;
		}
		HRESULT hr;
		switch (tapiEvent) {
			case TE_CALLNOTIFICATION: {
				logger->debug("TE_CALLNOTIFICATION");
				// TE_CALLNOTIFICATION means that the application is being notified of a new call.
				ITCallNotificationEvent* pNotify;
				hr = pEvent->QueryInterface( IID_ITCallNotificationEvent, (void **)&pNotify );

				if (FAILED(hr)) {
					logger->error("Incoming call, but failed to get the interface");
				} else {
					logger->debug("Incoming call.");

					ITCallInfo* pCall;
					hr = pNotify->get_Call( &pCall );
					pNotify->Release();
					if (FAILED(hr)) {
						logger->error("Cannot retrieve call: hr=%08X.", hr);
						return hr;
					}

					// check to see if we own the call
					CALL_PRIVILEGE cp;
					hr = pCall->get_Privilege( &cp );
//					if ( FAILED(hr) /* || (CP_OWNER != cp) */ ) {
					if ( FAILED(hr) || (CP_OWNER != cp) ) {
						// just ignore it if we don't own it
						logger->debug("Ignored event (we don't own it): hr=%08X.", hr);
						// logger->debug("Ignored event (failed to get privilege): hr=%08X.", hr);
						pCall->Release();
						return S_OK;
					}

					CALL_STATE callState;
					hr = pCall->get_CallState(&callState);
					if(FAILED(hr)) {
						logger->error("Cannot retrieve call state: hr=%08X.", hr);
						pCall->Release();
						return hr;
					}
					logger->debug("Call state: %08X.", callState);

					wstring strAddress;
					hr = getAddress<ITCallInfo>(logger, pTapi3, pCall, strAddress);
					if(FAILED(hr)) {
						pCall->Release();
						return hr;
					}
					logger->info("CallNotification on address %S.", strAddress.c_str());						

					ITBasicCallControl* pCallControl;
					hr = pCall->QueryInterface( IID_ITBasicCallControl, (void**)&pCallControl );
					if(FAILED(hr)) {
						logger->error("Error retrieving CallControl: hr=%08X", hr);
						pCall->Release();
						return hr;
					}

                    if(pTapi3->handoff.length() > 0) {
                        BSTR bstrHandoff = SysAllocString(pTapi3->handoff.c_str());
                        hr = pCallControl->HandoffDirect(bstrHandoff);
	                    SysFreeString(bstrHandoff);
                        if(FAILED(hr)) {
					        logger->warn("HandoffDirect(%S) failed: hr=%08X", pTapi3->handoff.c_str(), hr);
                        } else {
					        logger->debug("HandoffDirect(%S) succeeded", pTapi3->handoff.c_str());
						    pCall->Release();
						    return hr;
                        }
                    }

					int callID = pTapi3->getOrCreateCallID(pCallControl);
					if(callID > 0) {
						pCallControl->Release();
					} else {
						callID = -callID;
					}
					wstring callInfo[4];
					bool hasCallInfo = getCallInfo(logger, pCall, callInfo);
					pCall->Release();
					int method = -1;
					switch(callState) {
						//case CS_IDLE: 
						//case CS_INPROGRESS: 
						case CS_CONNECTED: method = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_TERMINAL_CONNECTION_TALKING; break;
						//case CS_DISCONNECTED: 
						case CS_OFFERING: method = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_TERMINAL_CONNECTION_RINGING; break;
						case CS_HOLD: method = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_TERMINAL_CONNECTION_HELD; break;
						//case CS_QUEUED:
						default: break;
					}
					if(method == -1) {
						logger->warn("No callback for callState=%08X", callState);
					} else {
						logger->debug("Calling callback for method %d on %S with callID=%d", method, strAddress.c_str(), callID);
						callback(method, callID, strAddress, net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_JNI_CAUSE_NEW_CALL, 
							(hasCallInfo ? callInfo : NULL));
					}
				}
				break;
			}
        
			case TE_CALLSTATE: {
				logger->debug("TE_CALLSTATE");
				// TE_CALLSTATE is a call state event.  pEvent is an ITCallStateEvent
				ITCallStateEvent* pCallStateEvent;

				// Get the interface
				hr = pEvent->QueryInterface( IID_ITCallStateEvent, (void **)&pCallStateEvent );
				if (FAILED(hr)) {
					logger->error("Cannot get interface IID_ITCallStateEvent: hr=%08X", hr);
					return hr;
				}

				// get the CallState that we are being notified of.
				CALL_STATE cs;
				hr = pCallStateEvent->get_State(&cs);
				if (FAILED(hr)) {
					logger->error("get_State() failed: hr=%08X", hr);
					pCallStateEvent->Release();
					return hr;
				}

				ITCallInfo* pCall;
				hr = pCallStateEvent->get_Call(&pCall);
				if (FAILED(hr)) {
					logger->error("get_Call() failed: hr=%08X", hr);
					pCallStateEvent->Release();
					return hr;
				}

				wstring strAddress;
				hr = getAddress<ITCallInfo>(logger, pTapi3, pCall, strAddress);
				if(FAILED(hr)) {
					pCall->Release();
					pCallStateEvent->Release();
					return hr;
				}			
				logger->info("CallState on address %S.", strAddress.c_str());

				ITBasicCallControl* pCallControl;
				hr = pCall->QueryInterface(IID_ITBasicCallControl, (void**)&pCallControl);
				if (FAILED(hr)) {
					logger->error("Interface ITBasicCallControl not supported: hr=%08X.", hr);
					pCall->Release();
					pCallStateEvent->Release();
					return hr;
				}
				int callID = pTapi3->getCallID(pCallControl);
				pCallControl->Release();

				wstring callInfo[4];
				bool hasCallInfo = getCallInfo(logger, pCall, callInfo);

				pCall->Release();
				pCallStateEvent->Release();
				logger->debug("CallState %d for callID %d.", cs, callID);
				int methodID = -1;
				switch(cs) {
					case CS_OFFERING: methodID = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_CONNECTION_ALERTING; break;
					case CS_DISCONNECTED: pTapi3->ReleaseTheCall(callID); methodID = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_CONNECTION_DISCONNECTED; break;
					//case CS_CONNECTED: methodID = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_CONNECTION_CONNECTED; break;
					case CS_CONNECTED: methodID = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_TERMINAL_CONNECTION_TALKING; break;
					// case CS_IDLE: logger->info("No callback for CS_IDLE."); break;
					case CS_INPROGRESS : methodID = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_CONNECTION_IN_PROGRESS; break;
					case CS_HOLD: methodID = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_TERMINAL_CONNECTION_HELD; break;
					// case CS_QUEUED: methodID = net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_CONNECTION_???; break;
				}
				if(methodID == -1) {
					logger->warn("No callback for cs=%d.", cs);
				} else {
					// logger->debug("Calling callback for %d on %S with callID=%d", methodID, strAddress.c_str(), callID);
//					callback(methodID, callID, strAddress, net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_JNI_CAUSE_NORMAL, NULL);
					callback(methodID, callID, strAddress, net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_JNI_CAUSE_NORMAL, (hasCallInfo ? callInfo : NULL));                    
				}
				break;
			}
			case TE_ADDRESS: {
				logger->debug("TE_ADDRESS");
				ITAddressEvent* pAddressEvent;
				hr = pEvent->QueryInterface( IID_ITAddressEvent, (void **)&pAddressEvent );
				if (FAILED(hr)) {
					logger->error("Address changed, but failed to get the interface");
				}
				else {

					wstring strAddress;
					hr = getAddress<ITAddressEvent>(logger, pTapi3, pAddressEvent, strAddress);
					if(FAILED(hr)) {
						pAddressEvent->Release();
						return hr;
					}			
					logger->info("Address changed event on %S.", strAddress.c_str());						

					logger->debug("Calling get_Event()....");
					ADDRESS_EVENT event;
					hr = pAddressEvent->get_Event(&event);
					if(FAILED(hr)) {
						logger->error("get_Event() failed: hr=%08X", hr);
						pAddressEvent->Release();
						return hr;
					}

					wstring strTerminal;
					hr = getTerminalName<ITAddressEvent>(logger, pAddressEvent, strTerminal);
					if(SUCCEEDED(hr)) {
						logger->info("TERMINAL NAME: %S.", strTerminal.c_str());
					}			

					pAddressEvent->Release();

					wstring info = L"Event=";
					switch(event) {
						case AE_STATE: info += L"AE_STATE"; break;
						case AE_CAPSCHANGE: info += L"AE_CAPSCHANGE"; break;
						case AE_RINGING: info += L"AE_RINGING"; break;
						case AE_CONFIGCHANGE: info += L"AE_CONFIGCHANGE"; break;
						case AE_FORWARD: info += L"AE_FORWARD"; break;
						case AE_NEWTERMINAL: info += L"AE_NEWTERMINAL"; break;
						case AE_REMOVETERMINAL: info += L"AE_REMOVETERMINAL"; break;
						case AE_MSGWAITON: info += L"AE_MSGWAITON"; break;
						case AE_MSGWAITOFF: info += L"AE_MSGWAITOFF"; break;
						default: info += L"<UNKNOWN>"; break;
					}
					logger->info("AddressEvent=%08X (%S)", event, info.c_str());
				}
				break;
			}
			case TE_CALLHUB: {
				logger->debug("TE_CALLHUB");
				ITCallHubEvent* pCallHubEvent;
				hr = pEvent->QueryInterface( IID_ITCallHubEvent, (void **)&pCallHubEvent );
				if (FAILED(hr)) {
					logger->error("CallHub changed, but failed to get the interface");
				}
				else {
					logger->debug("CallHub event.");
					CALLHUB_EVENT callHubEvent;
					hr = pCallHubEvent->get_Event(&callHubEvent);
					if(FAILED(hr)) {
						logger->error("get_Event() failed: hr=%08X.", hr);
						pCallHubEvent->Release();
						return hr;
					}
					char* hubEventName[] = {"CHE_CALLJOIN", "CHE_CALLLEAVE", "CHE_CALLHUBNEW", "CHE_CALLHUBIDLE"};
					logger->info("CALLHUB_EVENT = %08X (%s).", callHubEvent, hubEventName[callHubEvent]);

					ITCallInfo* pCall;
					hr = pCallHubEvent->get_Call(&pCall);
					if (FAILED(hr)) {
						logger->error("get_Call() failed: hr=%08X.", hr);
						pCallHubEvent->Release();
						return hr;
					} else if(pCall == NULL) {
						logger->debug("No call associated with this event.");
						pCallHubEvent->Release();
						return hr;
					}

					ITBasicCallControl* pCallControl;
					hr = pCall->QueryInterface( IID_ITBasicCallControl, (void**)&pCallControl );
					if(FAILED(hr)) {
						logger->error("Error retrieving CallControl: hr=%08X", hr);
					    pCallHubEvent->Release();
						pCall->Release();
						return hr;
					}
					int callID = pTapi3->getOrCreateCallID(pCallControl);
					if(callID > 0) {
						pCallControl->Release();
					} else {
						callID = -callID;
					}
					
					wstring callInfo[4];
					if(getCallInfo(logger, pCall, callInfo)) {
						wstring strAddress;
						hr = getAddress<ITCallInfo>(logger, pTapi3, pCall, strAddress);
						if(SUCCEEDED(hr)) {
							callback(net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_CALL_PRIVATE_DATA, 
								callID, strAddress, net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_JNI_CAUSE_NORMAL, callInfo);
						}
					}
					pCall->Release();
					pCallHubEvent->Release();
				}
				break;
			}
			case TE_CALLINFOCHANGE: {
				logger->debug("TE_CALLINFOCHANGE");
				ITCallInfoChangeEvent* pCallInfoChangeEvent;
				hr = pEvent->QueryInterface( IID_ITCallInfoChangeEvent, (void **)&pCallInfoChangeEvent );
				if (FAILED(hr)) {
					logger->error("CallInfo changed, but failed to get the interface");
				}
				else {
					logger->debug("CallInfoChanged event.");

					CALLINFOCHANGE_CAUSE cause;
					hr = pCallInfoChangeEvent->get_Cause(&cause);
					if (FAILED(hr)) {
						logger->error("get_Cause() failed: hr=%08X", hr);
						pCallInfoChangeEvent->Release();
						return hr;
					}
					char* causeName[] = {"CIC_OTHER",
										"CIC_DEVSPECIFIC",
										"CIC_BEARERMODE",
										"CIC_RATE",
										"CIC_APPSPECIFIC",
										"CIC_CALLID",
										"CIC_RELATEDCALLID",
										"CIC_ORIGIN",
										"CIC_REASON",
										"CIC_COMPLETIONID",
										"CIC_NUMOWNERINCR",
										"CIC_NUMOWNERDECR",
										"CIC_NUMMONITORS",
										"CIC_TRUNK",
										"CIC_CALLERID",
										"CIC_CALLEDID",
										"CIC_CONNECTEDID",
										"CIC_REDIRECTIONID",
										"CIC_REDIRECTINGID",
										"CIC_USERUSERINFO",
										"CIC_HIGHLEVELCOMP",
										"CIC_LOWLEVELCOMP",
										"CIC_CHARGINGINFO",
										"CIC_TREATMENT",
										"CIC_CALLDATA",
										"CIC_PRIVILEGE",
										"CIC_MEDIATYPE"
					};
					logger->debug("cause=%08X (%s)", cause, causeName[cause]);

					ITCallInfo* pCallInfo;
					hr = pCallInfoChangeEvent->get_Call(&pCallInfo);
					pCallInfoChangeEvent->Release();
					if (FAILED(hr)) {
						logger->error("get_Call() failed: hr=%08X", hr);
						return hr;
					}

					ITBasicCallControl* pCallControl;
					hr = pCallInfo->QueryInterface(IID_ITBasicCallControl, (void**)&pCallControl);
					if (FAILED(hr)) {
						logger->error("Interface ITBasicCallControl not supported: hr=%08X.", hr);
						pCallInfo->Release();
						return hr;
					}
					int callID = pTapi3->getCallID(pCallControl);

					wstring strAddress;
					hr = getAddress<ITCallInfo>(logger, pTapi3, pCallInfo, strAddress);
					pCallControl->Release();
					if(FAILED(hr)) {
						pCallInfo->Release();
						return hr;
					}

					wstring callInfo[4];
					if(getCallInfo(logger, pCallInfo, callInfo)) {
						wstring strAddress;
						hr = getAddress<ITCallInfo>(logger, pTapi3, pCallInfo, strAddress);
						if(SUCCEEDED(hr)) {
							callback(net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_CALL_PRIVATE_DATA, 
								callID, strAddress, net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_JNI_CAUSE_NORMAL, callInfo);
						}
					}
					pCallInfo->Release();
				}
				break;
			}
			case TE_DIGITEVENT: {
				logger->debug("TE_DIGITEVENT");
				ITDigitDetectionEvent* pDigitEvent;
				hr = pEvent->QueryInterface( IID_ITDigitDetectionEvent, (void **)&pDigitEvent);
				if (FAILED(hr)) {
					logger->error("DigitDetection changed, but failed to get the interface");
				}
				else {
					ITCallInfo* pCallInfo;
					hr = pDigitEvent->get_Call(&pCallInfo);
					if (FAILED(hr)) {
						logger->error("Failed to retrieve call info: hr=%08X.", hr);
						pCallInfo->Release();
						return hr;
					}
					wstring strAddress;
					hr = getAddress<ITCallInfo>(logger, pTapi3, pCallInfo, strAddress);
					if(FAILED(hr)) {
						pCallInfo->Release();
						return hr;
					}

                    unsigned char digit;
                    hr = pDigitEvent->get_Digit(&digit);
					if (FAILED(hr)) {
						logger->error("Failed to retrieve digit: hr=%08X.", hr);
						pCallInfo->Release();
						return hr;
					}
                    logger->info("Digit received on address %S: '%c' (%d)", strAddress.c_str(), digit, digit);

					ITBasicCallControl* pCallControl;
					hr = pCallInfo->QueryInterface(IID_ITBasicCallControl, (void**)&pCallControl);
					if (FAILED(hr)) {
						logger->error("Interface ITBasicCallControl not supported: hr=%08X.", hr);
						pCallInfo->Release();
						return hr;
					}
					int callID = pTapi3->getCallID(pCallControl);
					callback(net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider_METHOD_MEDIA_DIGIT_RECEIVED, 
						callID, strAddress, digit, NULL);
                    
                    pCallInfo->Release();
				}
				break;
			}
			default:
				logger->warn("tapiEvent=%08X", tapiEvent);
				break;
		}	
		return S_OK;
	} catch(...)  {
		logger->fatal("Exception thrown in TAPI3EventNotification::Event.");
		return E_FAIL;
	}
}
