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
package net.sourceforge.gjtapi.demo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.telephony.Address;
import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.Provider;
import javax.telephony.Terminal;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlCall;
import javax.telephony.callcontrol.CallControlTerminalConnection;
import javax.telephony.media.MediaBindException;
import javax.telephony.media.MediaConfigException;
import javax.telephony.media.MediaProvider;
import javax.telephony.media.MediaResourceException;
import javax.telephony.media.SignalDetectorEvent;

import net.sourceforge.gjtapi.media.GenericMediaService;

import org.apache.log4j.Logger;

public class GjtapiGui {
    private static final Logger logger = Logger.getLogger(GjtapiGui.class);
    
    private CallListenerObserver obsListener;
    private Provider provider;
    private Address address;    

    private JFrame gjtapiFrame;
    private JTextField txtCalledNumber;
    private JButton butCall;

    private JCheckBox ckPrivateData;
    
    private JTextField txtDTMFOut;
    private JButton butDTMFOut;

    private JTextField txtDTMFIn;
    /** The file to be played. */
    private JTextField txtPlayFile;
    /** A file chooser for the file to be played. */
    private JButton butSelectFile;
    /** Plays the selected file. */
    private JButton butPlayFile;
    /** The file to record to. */
    private JTextField txtRecordFile;
    /** Records to the specified file. */
    private JButton butRecordFile;
    /** Records to the speaker, i.e. play the received audio over the speaker. */
    private JButton butSpeaker;
    private JScrollPane callsScrollPane;
    private JList lstCalls;
    private JButton butAnswer;
    private JButton butHangUp;
    private JButton butHold;
    private JButton butUnHold;
    private JButton butJoin;
	
    private JScrollPane traceScrollPane;
    private JTextArea txtTrace;
    
    /**
     * Constructs a new object.
     */
    public GjtapiGui() {
        Logger.getRootLogger().addAppender(new GuiAppender(this));
    }
    
    private void initGjtapi(ProviderSelecion selection)
        throws GjtapiGuiException {
        obsListener = new CallListenerObserver();

        try {
            provider = selection.getProvider();
            logger.info("Provider " + provider.getName()
                    + " successfully loaded.");
        } catch (Exception e) {
            throw new GjtapiGuiException("Cannot load provider "
                    + provider.getName() + ".", e);
        }

        try {
            address = selection.getAddress();
            logger.debug("Address set to " + address.getName());
            logger.debug("Setting a listener on address " + address.getName()
                    + "...");
            address.addCallListener(obsListener);
        } catch (Exception e) {
            throw new GjtapiGuiException("Cannot set address "
                    + address.getName(), e);
        }
    }

    /**
     * Shows the GUI.
     */
    private void showGui() {
        gjtapiFrame.setVisible(true);
     }

    
    public void initGui() {
        gjtapiFrame = new JFrame("GJTAPI Demo - " + address.getName());
        
        JPanel tapiPanel = new JPanel(new BorderLayout());
        gjtapiFrame.getContentPane().add(tapiPanel, BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        tapiPanel.add(inputPanel, BorderLayout.NORTH);
        
        txtCalledNumber = new JTextField(16);
        inputPanel.add(txtCalledNumber, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(10, 10, 0, 10), 0, 0));

        butCall = new JButton("Call");
        butCall.setEnabled(false);
        inputPanel.add(butCall, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0, 
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                new Insets(10, 2, 0, 10), 0, 0));
                
        ckPrivateData = new JCheckBox("Use TAPI3 private data");
        inputPanel.add(ckPrivateData, new GridBagConstraints(3, 0, 2, 1, 0.0, 0.0, 
                GridBagConstraints.LINE_END, GridBagConstraints.NONE, 
                new Insets(10, 10, 0, 10), 0, 0));
                
