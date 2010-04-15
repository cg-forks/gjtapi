//
// TAPIUTILS.CPP
//
// This module implements the CTapiConnection class.
// This is a TAPI2 class to allow for transfer and conference on a Panasonic PBX
// that has TAPI3 broken.
//
#include "stdafx.h"
#include <string.h>
#include <stdio.h>
#include "Tapi2Utils.h"

// All TAPI line functions return 0 for SUCCESS, so define it.
#define SUCCESS 0

// TAPI version that this sample is designed to use.
#define SAMPLE_TAPI_VERSION 0x00020002

// Early TAPI version
#define EARLY_TAPI_VERSION 0x00010004

// Possible return error for resynchronization functions.
#define WAITERR_WAITABORTED  1

// A pointer to my class because TAPI needs a callback
CTapiConnection *MyThis;

#if FALSE
// Structures needed to handle special non-dialable characters.
#define g_sizeofNonDialable (sizeof(g_sNonDialable)/sizeof(g_sNonDialable[0]))

typedef struct {
    LONG lError;
    DWORD dwDevCapFlag;
    LPSTR szToken;
    LPSTR szMsg;
} NONDIALTOKENS;

NONDIALTOKENS g_sNonDialable[] = {
    {LINEERR_DIALBILLING,  LINEDEVCAPFLAGS_DIALBILLING,  "$", 
            "Wait for the credit card bong tone" },
    {LINEERR_DIALDIALTONE, LINEDEVCAPFLAGS_DIALDIALTONE, "W", 
            "Wait for the second dial tone" },
    {LINEERR_DIALDIALTONE, LINEDEVCAPFLAGS_DIALDIALTONE, "w", 
            "Wait for the second dial tone" },
    {LINEERR_DIALQUIET,    LINEDEVCAPFLAGS_DIALQUIET,    "@", 
            "Wait for the remote end to answer" },
    {LINEERR_DIALPROMPT,   0,                            "?", 
            "Press OK when you are ready to continue dialing"},
};
#endif

// 
// Constructor
//
CTapiConnection::CTapiConnection()
{
    m_bShuttingDown = FALSE;
    m_bStoppingCall = FALSE;
    m_bInitializing = FALSE;
    m_bTapiInUse = FALSE;
    m_dwNumDevs = 0;
	m_dwAPIVersion = 0x00020000;
    m_hCall = NULL;
    m_hLine = NULL;
    m_dwDeviceID = 0;
    m_hLineApp = NULL;

	logger = new Logger("CTapiConnection");

    MyThis = this;

};

//
// Destructor
//
CTapiConnection::~CTapiConnection()
{
    m_bInitialized = FALSE;

	lastCallMap.empty();

	delete logger;
	logger = NULL;
};

////////////////////////////////////////////////////////////////////////////////////
// _LineEventProc
//
// static CALLBACK function for LINE devices under TAPI
//
DWORD WINAPI _LineEventProc ( LPVOID pParam )
{
	CTapiConnection* pConn = (CTapiConnection*) pParam;
	pConn->LineEventProc();
	return 0;

}// _LineEventProc

//
//  FUNCTION: BOOL Create()
//
//  PURPOSE: Initializes TAPI
//
BOOL CTapiConnection::Create(char *szPhoneNumber)
{
	logger->debug("Entering Create");
    long lReturn = -1;

    // If we're already initialized, then initialization succeeds.
    if (m_hLineApp)
        return TRUE;

    // If we're in the middle of initializing, then fail, we're not done.
    if (m_bInitializing)
        return FALSE;

    m_bInitializing = TRUE;

	logger->debug("Initializing...");
    // Initialize TAPI
    do
    {
		LINEINITIALIZEEXPARAMS lip;
		ZeroMemory (&lip, sizeof(LINEINITIALIZEEXPARAMS));
        while (lReturn != 0)
		{
			lip.dwTotalSize = sizeof(LINEINITIALIZEEXPARAMS);
			lip.dwOptions = LINEINITIALIZEEXOPTION_USEEVENT;
			logger->debug("Calling lineInitializeEx...");
			lReturn = lineInitializeEx(&m_hLineApp, 
				NULL, 
				NULL, 
				"Tapi2Provider", 
				&m_dwNumDevs,
				&m_dwAPIVersion,
				&lip);
			logger->debug("lineInitializeEx lReturn=%08X", lReturn);
			if (lReturn != LINEERR_REINIT)
				break;
		}
		// If we were unsuccessful then return an error
		if (lReturn != 0) {
			logger->debug("lineInitializeEx Error: hr=%08X", lReturn);
			return FALSE;
		}

		// find the event handle
		m_hTapiEvent_L = lip.Handles.hEvent;
		if (m_hTapiEvent_L == NULL) {
			logger->debug("No line Handle hEvent");
			return FALSE;	//LINEERR_NOMEM;
		}

		// hook in the event handler
		// Create the thread which will monitor TAPI events.
		DWORD  _tid;     // thread id
		m_eventThread = CreateThread(
			NULL,
			0,
			_LineEventProc,
			(void*)this,
            0,	//CREATE_SUSPENDED,
            &_tid);
		if(m_eventThread == NULL)
		{
			logger->debug("Failed to create monitor thread");
			lineShutdown(m_hLineApp);
			m_hLineApp = NULL;
			return LINEERR_OPERATIONFAILED;
		}


        if (m_dwNumDevs == 0)
        {
            //AfxMessageBox("There are no telephony devices installed.");
            m_bInitializing = FALSE;
			logger->debug("There are no telephony devices installed.");
            return FALSE;
        }

		// open all the lines so that we can get the events
		HLINE hLine;
		for (DWORD dwDeviceID = 0; dwDeviceID < m_dwNumDevs; dwDeviceID++)
        {
			LONG openResult = lineOpen(m_hLineApp, dwDeviceID, &hLine, m_dwAPIVersion, 0,
				(DWORD)NULL, LINECALLPRIVILEGE_MONITOR, // | LINECALLPRIVILEGE_OWNER,
							 LINEMEDIAMODE_UNKNOWN, NULL);
			logger->debug("lineOpen on line: %u", hLine);
			lineIdMap[dwDeviceID] = hLine;
			if(openResult != 0) {
				hLine = NULL;
			}
		}

        if (HandleLineErr(lReturn))
            continue;
        else
        {
            OutputDebugString("lineInitialize unhandled error\n");
			logger->debug("lineInitialize unhandled error\n");
            m_bInitializing = FALSE;
            return FALSE;
        }
    }
    while(lReturn != SUCCESS);

    OutputDebugString("Tapi initialized.\n");
	logger->debug("Tapi initialized.");

    // If the user furnished a phone number copy it over.
    if (szPhoneNumber != (char *)NULL)
        strcpy(m_szPhoneNumber, szPhoneNumber);

    m_bInitializing = FALSE;
    return TRUE;
}

