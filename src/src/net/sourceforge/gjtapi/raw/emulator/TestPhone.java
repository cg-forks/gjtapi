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
import net.sourceforge.gjtapi.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.*;
import javax.telephony.*;

/**
 * This is a simple implementation of RawPhone that provides a GUI interface.
 * Currently simple calls may be made, received and disconnected.
 * MVC is not currently implemented, but should be.
 * Creation date: (2000-02-07 14:03:01)
 * @author: Richard Deadman
 */
public class TestPhone extends JPanel implements PhoneListener, RawPhone {
	static final long serialVersionUID = 2671358653119242976L;	// should never be serialized

class IvjEventHandler implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == TestPhone.this.getButton1()) 
				connEtoC1();
			if (e.getSource() == TestPhone.this.getButton2()) 
				connEtoC2();
			if (e.getSource() == TestPhone.this.getButton3()) 
				connEtoC3();
			if (e.getSource() == TestPhone.this.getButton4()) 
				connEtoC4();
			if (e.getSource() == TestPhone.this.getButton5()) 
				connEtoC5();
			if (e.getSource() == TestPhone.this.getButton6()) 
				connEtoC6();
			if (e.getSource() == TestPhone.this.getButton7()) 
				connEtoC7();
			if (e.getSource() == TestPhone.this.getButton8()) 
				connEtoC8();
			if (e.getSource() == TestPhone.this.getButton9()) 
				connEtoC9();
			if (e.getSource() == TestPhone.this.getButtonStar()) 
				connEtoC10();
			if (e.getSource() == TestPhone.this.getButton0()) 
				connEtoC11();
			if (e.getSource() == TestPhone.this.getButtonPound()) 
				connEtoC12();
			if (e.getSource() == TestPhone.this.getHookSwitch()) 
				connEtoC13();
			if (e.getSource() == TestPhone.this.getDialString()) 
				connEtoC14();
			if (e.getSource() == TestPhone.this.getDialButton()) 
				connEtoC15();
			if (e.getSource() == TestPhone.this.getHoldButton()) 
				connEtoC16();
		};
	}
	private JButton ivjButton0 = null;
	private JButton ivjButton1 = null;
	private JButton ivjButton2 = null;
	private JButton ivjButton3 = null;
	private JButton ivjButton4 = null;
	private JButton ivjButton5 = null;
	private JButton ivjButton6 = null;
	private JButton ivjButton7 = null;
	private JButton ivjButton8 = null;
	private JButton ivjButton9 = null;
	private JButton ivjButtonPound = null;
	private JButton ivjButtonStar = null;
	private JTextField ivjDialString = null;
	IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private JToggleButton ivjHookSwitch = null;
	private JTextField ivjStatusLine = null;
	private JButton ivjDialButton = null;
	private JLabel ivjPhoneLabel = null;
	private PhoneManager manager = null;
	private JButton ivjHoldButton = null;
	private PhoneModel model;
public TestPhone(String addr, PhoneManager pm) {
	super();
	
	this.setManager(pm);
	this.setModel(new PhoneModel(addr, this, this.getManager()));
	initialize();
}
/**
 * addLeg method comment.
 */
public boolean add(Leg leg) {
	return this.getModel().add(leg);
	// events?
}
/**
 * Answer a ringing call
 */
public boolean answer() {
	boolean result = this.getModel().answer();
	if (result)
		this.enable("Active", true, true, "Send");
	return result;
}
/**
 * Answer a ringing call
 */
public boolean answer(CallId id) {
	boolean result = this.getModel().answer(id);
	if (result)
		this.enable("Active", true, true, "Send");
	return result;
}
/**
 * Append the given string to the undialed dial string
 * Creation date: (2000-02-07 15:48:32)
 * @author: Richard Deadman
 */
private void appendToDialString(String toBeDialed) {
	if (this.getState() == RawPhone.IDLE)
		return;	// We don't allow digits when the phone is off-hook

	javax.swing.text.Document d = this.getDialString().getDocument();
	try {
		d.insertString(d.getEndPosition().getOffset() - 1,toBeDialed, null);
	} catch (javax.swing.text.BadLocationException ble) {
		System.out.println("Bad Location: " + (d.getEndPosition().getOffset() - 1));
	}
}
/**
 * bridged method comment.
 */
