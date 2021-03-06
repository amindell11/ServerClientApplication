package net;

public class HeadedMessage {
	InfoHeader header;
	String msg;
	public HeadedMessage(InfoHeader code, String msg){
		this.header=code;
		this.msg=msg;
	}
	public InfoHeader getHeader(){
		return header;
	}
	public String getHeadlessMessage(){
		return msg;
	}
	public String getFullMessageString(){
		return header.getHeaderLiteral()+msg;
	}
	public static HeadedMessage toHeadedMessage(String codedMsg){
		if(codedMsg.length()<InfoHeader.HEADER_LENGTH){
			System.err.println("Error in message\nMessage: "+codedMsg+"-not long enough to contain an infoCode");
			return null;
		}
		InfoHeader code=InfoHeader.parseInfoCode(codedMsg.substring(0,InfoHeader.HEADER_LENGTH));
		if(code==null){
			return null;
		}
		String msg=codedMsg.substring(InfoHeader.HEADER_LENGTH);
		return new HeadedMessage(code,msg);
	}
}
