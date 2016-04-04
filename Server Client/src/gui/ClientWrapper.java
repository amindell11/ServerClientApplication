package gui;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.Client;
import net.Config;
import net.ConnectionUtil;
public class ClientWrapper {
	public static void main(String[] arguments) throws UnknownHostException {
		String hostName=null;//TODO error handling
		Map<String,String> servers=ConnectionUtil.getOpenServers(Config.PORT);
		if(servers.size()==0){
			hostName=manualServerInput();
		}else if(servers.size()>1){
			hostName=serverSelection(servers);
		}else{
			hostName=servers.values().iterator().next();
		}
		Client client = new Client();
		if(client.openConnection(hostName, Config.PORT)){
			System.out.println("Connection Successful");
		}
		while (client.isConnected()) {
			client.update();
		}
	}
	public static String manualServerInput(){
		Scanner inp = new Scanner(System.in);
		System.out.println("unable to find any open servers. please manually input host ip: \n");
		String hostName = inp.nextLine().trim();
		inp.close();
		return hostName;

	}
	public static String serverSelection(Map<String, String> servers){
		Scanner inp = new Scanner(System.in);
		System.out.println("please select from the following open servers: \n"+servers.keySet());
		String hostName = inp.nextLine().trim();
		inp.close();
		return servers.get(hostName);
	}
}
