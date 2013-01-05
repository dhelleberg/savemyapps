package org.cirrus.mobi.savemyapps.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.cirrus.mobi.savemyapps.BuildConfig;
import org.cirrus.mobi.savemyapps.shareddata.Command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class HostCommService extends Service {
	private static final String TAG = "SMA/HostCommService";

	private static final int NOT_CONNECTED = -2;
	private static final int CONNECTING = -1;
	private static final int SOCKET_OPEN = 0;
	private static final int CONNECTED = 1;


	public static final String EXTRA_COMMAND = "EXTRA_CMD";
	public static final String EXTRA_PARAMS = "EXTRA_PARAMS";
	
	public static final int COMMAND_CONNECT = 1;
	public static final int COMMAND_BACKUP_PACKAGE = 2;

	private BlockingQueue<Command> commandsToSend = new ArrayBlockingQueue<Command>(30);

	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	private Thread connectorThread = null;
	private volatile int connectionStatus = NOT_CONNECTED;

	private Gson mGson;


	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {


			//TODO: check USB status && ADB Status otherwise it doesn't make any sense to continue here

			//TODO: enqueue Command
			//DEMO comman
			int command = msg.arg2;
			switch (command) {
			case COMMAND_BACKUP_PACKAGE:
				
				String[]packages = (String[]) msg.obj;
				Command cmd = new Command();
				cmd.command = Command.COMMAND_BACKUP_PACKAGE;
				cmd.params.put(Command.PARAM_BACKUP_PACKAGES, packages);
				commandsToSend.add(cmd);
				break;
			}
			
			//check connection status
			switch (connectionStatus) {
			case NOT_CONNECTED:
				connect();
				break;				
			}

			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			//stopSelf(msg.arg1);
		}

	}

	private void connect() {
		synchronized (this) {
			if(connectionStatus == NOT_CONNECTED)			
			{
				connectionStatus = CONNECTING;
				if(connectorThread == null)
				{

					connectorThread = new Thread(new Runnable() {
						public void run() {
							try {
								if(BuildConfig.DEBUG)
									Log.v(TAG, "Opening Socket...");
								ServerSocket socket = new ServerSocket(7676);								
								Socket connectedSocket = socket.accept();

								connectionStatus = CONNECTED;

								
								if(BuildConfig.DEBUG)
									Log.v(TAG, "Socket connected!" + connectedSocket.getRemoteSocketAddress());

								//read streams
								PrintWriter out = new PrintWriter(connectedSocket.getOutputStream(), true);
								BufferedReader in = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
								Scanner inScanner = new Scanner(in);

								while(true)
								{
									Command cmd = null;
									try {
										cmd = commandsToSend.take();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try
									{
										if(cmd != null)
										{				
											String json = mGson.toJson(cmd);
											if(BuildConfig.DEBUG)
												Log.v(TAG, "gonna send: "+json);
											out.write(json+"\n");
											out.flush();
											//read response
											String response = inScanner.nextLine();
											if(BuildConfig.DEBUG)
												Log.v(TAG, "response:"+response);
										}
									}
									catch(Exception e)
									{
										//outch no longer connected?
										if(BuildConfig.DEBUG)
											Log.v(TAG, "could not send command...", e);
										//are we still connected?
										if(BuildConfig.DEBUG)
											Log.v(TAG, "Diag: connected: "+connectedSocket.isConnected()+ " closed: "+connectedSocket.isClosed() +" in: "+connectedSocket.isInputShutdown() +" out: "+connectedSocket.isOutputShutdown());
									}

								}                

								/*Scanner scanner = new Scanner(in);
								while(true)
								{
									Log.v(TAG, scanner.nextLine());
								}*/


							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					connectorThread.start();
				}
			}
			else
				return;
		}	
	}



	public HostCommService() {
	}

	@Override
	public void onCreate() {
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mGson = new GsonBuilder().create();
		
		// Get the HandlerThread's Looper and use it for our Handler 
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		//register as USB attach reciever
		
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		final int command = intent.getIntExtra(EXTRA_COMMAND, -1);
		if(command != -1)	    	 
		{	      
			msg.arg2 = command;
			switch (command) {
			case COMMAND_BACKUP_PACKAGE:
				msg.obj = intent.getStringArrayExtra(EXTRA_PARAMS);
				break;
			}
			mServiceHandler.sendMessage(msg);
		}

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
