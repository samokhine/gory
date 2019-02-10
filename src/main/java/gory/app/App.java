package gory.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import gory.experiment.Experiment;
import gory.experiment.Experiment1;
import gory.experiment.Experiment2;
import gory.experiment.Experiment3;
import gory.experiment.Experiment4;
import gory.experiment.Experiment5;
import gory.service.OutputLogger;

public class App {
    public static void main(String[] args) throws IOException {
    	int experimentId = readExperimentId();
    	
    	Experiment experiment = null;
    	if(experimentId == 1) {
	    	experiment = new Experiment1();
    	} else if(experimentId == 2) {
	    	experiment = new Experiment2();
    	} else if(experimentId == 3) {
	    	experiment = new Experiment3();
    	} else if(experimentId == 4) {
	    	experiment = new Experiment4();
    	} else if(experimentId == 5) {
	    	experiment = new Experiment5();
    	}

    	OutputLogger logger = new OutputLogger("output.txt");

    	if(experiment != null) {
    		experiment.run(logger);
    	} else {
    		logger.writeLine("No experiments to run");
    	}
    	
    	logger.close();
    }
    
	private static int readExperimentId() {
		String experimentId = null;
		InputStream input = null;
		try {
			input = new FileInputStream("input.properties");

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			experimentId = prop.getProperty("experiment");
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
		
		return experimentId == null ? 0 : Integer.valueOf(experimentId);
	}
}
