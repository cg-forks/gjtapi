package net.sourceforge.gjtapi.util;

/*
	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 

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
/**
 * This is an ordered event manager in which one thread serially takes each event off of a queue
 * and asks an EventHandler to process the event.
 * Creation date: (2000-02-25 12:03:34)
 * @author: Richard Deadman
 */
public class OrderedEventManager extends EventManager {
	private java.lang.Runnable dispatcher = new Runnable() { // thread manager
		public void run() {
			boolean run = true;
			while (run) {
				Object param = null;
				try {
					// wait for an event
					param = get();
					// delegate in order to the handler
					handler.process(param);
				} catch (InterruptedException ie) {
					run = false;
				} catch (RuntimeException ex) {
					if (exHandler != null)
						exHandler.handleException(handler, ex, param);
					else
						throw ex;
				}
			}
		}
	};
/**
 * OrderedEventManager constructor comment.
 * @param eh com.uforce.util.EventHandler
 */
public OrderedEventManager(EventHandler eh) {
	this(eh, null);
}
/**
 * OrderedEventManager constructor comment.
 * @param eh com.uforce.util.EventHandler
 * @param name The name of the event handling thread.
 */
public OrderedEventManager(EventHandler eh, String name) {
	this(eh, name, new NullExceptionHandler());
}
/**
 * OrderedEventManager constructor comment.
 * @param eh com.uforce.util.EventHandler
 * @param name The name of the event handling thread.
 */
public OrderedEventManager(EventHandler eh, String name, ExceptionHandler exh) {
	super(eh, exh);
	
	// now start the manager thread running
	Thread t = new Thread(this.dispatcher, name);
	t.setDaemon(true);	// allows JVM to die while I'm alive
	t.start();
}
}
