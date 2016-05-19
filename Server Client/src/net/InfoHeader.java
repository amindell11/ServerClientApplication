package net;

public enum InfoHeader {
	PROBE("PRBS"),
	PROBE_RESPONSE("PRBR"),
	NEW_OBJECT("NOBJ"),
	UPDATE_OBJECT("UOBJ"),
	DELETE_OBJECT("DOBJ"),
	REQUEST_SERVER_NAME("RSNS"),
	SERVER_NAME_RESPONSE("RSNR"),
	REQUEST_CLUSTER_MEMBERSHIP("RCMS"),
	CLUSTER_REQUEST_ACCEPT("RCMA"),
	CLUSTER_REQUEST_DENIED("RCMD"),
	REQUEST_SERVER_INFO("RSIS"),
	SERVER_INFO_RESPONSE("RSIR");

	public static int HEADER_LENGTH=4;
	private String headerLiteral;
	private InfoHeader(String code){
		this.headerLiteral=code;
	}
	public String  getHeaderLiteral(){
		return headerLiteral;
	}
	public static InfoHeader parseInfoCode(String header){
		for(InfoHeader iHeader:InfoHeader.values()){
			if(iHeader.getHeaderLiteral().equals(header)){
				return iHeader;
			}
		}
		return null;
	}
}
