package net.sourceforge.gjtapi.test;

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
import net.sourceforge.gjtapi.raw.emulator.EmProvider;
import java.rmi.*;
import java.rmi.registry.*;
import net.sourceforge.gjtapi.raw.ProviderFactory;
import net.sourceforge.gjtapi.raw.remote.*;
/**
 * Test class for creating a server-side RawProvider.
 * Creation date: (2000-02-17 14:47:32)
 * @author: Richard Deadman
 */
public class TestServer {
/**
 * Start up the server with a given name
 * Creation date: (2000-02-17 14:48:47)
 * @author: Richard Deadman
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	String regName;
	if (args.length > 0)
		regName = args[0];
	else
		regName = "Emulator";

	// try to create a registry and look for an existing one if the creation failed
	Registry reg = null;
	try {
		reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
	} catch (RemoteException re) {
		try {
			reg = LocateRegistry.getRegistry();
		} catch (RemoteException re2) {
			System.err.println("Failed to create or locate an rmi registry.");
			System.exit(1);
		}
	}

	// Create a remote provider
	EmProvider ep = new EmProvider();
	ep.initialize(null);
	RemoteProvider rp = null;
	try {
		rp = new RemoteProviderImpl(ProviderFactory.createProvider(ep));
	} catch (Exception ex) {
		System.err.println("Error creating provider:");
		ex.printStackTrace();
		System.exit(1);
	}

	// add to the registry
	try {
		reg.bind(regName, rp);
	} catch (Exception ex) {
		System.err.println("Error binding provider:");
		ex.printStackTrace();
		System.exit(1);
	}

	System.out.println("Provider running");
}
}
