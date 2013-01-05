package org.cirrus.mobi.savemyapps.shareddata;

public class Response {
	
	public static final int RESPONSE_OK = 200;
	public static final int RESPONSE_CMD_NOT_FOUND = 404;
	public static final int RESPONSE_ERROR = 500;
	
	public int responseCode;
	public String message;
}
