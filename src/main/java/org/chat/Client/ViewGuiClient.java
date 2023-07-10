package org.chat.Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

import javax.swing.*;

public class ViewGuiClient {
	
	private final Client client;
	private JFrame frame = new JFrame("Chat");
	private JTextArea messages = new JTextArea(30, 20);
	private JTextArea users = new JTextArea(30, 15);
	private JPanel panel = new JPanel();
	private JTextField textField = new JTextField(40);
	private JButton buttonConnect = new JButton("connect");
	
	public ViewGuiClient(Client client) {
		this.client = client;
	}
	
	// метод, инициализирующий графический интерфейс клиентского приложения
	protected void initFrameClient() {
		messages.setEditable(false);
		users.setEditable(false);
		frame.add(new JScrollPane(messages), BorderLayout.CENTER);
		frame.add(new JScrollPane(users), BorderLayout.EAST);
		panel.add(textField);
		panel.add(buttonConnect);
		frame.add(panel, BorderLayout.SOUTH);
		frame.pack();
		frame.setLocationRelativeTo(null); // при запуске отображает окно по центру экрана
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// класс обработки события при закрытии окна приложения Сервера
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (client.isConnect()) {
					client.disableClient();
				}
				System.exit(0);
			}
		});
		frame.setVisible(true);
		buttonConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.connectToServer();
			}
		});
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.sendMessageOnServer(textField.getText());
				textField.setText("");
			}
		});
	}
	
	protected void addMessage(String text) {
		messages.append(text);
		RecordClient.read(text);
		String[] s = text.split(": ");
		if (s.length > 1) {
			System.out.println(s[1]);
			if (s[1].equals("/exit\n")) {
				client.disableClient();
			}
		}
	}
	
	// метод обновляющий список имен подключившихся пользователей
	protected void refreshListUsers(Set<String> listUsers) {
		users.setText("");
		if (client.isConnect()) {
			StringBuilder text = new StringBuilder("List of users:\n");
			for (String user : listUsers) {
				text.append(user + "\n");
			}
			users.append(text.toString());
		}
	}
	
	// вызывает окна для ввода имени пользователя
	protected String getNameUser() {
		return JOptionPane.showInputDialog(
			frame, "Enter username:",
			"Entering a username",
			JOptionPane.QUESTION_MESSAGE
		);
	}
	
	// вызывает окно ошибки с заданным текстом
	protected void errorDialogWindow(String text) {
		JOptionPane.showMessageDialog(
			frame, text,
			"Error", JOptionPane.ERROR_MESSAGE
		);
	}
	
}
