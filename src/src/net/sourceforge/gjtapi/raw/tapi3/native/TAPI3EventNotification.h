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
#ifndef __TAPI3EventNotification_H__
#define __TAPI3EventNotification_H__

#include <tapi3.h>
#include <string>
#include "Logger.h"
#include "net_sourceforge_gjtapi_raw_tapi3_Tapi3Provider.h"

using namespace std;

class MSTapi3;

typedef int MethodID;
typedef int Cause;
typedef void (CALLBACK *CallbackNotification)(MethodID methodID, int callID, wstring& address, Cause cause, wstring* callInfo);

/////////////////////////////////////////////////////////////////////////////
// TAPI3EventNotification
class TAPI3EventNotification : public ITTAPIEventNotification {
private:
	Logger* logger;
	MSTapi3* pTapi3;
	CallbackNotification callback;
    LONG m_dwRefCount;

public:
    // TAPI3EventNotification implements ITTAPIEventNotification
    HRESULT STDMETHODCALLTYPE Event(TAPI_EVENT TapiEvent, IDispatch * pEvent);
    
public:
    // constructor
    TAPI3EventNotification(MSTapi3* aMSTapi3, CallbackNotification aCallback);
    // destructor
    ~TAPI3EventNotification();

    // initialization function
    HRESULT Initialize() {
        m_dwRefCount = 1;
        return S_OK;
    }

    void Shutdown() {}

    // IUnknown implementation
	HRESULT STDMETHODCALLTYPE QueryInterface(REFIID iid, void **ppvObject) {
        if (iid == IID_ITTAPIEventNotification) {
            AddRef();
            *ppvObject = (void *)this;
            return S_OK;
        }
        if (iid == IID_IUnknown) {
            AddRef();
            *ppvObject = (void *)this;
            return S_OK;
        }
        return E_NOINTERFACE;
    }

    //
    // reference counting needs to be thread safe
    //    
    ULONG STDMETHODCALLTYPE AddRef() {
        ULONG l = InterlockedIncrement(&m_dwRefCount);
        return l;
    }
    
	ULONG STDMETHODCALLTYPE Release() {
        ULONG l = InterlockedDecrement(&m_dwRefCount);
        if ( 0 == l) {
            delete this;
        }
        return l;
    }


};

#endif //__TAPI3EventNotification_H__