public void bridged() {
	this.enable("Bridged (ready to join)", true, true, "Send");
	this.getHoldButton().setText("Join");
}
/**
 * Dial 0
 */
public void button0_ActionEvents() {
	this.appendToDialString("0");
	return;
}
/**
 * Send a 1 to the textField
 */
public void button1_ActionEvents() {
	this.appendToDialString("1");
	return;
}
/**
 * Add 2...
 */
public void button2_ActionEvents() {
	this.appendToDialString("2");
	return;
}
/**
 * Enter 3 as a dialed digit
 */
public void button3_ActionEvents() {
	this.appendToDialString("3");
	return;
}
/**
 * Dial 4
 */
public void button4_ActionEvents() {
	this.appendToDialString("4");
	return;
}
/**
 * Dial 5
 */
public void button5_ActionEvents() {
	this.appendToDialString("5");
	return;
}
/**
 * Dial 6
 */
public void button6_ActionEvents() {
	this.appendToDialString("6");
	return;
}
/**
 * Comment
 */
public void button7_ActionEvents() {
	this.appendToDialString("7");
	return;
}
/**
 * Dial 8
 */
public void button8_ActionEvents() {
	this.appendToDialString("8");
	return;
}
/**
 * Dial 9
 */
public void button9_ActionEvents() {
	this.appendToDialString("9");
	return;
}
/**
 * Dial an octothorpe
 */
public void buttonPound_ActionEvents() {
	this.appendToDialString("#");
	return;
}
/**
 * Dial an asterix
 */
public void buttonStar_ActionEvents() {
	this.appendToDialString("*");
	return;
}
/**
 * I must have a Held and active call to conference.
 */
public boolean conference() {
	boolean result = this.getModel().conference();
	if (result) {
		this.enable("Conference", true, true, "Send");
		this.getHoldButton().setText("Hold");
	} else
		this.setStatus("Conference Failed");
	return result;
}
/**
 * The phone is now in an active call
 */
public void connected() {
	this.enable("Active", true, true, "Send");
}
/**
 * connEtoC1:  (Button1.action. --> TestPhone.jButton1_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1() {
	try {
		// user code begin {1}
		// user code end
		this.button1_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC10:  (ButtonStar.action. --> TestPhone.buttonStar_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC10() {
	try {
		// user code begin {1}
		// user code end
		this.buttonStar_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC11:  (Button0.action. --> TestPhone.button0_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC11() {
	try {
		// user code begin {1}
		// user code end
		this.button0_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC12:  (ButtonPound.action. --> TestPhone.buttonPound_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC12() {
	try {
		// user code begin {1}
		// user code end
		this.buttonPound_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC13:  (HookSwitch.action. --> TestPhone.hookSwitch_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC13() {
	try {
		// user code begin {1}
		// user code end
		this.hookSwitch_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC14:  (DialString.action. --> TestPhone.dialString_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC14() {
	try {
		// user code begin {1}
		// user code end
		this.dialString_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC15:  (DialButton.action. --> TestPhone.dialString_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC15() {
	try {
		// user code begin {1}
		// user code end
		this.dialString_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC16:  (HoldButton.action. --> TestPhone.holdButton_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC16() {
	try {
		// user code begin {1}
		// user code end
		this.holdButton_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC2:  (Button2.action. --> TestPhone.button2_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2() {
	try {
		// user code begin {1}
		// user code end
		this.button2_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC3:  (Button3.action. --> TestPhone.button3_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3() {
	try {
		// user code begin {1}
		// user code end
		this.button3_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC4:  (Button4.action. --> TestPhone.button4_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC4() {
	try {
		// user code begin {1}
		// user code end
		this.button4_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC5:  (Button5.action. --> TestPhone.button5_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC5() {
	try {
		// user code begin {1}
		// user code end
		this.button5_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC6:  (Button6.action. --> TestPhone.button6_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC6() {
	try {
		// user code begin {1}
		// user code end
		this.button6_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC7:  (Button7.action. --> TestPhone.button7_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC7() {
	try {
		// user code begin {1}
		// user code end
		this.button7_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC8:  (Button8.action. --> TestPhone.button8_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC8() {
	try {
		// user code begin {1}
		// user code end
		this.button8_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC9:  (Button9.action. --> TestPhone.button9_ActionEvents()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC9() {
	try {
		// user code begin {1}
		// user code end
		this.button9_ActionEvents();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * consult method comment.
 */