//
//  FUNCTION: DialCall()
//
//  PURPOSE: Get a number from the user and dial it.
//
//BOOL CTapiConnection::DialCall(char *szPhoneNumber)
//{
//    long lReturn;
//    LPLINEDEVCAPS lpLineDevCaps = NULL;
//
//    if (m_bTapiInUse)
//    {
//        //AfxMessageBox("A call is already being handled.");
//        return FALSE;
//    }
//
//    // Make sure TAPI is initialized properly
//    if (!m_hLineApp)
//    {
//        if (!Create(NULL))
//            return FALSE;
//    }
//
//    // If there are no line devices installed on the machine, quit.
//    if (m_dwNumDevs < 1)
//        return FALSE;
//
//    // We now have a call active.  Prevent future calls.
//    m_bTapiInUse = TRUE;
//
//    // Get a phone number from the user.
//    if (szPhoneNumber == (char *)NULL)
//    {
//        if (m_szPhoneNumber == (char *)NULL)
//        {
//            HangupCall();
//            goto DeleteBuffers;
//        }
//    }
//    else 
//        strcpy(m_szPhoneNumber, szPhoneNumber);
//
//    // Get the line to use
//    lpLineDevCaps = GetDeviceLine(&m_dwAPIVersion, lpLineDevCaps);
//
//    // Need to check the DevCaps to make sure this line is usable.
//    if (lpLineDevCaps == NULL)
//    {
//        OutputDebugString("Error on Requested line\n");
//        goto DeleteBuffers;
//    }
//
//    if (!(lpLineDevCaps->dwBearerModes & LINEBEARERMODE_VOICE ))
//    {
//        //AfxMessageBox("The selected line doesn't support VOICE capabilities");
//        goto DeleteBuffers;
//    }
//
//    // Does this line have the capability to make calls?
//    if (!(lpLineDevCaps->dwLineFeatures & LINEFEATURE_MAKECALL))
//    {
//        //AfxMessageBox("The selected line doesn't support MAKECALL capabilities");
//        goto DeleteBuffers;
//    }
//
//    // Does this line have the capability for interactive voice?
//    if (!(lpLineDevCaps->dwMediaModes & LINEMEDIAMODE_INTERACTIVEVOICE))
//    {
//        //AfxMessageBox("The selected line doesn't support INTERACTIVE VOICE capabilities");
//        goto DeleteBuffers;
//    }
//
//    // Open the Line for an outgoing call.
//    do
//    {
//        lReturn = ::lineOpen(m_hLineApp, m_dwDeviceID, &m_hLine,
//            m_dwAPIVersion, 0, 0,
//            LINECALLPRIVILEGE_NONE, 0, 0);
//
//        if((lReturn == LINEERR_ALLOCATED)||(lReturn == LINEERR_RESOURCEUNAVAIL))
//        {
//            HangupCall();
//            OutputDebugString("Line is already in use by a non-TAPI application "
//                "or by another TAPI Service Provider.\n");
//            goto DeleteBuffers;
//        }
//
//        if (HandleLineErr(lReturn))
//            continue;
//        else
//        {
//            OutputDebugString("Unable to Use Line\n");
//            HangupCall();
//            goto DeleteBuffers;
//        }
//    }
//    while(lReturn != SUCCESS);
//
//    // Start dialing the number
//    if( MakeTheCall(lpLineDevCaps, m_szPhoneNumber))
//        OutputDebugString("lineMakeCall succeeded.\n");
//    else
//    {
//        OutputDebugString("lineMakeCall failed.\n");
//        HangupCall();
//    }
//
//DeleteBuffers:
//
//    if (lpLineDevCaps)
//        LocalFree(lpLineDevCaps);
//
//    return m_bTapiInUse;
//}

//
//  FUNCTION: CreateConsultationCall()
//
//  PURPOSE: Set up a consultation call to another number
//
BOOL CTapiConnection::CreateConsultationCall(char *szLineAddress, char *szPhoneNumber)
{
	// find the device 
	logger->debug("GetDeviceLineID");
	DWORD dwDeviceID = this->GetDeviceLineID(szLineAddress);

	// if a device was found, grab control
	if (dwDeviceID != NULL)
	{
		logger->debug("Line found... %x", dwDeviceID);
	}
	else
	{
		logger->debug("Line not found...");
	}

		// find an existing call
	logger->debug("Found line id %x for address %s", dwDeviceID, szLineAddress);
	HLINE line = this->lineIdMap[dwDeviceID];
	logger->debug("Found line %x", line);
	HCALL existingCall = this->lastCallMap[line];
	logger->debug("Found call %x for address %s", existingCall, szLineAddress);

	// Now grab control of the call
	LONG lResult = lineSetCallPrivilege(existingCall, LINECALLPRIVILEGE_OWNER);
	if (lResult != 0) {
		logger->debug("Could not grab ownership: 0x%x", lResult);
		return FALSE;
	} else {
		logger->debug("Set ownership privilege on 0x%x", existingCall);
	}

	// Now set up the transfer
	HCALL hConsultCall = NULL;
    
    lResult = lineSetupTransfer (existingCall, &hConsultCall, NULL);

	// store the consultation call so the event handler doesn't overwrite the last call
	consultationCallMap[line] = hConsultCall;

	if ((lResult == 0) || (lResult > 0x80000000)) {
		if(lResult == LINEERR_NOTOWNER) {
			logger->debug("Not owner");
		}
		logger->debug("Could not set up transfer: 0x%x", lResult);
		return FALSE;
	} else {
		logger->debug("Looking for reply status for transfer set-up 0x%x", lResult);
		LONG result = -1;
		int resultFound = 0;
		while(resultFound == 0) {
			replyStatusType::iterator iter = replyStatus.find(lResult);
			if( iter != replyStatus.end() ) {
				result = iter->second;
				replyStatus.erase(iter);
				resultFound = 1;
			} else {
				Sleep(5);
			}
		}
		if (result != 0) {
			logger->debug("Asynchronous transfer set up returned: 0x%x", result);
			return FALSE;
		} else {
			logger->debug("Asynchronous transfer set up returned: 0x%x", result);
		}
	}

	// dial the transfer
	Sleep(1000);	// pause a second to accomodate Panasonic delay
	lResult = lineDial (hConsultCall, szPhoneNumber, 0);
	if ((lResult == 0) || (lResult > 0x80000000)) {
		logger->debug("Could not dial the transfer call: 0x%x", lResult);

		// unhold
		lineUnhold(existingCall);

		return FALSE;
	} else {
		logger->debug("Looking for reply status for dialing async handle 0x%x", lResult);
		LONG result = -1;
		int resultFound = 0;
		while(resultFound == 0) {
			replyStatusType::iterator iter = replyStatus.find(lResult);
			if( iter != replyStatus.end() ) {
				result = iter->second;
				replyStatus.erase(iter);
				resultFound = 1;
			} else {
				Sleep(5);
			}
		}
		if (result != 0) {
			logger->debug("Asynchronous dial returned: 0x%x", result);
			return FALSE;
		} else {
			logger->debug("Asynchronous dial returned: 0x%x", result);
		}
	}


    return TRUE;
}

