package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	static final boolean sameComputer = false;

	String hostName;
	boolean connected;
	int portNumber;
	Socket socket;
	PrintWriter out;
	BufferedReader in;
	long ping;

	public boolean openConnection(String address, int portNumber) {
		System.out.println("Client connecting to address " + address);
		if (connected) {
			System.err.println(
					"Error: Client already connected\nplease disconnect from the current server before connecting to a new one.");
			return false;
		}
		this.hostName = address;
		this.portNumber = portNumber;
		try {
			socket = new Socket(hostName, portNumber);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			ConnectionUtil.sendMessage(out, InfoHeader.REQUEST_CLUSTER_MEMBERSHIP, null);
			HeadedMessage membershipStatusUpdate = ConnectionUtil.recieveMessage(in);
			if (membershipStatusUpdate.getHeader().equals(InfoHeader.CLUSTER_REQUEST_ACCEPT)) {
				connected = true;
			} else {
				connected = false;
				System.err.println(
						"Error: Server denied cluster join request. Message: "+membershipStatusUpdate.getHeadlessMessage());
			}
		} catch (IOException e) {
			System.err.println(
					"Error: No host found\n no server was found at address: " + hostName + " Port: " + this.portNumber);
		}
		return connected;
	}

	public boolean isConnected() {
		return connected;
	}

	public void update() {
		try {
			sendUpdate("test");
			recieveUpdate();
		} catch (IOException e) {
			System.out.println("Server has closed down.");
			closeConnection();
			return;
		}
	}

	public void sendUpdate(Object obj) throws IOException {
		// ConnectionUtil.sendMessage(out, InfoHeader.NEW_OBJECT, new
		// Gson().toJson(obj));
	}

	public void recieveUpdate() throws IOException {
		long time = System.nanoTime();
		String update = null;
		if ((update = in.readLine()) != null) {
			System.out.println(update);
		}
		ping = System.nanoTime() - time;
		ping /= 1000000;
	}

	public void closeConnection() {
		try {
			socket.close();
			connected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public PrintWriter getOutput(){
		return out;
	}

	public BufferedReader getInput() {
		return in;
	}

}