public boolean consult() {
	this.enable("Consulting", false, false, null);
	return true;
}
/**
 * Create a new active call
 * Creation date: (2000-03-01 9:09:34)
 * @author: Richard Deadman
 * @return true if the call was created.
 */
public boolean createCall() {
	boolean res = this.getModel().createCall();
	if (res)
		this.enable("Dialtone", true, false, "Dial");
	return res;
}
/**
 * Cause the phone to dial a set of digits
 */
public void dial(String digits) {
	try {
		this.getModel().dial(digits);
	} catch (RawStateException rse) {
		this.setStatus("Busy Signal");
	} catch (InvalidPartyException ipe) {
		this.setStatus("Party unknown: " + ipe.getMessage());
	}
}
/**
 * dialing method comment.
 */
public void dialing() {
	this.enable("Dialing...", false, false, null);
}
/**
 * Dial digits when the user enters <Enter> from digit line or presses
 * the Dial button
 */
public void dialString_ActionEvents() {
	Document doc = this.getDialString().getDocument();
	try {
		this.dial(doc.getText(0, doc.getLength()));
		doc.remove(0, doc.getLength());
	} catch (BadLocationException ble) {
		// then we have a real problem... just report it and ignore
		System.out.println("Dial String length inconsistency: " + doc.getLength());
	}
	return;
}
/**
 * drop method comment.
 */
public boolean drop() {
	boolean res = this.getModel().drop();
	if (res)
		this.enable("Call Dropped", false, false, "Dial");
	return res;
}
/**
 * Change the widget state of the Phone
 * Creation date: (2000-02-07 16:12:52)
 * @author: Richard Deadman
 */
private void enable(String status, boolean inputEnabled, boolean holdEnabled, String submitLabel) {
	
	this.setStatus(status);

	// enable or diable dialing input
	JToggleButton tog = this.getHookSwitch();
	if (inputEnabled)
		tog.setText("Off Hook");
	else
		tog.setText("On Hook");
	this.getDialString().setEnabled(inputEnabled);
	this.getDialButton().setEnabled(inputEnabled);
	this.getHoldButton().setEnabled(holdEnabled);
	if (submitLabel != null)
		this.getDialButton().setText(submitLabel);
}
/**
 * Return the address associated with the Phone.
 * Creation date: (2000-02-08 15:52:06)
 * @author: Richard Deadman
 * @return The phone address
 */
