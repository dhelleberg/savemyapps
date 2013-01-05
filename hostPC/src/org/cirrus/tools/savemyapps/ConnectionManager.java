package org.cirrus.tools.savemyapps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

import com.android.ddmlib.IDevice;

public class ConnectionManager {

	IDevice currentDevice = null;

	Logger logger = Logger.getLogger(ConnectionManager.class.getName());

	private ADBWrapper adbWrapper;

	Thread connectionThread = null;

	public void init() {
		this.adbWrapper = new ADBWrapper();
		this.adbWrapper.init(this);

	}

	public void addDevice(IDevice device) {
		logger.info("New device: "+device.getName());
		//currently: first come, first server
		if(this.currentDevice == null)
		{
			this.currentDevice = device;
			this.adbWrapper.createPortForward(currentDevice);
			//TODO: create socket
			this.createSocketConnection(currentDevice);
		}
	}

	private void createSocketConnection(IDevice currentDevice) {
		logger.info("Try to connect to: "+currentDevice.getName());
		if(this.connectionThread == null)
		{
			this.connectionThread = new Thread(new Runnable() {
				public void run() {
					try{
						Socket socket = new Socket("localhost", 7676);
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						
						Scanner sc = new Scanner(in);
						while(true)
						{
							logger.info(sc.nextLine());
							Thread.sleep(5000);
							out.write("ACK!\n");
							out.flush();
						}
						//out.write("Hello from the client side!\n");

					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			this.connectionThread.start();
		}
	}

	public void removeDevice(IDevice device) {
		if(currentDevice != null && device.getName().equals(currentDevice.getName()))
		{
			//TODO: disconnect socket
			currentDevice = null;
		}
	}
}
