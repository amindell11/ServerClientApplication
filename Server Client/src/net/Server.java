package net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;


public class Server {
	//static Gson jsonParser;
	static final boolean REQUIRE_UNIQUE_CLIENTS = false;
	HashMap<String, Thread> clients;
	int maxClients;
	int port;
	private boolean hasBeenInitialized;
	ServerSocket serverSocket = null;
	String name;
	String address;

	public Server(int port, String name) {
		this.port = port;
		this.name = name;
		hasBeenInitialized = false;
		try {
			address=InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		maxClients=20;
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
		if(!hasBeenInitialized){
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

	public boolean acceptClient(Socket socket, ClientThread clientThread) {
		InetAddress address = socket.getInetAddress();
		if (clients.containsKey(address.getHostAddress()) && REQUIRE_UNIQUE_CLIENTS) {
			closeClient(clientThread);
			System.err.println("Error: Client at address " + address
					+ " is already open. Please close any other clients and try again");
			return false;
		} else {
			clients.put(clientThread.getClientAddress(), clientThread);
			System.out.println("Welcome, " + address.getHostName());
			return true;
		}

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
	public void close(){
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HashMap<String, Thread> client=new HashMap<>();
		hasBeenInitialized=false;
		ServerSocket serverSocket = null;
	}
}