public java.lang.String getAddress() {
	return this.getModel().getAddress();
}
/**
 * Return the Button0 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton0() {
	if (ivjButton0 == null) {
		try {
			ivjButton0 = new javax.swing.JButton();
			ivjButton0.setName("Button0");
			ivjButton0.setText("0");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton0;
}
/**
 * Return the Button1 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton1() {
	if (ivjButton1 == null) {
		try {
			ivjButton1 = new javax.swing.JButton();
			ivjButton1.setName("Button1");
			ivjButton1.setText("1");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton1;
}
/**
 * Return the Button2 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton2() {
	if (ivjButton2 == null) {
		try {
			ivjButton2 = new javax.swing.JButton();
			ivjButton2.setName("Button2");
			ivjButton2.setText("2");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton2;
}
/**
 * Return the Button3 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton3() {
	if (ivjButton3 == null) {
		try {
			ivjButton3 = new javax.swing.JButton();
			ivjButton3.setName("Button3");
			ivjButton3.setText("3");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton3;
}
/**
 * Return the Button4 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton4() {
	if (ivjButton4 == null) {
		try {
			ivjButton4 = new javax.swing.JButton();
			ivjButton4.setName("Button4");
			ivjButton4.setText("4");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton4;
}
/**
 * Return the Button5 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton5() {
	if (ivjButton5 == null) {
		try {
			ivjButton5 = new javax.swing.JButton();
			ivjButton5.setName("Button5");
			ivjButton5.setText("5");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton5;
}
/**
 * Return the Button6 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton6() {
	if (ivjButton6 == null) {
		try {
			ivjButton6 = new javax.swing.JButton();
			ivjButton6.setName("Button6");
			ivjButton6.setText("6");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton6;
}
/**
 * Return the Button7 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton7() {
	if (ivjButton7 == null) {
		try {
			ivjButton7 = new javax.swing.JButton();
			ivjButton7.setName("Button7");
			ivjButton7.setText("7");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton7;
}
/**
 * Return the Button8 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton8() {
	if (ivjButton8 == null) {
		try {
			ivjButton8 = new javax.swing.JButton();
			ivjButton8.setName("Button8");
			ivjButton8.setText("8");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton8;
}
/**
 * Return the Button9 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButton9() {
	if (ivjButton9 == null) {
		try {
			ivjButton9 = new javax.swing.JButton();
			ivjButton9.setName("Button9");
			ivjButton9.setText("9");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButton9;
}
/**
 * Return the ButtonPound property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButtonPound() {
	if (ivjButtonPound == null) {
		try {
			ivjButtonPound = new javax.swing.JButton();
			ivjButtonPound.setName("ButtonPound");
			ivjButtonPound.setText("#");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButtonPound;
}
/**
 * Return the ButtonStar property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButtonStar() {
	if (ivjButtonStar == null) {
		try {
			ivjButtonStar = new javax.swing.JButton();
			ivjButtonStar.setName("ButtonStar");
			ivjButtonStar.setText("*");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButtonStar;
}
/**
 * Delegate to model
 */
public RawCall[] getCalls() {
	return this.getModel().getCalls();
}
/**
 * Return the DialButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getDialButton() {
	if (ivjDialButton == null) {
		try {
			ivjDialButton = new javax.swing.JButton();
			ivjDialButton.setName("DialButton");
			ivjDialButton.setText("Dial");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDialButton;
}
/**
 * Return the DialString property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getDialString() {
	if (ivjDialString == null) {
		try {
			ivjDialString = new javax.swing.JTextField();
			ivjDialString.setName("DialString");
			ivjDialString.setEnabled(false);
			ivjDialString.setHorizontalAlignment(javax.swing.JTextField.LEFT);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDialString;
}
/**
 * Return the HoldButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getHoldButton() {
	if (ivjHoldButton == null) {
		try {
			ivjHoldButton = new javax.swing.JButton();
			ivjHoldButton.setName("HoldButton");
			ivjHoldButton.setText("Hold");
			ivjHoldButton.setEnabled(false);
			ivjHoldButton.setActionCommand("HoldButton");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjHoldButton;
}
/**
 * Return the HookSwitch property value.
 * @return javax.swing.JToggleButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JToggleButton getHookSwitch() {
	if (ivjHookSwitch == null) {
		try {
			ivjHookSwitch = new javax.swing.JToggleButton();
			ivjHookSwitch.setName("HookSwitch");
			ivjHookSwitch.setToolTipText("Clickhere to pick up or put down the phone");
			ivjHookSwitch.setText("On Hook");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjHookSwitch;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-09 10:27:27)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.emulator.PhoneManager
 */
private PhoneManager getManager() {
	return manager;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-29 13:02:24)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.emulator.PhoneModel
 */
PhoneModel getModel() {
	return model;
}
/**
 * Return the PhoneLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getPhoneLabel() {
	if (ivjPhoneLabel == null) {
		try {
			ivjPhoneLabel = new javax.swing.JLabel();
			ivjPhoneLabel.setName("PhoneLabel");
			ivjPhoneLabel.setText("Address Unknown");
			ivjPhoneLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPhoneLabel;
}
/**
 * Get the state of the phone
 * Creation date: (2000-02-07 16:12:52)
 * @author: Richard Deadman
 * @return The current state
 */
