package ca.deadman.gjtapi.raw.remote.webservices;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/*
	Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca)

	All rights reserved.

	This software is dual licenced under the GPL and a commercial license.
	If you wish to use under the GPL, the following license applies, otherwise
	please contact Deadman Consulting at sales@deadman.ca for commercial licensing.

    ---

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

/**
 * Factory class that is used to create the appropriate Adapter by looking it up in a properties file
 * @author rdeadman
 *
 */
public class AdapterFactory {
	// the resource file name
	private final static String RES_NAME = "server.props";
	// the name of the property that points to the adapter class
	private final static String ADAPTER_CLASS = "ca.deadman.gjtapi.wsAdapter";

	/**
	 * Constructor for AdapterFactory.
	 */
	public AdapterFactory() {
		super();
	}

	public GJtapiWebServiceIF getAdapter() {
		GJtapiWebServiceIF adapter = null;

		// load the properties from a Resource file
		Properties props = new Properties();
		try {
			props.load(this.getClass().getResourceAsStream(RES_NAME));
		} catch (IOException ioe) {
			// eat and hope that the initialize method sets my required properties
		} catch (NullPointerException npe) {
		}

		// first check if the adapter class name is in the properties set
		String classname = props.getProperty(ADAPTER_CLASS, "ca.deadman.gjtapi.raw.remote.webservices.GJtapiWebServiceAdapter");

		// instantiate
		Object o = null;
		Throwable cause = null;
		try {
			Class[] constructorParamClasses = {Properties.class};
			Class constructor = Class.forName(classname);
			try {
				Constructor con = constructor.getConstructor(constructorParamClasses);
				Object[] propSet = {props};
				o = con.newInstance(propSet);
			} catch (NoSuchMethodException nsme) {
				cause = nsme;
				// fall through to null test
			} catch (InvocationTargetException ite) {
				cause = ite.getCause();
				// fall through to null test
			}
			if (o == null) {
				// try empty constructor
				//o = constructor.newInstance();
			}
		} catch (ClassNotFoundException cnfe) {
			cause = cnfe;
			// fall through
		} catch (InstantiationException ie) {
			cause = ie;
			// fall through
		} catch (IllegalAccessException iae) {
			cause = iae;
			// fall through
		}

		if (o == null) {
			throw new RuntimeException("Error creating server Adapter", cause);
		}
		if (o instanceof GJtapiWebServiceIF) {
			adapter = (GJtapiWebServiceIF)o;
		} else {
			System.err.println("Could not instantiate GJTAPI Adapter, not correct interface: " + classname);
			throw new RuntimeException("Incorrect classname defined for GjtapiWebService adapter: " + classname);
		}
		return adapter;
	}

}