BOOL CTapiConnection::Transfer(char *szLineAddress, bool transferFlag) {
	// complete the transfer
	logger->debug("Finishing the transfer");
	// find the device 
	logger->debug("GetDeviceLineID");
	DWORD dwDeviceID = this->GetDeviceLineID(szLineAddress);

	// if a device was found, grab control
	if (dwDeviceID != NULL)
	{
		logger->debug("Line found... %x", dwDeviceID);
	}
	else
	{
		logger->debug("Line not found...");
	}

		// find an existing call
	logger->debug("Found line id %x for address %s", dwDeviceID, szLineAddress);
	HLINE line = this->lineIdMap[dwDeviceID];
	logger->debug("Found line %x", line);
	HCALL existingCall = this->lastCallMap[line];
	logger->debug("Found call %x for address %s", existingCall, szLineAddress);
	HCALL hConsultCall = this->consultationCallMap[line];
	logger->debug("Found consultation call %x for address %s", hConsultCall, szLineAddress);

	LONG lResult = 0;
	if(transferFlag) {
		lResult = lineCompleteTransfer (existingCall, hConsultCall, NULL, LINETRANSFERMODE_TRANSFER);
	} else {
		HCALL hConfCall = NULL;
		lResult = lineCompleteTransfer (existingCall, hConsultCall, &hConfCall, LINETRANSFERMODE_CONFERENCE);
	}
	if ((lResult == 0) || (lResult > 0x80000000)) {
		logger->debug("Could not complete transfer: 0x%x", lResult);
		return FALSE;
	//} else {
	//	LONG result = -1;
	//	int resultFound = 0;
	//	while(resultFound == 0) {
	//		replyStatusType::iterator iter = replyStatus.find(lResult);
	//		if( iter != replyStatus.end() ) {
	//			result = iter->second;
	//			replyStatus.erase(iter);
	//			resultFound = 1;
	//		} else {
	//			Sleep(5);
	//		}
	//	}
	//	if (result != 0) {
	//		logger->debug("Asynchronous transfer complete returned: 0x%x", result);
	//		return FALSE;
	//	}
	}

	return TRUE;
}

//
//  FUNCTION: void GetDeviceLine()
//
//  PURPOSE: Gets the first available line device.
//
//
//LPLINEDEVCAPS CTapiConnection::GetDeviceLine(DWORD *pdwAPIVersion, LPLINEDEVCAPS lpLineDevCaps)
//{
//    DWORD dwDeviceID;
//    char szLineUnavail[] = "Line Unavailable";
//    char szLineUnnamed[] = "Line Unnamed";
//    char szLineNameEmpty[] = "Line Name is Empty";
//    LPSTR lpszLineName;
//    long lReturn;
//    char buf[MAX_PATH];
//    LINEEXTENSIONID lineExtID;
//    BOOL bDone = FALSE;
//
//    for (dwDeviceID = 0; (dwDeviceID < m_dwNumDevs) && !bDone; dwDeviceID ++)
//    {
//
//        lReturn = ::lineNegotiateAPIVersion(m_hLineApp, dwDeviceID, 
//            EARLY_TAPI_VERSION, SAMPLE_TAPI_VERSION,
//            pdwAPIVersion, &lineExtID);
//
//        if ((HandleLineErr(lReturn))&&(*pdwAPIVersion))
//        {
//            lpLineDevCaps = MylineGetDevCaps(lpLineDevCaps, dwDeviceID, *pdwAPIVersion);
//
//            if ((lpLineDevCaps -> dwLineNameSize) &&
//                (lpLineDevCaps -> dwLineNameOffset) &&
//                (lpLineDevCaps -> dwStringFormat == STRINGFORMAT_ASCII))
//            {
//                // This is the name of the device.
//                lpszLineName = ((char *) lpLineDevCaps) + 
//                    lpLineDevCaps -> dwLineNameOffset;
//                sprintf(buf, "Name of device is: %s\n", lpszLineName);
//                OutputDebugString(buf);
//            }
//            else  // DevCaps doesn't have a valid line name.  Unnamed.
//                lpszLineName = szLineUnnamed;
//        }
//        else  // Couldn't NegotiateAPIVersion.  Line is unavail.
//            lpszLineName = szLineUnavail;
//
//        // If this line is usable and we don't have a default initial
//        // line yet, make this the initial line.
//        if ((lpszLineName != szLineUnavail) && 
//            (lReturn == SUCCESS )) 
//        {          
//            m_dwDeviceID = dwDeviceID;
//            bDone = TRUE;
//        }
//        else 
//            m_dwDeviceID = MAXDWORD;
//    }
//    return (lpLineDevCaps);
//}