public int getState() {
	return this.getModel().getState();
}
/**
 * Return the StatusLine property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getStatusLine() {
	if (ivjStatusLine == null) {
		try {
			ivjStatusLine = new javax.swing.JTextField();
			ivjStatusLine.setName("StatusLine");
			ivjStatusLine.setEditable(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjStatusLine;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	exception.printStackTrace(System.out);
}
/**
 * Hold the phone
 */
public boolean hold() {
	return this.getModel().hold();
}
/**
 * Toggle the phone hold state
 */
public void holdButton_ActionEvents() {
	switch (this.getState()) {
		case RawPhone.ACTIVE: {
			this.hold();
			break;
		} 
		case RawPhone.HOLD: {
			this.unHold();
			break;
		} 
		case RawPhone.BRIDGED: {
			this.conference();
			break;
		} 
		default: {
			System.err.println("Error in hold button action.");
		}
	}
}
/**
 * Tell the state machine to change the phone's state
 */
public void hookSwitch_ActionEvents() {
	if (this.isOffHook()) {
		if (this.getState() == RawPhone.RINGING)	// answer
			this.answer();
		else {		// create new call
			this.createCall();
		}
	} else {
		// exit any active calls
		this.drop();
	}
}
/**
 * The call has been removed
 */
public void idle() {
	if (this.isOffHook())
		this.createCall();
	else
		this.enable("Idle", false, false, "Dial");
}
/**
 * React to the joining of a call
 */
