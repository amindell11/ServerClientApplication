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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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
	public static HashMap<String,String> getOpenServers(int portNumber) {
		HashMap<String,String> openServers = new HashMap<>();
		System.setProperty("java.net.preferIPv4Stack", "true");
		InetAddress broadcast = getBroadcastAddress();
		try {
			Runtime.getRuntime().exec("ping " + broadcast.getHostName());
			Process p = Runtime.getRuntime().exec("arp -a");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			in.readLine();
			in.readLine();
			String line;
			while ((line = in.readLine()) != null) {
				String host = parseIpAddress(line);
				if (testConnection(host, portNumber, 40,100)) {
					openServers.put(host,getServerName(host, portNumber, 40,100));
				}
			}
		} catch (IOException e) {
		}
		return openServers;

	}
	public static boolean testConnection(String hostName, int port, int timeOut, long msgTimeOut) {
		try {
			HeadedMessage msg=singleExchangeConnection(hostName,port,timeOut,msgTimeOut,new HeadedMessage(InfoHeader.PROBE,null));
			System.out.println("Probe returned "+msg.getFullMessageString()+", validating message");
			boolean didProbeReturn=msg != null&&msg.getHeader()==InfoHeader.PROBE_RESPONSE;
			return didProbeReturn;
		} catch (IOException e) {
			return false;
		}
	}
	public static HeadedMessage singleExchangeConnection(String hostName,int port,int timeOut,long msgTimeOut,HeadedMessage msg) throws IOException{
		System.out.println("Trying to connect to "+hostName);
		Socket s = new Socket();
		s.connect(new InetSocketAddress(hostName, port), timeOut);
		System.out.println("Connection Established, sending message "+hostName);
		PrintWriter out = new PrintWriter(s.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		sendMessage(out, msg);
		HeadedMessage returnedMsg = recieveMessage(in,msgTimeOut);
		s.close();
		return returnedMsg;
	}
	public static String getServerName(String hostName, int port, int timeOut, long msgTimeOut){
		String name=null;
		try {
			System.out.println("preparing to send server name request");
			HeadedMessage msg=singleExchangeConnection(hostName,port,timeOut,msgTimeOut,new HeadedMessage(InfoHeader.REQUEST_SERVER_NAME,null));
			System.out.println("name request returned, validating message");
			if(msg != null&&msg.getHeader()==InfoHeader.SERVER_NAME_RESPONSE){
				name=msg.getHeadlessMessage();
			}
		} catch (IOException e) {
		}
		return name;
	}
	public static HeadedMessage recieveMessage(BufferedReader in) throws IOException {
		return recieveMessage(in,Integer.MAX_VALUE);
	}

	public static HeadedMessage recieveMessage(BufferedReader in, long timeOut) throws IOException {
		long start = System.currentTimeMillis();
		while (!in.ready()) {
			if (System.currentTimeMillis() - start > timeOut)
				return null;
		}
		String msg = in.readLine();
		System.out.println("recieved Message: "+msg);
		return HeadedMessage.toHeadedMessage(msg);
	}
	public static void sendMessage(PrintWriter out,HeadedMessage message) throws IOException {
		out.println(message.getFullMessageString());
		out.flush();
	}
	public static void sendMessage(PrintWriter out, InfoHeader msgCode, String msg) throws IOException {
		HeadedMessage message = new HeadedMessage(msgCode, msg);
		sendMessage(out,message);
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
					InetAddress temp=interfaceAddress.getBroadcast();
					if(temp!=null){
						broadcast=temp;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return broadcast;
	}

}
