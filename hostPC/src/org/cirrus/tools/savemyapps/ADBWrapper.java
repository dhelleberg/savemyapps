package org.cirrus.tools.savemyapps;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.sun.tools.internal.ws.wsdl.document.jaxws.Exception;

public class ADBWrapper implements IDeviceChangeListener {

	private static final Logger logger = Logger.getLogger(ADBWrapper.class.getName());

	private final static String ADB_SDK_PATH = "/platform-tools/adb";

	private File adbExec = null;

	private AndroidDebugBridge androidDeviceBridge;

	private ConnectionManager cm;

	public void init(ConnectionManager cm) {
		this.cm = cm;
		findAdb();
		initAdb();
	}

	private void initAdb() {
		AndroidDebugBridge.init(false);
		this.androidDeviceBridge = AndroidDebugBridge.createBridge(adbExec.getAbsolutePath(), false);

		AndroidDebugBridge.addDeviceChangeListener(this);

		//try a bit to get already connected devices
		int trials = 10;
		while (trials > 0) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (this.androidDeviceBridge.hasInitialDeviceList()) {
				IDevice[] devices =  this.androidDeviceBridge.getDevices();
				for (IDevice iDevice : devices) {
					cm.addDevice(iDevice);
				}
				break;
			}
			trials--;
		}

	}

	private void findAdb() {
		//check adb executable
		logger.fine("Start searching for adb...");
		String android_sdk_home = System.getenv("ANDROID_SDK");
		if(android_sdk_home != null)
		{
			File adb_candidate = checkFile(android_sdk_home + ADB_SDK_PATH);
			if(adb_candidate != null)
			{
				logger.info("Will use adb exec from: "+adb_candidate.getAbsolutePath());
				this.adbExec = adb_candidate;
				return;
			}				
		}

		String android_home = System.getenv("ANDROID_HOME");
		if(android_home != null)
		{
			File adb_candidate = checkFile(android_home + ADB_SDK_PATH);
			if(adb_candidate != null)
			{
				logger.info("Will use adb exec from: "+adb_candidate.getAbsolutePath());
				this.adbExec = adb_candidate;
				return;
			}				
		}
		//try shipped one
		File adb_candidate = checkFile("ADB_BINARIES/adb");
		if(adb_candidate != null)
		{
			logger.info("Will use adb exec from: "+adbExec.getAbsolutePath());
			this.adbExec = adb_candidate;
			return;
		}				
		throw new RuntimeException("Could not find adb exec-File. Please set ANDROID_HOME or ANDROID_SDK dir!");
	}
	private File checkFile(String path) {
		File adbExec = new File(path);
		if(adbExec.exists())
		{
			if(adbExec.canExecute())
			{				
				return adbExec;
			}
		}
		return null;
	}

	@Override
	public void deviceChanged(IDevice device, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceConnected(IDevice device) {
		logger.info("Device connected: "+device.getName());
		//notfiy connection manager that we might have a candidate
		cm.addDevice(device);
		//init portforwarding

	}

	public void createPortForward(IDevice device)
	{
		try {
			device.createForward(7676, 7676);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdbCommandRejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void deviceDisconnected(IDevice device) {
		logger.info("Device disconnected: "+device.getName());
		cm.removeDevice(device);		
	}

	public void backupPackages(String[] packages) throws IOException {
		StringBuilder packagelist = new StringBuilder();
		for (String package_: packages) {
			packagelist.append(package_).append(" ");
		}
		Process adbProcess;
		try {
			adbProcess = Runtime.getRuntime().exec(adbExec.getAbsolutePath() + " backup -f tmp.ab " + packagelist.toString());
			adbProcess.waitFor();
			Scanner sc = new Scanner(adbProcess.getErrorStream());
			if (sc.hasNext()) {
				String error = sc.nextLine();
				logger.severe("error executing adb: "+error);
				throw new IOException(error);
			}
		} catch (IOException e) {
			logger.severe("Exception executing adb...");
			e.printStackTrace();
			throw new IOException(e);
		} catch (InterruptedException e) {
			logger.severe("Exception executing adb...");
			e.printStackTrace();
			throw new IOException(e);
		}
		
	}

}