public void inCall(Leg newLeg) {
	this.enable("New Call", true, true, "Dial");
}
/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	getButton1().addActionListener(ivjEventHandler);
	getButton2().addActionListener(ivjEventHandler);
	getButton3().addActionListener(ivjEventHandler);
	getButton4().addActionListener(ivjEventHandler);
	getButton5().addActionListener(ivjEventHandler);
	getButton6().addActionListener(ivjEventHandler);
	getButton7().addActionListener(ivjEventHandler);
	getButton8().addActionListener(ivjEventHandler);
	getButton9().addActionListener(ivjEventHandler);
	getButtonStar().addActionListener(ivjEventHandler);
	getButton0().addActionListener(ivjEventHandler);
	getButtonPound().addActionListener(ivjEventHandler);
	getHookSwitch().addActionListener(ivjEventHandler);
	getDialString().addActionListener(ivjEventHandler);
	getDialButton().addActionListener(ivjEventHandler);
	getHoldButton().addActionListener(ivjEventHandler);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("TestPhone");
		setLayout(new java.awt.GridBagLayout());
		setSize(393, 275);

		java.awt.GridBagConstraints constraintsButton1 = new java.awt.GridBagConstraints();
		constraintsButton1.gridx = 0; constraintsButton1.gridy = 1;
		constraintsButton1.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton1.weightx = 1.0;
		constraintsButton1.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton1(), constraintsButton1);

		java.awt.GridBagConstraints constraintsButton2 = new java.awt.GridBagConstraints();
		constraintsButton2.gridx = 1; constraintsButton2.gridy = 1;
		constraintsButton2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton2.weightx = 1.0;
		constraintsButton2.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton2(), constraintsButton2);

		java.awt.GridBagConstraints constraintsButton3 = new java.awt.GridBagConstraints();
		constraintsButton3.gridx = 2; constraintsButton3.gridy = 1;
		constraintsButton3.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton3.weightx = 1.0;
		constraintsButton3.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton3(), constraintsButton3);

		java.awt.GridBagConstraints constraintsButton4 = new java.awt.GridBagConstraints();
		constraintsButton4.gridx = 0; constraintsButton4.gridy = 2;
		constraintsButton4.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton4.weightx = 1.0;
		constraintsButton4.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton4(), constraintsButton4);

		java.awt.GridBagConstraints constraintsButton5 = new java.awt.GridBagConstraints();
		constraintsButton5.gridx = 1; constraintsButton5.gridy = 2;
		constraintsButton5.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton5.weightx = 1.0;
		constraintsButton5.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton5(), constraintsButton5);

		java.awt.GridBagConstraints constraintsButton6 = new java.awt.GridBagConstraints();
		constraintsButton6.gridx = 2; constraintsButton6.gridy = 2;
		constraintsButton6.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton6.weightx = 1.0;
		constraintsButton6.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton6(), constraintsButton6);

		java.awt.GridBagConstraints constraintsButton7 = new java.awt.GridBagConstraints();
		constraintsButton7.gridx = 0; constraintsButton7.gridy = 3;
		constraintsButton7.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton7.weightx = 1.0;
		constraintsButton7.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton7(), constraintsButton7);

		java.awt.GridBagConstraints constraintsButton8 = new java.awt.GridBagConstraints();
		constraintsButton8.gridx = 1; constraintsButton8.gridy = 3;
		constraintsButton8.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton8.weightx = 1.0;
		constraintsButton8.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton8(), constraintsButton8);

		java.awt.GridBagConstraints constraintsButton9 = new java.awt.GridBagConstraints();
		constraintsButton9.gridx = 2; constraintsButton9.gridy = 3;
		constraintsButton9.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton9.weightx = 1.0;
		constraintsButton9.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton9(), constraintsButton9);

		java.awt.GridBagConstraints constraintsButton0 = new java.awt.GridBagConstraints();
		constraintsButton0.gridx = 1; constraintsButton0.gridy = 4;
		constraintsButton0.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButton0.weightx = 1.0;
		constraintsButton0.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButton0(), constraintsButton0);

		java.awt.GridBagConstraints constraintsButtonStar = new java.awt.GridBagConstraints();
		constraintsButtonStar.gridx = 0; constraintsButtonStar.gridy = 4;
		constraintsButtonStar.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButtonStar.weightx = 1.0;
		constraintsButtonStar.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButtonStar(), constraintsButtonStar);

		java.awt.GridBagConstraints constraintsButtonPound = new java.awt.GridBagConstraints();
		constraintsButtonPound.gridx = 2; constraintsButtonPound.gridy = 4;
		constraintsButtonPound.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButtonPound.weightx = 1.0;
		constraintsButtonPound.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButtonPound(), constraintsButtonPound);

		java.awt.GridBagConstraints constraintsDialString = new java.awt.GridBagConstraints();
		constraintsDialString.gridx = 0; constraintsDialString.gridy = 0;
		constraintsDialString.gridwidth = 2;
		constraintsDialString.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsDialString.weightx = 1.0;
		constraintsDialString.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getDialString(), constraintsDialString);

		java.awt.GridBagConstraints constraintsStatusLine = new java.awt.GridBagConstraints();
		constraintsStatusLine.gridx = 0; constraintsStatusLine.gridy = 5;
		constraintsStatusLine.gridwidth = 3;
		constraintsStatusLine.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsStatusLine.weightx = 1.0;
		constraintsStatusLine.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getStatusLine(), constraintsStatusLine);

		java.awt.GridBagConstraints constraintsDialButton = new java.awt.GridBagConstraints();
		constraintsDialButton.gridx = 2; constraintsDialButton.gridy = 0;
		constraintsDialButton.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsDialButton.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getDialButton(), constraintsDialButton);

		java.awt.GridBagConstraints constraintsHookSwitch = new java.awt.GridBagConstraints();
		constraintsHookSwitch.gridx = 0; constraintsHookSwitch.gridy = 6;
		constraintsHookSwitch.gridwidth = 2;
		constraintsHookSwitch.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsHookSwitch.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getHookSwitch(), constraintsHookSwitch);

		java.awt.GridBagConstraints constraintsHoldButton = new java.awt.GridBagConstraints();
		constraintsHoldButton.gridx = 2; constraintsHoldButton.gridy = 6;
		constraintsHoldButton.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getHoldButton(), constraintsHoldButton);

		java.awt.GridBagConstraints constraintsPhoneLabel = new java.awt.GridBagConstraints();
		constraintsPhoneLabel.gridx = 0; constraintsPhoneLabel.gridy = 7;
		constraintsPhoneLabel.gridwidth = 3;
		constraintsPhoneLabel.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsPhoneLabel.insets = new java.awt.Insets(4, 10, 4, 10);
		add(getPhoneLabel(), constraintsPhoneLabel);
		initConnections();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	this.getPhoneLabel().setText(this.getAddress());
	// user code end
}
/**
 * Test if the phone is off-hook
 * Creation date: (2000-02-09 12:44:23)
 * @author: Richard Deadman
 * @return true if the phone is off-hook
 */
