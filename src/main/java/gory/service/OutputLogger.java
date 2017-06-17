package gory.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class OutputLogger {
	private PrintWriter writer;
	
	public OutputLogger(String fileName) throws IOException {
		writer = new PrintWriter(new FileWriter(fileName));
	}
	
	public void writeLine(String line) {
		writer.println(line);
		System.out.println(line);
	}
	
	public void close() {
		writer.close();
	}
}
