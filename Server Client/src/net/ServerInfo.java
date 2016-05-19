package net;

public final class ServerInfo {
	String address;
	String name;
	int numClients;
	int maxClients;
	public ServerInfo(String address,String name, int numClients, int maxClients){
		this.address=address;
		this.name=name;
		this.numClients=numClients;
		this.maxClients=maxClients;
	}
	public void setServerAddress(String address){
		this.address=address;
	}
	public String getAddress(){
		return address;
	}
	public String getServerName(){
		return name;
	}
	public int getNumClients(){
		return numClients;
	}
	public int getMaxClients(){
		return maxClients;
	}
}
