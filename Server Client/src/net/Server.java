package net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Server {
	// static Gson jsonParser;
	static final boolean REQUIRE_UNIQUE_CLIENTS = false;
	HashMap<String, ClientThread> clients;
	int maxClients;
	int port;
	private boolean hasBeenInitialized;
	ServerSocket serverSocket = null;
	String name;
	String address;

	public Server(int port, String name) {
		this(port,name,Config.MAX_CLIENTS);
	}
	public Server(int port,String name,int maxClients){
		this.port = port;
		this.name = name;
		this.maxClients = maxClients;
		hasBeenInitialized = false;
		try {
			address = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public Server(int port) {
		this(port, "default");
	}

	public boolean init() {
		clients = new HashMap<>();
		try {
			serverSocket = new ServerSocket(port);
			hasBeenInitialized = true;
		} catch (IOException e) {
			if (e.getClass().equals(java.net.BindException.class)) {
				System.out.println(
						"Address already in use; this computer already has a server running on it. Please close the server and try again.");
			} else {
				e.printStackTrace();
			}
		}
		System.out.println("Server is now running at address " + address + " port " + port);
		return hasBeenInitialized;
	}

	public void update() {
		if (!hasBeenInitialized) {
			System.err.println("please start the server before attempting to update it.");
			return;
		}
		Socket socket = null;
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			System.out.println("I/O error: " + e);
		}
		addClient(socket);
	}

	public HeadedMessage acceptClient(Socket socket, ClientThread clientThread) {
		InfoHeader head;
		String message;
		InetAddress address = socket.getInetAddress();
		if (clients.containsKey(address.getHostAddress()) && REQUIRE_UNIQUE_CLIENTS) {
			closeClient(clientThread);
			head=InfoHeader.CLUSTER_REQUEST_DENIED;
			message = "Client at address " + address
					+ " is already open. Please close any other clients and try again";
			System.err.println("Error: "+message);
		} else if (clients.size() >= maxClients) {
			head=InfoHeader.CLUSTER_REQUEST_DENIED;
			message="Server full";
			System.err.println("Error: "+message);

		} else {
			head=InfoHeader.CLUSTER_REQUEST_ACCEPT;
			message=null;
			clients.put(clientThread.getClientAddress(), clientThread);
			System.out.println("Welcome, " + address.getHostName());
		}
		return new HeadedMessage(head,message);

	}

	public void addClient(Socket socket) {
		ClientThread clientThread = new ClientThread(socket, this);
		System.out.println("Connection established with a client");
		clientThread.start();
	}

	public void closeClient(ClientThread clientThread) {
		clients.remove(clientThread.getClientAddress());
		clientThread.interrupt();
	}

	public static void main(String[] arguments) {
		Server server = new Server(Config.PORT, "test");
		server.init();
		while (true) {
			server.update();
		}
	}
	public void announceToClients(HeadedMessage msg){
		for(ClientThread s:clients.values()){
			try {
				ConnectionUtil.sendMessage(s.out, msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public String getAddress(){
		return address;
	}
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HashMap<String, Thread> client = new HashMap<>();
		hasBeenInitialized = false;
		ServerSocket serverSocket = null;
	}
}