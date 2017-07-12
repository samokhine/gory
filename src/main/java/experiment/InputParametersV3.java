package experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;

@Getter
public class InputParametersV3 {
	private int numberOfDigits; // m
	
	public InputParametersV3(String fileName) {

		InputStream input = null;
		try {
			input = new FileInputStream(fileName);

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			numberOfDigits = Integer.valueOf(prop.getProperty("m"));
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
