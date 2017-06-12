package gory.app;

import gory.service.PartitionBuilder;

public class App {
    public static void main(String[] args) throws InterruptedException {
    	PartitionBuilder builder = new PartitionBuilder(7, 3);
    	builder.build();
    }
}
