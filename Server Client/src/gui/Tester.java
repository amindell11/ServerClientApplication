package gui;

import java.io.IOException;
import java.io.Serializable;

import net.Client;
import net.Config;
import net.ConnectionUtil;

public class Tester implements Serializable{
	int a;
	String b;
	public Tester(){
		a=5;
		b="hi";
	}
	public String toString(){
		return "this is a tester object, "+a+" "+b;
	}
	public static void main(String[] args) throws IOException{
		Client c=new Client();
		c.openConnection("0.0.0.0", Config.PORT);
		ConnectionUtil.sendObject(c.getOut(),"testObj",new Tester());
	}
}
