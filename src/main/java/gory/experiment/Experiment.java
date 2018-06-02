package gory.experiment;

import java.io.IOException;

import gory.service.OutputLogger;

public interface Experiment {
	public void run(OutputLogger logger) throws IOException;
}
