package org.chat.Client;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RecordClient {
	
	public static void read(String text) {
		
		try (FileWriter fileWriter = new FileWriter("File1.log", true)) {
			String dateTime = DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm:ss a")
				.format(LocalDateTime.now());
			fileWriter.append(" " + dateTime + ": ");
			fileWriter.append(text);// Записываем строку в файл, добавляя пробелы
			fileWriter.flush(); // Очищаем буфер потока
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
