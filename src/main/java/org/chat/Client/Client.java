package org.chat.Client;

import java.io.IOException;
import java.net.Socket;

import org.chat.Connection.Connection;
import org.chat.Connection.Message;
import org.chat.Connection.MessageType;

public class Client {
	
	private static ModelGuiClient model;
	private static ViewGuiClient gui;
	
	private Connection connection;
	private volatile boolean isConnect = false; // флаг отображающий состояние подключения клиента серверу
	
	// точка входа в клиентское приложение
	public static void main(String[] args) {
		Client client = new Client();
		model = new ModelGuiClient();
		gui = new ViewGuiClient(client);
		gui.initFrameClient();
		while (true) {
			if (client.isConnect()) {
				client.nameUserRegistration();
				client.receiveMessageFromServer();
				client.setConnect(false);
			}
		}
	}
	
	public boolean isConnect() {
		return isConnect;
	}
	
	public void setConnect(boolean connect) {
		isConnect = connect;
	}
	
	// метод подключения клиента серверу
	protected void connectToServer() {
		// если клиент не подключен к серверу, то..
		if (!isConnect) {
			while (true) {
				try {
					// создаем сокет и объект connection
					Socket socket = new Socket(Settings.getIP(), Settings.getPort());
					connection = new Connection(socket);
					isConnect = true;
					gui.addMessage("Service message: You have connected to the server.\n");
					break;
				} catch (Exception e) {
					gui.errorDialogWindow(
						"An error has occurred! You may have entered the wrong server address or port. try again");
					break;
				}
			}
		} else {
			gui.errorDialogWindow("You are already connected!");
		}
	}
	
	// метод, реализующий регистрацию имени пользователя со стороны клиентского приложения
	protected void nameUserRegistration() {
		while (true) {
			try {
				Message message = connection.receive();
				// приняли от сервера сообщение, если это запрос имени, то вызываем окна ввода имени, отправляем на
				// сервер имя
				if (message.getTypeMessage() == MessageType.REQUEST_NAME_USER) {
					String nameUser = gui.getNameUser();
					connection.send(new Message(MessageType.USER_NAME, nameUser));
				}
				// если сообщение - имя уже используется, выводим соответствующее имя с ошибкой, повторяем ввод имени
				if (message.getTypeMessage() == MessageType.NAME_USED) {
					gui.errorDialogWindow("This name is already in use, please enter another one");
					String nameUser = gui.getNameUser();
					connection.send(new Message(MessageType.USER_NAME, nameUser));
				}
				// если имя принято, получаем множество всех подключившихся пользователей, выходим из цикла
				if (message.getTypeMessage() == MessageType.NAME_ACCEPTED) {
					gui.addMessage("This name is already in use, please enter another one!\n");
					model.setUsers(message.getListUsers());
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				gui.errorDialogWindow("An error occurred while registering the name. Try to reconnect");
				try {
					connection.close();
					isConnect = false;
					break;
				} catch (IOException ex) {
					gui.errorDialogWindow("Error closing connection");
				}
			}
			
		}
	}
	
	// метод отправки сообщения предназначенного для других пользователей на сервер
	protected void sendMessageOnServer(String text) {
		try {
			connection.send(new Message(MessageType.TEXT_MESSAGE, text));
		} catch (Exception e) {
			gui.errorDialogWindow("Error sending message");
		}
	}
	
	// метод принимающий с сервера сообщение от других клиентов
	protected void receiveMessageFromServer() {
		while (isConnect) {
			try {
				Message message = connection.receive();
				// если тип TEXT_MESSAGE, то добавляем текст сообщения в окно переписки
				if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
					gui.addMessage(message.getTextMessage());
				}
				// если сообщение с типом USER_ADDED добавляем сообщение в окно переписки о новом пользователе
				if (message.getTypeMessage() == MessageType.USER_ADDED) {
					model.addUser(message.getTextMessage());
					gui.refreshListUsers(model.getUsers());
					gui.addMessage(
						String.format("Service message: user %s has joined the chat.\n", message.getTextMessage()));
				}
				
			} catch (Exception e) {
				gui.errorDialogWindow("Error while receiving message from server.");
				setConnect(false);
				gui.refreshListUsers(model.getUsers());
				break;
			}
		}
	}
	
	// метод реализующий отключение нашего клиента от чата
	protected void disableClient() {
		try {
			if (isConnect) {
				connection.send(new Message(MessageType.DISABLE_USER));
				model.getUsers().clear();
				isConnect = false;
				gui.refreshListUsers(model.getUsers());
			} else {
				gui.errorDialogWindow("You are already disabled.");
			}
		} catch (Exception e) {
			gui.errorDialogWindow("Service message: An error occurred while disconnecting.");
		}
	}
	
}