        txtDTMFOut = new JTextField(16);
        inputPanel.add(txtDTMFOut, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 10, 0, 10), 0, 0));

        butDTMFOut = new JButton("DTMF");
        butDTMFOut.setEnabled(false);
        inputPanel.add(butDTMFOut, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, 
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 2, 0, 10), 0, 0));

        txtPlayFile = new JTextField();
        inputPanel.add(txtPlayFile, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 10, 0, 10), 0, 0));
        butSelectFile = new JButton("Select...");
        butSelectFile.setEnabled(false);
        inputPanel.add(butSelectFile, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 2, 0, 10), 0, 0));
        butSelectFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser();
                FileFilter filter = new WaveFileFilter();
                chooser.setFileFilter(filter);
                int ret = chooser.showOpenDialog(gjtapiFrame);;
                if (ret == JFileChooser.CANCEL_OPTION) {
                    return;
                }
                File file = chooser.getSelectedFile();
                try {
                    txtPlayFile.setText(file.getCanonicalPath());
                    butPlayFile.setEnabled(true);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        butPlayFile = new JButton("Play");
        butPlayFile.setEnabled(false);
        inputPanel.add(butPlayFile, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, 
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 2, 0, 10), 0, 0));
        butPlayFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                GenericMediaService ms = new GenericMediaService(
                        (MediaProvider) provider);
                TerminalConnection terminalConnection =
                    getSelectedTerminalConnection();
                if (terminalConnection == null) {
                    return;
                }
                Terminal terminal = terminalConnection.getTerminal();
                try {
                    ms.bindToTerminal(null, terminal);
                    String fileName = txtPlayFile.getText();
                    File file = new File(fileName);
                    ms.play(file.toURI().toURL().toString(), 0, null, new Hashtable());
                } catch (MediaBindException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MediaConfigException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MediaResourceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        txtRecordFile = new JTextField("out.wav");
        inputPanel.add(txtRecordFile, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 10, 0, 10), 0, 0));
        butRecordFile = new JButton("Record");
        butRecordFile.setEnabled(false);
        inputPanel.add(butRecordFile, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, 
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 2, 0, 10), 0, 0));
        butRecordFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                GenericMediaService ms = new GenericMediaService(
                        (MediaProvider) provider);
                TerminalConnection terminalConnection =
                    getSelectedTerminalConnection();
                if (terminalConnection == null) {
                    return;
                }
                Terminal terminal = terminalConnection.getTerminal();
                try {
                    ms.bindToTerminal(null, terminal);
                    String fileName = txtRecordFile.getText();
                    File file = new File(fileName);
                    ms.record(file.toURI().toURL().toString(), null, new Hashtable());
                } catch (MediaBindException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MediaConfigException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MediaResourceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        butSpeaker = new JButton("Speaker");
        butSpeaker.setEnabled(false);
        inputPanel.add(butSpeaker, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, 
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 2, 0, 10), 0, 0));

        JLabel lbDTMFIn = new JLabel("Received digits");
        inputPanel.add(lbDTMFIn, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, 
                GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, 
                new Insets(10, 10, 0, 2), 0, 0));
        
        txtDTMFIn = new JTextField(32);
        txtDTMFIn.setEditable(false);
        inputPanel.add(txtDTMFIn, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, 
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(10, 2, 0, 0), 0, 0));
        
        inputPanel.add(new JPanel(), new GridBagConstraints(4, 0, 1, 3, 1.0, 1.0, 
                GridBagConstraints.PAGE_END, GridBagConstraints.BOTH, 
                new Insets(10, 10, 0, 10), 0, 0));

        JPanel callPanel = new JPanel(new GridBagLayout());
        tapiPanel.add(callPanel, BorderLayout.CENTER);
        
        lstCalls = new JList(obsListener);
        callsScrollPane = new JScrollPane(lstCalls);
        callsScrollPane.setPreferredSize(new Dimension(480, 72));
        callPanel.add(callsScrollPane, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(10, 10, 0, 10), 0, 0));

        butAnswer = new JButton("Answer");
        butAnswer.setEnabled(false);
        callPanel.add(butAnswer, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(10, 2, 0, 0), 0, 0));

        butHold = new JButton("Hold");
        butHold.setEnabled(false);
        callPanel.add(butHold, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(10, 2, 0, 0), 0, 0));

        butHangUp = new JButton("Hang up");
        butHangUp.setEnabled(false);
        callPanel.add(butHangUp, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 2, 0, 0), 0, 0));

        butUnHold = new JButton("UnHold");
        butUnHold.setEnabled(false);
        callPanel.add(butUnHold, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                new Insets(2, 2, 0, 0), 0, 0));

        butJoin= new JButton("Join");
        butJoin.setEnabled(false);
        callPanel.add(butJoin, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, 
                new Insets(2, 2, 0, 0), 0, 0));
        
        
        callPanel.add(new JPanel(), new GridBagConstraints(3, 0, 1, 2, 1.0, 1.0, 
                GridBagConstraints.PAGE_END, GridBagConstraints.BOTH, 
                new Insets(10, 10, 0, 0), 0, 0));

        JPanel tracePanel = new JPanel(new BorderLayout());
        tracePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        gjtapiFrame.getContentPane().add(tracePanel, BorderLayout.CENTER);
        txtTrace = new JTextArea(16, 80);
        traceScrollPane = new JScrollPane(txtTrace);
        tracePanel.add(traceScrollPane, BorderLayout.CENTER);

        butCall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            call(getCalledNumber());
                        } catch (GjtapiGuiException e) {
                            logger.error(e.getMessage(), e.getCause());
                            new MessageBox("Call error", "Cannot call.", e).setVisible(true);
                        }
                    }
                }.start();
            }
        });
		
        ckPrivateData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new Thread() {
                    @Override
                    public void run() {
                        obsListener.setUsePrivateData(ckPrivateData.isSelected());
                    }
                }.start();
            }
        });
        
        butDTMFOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            GenericMediaService ms = new GenericMediaService((MediaProvider)provider);
                            Terminal terminal = getSelectedTerminalConnection().getTerminal();
                            ms.bindToTerminal(null, terminal);
                            ms.sendSignals(txtDTMFOut.getText(), null, null);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e.getCause());
                            new MessageBox("DTMF error", "Cannot send DTMF.", e).setVisible(true);
                        }
                    }
                }.start();
            }
        });
		
        butAnswer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            final TerminalConnection terminalConnection =
                                getSelectedTerminalConnection();
                            if(terminalConnection != null) {
                                    terminalConnection.answer();
                            }
                        } catch (Exception e) {
                            logger.error("Cannot answer.", e);
                            new MessageBox("Answer error", "Cannot answer.", e).setVisible(true);
                        }
                    }
                }.start();
            }
        });
        
        butHangUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Connection connection = getSelectedConnection();
                            if(connection == null) {
                                return;
                            }
                            logger.debug("HangUp for connection in state "
                                    + TapiUtil.getConnectionStateName(connection));
                            TerminalConnection terminalConnection = getSelectedTerminalConnection();
                            int state = TapiUtil.getTerminalConnectionState(terminalConnection);
                            if(state == CallControlTerminalConnection.RINGING) {
                                terminalConnection.answer();
                                synchronized(terminalConnection) {
                                    terminalConnection.wait(3000);
                                    state = TapiUtil.getTerminalConnectionState(terminalConnection);
                                    if(state != CallControlTerminalConnection.TALKING) {
                                        JOptionPane.showMessageDialog(gjtapiFrame, "Cannot reject the call.");
                                        return;
                                    }
                                }
                            }
                            Thread.sleep(500);
                            connection.disconnect();
                        } catch (Exception e) {
                            logger.error("Cannot disconnect.", e);
                            new MessageBox("HangUp error", "Cannot disconnect.", e).setVisible(true);
                        }
                    }
                }.start();
            }
        });

        butHold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new Thread() {
                    @Override
                    public void run() {
		                try {
		                    TerminalConnection terminalConnection = getSelectedTerminalConnection();
		                    if(terminalConnection != null && terminalConnection instanceof CallControlTerminalConnection) {
		                        CallControlTerminalConnection ccTermConn = (CallControlTerminalConnection)terminalConnection;
		                        ccTermConn.hold();
		                    }
		                } catch (Exception e) {
		                    logger.error("Cannot hold.", e);
		                    new MessageBox("Hold error", "Cannot hold.", e).setVisible(true);
		                }
                    }
                }.start();
            }
        });

        butUnHold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            TerminalConnection terminalConnection = getSelectedTerminalConnection();
                            if(terminalConnection != null && terminalConnection instanceof CallControlTerminalConnection) {
                                CallControlTerminalConnection ccTermConn = (CallControlTerminalConnection)terminalConnection;
                                ccTermConn.unhold();
                            }
                        } catch (Exception e) {
                            logger.error("Cannot unhold.", e);
                            new MessageBox("Unhold error", "Cannot unhold.", e).setVisible(true);
                        }
                    }
                }.start();
            }
        });

        butJoin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            TerminalConnection terminalConnection = getSelectedTerminalConnection();
                            if(terminalConnection != null && terminalConnection instanceof CallControlTerminalConnection) {
                                CallControlCall call1 = (CallControlCall)terminalConnection.getConnection().getCall();
                                CallControlCall call2 = null;

                                for(int i = 0; i < obsListener.size(); i++) {
                                    if(i != lstCalls.getSelectedIndex()) {
                                        call2= (CallControlCall)((CallListenerObserver.Item) obsListener.get(i)).getConnection().getCall();
                                        break;
                                    }
                                }
                                if(call2 != null) {
                                    call1.conference(call2);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Cannot join.", e);
                            new MessageBox("Join error", "Cannot join.", e).setVisible(true);
                        }
                    }
                }.start();
            }
        });

		txtCalledNumber.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateButCall();
            }
            public void insertUpdate(DocumentEvent e) {
                updateButCall();
            }
            public void removeUpdate(DocumentEvent e) {
                updateButCall();
            }            
        });
		
		txtDTMFOut.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateButDTMF();
            }
            public void insertUpdate(DocumentEvent e) {
                updateButDTMF();
            }
            public void removeUpdate(DocumentEvent e) {
                updateButDTMF();
            }            
        });

        lstCalls.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
                updateAllControls();            
            }
        });
		lstCalls.getModel().addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e) {
                updateSelection();
            }
            public void intervalAdded(ListDataEvent e) {
                updateSelection();
            }
            public void intervalRemoved(ListDataEvent e) {
            	// Nothing to do here. The SelectionListener will call updateAllControls() if needed.
            }
            private void updateSelection() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(lstCalls.getModel().getSize() == 1) {
                            lstCalls.setSelectedIndex(0);
                        }
                        updateAllControls();
                    }
                });
            }
        });		
	            
	    gjtapiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    gjtapiFrame.pack();
    }

    private DTMFThread dtmfThread = null;
    private TerminalConnection dtmfTerminalConnection = null;
    private void updateDTMFThread() {
        TerminalConnection tcon = getSelectedTerminalConnection();
        if(tcon != dtmfTerminalConnection) {
            if(dtmfThread != null) {
                dtmfThread.interrupt();
                try {
                    dtmfThread.join();
                } catch(InterruptedException e) {
                    logger.warn("Join interrupted", e);
                }
            }
            if(tcon != null) {
                dtmfThread = new DTMFThread(tcon.getTerminal());
                dtmfThread.start();
            } else {
                dtmfThread = null;
            }
            dtmfTerminalConnection = tcon;
        }
    }
    
    private class DTMFThread extends Thread {
        private final Terminal terminal; 
        public DTMFThread(Terminal terminal) {
            this.terminal = terminal;
        }
        
        @Override
        public void run() {
            final GenericMediaService ms = new GenericMediaService((MediaProvider)provider);
            try {
                ms.bindToTerminal(null, terminal);
                while(!isInterrupted()) {
                    SignalDetectorEvent ev = ms.retrieveSignals(1, null, null, null);
                    if (ev != null) {
                        logger.debug("Retrieved digit: " + ev.getSignalString());
                        txtDTMFIn.setText(txtDTMFIn.getText() + ev.getSignalString());
                    }
                }
            } catch(Exception e) {
                if(e.getCause() == null || !(e.getCause() instanceof InterruptedException)) {
                    logger.error("Failed to retrieve DTMF.", e);
                }
            }
            logger.debug("DTMFThread terminated.");
        }
    };
    
    
    private void updateAllControls() {
        updateButCall();
        updateButDTMF();
        updateCallControls();
        updateDTMFThread();
    }
	
    private void updateButCall() {
        int len = txtCalledNumber.getText().length();
        boolean enabled = (len > 0);
//        enabled &= (lstCalls.getModel().getSize() == 0); 
        butCall.setEnabled(enabled);
    }
    
    private void updateButDTMF() {
        int len = txtDTMFOut.getText().length();
        boolean enabled = (len > 0);
        TerminalConnection terminalConnection;
		try {
			terminalConnection = getSelectedTerminalConnection();
		} catch (RuntimeException e) {
            new MessageBox("DTMF error", "Cannot send digits.", e).setVisible(true);
			return;
		}
		int ccTermConnState = (terminalConnection != null) ? 
                		TapiUtil.getTerminalConnectionState(terminalConnection) : CallControlTerminalConnection.UNKNOWN;        
        butDTMFOut.setEnabled(enabled & ccTermConnState == CallControlTerminalConnection.TALKING);
    }
    
    private void updateCallControls() {
        Connection connection = getSelectedConnection();
        TerminalConnection terminalConnection = getSelectedTerminalConnection();
        int connState = (connection != null) ? connection.getState() : Connection.UNKNOWN;
        int ccTermConnState = (terminalConnection != null) ? 
                		TapiUtil.getTerminalConnectionState(terminalConnection) : CallControlTerminalConnection.UNKNOWN;
        butAnswer.setEnabled(ccTermConnState == CallControlTerminalConnection.RINGING && connState == Connection.ALERTING);
        butHangUp.setEnabled(
                connState == Connection.CONNECTED || 
                connState == Connection.ALERTING || 
                connState == Connection.INPROGRESS || 
                connState == Connection.FAILED);
        butSelectFile.setEnabled(connState == Connection.CONNECTED);
        if (connState != Connection.CONNECTED) {
            butPlayFile.setEnabled(false);
        }
        butRecordFile.setEnabled(connState == Connection.CONNECTED);
        boolean holdEnabled = (ccTermConnState == CallControlTerminalConnection.TALKING);
        // TODO - this should be used only for swapOnHold
        // allow hold only if there is a terminalConnection in state HELD
//        if(holdEnabled) {
//        	holdEnabled = false;
//        	for(int i=0; i<obsListener.size(); i++) {
//                if(i != lstCalls.getSelectedIndex()) {
//                    TerminalConnection heldTerminalConnection = 
//                        ((CallListenerObserver.Item)obsListener.get(i)).getTerminalConnection();
//                    int currState = TapiUtil.getTerminalConnectionState(heldTerminalConnection);
//                    if(currState == CallControlTerminalConnection.HELD) {
//                    	holdEnabled = true;
//                    	break;
//                    }
//                }
//        	}
//        }
        butHold.setEnabled(holdEnabled);

        boolean unholdEnabled = (ccTermConnState == CallControlTerminalConnection.HELD);
        // TODO - this should be used only for swapOnHold
        // allow unhold only if there is a terminalConnection in state TALKING
//        if(unholdEnabled) {
//        	unholdEnabled = false;
//        	for(int i=0; i<obsListener.size(); i++) {
//                if(i != lstCalls.getSelectedIndex()) {
//                    TerminalConnection talkingTerminalConnection = 
//                        ((CallListenerObserver.Item)obsListener.get(i)).getTerminalConnection();
//                    int currState = TapiUtil.getTerminalConnectionState(talkingTerminalConnection);
//                    if(currState == CallControlTerminalConnection.TALKING) {
//                    	unholdEnabled = true;
//                    	break;
//                    }
//                }
//        	}
//        }
        butUnHold.setEnabled(unholdEnabled);
        
        boolean joinEnabled = lstCalls.getModel().getSize() > 1;
        butJoin.setEnabled(joinEnabled);
    }
    
    private String getCalledNumber() {
        return txtCalledNumber.getText();
    }

    private TerminalConnection getSelectedTerminalConnection() {
        TerminalConnection terminalConnection = null;
        CallListenerObserver.Item item = (CallListenerObserver.Item)lstCalls.getSelectedValue();
        if(item != null) {
            terminalConnection = item.getTerminalConnection();
        }
        return terminalConnection;
    }

    private Connection getSelectedConnection() {
        Connection connection = null;
        if(obsListener.getSize() > 0) {
            CallListenerObserver.Item item = 
                (CallListenerObserver.Item) lstCalls.getSelectedValue();
            if(item != null) {
                connection = item.getConnection();
            }
        }
        return connection;
    }

    private void call(String toAddr) throws GjtapiGuiException {
		try {
            logger.debug("Attempting to create call...");
            Call call = provider.createCall();
            
            logger.debug("Attempting to get terminals for an address...");
            Terminal[] terminals = address.getTerminals();

            logger.debug("Attempting to connect call...");
            call.connect(terminals[0], address, toAddr);

            logger.debug("Attempting to get call connections...");
            Connection cons[] = call.getConnections();
            logger.debug(" success: " + cons.length + " connections.");            
        } catch (Exception e) {
            throw new GjtapiGuiException("Call failed.", e);
        }
    }

    public void addLogEntry(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if((txtTrace != null) && txtTrace.isShowing()) {
                    txtTrace.append(message);
                }
            }
        });
    }

    /**
     * Starts the program
     * @param args command line arguments
     *
     * <p>
     * The first argument is expected to be the fully qualified name of the
     * provider.
     * </p>
     */
    public static void main(String[] args) {
        ProviderSelecion selection = new ProviderSelecion();
        if (selection.isCanceled()) {
            System.exit(0);
        }
        GjtapiGui gui = new GjtapiGui();
        try {
            gui.initGjtapi(selection);
        } catch (GjtapiGuiException e) {
            logger.error(e.getMessage(), e);
            new MessageBox("Gjtapi initialization error",
                    "Gjtapi initialization failed.", e).setVisible(true);
            System.exit(-1);
        }
        gui.initGui();
        gui.showGui();
    }
}
