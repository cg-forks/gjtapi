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
#include <time.h>
#include <sys/timeb.h>
#include <stdarg.h>
#include <signal.h>
#include "Logger.h"

const int MAX_MSG_LEN = 1024;

FILE* Logger::logFile = NULL;

// constructor
Logger::Logger(const char* name) {
	m_name = new char[strlen(name)+1];
	strcpy(m_name, name);
}


// destructor
Logger::~Logger() {
	delete[] m_name;
}

void Logger::setLogFile(const char* path) {
	if(logFile != NULL) {
		fclose(logFile);
	}
	logFile = fopen(path, "a");
}

#define LOG_MESSAGE(level) \
	char buffer[MAX_MSG_LEN]; \
	va_list args; \
	va_start(args, format); \
	vsprintf(buffer, format, args); \
	logMsg(level, buffer)

void Logger::debug(const char* format, ...) {
	LOG_MESSAGE(DEBUG_LEVEL);
}

void Logger::info(const char* format, ...) {
	LOG_MESSAGE(INFO_LEVEL);
}

void Logger::warn(const char* format, ...) {
	LOG_MESSAGE(WARN_LEVEL);
}

void Logger::error(const char* format, ...) {
	LOG_MESSAGE(ERROR_LEVEL);
}

void Logger::fatal(const char* format, ...) {
	LOG_MESSAGE(FATAL_LEVEL);
}

const char* levelNames[] = {"DEBUG", "INFO", "WARN", "ERROR", "FATAL"};
void Logger::logMsg(int level, const char* msg) {
	if(logFile == NULL) return;
    struct _timeb	timebuffer;				// time struct
	char			*pchTimeline = NULL;	// time string

	try {
		_ftime( &timebuffer );
		pchTimeline = ctime( & ( timebuffer.time ) );

		if(NULL != pchTimeline) {
			if(NULL != logFile) {
				fprintf(logFile, "%.8s.%03u\t%-5s: %s - %s\n", &pchTimeline[11], timebuffer.millitm, levelNames[level], m_name, msg);
				fflush(logFile);
			}
		}
	} catch(...) {
	}
}
