package org.chat.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Settings {
	
	private static final String FILE_PATH = "settings.txt";
	
	public static String getIP() {
		String ip = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
			reader.readLine();
			ip = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ip;
	}
	
	public static int getPort() {
		int port = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
			String line = reader.readLine();
			if (line != null) {
				port = Integer.parseInt(line.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return port;
	}
	
}
