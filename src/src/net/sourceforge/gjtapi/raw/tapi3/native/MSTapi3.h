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
#ifndef __MSTAPI3_H__
#define __MSTAPI3_H__

#include "Logger.h"
#include "TAPI3EventNotification.h"
#include <tapi3.h>
#include <list>
#include <map>

class MSTapi3 {
	friend class TAPI3EventNotification;
private:
	Logger* logger;
	ITTAPI* tapi;
	CallbackNotification callback;
	TAPI3EventNotification* tapi3EventNotification;
	ULONG gulAdvise;
	bool swapOnHold;
	wstring handoff;
	wstring extensionPrefix;

	typedef map<wstring, ITAddress*> AddressMap;
	AddressMap addresses;
	list<long> callNotificationRegisters;

	typedef map<int, ITBasicCallControl*> CallMap;
	CallMap calls;
	int currCallID;
	bool isUp;	// delay event handling until all lines registered
	bool isDown;

	// callcontrol modi
	const static int TAPICALLCONTROLMODE_NONE = 0;
    const static int TAPICALLCONTROLMODE_SETUPTRANSFER = 1;
    const static int TAPICALLCONTROLMODE_TRANSFER = 2;
    const static int TAPICALLCONTROLMODE_SETUPCONFERENCE = 3;
    const static int TAPICALLCONTROLMODE_CONFERENCE = 4;

public:
    // constructor
    MSTapi3();

    // destructor
    ~MSTapi3();

	HRESULT InitializeTapi(CallbackNotification aCallback);
	void ShutdownTapi();
	list<wstring> getAddressNames();

	int reserveCall(wstring& address);
	HRESULT MakeTheCall(int callID, wstring& address, wstring& destination, int mode);
	HRESULT Dial(int callID, wstring& destination);
	HRESULT AnswerTheCall(int callID);
	HRESULT DisconnectTheCall(int callID);
	HRESULT HoldTheCall(int callID);
	HRESULT UnHoldTheCall(int callID);
	HRESULT JoinCalls(int callID1, int callID2, wstring& address, wstring& terminal, int mode);
	HRESULT BlindTransfer(int callID, wstring& destination);
	HRESULT ConsultationStart(int callID, wstring& address, wstring& destination);
	HRESULT AssistedTransferFinish(int callID);
	HRESULT ConferenceFinish(int callID);
	HRESULT SendDigits(wstring terminal, wstring digits);
	void ReleaseTheCall(int callID);
	long SendLineDevSpecific(int callID, wstring& address, BYTE* bytes, DWORD bytesSize);
	void setSwapOnHold(bool enabled) { swapOnHold = enabled; }
    void setHandoff(wstring& appName) { handoff = appName; }
	void setExtPrefix(wstring& extPrefix) { extensionPrefix = extPrefix; }

    HRESULT getAddressName(ITAddress* pITAddress, wstring& strAddress);

private:
	HRESULT RegisterTapiEventInterface();
	HRESULT UnregisterTapiEventInterface();

	HRESULT ListenOnAddresses();
	HRESULT ListenOnThisAddress(ITAddress* pAddress, long* pRegister);

	BOOL IsVideoCaptureStream(ITStream* pStream);
	HRESULT GetVideoRenderTerminal(ITAddress* pAddress, ITTerminal** ppTerminal);
	HRESULT EnablePreview(ITAddress* pAddress, ITStream* pStream);
	HRESULT GetTerminal(ITAddress* pAddress, ITStream* pStream, ITTerminal** ppTerminal);
	HRESULT SelectTerminalsOnCall(ITAddress* pAddress, ITBasicCallControl* pCall);

    HRESULT DetectDigits(ITBasicCallControl* pCallControl);

	void UnregisterCallNotifications();

	BOOL AddressSupportsMediaType(ITAddress* pAddress, long lMediaType);

	void ReleaseAddresses();
	void ReleaseCalls(ITAddress* pAddr);

	// returns the callID
	int putCall(ITBasicCallControl* pCallControl);
	ITBasicCallControl* getCallControl(int callID);
	void removeCallControl(int callID);
	int getCallID(ITBasicCallControl* pCallControl);
	int getOrCreateCallID(ITBasicCallControl* pCallControl);

	ITAddress* getITAddress(wstring& address);

	HRESULT createCall(int callID, wstring& address, wstring& destination);
};

#endif
