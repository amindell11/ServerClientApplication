package net;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
	static final int timeOut=3000;
	protected Socket socket;
	InputStream inp;
	BufferedReader brinp;
	PrintWriter out;
	Server server;
	int timeSinceCommed;

	public ClientThread(Socket clientSocket,Server server) {
		this.socket = clientSocket;
		this.server=server;
		try {
			inp = socket.getInputStream();
			brinp = new BufferedReader(new InputStreamReader(inp));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String line;
		while (!isInterrupted()&&!out.checkError()) {
			try {
				if (brinp.ready()) {
					timeSinceCommed=0;
					line = brinp.readLine();
					if (line == null) {
						return;
					}
					handleMessage(line);
				}else if(timeSinceCommed>timeOut){
					ConnectionUtil.sendMessage(out, InfoHeader.PROBE,null);
				}else{
					timeSinceCommed++;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	public void handleMessage(String message) throws IOException{
		HeadedMessage codedMsg=HeadedMessage.toHeadedMessage(message);
		switch(codedMsg.header){
		case DELETE_OBJECT:
			break;
		case NEW_OBJECT:
			break;
		case PROBE:
			System.out.println("Probe Recieved, Responding");
			ConnectionUtil.sendMessage(out, InfoHeader.PROBE_RESPONSE,null);
			break;
		case PROBE_RESPONSE:
			break;
		case UPDATE_OBJECT:
			break;
		case REQUEST_SERVER_NAME:
			System.out.println("Server Name Request Recieved, Responding");
			ConnectionUtil.sendMessage(out, InfoHeader.SERVER_NAME_RESPONSE,server.name);
			break;
		case SERVER_NAME_RESPONSE:
			break;
		case CLUSTER_REQUEST_ACCEPT:
			break;
		case CLUSTER_REQUEST_DENIED:
			break;
		case REQUEST_CLUSTER_MEMBERSHIP:
			InfoHeader header=null;
			if(server.acceptClient(socket,this))header=InfoHeader.CLUSTER_REQUEST_ACCEPT;
			else header=InfoHeader.CLUSTER_REQUEST_DENIED;
			ConnectionUtil.sendMessage(out, header,null);
			break;
		default:
			break;
		}
	}

	public String getClientAddress() {
		return socket.getInetAddress().getHostAddress();
	}
}