//
//  FUNCTION: void GetDeviceLineID()
//
//  PURPOSE: Gets a device id for a particular name
//
//
HLINE CTapiConnection::GetDeviceLineID(char *lineName)
{
	// look up in the cache
	lineNameMapType::iterator iter = lineNameMap.find(lineName);
	if( iter != lineNameMap.end() ) {
		return iter->second;
	}

    DWORD dwDeviceID;
    char szLineUnavail[] = "Line Unavailable";
    char szLineUnnamed[] = "Line Unnamed";
    char szLineNameEmpty[] = "Line Name is Empty";
    LPSTR lpszLineName;
    long lReturn;
	char buf[MAX_PATH];
    LINEEXTENSIONID lineExtID;
    BOOL bDone = FALSE;
	DWORD dwAPIVersion;

	size_t nameLength = strlen(lineName);

	logger->debug("In GetDeviceLineID");
	logger->debug("Checking %u devices for %s", m_dwNumDevs, lineName);
    LPLINEDEVCAPS lpLineDevCaps = NULL;
    for (dwDeviceID = 0; (dwDeviceID < m_dwNumDevs) && !bDone; dwDeviceID ++)
    {
		logger->debug("Checking line %u.", dwDeviceID);
        lReturn = ::lineNegotiateAPIVersion(m_hLineApp, dwDeviceID, 
            EARLY_TAPI_VERSION, SAMPLE_TAPI_VERSION,
            &dwAPIVersion, &lineExtID);

        if ((HandleLineErr(lReturn))&&(dwAPIVersion))
        {
			logger->debug("Looking for device info");

			lpLineDevCaps = MylineGetDevCaps(lpLineDevCaps, dwDeviceID, m_dwAPIVersion);
			logger->debug("Looking for name...");
            if ((lpLineDevCaps != NULL) &&
				(lpLineDevCaps -> dwLineNameSize) &&
                (lpLineDevCaps -> dwLineNameOffset) &&
                (lpLineDevCaps -> dwStringFormat == STRINGFORMAT_ASCII))
            {
                // This is the name of the device.
                lpszLineName = ((char *) lpLineDevCaps) + 
                    lpLineDevCaps -> dwLineNameOffset;
                sprintf(buf, "Name of device is: %.259s", lpszLineName);
				logger->debug(buf);
				//char buf2[MAX_PATH];
				//strncpy(buf2, lpszLineName, nameLength);
				//buf2[nameLength] = 0;	//end string
                //sprintf(buf, "Name of device is (buf2): %.20s", buf2);
				//logger->debug(buf);
                //OutputDebugString(buf);

				if(strcmp(lpszLineName, lineName) == 0)
				{
					OutputDebugString("Found the line.");
					logger->debug("Found the line.");
					lineNameMap[lineName] = dwDeviceID;
					// free calloc'd space
					if(lpLineDevCaps != NULL) {
						LocalFree(lpLineDevCaps);
					}


					return dwDeviceID;
				}
            }
			else
			{
				logger->debug("No name");
			}
        }
		else
		{
			logger->debug("Could not get device information");
		}
    }
	// free the calloc'd space
	if(lpLineDevCaps != NULL) {
		LocalFree(lpLineDevCaps);
	}

    return NULL;
}

//
//  FUNCTION: MylineGetDevCaps(LPLINEDEVCAPS, DWORD , DWORD)
//
//  PURPOSE: Gets a LINEDEVCAPS structure for the specified line.
//
//  COMMENTS:
//
//    This function is a wrapper around lineGetDevCaps to make it easy
//    to handle the variable sized structure and any errors received.
//    
//    The returned structure has been allocated with LocalAlloc,
//    so LocalFree has to be called on it when you're finished with it,
//    or there will be a memory leak.
//
//    Similarly, if a lpLineDevCaps structure is passed in, it *must*
//    have been allocated with LocalAlloc and it could potentially be 
//    LocalFree()d.
//
//    If lpLineDevCaps == NULL, then a new structure is allocated.  It is
//    normal to pass in NULL for this parameter unless you want to use a 
//    lpLineDevCaps that has been returned by a previous I_lineGetDevCaps
//    call.
//
//

LPLINEDEVCAPS CTapiConnection::MylineGetDevCaps(
    LPLINEDEVCAPS lpLineDevCaps,
    DWORD dwDeviceID, DWORD dwAPIVersion)
{
    // Allocate enough space for the structure plus 1024.
    size_t sizeofLineDevCaps = sizeof(LINEDEVCAPS) + 1024;
    long lReturn;
    
    // Continue this loop until the structure is big enough.
    while(TRUE)
    {
        // Make sure the buffer exists, is valid and big enough.
        lpLineDevCaps = 
            (LPLINEDEVCAPS) CheckAndReAllocBuffer(
                (LPVOID) lpLineDevCaps, // Pointer to existing buffer, if any
                sizeofLineDevCaps);      // Minimum size the buffer should be

        if (lpLineDevCaps == NULL)
            return NULL;

        // Make the call to fill the structure.
        do
        {            
            lReturn = 
                ::lineGetDevCaps(m_hLineApp, 
                    dwDeviceID, dwAPIVersion, 0, lpLineDevCaps);

            if (HandleLineErr(lReturn))
                continue;
            else
            {
                OutputDebugString("lineGetDevCaps unhandled error/n");
                LocalFree(lpLineDevCaps);
                return NULL;
            }
        }
        while (lReturn != SUCCESS);

        // If the buffer was big enough, then succeed.
        if ((lpLineDevCaps -> dwNeededSize) <= (lpLineDevCaps -> dwTotalSize))
            return lpLineDevCaps;

        // Buffer wasn't big enough.  Make it bigger and try again.
        sizeofLineDevCaps = lpLineDevCaps -> dwNeededSize;
    }
}

