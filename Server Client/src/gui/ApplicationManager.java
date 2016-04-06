package gui;

import java.awt.TextArea;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import net.Client;
import net.Config;
import net.Server;

public class ApplicationManager extends JFrame{
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	ApplicationManager frame=new ApplicationManager();
                frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                frame.enterScreen(new HomePanel(frame));
                frame.setVisible(true);
            }
        });
    }
    public void enterScreen(ComponentPanel screenPanel){
    	getContentPane().removeAll();
    	invalidate();
    	getContentPane().add(screenPanel);
    	revalidate();
    	pack();
    	repaint();
    }
	public Server createServer(final String serverName) {
		final Server server=new Server(Config.PORT, serverName);;
		Thread serverThread = new Thread(new Runnable() {
			public void run() {
				if (server.init()) {
					while (true) {
						server.update();
					}
				}
			}
		});
		serverThread.start();
		return server;
	}
	public Client createClient(final String host){
		final Client client = new Client();
		Thread localClientThread = new Thread(new Runnable() {
			public void run() {
				client.openConnection(host, Config.PORT);
				while (client.isConnected()) {
					//client.update();
				}
			}
		});
		localClientThread.start();
		return client;
	}
	public void reallocatePrint(TextArea textArea1) {
		PrintStream printStream = new PrintStream(new CustomOutputStream(textArea1));
		System.setOut(printStream);
		System.setErr(printStream);
	}
}
