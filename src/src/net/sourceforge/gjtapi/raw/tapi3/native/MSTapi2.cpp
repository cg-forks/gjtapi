/*
	Copyright (c) 2009 Richard Deadman, Deadman Consulting, http://www.deadman.ca 
	
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
#include "MSTapi2.h"
//#include "TAPI3EventNotification.h"
//#include "Util.h"
#include "Tapi2Utils.h"

	CTapiConnection* tapiConnection = NULL;

// constructor
MSTapi2::MSTapi2() {
	logger = new Logger("MSTapi2");

	//InitializeTapi2();
}

// destructor
MSTapi2::~MSTapi2() {
	logger->debug("MSTapi2 dtor.");

	logger->debug("Deleting logger...");
	delete logger;
	logger = NULL;
}

//////////////////////////////////////////////////////////////
// InitializeTapi2
//
// Various tapi initializations
///////////////////////////////////////////////////////////////
HRESULT MSTapi2::InitializeTapi2() {

	// Initialize a connection with TAPI and determine if there 
    // are any TAPI complient devices installed.
	logger->debug("Creating app handle");
	tapiConnection = new CTapiConnection();
	if(!tapiConnection->Create(NULL)) {
		logger->debug("There are no TAPI devices installed!");
		return FALSE;
	} else {
		logger->debug("Created tapi connection");
	}

    return TRUE;
}

/////////////////////////////////////////////////////////////////////
// Start transfering the call to another number
// This is an assisted transfer, with a consultation call.
/////////////////////////////////////////////////////////////////////
HRESULT MSTapi2::ConsultationStart(wstring& address, wstring& destination) {
	//InitializeTapi2();
	logger->debug("Tapi2 Consultation Start(%S, %S)", address.c_str(), destination.c_str());

	char narrowAddress[MAX_PATH], narrowDestination[MAX_PATH];
	sprintf(narrowAddress, "%S", address.c_str());
	sprintf(narrowDestination, "%S", destination.c_str());

	logger->debug("Looking for address 1: %s", narrowAddress);
	if(tapiConnection != NULL) {
		tapiConnection->CreateConsultationCall(narrowAddress, narrowDestination);
	} else {
		logger->debug("tapiConnection not initialized");
	}

	return TRUE;
}

/////////////////////////////////////////////////////////////////////
// Start transfering the call to another number
// This is an assisted transfer, with a consultation call.
/////////////////////////////////////////////////////////////////////
HRESULT MSTapi2::ConsultationFinish(wstring& address, bool finishFlag) {
	logger->debug("Tapi2 Consultation Finish(%S)", address.c_str());

	char narrowAddress[MAX_PATH];
	sprintf(narrowAddress, "%S", address.c_str());

	if(tapiConnection != NULL) {
		tapiConnection->Transfer(narrowAddress, finishFlag);
	} else {
		logger->debug("tapiConnection not initialized");
	}

	return TRUE;
}


