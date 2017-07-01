package gory.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;

@Getter
public class InputParametersV1 {
	private int numberOfDigits; // m
	private int sumOfDigits; // n
	private int numberOfGraphBuildingSteps; // T
	private int numberOfGraphs; // R
	
	public InputParametersV1(String fileName) {

		InputStream input = null;
		try {
			input = new FileInputStream(fileName);

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			numberOfDigits = Integer.valueOf(prop.getProperty("m"));
			sumOfDigits = Integer.valueOf(prop.getProperty("n"));
			numberOfGraphBuildingSteps = Integer.valueOf(prop.getProperty("T"));
			numberOfGraphs = Integer.valueOf(prop.getProperty("R"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
