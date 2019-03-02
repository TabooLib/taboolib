package me.skymc.taboolib.client;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

@Deprecated
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
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		// SETTINGS
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new BevelBorder(BevelBorder.RAISED));
		scrollPane.setViewportView(textArea);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		setSize(700, 500);
		setVisible(true);
		
		// CON'T EDIT
		textArea.setEditable(false);
		textArea.setFont(new Font("黑体", 0, 18));
		
		textArea.setBackground(Color.black);
		textArea.setForeground(Color.LIGHT_GRAY);
		
		addString(title);
		addString("");
	}

	public void addString(String a) {
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
