package net.sourceforge.gjtapi.raw.emulator;

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
import java.util.*;
import javax.swing.*;
import java.awt.*;
/**
 * This is a test wrapper for a Phone Manager.
 * Creation date: (2000-02-09 11:04:41)
 * @author: Richard Deadman
 */
public class TestManager extends PhoneManager {
	private JPanel canvas = new JPanel();
	
	private JFrame frame = null;
	
/**
 * Create a TestManager that displays all the TestPhones
 */
public TestManager(String[] addresses) {
	super(addresses, null);

	this.initialize();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-09 11:23:25)
 * @author: 
 * @return javax.swing.JPanel
 */
private javax.swing.JPanel getCanvas() {
	return canvas;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}
/**
 * Initialize the class.
 */
private void initialize() {
	try {
		JPanel canvas = this.getCanvas();
		canvas.setName("Test Manager");
		canvas.setLayout(new java.awt.GridBagLayout());
		canvas.setSize(500, 560);

		// Now we add each phone to the grid bag layout
		Hashtable<String, TestPhone> phones = this.getPhones();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		Enumeration<TestPhone> e = phones.elements();
		while (e.hasMoreElements()) {
			canvas.add((TestPhone)e.nextElement(), gbc);
			// update the grid location for the next placement
			if (gbc.gridx == 0)
				gbc.gridx = 1;
			else {
				gbc.gridx = 0;
				gbc.gridy++;
			}
		}
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		javax.swing.JFrame frame = new TestManager(args).show();
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JPanel");
		exception.printStackTrace(System.out);
	}
}
/**
 * Create a view for the TestManager
 * Creation date: (2000-02-10 13:11:37)
 * @author: Richard Deadman
 * @return The top-level frome
 */
JFrame show() {
	JFrame frame = new javax.swing.JFrame();
	JPanel canvas = this.getCanvas();
	JScrollPane scroller = new JScrollPane(canvas);
	frame.setContentPane(scroller);
	frame.setSize(canvas.getSize());
	frame.setVisible(true);
	this.setFrame(frame);
	return frame;
}

/**
 * Dispose of the GUI frame and its screen resources.
 *
 */
void close() {
	JFrame frame = this.getFrame();
	if (frame != null) {
		frame.dispose();
		this.setFrame(null);
	}
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	// Insert code to print the receiver here.
	// This implementation forwards the message to super. You may replace or supplement this.
	return super.toString();
}

private void setFrame(JFrame frame) {
	this.frame = frame;
}

private JFrame getFrame() {
	return frame;
}
}