private boolean isOffHook() {
	return this.getHookSwitch().isSelected();
}
/**
 * Tell the phone to go on or off hook
 */
public boolean offHook(boolean off) {
	JToggleButton tog = this.getHookSwitch();
	if (this.isOffHook() != off)
		tog.doClick();
	return this.isOffHook();
}
/**
 * onHold method comment.
 */
public boolean onHold() {
	this.enable("On Hold", true, true, "Dial");
	this.getHoldButton().setText("Unhold");
	return true;
}
/**
 * Note the reception or DTMF digits
 */
public void receiveDTMF(String digits) {
	this.getModel().receiveDTMF(digits);
	this.setStatus("Received DTMF: " + digits);
}
/**
 * receiving method comment.
 */
public void receiving(String digits) {
	this.enable("Receiving " + digits, true, true, null);
}
/**
 * drop method comment.
 */
public boolean remove(Leg leg) {
	this.enable("Call Dropped", false, false, "Dial");
	return true;
}
/**
 * reportDTMF method comment.
 */
public String reportDTMF(int num) {
	return this.getModel().reportDTMF(num);
}
/**
 * ringing method comment.
 */
public void ringing() {
	this.enable("Ringing", false, false, null);
}
/**
 * Notes whether to send detected DTMF signals as events
 * Creation date: (2000-05-10 13:57:17)
 * @author: Richard Deadman
 * @param flag if true, send the events, otherwise suppress them
 */
public void sendDetectedDtmf(boolean flag) {
	this.getModel().sendDetectedDtmf(flag);
}
/**
 * Forward the message (DTMF signals) to all call participants
 */
public void sendDTMF(java.lang.String msg) {
	this.getModel().sendDTMF(msg);
}
/**
 * sending method comment.
 */
public void sending() {
	this.enable("Sending DTMF...", true, true, null);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-09 10:27:27)
 * @author: 
 * @param newManager net.sourceforge.gjtapi.raw.emulator.PhoneManager
 */
private void setManager(PhoneManager newManager) {
	manager = newManager;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-29 13:02:24)
 * @author: 
 * @param newModel net.sourceforge.gjtapi.raw.emulator.PhoneModel
 */
private void setModel(PhoneModel newModel) {
	model = newModel;
}
/**
 * Set the status for the phone
 * Creation date: (2000-02-07 15:48:32)
 * @author: Richard Deadman
 */
public void setStatus(String status) {
	javax.swing.text.Document d = this.getStatusLine().getDocument();
	int length = d.getLength();
	try {
		d.remove(0, length);
		d.insertString(0, status, null);
	} catch (javax.swing.text.BadLocationException ble) {
		System.out.println("Bad Status Location: " + (d.getEndPosition().getOffset() - 1));
	}
}
/**
 * swap method comment.
 */
public void swap(Leg oldLeg, Leg newLeg) {
	this.getModel().swap(oldLeg, newLeg);
}
/**
 * swap method comment.
 */
public Leg swap(RawCall call, Leg oldLeg, TelephonyListener sink) {
	return this.getModel().swap(call, oldLeg, sink);
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "TestPhone with address: " + this.getAddress() + ".  Currently in state: " + this.getState();
}
/**
 * unHold method comment.
 */
public boolean unHold() {
	boolean res = this.getModel().unHold();
	if (res) {
		this.enable("Off Hold", true, true, "Send");
		this.getHoldButton().setText("Hold");
	}
	return res;
		
}
}