//
//  FUNCTION: LPVOID CheckAndReAllocBuffer(LPVOID, size_t, LPCSTR)
//
//  PURPOSE: Checks and ReAllocates a buffer if necessary.
//
LPVOID CTapiConnection::CheckAndReAllocBuffer(LPVOID lpBuffer, size_t sizeBufferMinimum)
{
    size_t sizeBuffer;

    if (lpBuffer == NULL)  // Allocate the buffer if necessary. 
    {
        sizeBuffer = sizeBufferMinimum;
        lpBuffer = (LPVOID) LocalAlloc (LPTR, sizeBuffer);
            
        if (lpBuffer == NULL)
        {
            OutputDebugString("LocalAlloc failed in CheckAndReAllocBuffer./n");
            return NULL;
        }
    }
    else // If the structure already exists, make sure its good.
    {
        sizeBuffer = LocalSize((HLOCAL) lpBuffer);

        if (sizeBuffer == 0) // Bad pointer?
        {
            OutputDebugString("LocalSize returned 0 in CheckAndReAllocBuffer/n");
            return NULL;
        }

        // Was the buffer big enough for the structure?
        if (sizeBuffer < sizeBufferMinimum)
        {
            OutputDebugString("Reallocating structure\n");
            LocalFree(lpBuffer);
            return CheckAndReAllocBuffer(NULL, sizeBufferMinimum);
        }
    }

    memset(lpBuffer, 0, sizeBuffer);       
    ((LPVARSTRING) lpBuffer ) -> dwTotalSize = (DWORD) sizeBuffer;
    return lpBuffer;
}


//
//  FUNCTION: MylineGetAddressCaps(LPLINEADDRESSCAPS, ..)
//
//  PURPOSE: Return a LINEADDRESSCAPS structure for the specified line.
////
//LPLINEADDRESSCAPS CTapiConnection::MylineGetAddressCaps (
//    LPLINEADDRESSCAPS lpLineAddressCaps,
//    DWORD dwDeviceID, DWORD dwAddressID,
//    DWORD dwAPIVersion, DWORD dwExtVersion)
//{
//    size_t sizeofLineAddressCaps = sizeof(LINEADDRESSCAPS) + 1024;
//    long lReturn;
//    
//    // Continue this loop until the structure is big enough.
//    while(TRUE)
//    {
//        // Make sure the buffer exists, is valid and big enough.
//        lpLineAddressCaps = 
//            (LPLINEADDRESSCAPS) CheckAndReAllocBuffer(
//                (LPVOID) lpLineAddressCaps,
//                sizeofLineAddressCaps);
//
//        if (lpLineAddressCaps == NULL)
//            return NULL;
//            
//        // Make the call to fill the structure.
//        do
//        {
//            lReturn = 
//                ::lineGetAddressCaps(m_hLineApp,
//                    dwDeviceID, dwAddressID, dwAPIVersion, dwExtVersion,
//                    lpLineAddressCaps);
//
//            if (HandleLineErr(lReturn))
//                continue;
//            else
//            {
//                OutputDebugString("lineGetAddressCaps unhandled error\n");
//                LocalFree(lpLineAddressCaps);
//                return NULL;
//            }
//        }
//        while (lReturn != SUCCESS);
//
//        // If the buffer was big enough, then succeed.
//        if ((lpLineAddressCaps -> dwNeededSize) <= 
//            (lpLineAddressCaps -> dwTotalSize))
//        {
//            return lpLineAddressCaps;
//        }
//
//        // Buffer wasn't big enough.  Make it bigger and try again.
//        sizeofLineAddressCaps = lpLineAddressCaps -> dwNeededSize;
//    }
//}

//
//  FUNCTION: MakeTheCall(LPLINEDEVCAPS, LPCSTR)
//
//  PURPOSE: Dials the call
//
//
//BOOL CTapiConnection::MakeTheCall(LPLINEDEVCAPS lpLineDevCaps, LPCTSTR lpszAddress)
//{
//    LPLINECALLPARAMS  lpCallParams = NULL;
//    LPLINEADDRESSCAPS lpAddressCaps = NULL;
//    long lReturn;
//    BOOL bFirstDial = TRUE;
//                               
//    // Get the capabilities for the line device we're going to use.
//    lpAddressCaps = MylineGetAddressCaps(lpAddressCaps,
//        m_dwDeviceID, 0, m_dwAPIVersion, 0);
//    if (lpAddressCaps == NULL)
//        return FALSE;
//
//    // Setup our CallParams.
//    lpCallParams = CreateCallParams (lpCallParams, lpszAddress);
//    if (lpCallParams == NULL)
//        return FALSE;
//
//    do
//    {                   
//        if (bFirstDial)
//            //lReturn = ::lineMakeCall(m_hLine, &m_hCall, lpszAddress, 0, lpCallParams);
//            lReturn = WaitForReply( ::lineMakeCall(m_hLine, &m_hCall, lpszAddress, 
//                        0, lpCallParams) );
//        else
//            lReturn = WaitForReply(::lineDial(m_hCall, lpszAddress, 0));
//
//        if (lReturn == WAITERR_WAITABORTED)
//        {
//             OutputDebugString("While Dialing, WaitForReply aborted.\n");
//             goto errExit;
//        }
//            
//        if (HandleLineErr(lReturn))
//            continue;
//        else
//        {
//            if (bFirstDial)
//                OutputDebugString("lineMakeCall unhandled error\n");
//            else
//                OutputDebugString("lineDial unhandled error\n");
//
//            goto errExit;
//        }
//    }
//    while (lReturn != SUCCESS);
//        
//    bFirstDial = FALSE;
//
//    if (lpCallParams)
//        LocalFree(lpCallParams);
//    if (lpAddressCaps)
//        LocalFree(lpAddressCaps);
//    
//    return TRUE;
//    
//  errExit:
//    if (lpCallParams)
//        LocalFree(lpCallParams);
//    if (lpAddressCaps)
//        LocalFree(lpAddressCaps);
//
//    //AfxMessageBox("Dial failed.");
//
//    return FALSE;
//}

