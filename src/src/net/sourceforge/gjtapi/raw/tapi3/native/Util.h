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
#if !defined(AFX_UTIL_H__CD55E635_384E_4EB0_9CD0_1691C0F605FA__INCLUDED_)
#define AFX_UTIL_H__CD55E635_384E_4EB0_9CD0_1691C0F605FA__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <tapi3.h>
#include <string>
#include "MSTapi3.h"

using namespace std;

bool getCallInfo(Logger* logger, ITCallInfo* pCall, wstring callInfo[4]);

// class T must offer the method: HRESULT get_Address(ITAddress**);
template<class T> HRESULT getAddress(Logger* logger, MSTapi3* msTapi3, T* pInfo, wstring& strAddress) {
	logger->debug("Calling getAddress()...");
	ITAddress* pAddress;
	HRESULT hr = pInfo->get_Address(&pAddress);
	if(FAILED(hr)) {
		logger->error("get_Address() failed: hr=%08X", hr);
		return hr;
	}

    hr = msTapi3->getAddressName(pAddress, strAddress);
	pAddress->Release();
	if(FAILED(hr)) {
		logger->error("get_AddressName() failed: hr=%08X", hr);
		return hr;
	}
	logger->info("getAddress() returning %S.", strAddress.c_str());						

	return S_OK;
}

// class T must offer the method: HRESULT get_Terminal(ITTerminal**);
template<class T> HRESULT getTerminalName(Logger* logger, T* pInfo, wstring& strTerminalName) {
	logger->debug("Calling get_Terminal()....");
	ITTerminal* pTerminal;
	HRESULT hr = pInfo->get_Terminal(&pTerminal);
	if(FAILED(hr)) {
		logger->error("get_Terminal() failed: hr=%08X", hr);
		return hr;
	}
	logger->debug("Calling get_Name()....");
	BSTR bstrTerminalName;
	hr = pTerminal->get_Name(&bstrTerminalName);
	if(FAILED(hr)) {
		logger->error("get_TerminalName() failed: hr=%08X", hr);
		pTerminal->Release();
		return hr;
	}
	strTerminalName = bstrTerminalName;
	SysFreeString(bstrTerminalName);
	logger->info("Terminal name: %S.", strTerminalName.c_str());						
	pTerminal->Release();
	return S_OK;
}

#endif
