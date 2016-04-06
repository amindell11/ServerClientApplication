package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionUtil {
	public static String parseIpAddress(String read) {
		String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(read);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return "0.0.0.0";
		}

	}

	public static ArrayList<String> getAllIPs() {
		ArrayList<String> ip = new ArrayList<>();
		for (int x = 0; x < 40; x++) {
			for (int y = 0; y < 255; y++) {
				ip.add("10.208." + x + "." + y);
			}
		}
		return ip;
	}

	public static List<String> getAvailibleIPs() {
		final int threads = 4000;
		final int timeOut = 1040;
		final List<String> addresses = Collections.synchronizedList(new ArrayList<String>());
		ArrayList<String> allIPs = getAllIPs();
		ArrayList<ArrayList<String>> s = subdivideArray(allIPs, threads);
		List<Thread> ts = new ArrayList<>();
		for (final ArrayList<String> list : s) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					addresses.addAll(getAvailibleIPs(list, timeOut));
				}
			});
			ts.add(t);
			t.start();
		}
		try {
			for (Thread t : ts) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(addresses.size()==0)addresses.addAll(getAvailibleIPsARP());
		return addresses;
	}

	public static ArrayList<String> getAvailibleIPs(ArrayList<String> ips, int timeOut) {
		ArrayList<String> list = new ArrayList<>();
		for (String s : ips) {
			try {
				if (InetAddress.getByName(s).isReachable(timeOut)) {
					// System.out.println(s);
					list.add(s);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	private static ArrayList<ArrayList<String>> subdivideArray(List<String> list, int threads) {
		ArrayList<ArrayList<String>> listOfLists = new ArrayList<>();
		int itemsPerlist = list.size() / threads;
		if (list.size() < threads)
			itemsPerlist = 1;
		int index = 0;
		int listIndex = 0;
		ArrayList<String> listItem = new ArrayList<>();
		while (index < list.size()) {
			if (listIndex >= itemsPerlist) {
				listOfLists.add(listItem);
				listItem = new ArrayList<>();
				listIndex = 0;
			} else {
				listItem.add(list.get(index));
				listIndex++;
				index++;
			}
		}
		return listOfLists;
	}

	public static ArrayList<String> getAvailibleIPsARP() {
		ArrayList<String> addresses = new ArrayList<>();
		try {
			System.setProperty("java.net.preferIPv4Stack", "true");
			InetAddress broadcast = getBroadcastAddress();
			Runtime.getRuntime().exec("ping " + broadcast.getHostName());
			Process p = Runtime.getRuntime().exec("arp -a");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			in.readLine();
			in.readLine();
			String line;
			while ((line = in.readLine()) != null) {
				String host = parseIpAddress(line);
				addresses.add(host);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return addresses;
	}

	public static Map<String, String> getOpenServers(final int portNumber) {
		List<String> list1 = getAvailibleIPs();
		ArrayList<ArrayList<String>> s = subdivideArray(list1, 3000);
		System.out.println(s);
		final Map<String, String> openServers = Collections.synchronizedMap(new HashMap<String, String>());
		List<Thread> ts = new ArrayList<>();
		for (final ArrayList<String> list : s) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					for (String host : getOpenServers(list, portNumber)) {
						openServers.put(getServerName(host, portNumber, 500, 3000), host);
					}
				}
			});
			ts.add(t);
			t.start();
		}
		try {
			for (Thread t : ts) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return openServers;

	}

	public static List<String> getOpenServers(List<String> servers, int port) {
		ArrayList<String> list = new ArrayList<>();
		for (String s : servers) {
			if (testConnection(s, port, 500, 3000)) {
				System.out.println(true);
				list.add(s);
			}
		}
		return list;
	}

	public static boolean testConnection(String hostName, int port, int timeOut, long msgTimeOut) {
		try {
			HeadedMessage msg = singleExchangeConnection(hostName, port, timeOut, msgTimeOut,
					new HeadedMessage(InfoHeader.PROBE, null));
			boolean didProbeReturn = msg != null && msg.getHeader() == InfoHeader.PROBE_RESPONSE;
			return didProbeReturn;
		} catch (IOException e) {
			return false;
		}
	}

	public static HeadedMessage singleExchangeConnection(String hostName, int port, int timeOut, long msgTimeOut,
			HeadedMessage msg) throws IOException {
		System.out.println("Trying to connect to " + hostName);
		Socket s = new Socket();
		s.connect(new InetSocketAddress(hostName, port), timeOut);
		System.out.println("Connection Established, sending message " + hostName);
		PrintWriter out = new PrintWriter(s.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		sendMessage(out, msg);
		HeadedMessage returnedMsg = recieveMessage(in, msgTimeOut);
		s.close();
		return returnedMsg;
	}

	public static String getServerName(String hostName, int port, int timeOut, long msgTimeOut) {
		String name = null;
		try {
			System.out.println("preparing to send server name request");
			HeadedMessage msg = singleExchangeConnection(hostName, port, timeOut, msgTimeOut,
					new HeadedMessage(InfoHeader.REQUEST_SERVER_NAME, null));
			System.out.println("name request returned, validating message");
			if (msg != null && msg.getHeader() == InfoHeader.SERVER_NAME_RESPONSE) {
				name = msg.getHeadlessMessage();
			}
		} catch (IOException e) {
		}
		return name;
	}

	public static HeadedMessage recieveMessage(BufferedReader in) throws IOException {
		return recieveMessage(in, Integer.MAX_VALUE);
	}

	public static HeadedMessage recieveMessage(BufferedReader in, long timeOut) throws IOException {
		long start = System.currentTimeMillis();
		while (!in.ready()) {
			if (System.currentTimeMillis() - start > timeOut)
				return null;
		}
		String msg = in.readLine();
		System.out.println("recieved Message: " + msg);
		return HeadedMessage.toHeadedMessage(msg);
	}

	public static void sendMessage(PrintWriter out, HeadedMessage message) throws IOException {
		out.println(message.getFullMessageString());
		out.flush();
	}

	public static void sendMessage(PrintWriter out, InfoHeader msgCode, String msg) throws IOException {
		HeadedMessage message = new HeadedMessage(msgCode, msg);
		sendMessage(out, message);
	}

	public static InetAddress getBroadcastAddress() {
		InetAddress broadcast = null;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback())
					continue;
				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress temp = interfaceAddress.getBroadcast();
					if (temp != null) {
						broadcast = temp;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return broadcast;
	}

}