//
//  FUNCTION: CreateCallParams(LPLINECALLPARAMS, LPCSTR)
//
//  PURPOSE: Allocates and fills a LINECALLPARAMS structure
//
//
//LPLINECALLPARAMS CTapiConnection::CreateCallParams (
//    LPLINECALLPARAMS lpCallParams, LPCSTR lpszDisplayableAddress)
//{
//    size_t sizeDisplayableAddress;
//
//    if (lpszDisplayableAddress == NULL)
//        lpszDisplayableAddress = "";
//        
//    sizeDisplayableAddress = strlen(lpszDisplayableAddress) + 1;
//                          
//    lpCallParams = (LPLINECALLPARAMS) CheckAndReAllocBuffer(
//        (LPVOID) lpCallParams, 
//        sizeof(LINECALLPARAMS) + sizeDisplayableAddress);
//
//    if (lpCallParams == NULL)
//        return NULL;
//
//    // This is where we configure the line.
//    lpCallParams -> dwBearerMode = LINEBEARERMODE_VOICE;
//    lpCallParams -> dwMediaMode  = LINEMEDIAMODE_INTERACTIVEVOICE;
//
//    // This specifies that we want to use only IDLE calls and
//    // don't want to cut into a call that might not be IDLE (ie, in use).
//    lpCallParams -> dwCallParamFlags = LINECALLPARAMFLAGS_IDLE;
//                                    
//    // if there are multiple addresses on line, use first anyway.
//    // It will take a more complex application than a simple tty app
//    // to use multiple addresses on a line anyway.
//    lpCallParams -> dwAddressMode = LINEADDRESSMODE_ADDRESSID;
//
//    // Address we are dialing.
//    lpCallParams -> dwDisplayableAddressOffset = sizeof(LINECALLPARAMS);
//    lpCallParams -> dwDisplayableAddressSize = sizeDisplayableAddress;
//    strcpy((LPSTR)lpCallParams + sizeof(LINECALLPARAMS),
//           lpszDisplayableAddress);
//
//    return lpCallParams;
//}

//
//  FUNCTION: long WaitForReply(long)
//
//  PURPOSE: Resynchronize by waiting for a LINE_REPLY 
//
//  PARAMETERS:
//    lRequestID - The asynchronous request ID that we're
//                 on a LINE_REPLY for.
//
//  RETURN VALUE:
//    - 0 if LINE_REPLY responded with a success.
//    - LINEERR constant if LINE_REPLY responded with a LINEERR
//    - 1 if the line was shut down before LINE_REPLY is received.
//
//  COMMENTS:
//
//    This function allows us to resynchronize an asynchronous
//    TAPI line call by waiting for the LINE_REPLY message.  It
//    waits until a LINE_REPLY is received or the line is shut down.
//
//    Note that this could cause re-entrancy problems as
//    well as mess with any message preprocessing that might
//    occur on this thread (such as TranslateAccelerator).
//
//    This function should to be called from the thread that did
//    lineInitialize, or the PeekMessage is on the wrong thread
//    and the synchronization is not guaranteed to work.  Also note
//    that if another PeekMessage loop is entered while waiting,
//    this could also cause synchronization problems.
//
//    One more note.  This function can potentially be re-entered
//    if the call is dropped for any reason while waiting.  If this
//    happens, just drop out and assume the wait has been canceled.  
//    This is signaled by setting bReentered to FALSE when the function 
//    is entered and TRUE when it is left.  If bReentered is ever TRUE 
//    during the function, then the function was re-entered.
//
//    This function times out and returns WAITERR_WAITTIMEDOUT
//    after WAITTIMEOUT milliseconds have elapsed.
//
//


//long CTapiConnection::WaitForReply (long lRequestID)
//{
//    static BOOL bReentered;
//    bReentered = FALSE;
//
//    if (lRequestID > SUCCESS)
//    {
//        MSG msg; 
//        DWORD dwTimeStarted;
//
//        m_bReplyReceived = FALSE;
//        m_dwRequestedID = (DWORD) lRequestID;
//
//        // Initializing this just in case there is a bug
//        // that sets m_bReplyRecieved without setting the reply value.
//        m_lAsyncReply = LINEERR_OPERATIONFAILED;
//
//        dwTimeStarted = GetTickCount();
//
//        while(!m_bReplyReceived)
//        {
//            if (PeekMessage(&msg, 0, 0, 0, PM_REMOVE))
//            {
//                TranslateMessage(&msg);
//                DispatchMessage(&msg);
//            }
//
//            // This should only occur if the line is shut down while waiting.
//            if ((m_hCall != NULL) &&(!m_bTapiInUse || bReentered))
//            {
//                bReentered = TRUE;
//                return WAITERR_WAITABORTED;
//            }
//
//            // Its a really bad idea to timeout a wait for a LINE_REPLY.
//            // If we are execting a LINE_REPLY, we should wait till we get
//            // it; it might take a long time to dial (for example).
//
//            // If 5 seconds go by without a reply, it might be a good idea
//            // to display a dialog box to tell the user that a
//            // wait is in progress and to give the user the capability to
//            // abort the wait.
//        }
//
//        bReentered = TRUE;
//        return m_lAsyncReply;
//    }
//
//    bReentered = TRUE;
//    return lRequestID;
//}

////////////////////////////////////////////////////////////////////////////////////
// CTapiConnection::LineEventProc
//
// This function is managed by a thread which spins waiting for events from
// TAPI.
//
void CTapiConnection::LineEventProc()
{
	LINEMESSAGE lm;
	LONG lResult;

	// Spin forever waiting on TAPI to shutdown
	do
	{
		// Get the TAPI message
		lResult = lineGetMessage(m_hLineApp, &lm, 5000);
		if (lResult == 0)
		{
			logger->debug("TAPI2 event found");
			// If this is an asynch request completing, then mark it in our list
			// and then pass it down to the line.
			if (lm.dwMessageID == LINE_REPLY) {
				LONG lResult = lm.dwParam2;
				logger->debug("LINE_REPLY 0x%x found for request 0x%x", lResult, lm.dwParam1);
				//OnRequestComplete (lm.dwParam1, lm.dwParam2);
				replyStatus[lm.dwParam1] = lResult;
			}
              
			// If this is a LINE_CREATE message, then a new line has been dynamically
			// added to TAPI (Plug&Play).  Manage it.
			else if (lm.dwMessageID == LINE_CREATE) {
				//OnLineCreate (lm.dwParam1);
			}

			// If this is a LINE_REMOVE message, then a line has been removed from the system.
			else if (lm.dwMessageID == LINE_REMOVE) {
				//OnLineRemove (lm.dwParam1);
			}

			else if (lm.dwMessageID == LINE_CLOSE) {
				//OnLineClose (lm.dwParam1);
			}

			else // Line or call message.
			{
				DWORD device = lm.hDevice;
				logger->debug("Event for device %u and message %u.", device, lm.dwMessageID);
				switch (lm.dwMessageID)
				{
					case LINE_APPNEWCALL:
						// store the call against the line
						HLINE line = (HLINE)device;
						//DWORD dwAddressId = (DWORD) lm.dwParam1;
						HCALL callHandle = (HCALL)lm.dwParam2;
						// see if this call is in out consultation map already
						HCALL consultHandle = consultationCallMap[line];
						logger->debug("Found Consultation call when handling event handler: 0x%x", consultHandle);
						if (consultHandle != callHandle) {
							lastCallMap[line] = callHandle;
							logger->debug("Storing call %x against line %x", callHandle, line);
						}
						break;
				}
			}
		}

		// Kill any completed requests still in queue.
		//PurgeRequests(5000);

	} while (lResult != LINEERR_INVALAPPHANDLE);

	m_eventThread = NULL;

}// CTapiConnection::LineEventProc

