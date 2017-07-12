package experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;

@Getter
public class InputParametersV2 {
	private String strategyFileName;
	private int distance;
	
	public InputParametersV2(String fileName) {

		InputStream input = null;
		try {
			input = new FileInputStream(fileName);

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			strategyFileName = prop.getProperty("file");
			distance = Integer.valueOf(prop.getProperty("distance"));
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
