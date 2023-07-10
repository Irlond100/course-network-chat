package org.chat.Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.chat.Connection.Connection;
import org.chat.Connection.Message;
import org.chat.Connection.MessageType;

public class Server {
	
	private static ModelGuiServer model; // объект класса модели
	private static ViewGuiServer gui; // объект класса представления
	private static volatile boolean isServerStart = false; // флаг отражающий состояние сервера запущен/остановлен
	public ServerSocket serverSocket;
	
	// точка входа для приложения сервера
	public static void main(String[] args) {
		Server server = new Server();
		gui = new ViewGuiServer(server);
		model = new ModelGuiServer();
		gui.initFrameServer();
		
		// цикл снизу ждет true от флага isServerStart (при старте сервера в методе startServer устанавливается в true)
		// после чего запускается бесконечный цикл принятия подключения от клиента в методе acceptServer
		// до тех пор, пока сервер не остановится, либо не возникнет исключение
		while (true) {
			if (isServerStart) {
				server.acceptServer();
				isServerStart = false;
			}
		}
	}
	
	// метод, запускающий сервер
	public void startServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
			isServerStart = true;
			gui.refreshDialogWindowServer("Server started.\n");
		} catch (Exception e) {
			gui.refreshDialogWindowServer("Failed to start server.\n");
		}
		
	}
	
	// метод останавливающий сервер
	protected void stopServer() {
		try {
			// если серверныйСокет не имеет ссылки или не запущен
			if (serverSocket != null && !serverSocket.isClosed()) {
				for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
					user.getValue().close();
				}
				serverSocket.close();
				model.getAllUsersMultiChat().clear();
				gui.refreshDialogWindowServer("Server stopped.\n");
			} else {
				gui.refreshDialogWindowServer("Server not running - nothing to stop!\n");
			}
		} catch (Exception e) {
			gui.refreshDialogWindowServer("Failed to stop server.\n");
		}
	}
	
	// метод, в котором в бесконечном цикле сервер принимает новое Socket подключение от клиента
	protected void acceptServer() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				new ServerThread(socket).start();
			} catch (Exception e) {
				gui.refreshDialogWindowServer("Server connection lost.\n");
				break;
			}
		}
	}
	
	// метод, рассылающий заданное сообщение всем клиентам из Map
	protected void sendMessageAllUsers(Message message) {
		for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
			try {
				user.getValue().send(message);
			} catch (Exception e) {
				gui.refreshDialogWindowServer("Error sending message to all users!\n");
			}
		}
	}
	
	// класс-поток, который запускается при принятии сервером нового Socket соединения с клиентом, в конструктор
	// передается объект класса Socket
	private class ServerThread extends Thread {
		
		private Socket socket;
		
		public ServerThread(Socket socket) {
			this.socket = socket;
		}
		
		// метод, который реализует запрос сервера у клиента имени и добавлении имени в Map
		private String requestAndAddingUser(Connection connection) {
			while (true) {
				try {
					// посылаем клиенту сообщение-запрос имени
					connection.send(new Message(MessageType.REQUEST_NAME_USER));
					Message responseMessage = connection.receive();
					String userName = responseMessage.getTextMessage();
					// получили ответ с именем и проверяем не занято ли это имя другим клиентом
					if (responseMessage.getTypeMessage() == MessageType.USER_NAME && userName != null &&
						!userName.isEmpty() && !model.getAllUsersMultiChat().containsKey(userName))
					{
						// добавляем имя в Map
						model.addUser(userName, connection);
						Set<String> listUsers = new HashSet<>();
						for (Map.Entry<String, Connection> users : model.getAllUsersMultiChat().entrySet()) {
							listUsers.add(users.getKey());
						}
						// отправляем клиенту множество имен всех уже подключившихся пользователей
						connection.send(new Message(MessageType.NAME_ACCEPTED, listUsers));
						// отправляем всем клиентам сообщение о новом пользователе
						sendMessageAllUsers(new Message(MessageType.USER_ADDED, userName));
						return userName;
					}
					// если такое имя уже занято отправляем сообщение клиенту, что имя используется
					else {
						connection.send(new Message(MessageType.NAME_USED));
					}
				} catch (Exception e) {
					gui.refreshDialogWindowServer("An error occurred while requesting and adding a new user\n");
				}
			}
		}
		
		// метод, реализующий обмен сообщениями между пользователями
		private void messagingBetweenUsers(Connection connection, String userName) {
			while (true) {
				try {
					Message message = connection.receive();
					// приняли сообщение от клиента, если тип сообщения TEXT_MESSAGE то пересылаем его всем
					// пользователям
					if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
						String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
						sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));
						
					}
					// если тип сообщения DISABLE_USER, то рассылаем всем пользователям, что данный пользователь
					// покинул чат,
					// удаляем его из Map, закрываем его connection
					if (message.getTypeMessage() == MessageType.DISABLE_USER) {
						sendMessageAllUsers(new Message(MessageType.REMOVED_USER, userName));
						model.removeUser(userName);
						connection.close();
						gui.refreshDialogWindowServer(
							String.format("Remote access user %s disconnected.\n", socket.getRemoteSocketAddress()));
						break;
					}
				} catch (Exception e) {
					gui.refreshDialogWindowServer(
						String.format("An error occurred while sending a message from user %s, or disconnected!\n",
							userName));
					break;
				}
			}
		}
		
		@Override
		public void run() {
			gui.refreshDialogWindowServer(
				String.format("New user connected with remote socket - %s.\n", socket.getRemoteSocketAddress()));
			try {
				// получаем connection при помощи принятого сокета от клиента и запрашиваем имя, регистрируем, запускаем
				// цикл обмена сообщениями между пользователями
				Connection connection = new Connection(socket);
				String nameUser = requestAndAddingUser(connection);
				messagingBetweenUsers(connection, nameUser);
			} catch (Exception e) {
				gui.refreshDialogWindowServer(
					String.format("An error occurred while sending a message from a user!\n"));
			}
		}
		
	}
	
}


