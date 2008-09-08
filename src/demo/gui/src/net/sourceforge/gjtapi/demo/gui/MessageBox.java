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
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MessageBox extends JDialog {
    private JPanel panelBut = new JPanel(new FlowLayout());

    public MessageBox(String title, String header, String message) {
        JButton butClose = new JButton("Close");
        butClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        panelBut.add(butClose);
        initComponents(title, header, message);
    }

    public MessageBox(String title, String header, Exception e) {
        this(title, header, getStackTrace(e));
    }

    public static String getStackTrace(Exception e) {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(s));
        return s.toString();
    }
    
    private void initComponents(String title, String header, String message) {
        setModal(true);
        setTitle(title);
        getContentPane().add(new JLabel(header), BorderLayout.NORTH);
        JScrollPane msgPane = new JScrollPane();
        JTextArea msgArea = new JTextArea(message);
        msgArea.setEditable(false);
        msgPane.setViewportView(msgArea);
        getContentPane().add(msgPane, BorderLayout.CENTER);
        getContentPane().add(panelBut, BorderLayout.SOUTH);

        Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dim = new Dimension(scrDim.width * 3 / 4, scrDim.height * 3 / 4);
        Dimension pref = msgPane.getPreferredSize();
        if(pref.width > dim.width) pref.width = dim.width;
        if(pref.width < 200) pref.width = 200;
        if(pref.height > dim.height) pref.height = dim.height;
        if(pref.height < 200) pref.height = 200;
        msgPane.setPreferredSize(pref);

        pack();

        pref = getSize();
        setLocation((scrDim.width - pref.width) / 3, (scrDim.height - pref.height) / 3);
    }
}