//
//  FUNCTION: lineCallbackFunc(..)
//
//  PURPOSE: Receive asynchronous TAPI events
//
//void CALLBACK CTapiConnection::lineCallbackFunc(
//    DWORD dwDevice, DWORD dwMsg, DWORD dwCallbackInstance, 
//    DWORD dwParam1, DWORD dwParam2, DWORD dwParam3)
//{
//	MyThis->logger->debug("Callback event");
//    // Handle the line messages.
//    switch(dwMsg)
//    {
//        case LINE_CALLSTATE:
//			MyThis->logger->debug("Call state change detected");
//            MyThis->HandleLineCallState(dwDevice, dwMsg, dwCallbackInstance, dwParam1, dwParam2,
//                dwParam3);
//            break;
//
//        case LINE_CLOSE:
//            // Line has been shut down.  
//            //ASSERT(MyThis);
//            MyThis->m_hLine = NULL;
//            MyThis->m_hCall = NULL;
//            MyThis->HangupCall(); // all handles invalidated by this time
//            break;
//
//        case LINE_REPLY:
//            if ((long) dwParam2 != SUCCESS)
//                OutputDebugString("LINE_REPLY error\n");
//            else
//                OutputDebugString("LINE_REPLY: successfully replied.\n");
//            break;
//
//        case LINE_CREATE:
//            //ASSERT(MyThis);
//            if (MyThis->m_dwNumDevs <= dwParam1)
//                MyThis->m_dwNumDevs = dwParam1+1;
//            break;
//
//        default:
//            OutputDebugString("lineCallbackFunc message ignored\n");
//            break;
//    }
//    return;
//}

//
//  FUNCTION: HandleLineCallState(..)
//
//  PURPOSE: Handle LINE_CALLSTATE asynchronous messages.
//

//void CTapiConnection::HandleLineCallState(
//    DWORD dwDevice, DWORD dwMessage, DWORD dwCallbackInstance,
//    DWORD dwParam1, DWORD dwParam2, DWORD dwParam3)
//{
//
//    // Error if this CALLSTATE doesn't apply to our call in progress.
//    if ((HCALL) dwDevice != m_hCall)
//    {
//        OutputDebugString("LINE_CALLSTATE: Unknown device ID ");
//        return;
//    }
//
//
//    // dwParam1 is the specific CALLSTATE change that is occurring.
//    switch (dwParam1)
//    {
//        case LINECALLSTATE_DIALTONE:
//            OutputDebugString("Dial Tone\n");
//            break;
//
//        case LINECALLSTATE_DIALING:
//            OutputDebugString("Dialing\n");
//            break;
//
//        case LINECALLSTATE_PROCEEDING:
//            OutputDebugString("Proceeding\n");
//            break;
//
//        case LINECALLSTATE_RINGBACK:
//            OutputDebugString("RingBack\n");
//            break;
//
//        case LINECALLSTATE_BUSY:
//            OutputDebugString("Line busy, shutting down\n");
//            HangupCall();
//            break;
//
//        case LINECALLSTATE_IDLE:
//            OutputDebugString("Line idle\n");
//            HangupCall();
//            break;
//
//        case LINECALLSTATE_SPECIALINFO:
//            OutputDebugString("Special Info, probably couldn't dial number\n");
//            HangupCall();
//            break;
//
//        case LINECALLSTATE_DISCONNECTED:
//        {
//            LPSTR pszReasonDisconnected;
//
//            switch (dwParam2)
//            {
//                case LINEDISCONNECTMODE_NORMAL:
//                    pszReasonDisconnected = "Remote Party Disconnected";
//                    break;
//
//                case LINEDISCONNECTMODE_UNKNOWN:
//                    pszReasonDisconnected = "Disconnected: Unknown reason";
//                    break;
//
//                case LINEDISCONNECTMODE_REJECT:
//                    pszReasonDisconnected = "Remote Party rejected call";
//                    break;
//
//                case LINEDISCONNECTMODE_PICKUP:
//                    pszReasonDisconnected = 
//                        "Disconnected: Local phone picked up";
//                    break;
//
//                case LINEDISCONNECTMODE_FORWARDED:
//                    pszReasonDisconnected = "Disconnected: Forwarded";
//                    break;
//
//                case LINEDISCONNECTMODE_BUSY:
//                    pszReasonDisconnected = "Disconnected: Busy";
//                    break;
//
//                case LINEDISCONNECTMODE_NOANSWER:
//                    pszReasonDisconnected = "Disconnected: No Answer";
//                    break;
//
//                case LINEDISCONNECTMODE_BADADDRESS:
//                    pszReasonDisconnected = "Disconnected: Bad Address";
//                    break;
//
//                case LINEDISCONNECTMODE_UNREACHABLE:
//                    pszReasonDisconnected = "Disconnected: Unreachable";
//                    break;
//
//                case LINEDISCONNECTMODE_CONGESTION:
//                    pszReasonDisconnected = "Disconnected: Congestion";
//                    break;
//
//                case LINEDISCONNECTMODE_INCOMPATIBLE:
//                    pszReasonDisconnected = "Disconnected: Incompatible";
//                    break;
//
//                case LINEDISCONNECTMODE_UNAVAIL:
//                    pszReasonDisconnected = "Disconnected: Unavail";
//                    break;
//
//                case LINEDISCONNECTMODE_NODIALTONE:
//                    pszReasonDisconnected = "Disconnected: No Dial Tone";
//                    break;
//
//                default:
//                    pszReasonDisconnected = 
//                        "Disconnected: LINECALLSTATE; Bad Reason";
//                    break;
//
//            }
//
//            OutputDebugString(pszReasonDisconnected);
//            OutputDebugString("\n");
//            HangupCall();
//            break;
//        }
//
//        case LINECALLSTATE_CONNECTED:  // CONNECTED!!!
//            OutputDebugString("Connected!\n");
//            break;
//
//        default:
//            OutputDebugString("Unhandled LINECALLSTATE message\n");
//            break;
//    }
//}

