package org.cirrus.mobi.savemyapps.shareddata;

import java.util.HashMap;
import java.util.Map;

public class Command {
	
	public static final int COMMAND_BACKUP_PACKAGE = 1;
	public static final String PARAM_BACKUP_PACKAGES = "packages";
		
	/** Fields to serialize */
	public int command;	
	public Map<String, String[]>params = new HashMap<String, String[]>();
	

}
