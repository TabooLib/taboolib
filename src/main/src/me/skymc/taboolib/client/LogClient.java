package me.skymc.taboolib.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

public class LogClient extends JFrame {

	/**
	 *  DEFAULT	VERSION UID
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea();
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public LogClient(String title) {
		super(title);
		
		// DEFAULT CLOSE OPERATION
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// SETTINGS
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new BevelBorder(BevelBorder.RAISED));
		scrollPane.setViewportView(textArea);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		setSize(700, 500);
		setVisible(true);
		
		// CON'T EDIT
		textArea.setEditable(false);
		textArea.setFont(new Font("ºÚÌå", 0, 18));
		
		textArea.setBackground(Color.black);
		textArea.setForeground(Color.LIGHT_GRAY);
		
		addstr(title);
		addstr("");
	}
	
	public void addString(String a) {
		
		textArea.append("[" + sdf.format(System.currentTimeMillis()) + " NONE]: " + a + '\n');
		textArea.setSelectionStart(textArea.getText().length());
	}
	
	public void addstr(String a) {
		
		textArea.append(a + '\n');
		textArea.setSelectionStart(textArea.getText().length());
	}
	
	public void info(String a) {
		
		textArea.append("[" + sdf.format(System.currentTimeMillis()) + " INFO]: " + a + '\n');
		textArea.setSelectionStart(textArea.getText().length());
	}
	
	public void warn(String a) {
		
		textArea.append("[" + sdf.format(System.currentTimeMillis()) + " WARN]: " + a + '\n');
		textArea.setSelectionStart(textArea.getText().length());
	}
}
