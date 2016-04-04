package gui;

import javax.swing.JFrame;

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
	public void createServer(final String serverName) {
		Thread serverThread = new Thread(new Runnable() {
			public void run() {
				Server server = new Server(Config.PORT, serverName);
				if (server.init()) {
					while (true) {
						server.update();
					}
				}
			}
		});
		serverThread.start();
	}
	public void createClient(final String host){
		Thread localClientThread = new Thread(new Runnable() {
			public void run() {
				Client client = new Client();
				client.openConnection(host, Config.PORT);
				while (client.isConnected()) {
					client.update();
				}
			}
		});
		localClientThread.start();
	}
	
}
