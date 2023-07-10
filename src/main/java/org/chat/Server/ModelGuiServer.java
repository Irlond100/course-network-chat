package org.chat.Server;

import java.util.HashMap;
import java.util.Map;

import org.chat.Connection.Connection;

public class ModelGuiServer {
	
	// модель хранит карту со всеми подключившимися клиентами ключ - имя клиента, значение - объект connection
	private Map<String, Connection> allUsersMultiChat = new HashMap<>();
	
	public Map<String, Connection> getAllUsersMultiChat() {
		return allUsersMultiChat;
	}
	
	protected void addUser(String nameUser, Connection connection) {
		allUsersMultiChat.put(nameUser, connection);
	}
	
	protected void removeUser(String nameUser) {
		allUsersMultiChat.remove(nameUser);
	}
	
}