//
//  FUNCTION: HandleLineErr(long)
//
//  PURPOSE: Handle several of the standard LINEERR errors

BOOL CTapiConnection::HandleLineErr(long lLineErr)
{
    BOOL bRet = FALSE;

    // lLineErr is really an async request ID, not an error.
    if (lLineErr > SUCCESS)
        return bRet;

    // All we do is dispatch the correct error handler.
    switch(lLineErr)
    {
        case SUCCESS:
            bRet = TRUE;
            break;

        case LINEERR_INVALCARD:
        case LINEERR_INVALLOCATION:
        case LINEERR_INIFILECORRUPT:
            OutputDebugString("The values in the INI file are invalid.\n");
            break;

        case LINEERR_NODRIVER:
            OutputDebugString("There is a problem with your Telephony device driver.\n");
            break;

        case LINEERR_REINIT:
            ShutdownTAPI();
            break;

        case LINEERR_NOMULTIPLEINSTANCE:
            OutputDebugString("Remove one of your copies of your Telephony driver.\n");
            break;

        case LINEERR_NOMEM:
            OutputDebugString("Out of memory. Cancelling action.\n");
            break;

        case LINEERR_OPERATIONFAILED:
            OutputDebugString("The TAPI operation failed.\n");
            break;

        case LINEERR_RESOURCEUNAVAIL:
            OutputDebugString("A TAPI resource is unavailable at this time.\n");
            break;

        // Unhandled errors fail.
        default:
            break;
    }
    return bRet;
}


//
//  FUNCTION: BOOL HangupCall()
//
//  PURPOSE: Hangup the call in progress if it exists.

//BOOL CTapiConnection::HangupCall()
//{         
//    LPLINECALLSTATUS pLineCallStatus = NULL;
//    long lReturn;
//
//    // Prevent HangupCall re-entrancy problems.
//    if (m_bStoppingCall)
//        return TRUE;
//
//    // If Tapi is not being used right now, then the call is hung up.
//    if (!m_bTapiInUse)
//        return TRUE;
//
//    m_bStoppingCall = TRUE;
//
//    OutputDebugString("Stopping Call in progress\n");
//
//    // If there is a call in progress, drop and deallocate it.
//    if (m_hCall)
//    {
//        pLineCallStatus = (LPLINECALLSTATUS)malloc(sizeof(LINECALLSTATUS));
//
//        if (!pLineCallStatus)
//        {
//            ShutdownTAPI();
//            m_bStoppingCall = FALSE;
//            return FALSE;
//        }
//
//        lReturn = ::lineGetCallStatus(m_hCall, pLineCallStatus);
//
//        // Only drop the call when the line is not IDLE.
//        if (!((pLineCallStatus -> dwCallState) & LINECALLSTATE_IDLE))
//        {
//            WaitForReply(lineDrop(m_hCall, NULL, 0));
//            OutputDebugString("Call Dropped.\n");
//        }
//
//        // The call is now idle.  Deallocate it!
//        do
//        {
//            lReturn = ::lineDeallocateCall(m_hCall);
//            if (HandleLineErr(lReturn))
//                continue;
//            else
//            {
//                OutputDebugString("lineDeallocateCall unhandled error\n");
//                break;
//            }
//        }
//        while(lReturn != SUCCESS);
//
//        OutputDebugString("Call Deallocated.\n");
//
//    }
//
//    // if we have a line open, close it.
//    if (m_hLine)
//    {
//       lReturn = ::lineClose(m_hLine);
//       if (!HandleLineErr(lReturn))
//           OutputDebugString("lineClose unhandled error\n");
//
//        OutputDebugString("Line Closed.\n");
//    }
//
//    // Clean up.
//    m_hCall = NULL;
//    m_hLine = NULL;
//    m_bTapiInUse = FALSE;
//    m_bStoppingCall = FALSE; // allow HangupCall to be called again.
//
//    // Need to free buffer returned from lineGetCallStatus
//    if (pLineCallStatus)
//        free(pLineCallStatus);  
//        
//    OutputDebugString("Call stopped\n");
//    return TRUE;
//}

//
//  FUNCTION: BOOL ShutdownTAPI()
//
//  PURPOSE: Shuts down all use of TAPI
//
BOOL CTapiConnection::ShutdownTAPI()
{
    long lReturn;

    // If we aren't initialized, then Shutdown is unnecessary.
    if (m_hLineApp == NULL)
        return TRUE;

    // Prevent ShutdownTAPI re-entrancy problems.
    if (m_bShuttingDown)
        return TRUE;

    m_bShuttingDown = TRUE;

    //HangupCall();
    
    do
    {
        lReturn = ::lineShutdown(m_hLineApp);
        if (HandleLineErr(lReturn))
            continue;
        else
        {
            OutputDebugString("lineShutdown unhandled error\n");
            break;
        }
    }
    while(lReturn != SUCCESS);

    m_bTapiInUse = FALSE;
    m_hLineApp = NULL;
    m_hCall = NULL;
    m_hLine = NULL;
    m_bShuttingDown = FALSE;

    OutputDebugString("TAPI uninitialized.\n");

    return TRUE;
}
