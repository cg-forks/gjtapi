package net.sourceforge.gjtapi.raw.javasound.desktopAgent;

import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.AbstractButton;
import javax.swing.UIManager;
import javax.swing.JFrame;


public class DesktopAgentGUI extends JPanel implements ActionListener, WindowListener, Runnable {

    private DesktopAgent desktopAgent;
    private JButton bcall;
    private JButton bhangup;
    private JLabel label;
    private JComboBox playbackDevices;
    private JComboBox captureDevices;
    private GridBagConstraints c = new GridBagConstraints();

    public DesktopAgentGUI(DesktopAgent desktopAgent) {
        this.desktopAgent = desktopAgent;

        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.GRAY)));

        this.setPreferredSize(new Dimension(250, 300));
        this.setMinimumSize(new Dimension(250, 300));
        this.setMaximumSize(new Dimension(250, 300));

        this.setLayout(new GridBagLayout());

        //Status Display
        label = new JLabel();
        label.setPreferredSize(new Dimension(150, 30));
        label.setMinimumSize(new Dimension(150, 30));
        label.setMaximumSize(new Dimension(150, 30));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        label.setBackground(Color.BLACK);
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setText("Welcome");

        JLabel label2 = new JLabel("PlayBack Device:");
        //Create the combo box
        String[] playbackDevList = desktopAgent.getPlaybackMixers();
        playbackDevices = new JComboBox(playbackDevList);
        int i = 0;
        for (String st : playbackDevList) {
            if (st.compareToIgnoreCase(desktopAgent.getSelPlaybackMixer()) == 0) {
                playbackDevices.setSelectedIndex(i);
                break;
            }
            i++;
        }
        //desktopAgent.selectPlaybackMixer((String) playbackDevices.getSelectedItem());
        playbackDevices.setToolTipText("Select a playback device");
        playbackDevices.setActionCommand("PlayBack Devices");
        playbackDevices.addActionListener(this);

        JLabel label3 = new JLabel("Capture Device:");
        //Create the combo box
        String[] captureDevList = desktopAgent.getCaptureMixers();
        captureDevices = new JComboBox(desktopAgent.getCaptureMixers());
        i = 0;
        for (String st : captureDevList) {
            if (st.compareToIgnoreCase(desktopAgent.getSelCaptureMixer()) == 0) {
                captureDevices.setSelectedIndex(i);
                break;
            }
            i++;
        }
        //desktopAgent.selectCaptureMixer((String) captureDevices.getSelectedItem());
        captureDevices.setToolTipText("Select a capture device");
        captureDevices.setActionCommand("Capture Devices");
        captureDevices.addActionListener(this);

        //Call Button
        bcall = new JButton("Incoming Call");
        bcall.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        bcall.setMnemonic(KeyEvent.VK_C);
        bcall.setToolTipText("Simulate an Incoming Call");
        bcall.setActionCommand("Incoming Call");
        bcall.addActionListener(this);

        //Call Button
        bhangup = new JButton("Hang Up");
        bhangup.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        bhangup.setMnemonic(KeyEvent.VK_H);
        bhangup.setToolTipText("Hang Up Call");
        bhangup.setActionCommand("Hang Up");
        bhangup.setEnabled(false);
        bhangup.addActionListener(this);

        //Add Components to this container, using the default FlowLayout.
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(30, 0, 0, 0);
        add(label, c);

        c.gridy++;
        c.insets = new Insets(30, 0, 0, 0);
        add(label2, c);

        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        add(playbackDevices, c);

        c.gridy++;
        c.insets = new Insets(20, 0, 0, 0);
        add(label3, c);

        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        add(captureDevices, c);

        c.gridy++;
        c.insets = new Insets(30, 0, 0, 0);
        add(bcall, c);

        c.gridy++;
        c.insets = new Insets(5, 0, 30, 0);
        add(bhangup, c);

    }

    public void actionPerformed(ActionEvent e) {
        if ("Incoming Call".equals(e.getActionCommand())) {
            label.setText("Calling...");
            bcall.setEnabled(false);
            bhangup.setEnabled(true);
            desktopAgent.incomingCall();
        } else if ("Hang Up".equals(e.getActionCommand())) {
            hangup();
            desktopAgent.remoteHangup();
        } else if ("Answer".equals(e.getActionCommand())) {
            accepted();
            desktopAgent.accepted();

        } else if ("PlayBack Devices".equals(e.getActionCommand())) {
            JComboBox cb = (JComboBox) e.getSource();
            desktopAgent.selectPlaybackMixer((String) cb.getSelectedItem());
        } else if ("Capture Devices".equals(e.getActionCommand())) {
            JComboBox cb = (JComboBox) e.getSource();
            desktopAgent.selectCaptureMixer((String) cb.getSelectedItem());
        }
    }

    /**
     * Accepts Call
     */
    public void accepted() {
        label.setText("Call in progress...");
        bcall.setEnabled(false);
        bhangup.setEnabled(true);
    }

    /**
     * Hangs up call
     */
    public void hangup() {
        label.setText("Ready");
        bhangup.setEnabled(false);
        bcall.setEnabled(true);
        bcall.setText("Incoming Call");
        bcall.setToolTipText("Simulate an Incoming Call");
        bcall.setActionCommand("Incoming Call");
    }


    /**
     * Updates GUI in case of a call made by the JTAPI
     * Simulates a call made to an outside client
     */
    public void call() {
        label.setText("Ringing...");
        bcall.setText("Answer");
        bcall.setActionCommand("Answer");
        bcall.setToolTipText("Answer Call");
    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        //set windows look&feel
       try {
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
       } catch (Exception e) {
           System.err.println("Could not initialize default LookAndFeel");
       }

        //Create and set up the window.
        JFrame frame = new JFrame("Desktop Agent (" + desktopAgent.getAddress() +
                                  ")");
        frame.addWindowListener(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE/* .HIDE_ON_CLOSE*/ /* .EXIT_ON_CLOSE*/);
        frame.setLocation(300,250);

        //Create and set up the content pane.
        DesktopAgentGUI newContentPane = this;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void run() {
        createAndShowGUI();
    }


    //	 ************************ WindowListener Methods ***********************

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
        desktopAgent.shutdown();
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }


}
