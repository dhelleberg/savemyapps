package org.cirrus.tools.savemyapps;
/**
 *	 This file is part of SaveMyApps
 *
 *   SaveMyApps is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SaveMyApps is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SaveMyApps.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.LogManager;

public class SaveMyAppsDaemon {

	private ConnectionManager connectionManager;
	private Image imageNotConnected;
	private Image imageConnected;
	private TrayIcon trayIcon;
	
	public static void main(String[] args) {
		SaveMyAppsDaemon saveMyAppsDeamon = new SaveMyAppsDaemon();
		saveMyAppsDeamon.init();
	}

	private void init() {
		initLogging();
		initTray();
		this.connectionManager = new ConnectionManager();
		this.connectionManager.init(this);
	}

	public void setConnectionState(int state)
	{
		switch (state) {
		case ConnectionManager.STATE_CONNECTED:
			trayIcon.setImage(imageConnected);
			break;
		case ConnectionManager.STATE_NOT_CONNECTED:
			trayIcon.setImage(imageNotConnected);
			break;
		}
	}

	private void initLogging() {
		System.setProperty( "java.util.logging.config.file", "logging.properties" );

		try { 
			LogManager.getLogManager().readConfiguration(); 
		}
		catch ( Exception e ) { 
			System.err.println("could not init logging, missing loggoing.properties file?");
			e.printStackTrace(); 
		}

	}


	private void initTray()
	{
		
		if (SystemTray.isSupported()) {

			SystemTray tray = SystemTray.getSystemTray();
			imageConnected = Toolkit.getDefaultToolkit().getImage("android-tray.gif");
			imageNotConnected = Toolkit.getDefaultToolkit().getImage("android-tray-nc.gif");

			/*MouseListener mouseListener = new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					System.out.println("Tray Icon - Mouse clicked!");                 
				}

				public void mouseEntered(MouseEvent e) {
					System.out.println("Tray Icon - Mouse entered!");                 
				}

				public void mouseExited(MouseEvent e) {
					System.out.println("Tray Icon - Mouse exited!");                 
				}

				public void mousePressed(MouseEvent e) {
					System.out.println("Tray Icon - Mouse pressed!");                 
				}

				public void mouseReleased(MouseEvent e) {
					System.out.println("Tray Icon - Mouse released!");                 
				}
			};*/

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			};

			ActionListener reconnectListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					connectionManager.reconnect();
				}
			};

			PopupMenu popup = new PopupMenu();
			MenuItem exitItem = new MenuItem("Exit");
			MenuItem reconnectItem = new MenuItem("Reconnect");
			exitItem.addActionListener(exitListener);
			reconnectItem.addActionListener(reconnectListener);
			popup.add(reconnectItem);
			popup.add(exitItem);

			trayIcon = new TrayIcon(imageNotConnected, "Save My Apps", popup);
			

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					trayIcon.displayMessage("Action Event", 
							"An Action Event Has Been Performed!",
							TrayIcon.MessageType.INFO);
				}
			};

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(actionListener);
			//trayIcon.addMouseListener(mouseListener);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
			}

		} else {

			//  System Tray is not supported

		}
	}

}