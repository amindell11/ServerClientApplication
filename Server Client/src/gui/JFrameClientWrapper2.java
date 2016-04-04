package gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.Client;
import net.Config;
import net.ConnectionUtil;
import net.InfoHeader;
import net.Server;

public class JFrameClientWrapper2 {

	public static void main(String s[]) {
		JFrame frame = new JFrame("Client Window");
		JPanel panel = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel panel4 = new JPanel();
		JPanel panel5 = new JPanel();
		panel.setLayout(new FlowLayout());
		panel2.setLayout(new FlowLayout());
		panel3.setLayout(new FlowLayout());
		panel4.setLayout(new FlowLayout());
		panel5.setLayout(new FlowLayout());
		JTextArea textArea = new JTextArea(50, 10);
		JTextArea textArea5 = new JTextArea(50, 10);
		panel4.add(textArea);
		textArea5.setRows(6);
		JTextField consoleIn = new JTextField(10);
		JComboBox header=new JComboBox(InfoHeader.values());
		panel5.add(textArea5);
		panel5.add(consoleIn);
		panel5.add(header);
		JLabel label = new JLabel("ASTEROIDS");
		JTextField serverName = new JTextField(10);
		JLabel label2 = new JLabel("Server Name:");
		panel3.add(label2);
		panel3.add(serverName);

		JButton button = new JButton();
		button.setText("Create a Game");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.getContentPane().removeAll();
				frame.getContentPane().add(panel3);
				frame.revalidate();
				frame.repaint();
			}
		});
		JButton button2 = new JButton();
		button2.setText("Join a Game");
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						HashMap<String, String> openServers = ConnectionUtil.getOpenServers(Config.PORT);
						for (String s : openServers.keySet()) {
							JButton button = new JButton();
							button.setText(s);
							button.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									Thread localClientThread = new Thread(new Runnable() {
										public void run() {
											Client client = new Client();
											client.openConnection(openServers.get(s), Config.PORT);
											JButton consoleSubmit=new JButton();
											consoleSubmit.setText("Submit");
											consoleSubmit.addActionListener(new ActionListener(){
												@Override
												public void actionPerformed(ActionEvent arg0) {
													try {
														ConnectionUtil.sendMessage(client.getOutput(), (InfoHeader) header.getSelectedItem(),consoleIn.getText());
														System.out.println(ConnectionUtil.recieveMessage(client.getInput(),1000).getFullMessageString());
													} catch (IOException e) {
														e.printStackTrace();
													}
												}
											});
											panel5.add(consoleSubmit);
											while (client.isConnected()) {
												client.update();
											}
											JButton leave= new JButton();
											leave.setText("Leave Server");
											leave.addActionListener(new ActionListener() {
												
												@Override
												public void actionPerformed(ActionEvent e) {
													System.out.println(true);
													client.closeConnection();
													frame.getContentPane().removeAll();
													frame.getContentPane().add(panel2);
													frame.setSize(150, 300);
													frame.revalidate();
													frame.repaint();
												}
											});
											panel5.add(leave);
										}
									});
									localClientThread.start();
									reallocatePrint(textArea5);
									frame.getContentPane().removeAll();
									frame.getContentPane().add(panel5);
									frame.setSize(600, 300);
									frame.revalidate();
									frame.repaint();
								}
							});
							panel2.add(button);
						}
						frame.getContentPane().removeAll();
						frame.getContentPane().add(panel2);
						frame.revalidate();
						frame.repaint();
					}

				}).start();

				frame.getContentPane().removeAll();
				frame.getContentPane().add(new JLabel("finding servers...", JLabel.CENTER));
				frame.revalidate();
				frame.repaint();
			}
		});
		JButton button3 = new JButton();
		button3.setText("Create");
		button3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Thread serverThread = new Thread(new Runnable() {
					public void run() {
						String name = serverName.getText();
						Server server = new Server(Config.PORT, name);
						if (server.init()) {
							while (true) {
								server.update();
							}
						}
					}
				});
				serverThread.start();
				Thread localClientThread = new Thread(new Runnable() {
					public void run() {
						Client client = new Client();
						client.openConnection("0.0.0.0", Config.PORT);
						while (client.isConnected()) {
							client.update();
						}
					}
				});
				localClientThread.start();
				reallocatePrint(textArea);
				frame.getContentPane().removeAll();
				frame.getContentPane().add(panel4);
				frame.setSize(600, 300);
				frame.revalidate();
				frame.repaint();
			}
		});
		panel3.add(button3);
		panel.add(label);
		panel.add(button);
		panel.add(button2);
		frame.getContentPane().add(panel);
		frame.setSize(150, 300);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void reallocatePrint(JTextArea textArea) {
		PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
		System.setOut(printStream);
		System.setErr(printStream);
	}
}