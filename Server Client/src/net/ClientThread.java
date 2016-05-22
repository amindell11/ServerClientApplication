package net;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

public class ClientThread extends Thread {
	static final int timeOut=10000000;
	protected Socket socket;
	InputStream inp;
	BufferedReader brinp;
	PrintWriter out;
	Server server;
	int timeSinceCommed;
	ObjectInputStream ois;

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
		System.out.println("Established connection with client "+socket.getPort());
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
					//System.out.println("Client "+socket.getPort()+" timed out, probing");
					ConnectionUtil.sendMessage(out, InfoHeader.PROBE,null);
					timeSinceCommed=0;
				}else{
					timeSinceCommed++;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		System.out.println("Client"+socket.getPort()+" closed. exiting thread");
	}
	public void handleMessage(String message) throws IOException{
		System.out.println("Server recieved message, "+message+" from client "+socket.getPort());
		HeadedMessage codedMsg=HeadedMessage.toHeadedMessage(message);
		switch(codedMsg.header){
		case DELETE_OBJECT:
			break;
		case NEW_OBJECT:
			System.out.println("recieved object with tag "+codedMsg.getHeadlessMessage());
			try {
				ois=new ObjectInputStream(inp);
				System.out.println(ois.readObject());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
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
			ConnectionUtil.sendMessage(out, server.acceptClient(socket, this));
			break;
		case REQUEST_SERVER_INFO:
			System.out.println("Server Info Request Recieved, Responding");
			ServerInfo info=new ServerInfo(server.address,server.name,server.clients.size(),server.maxClients);
			ConnectionUtil.sendMessage(out, InfoHeader.SERVER_INFO_RESPONSE,new Gson().toJson(info));
			break;
		case SERVER_INFO_RESPONSE:
			break;
		default:
			break;

		}
	}

	public String getClientAddress() {
		return socket.getInetAddress().getHostAddress();
	}
}