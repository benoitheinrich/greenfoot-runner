package bh.greenfoot.runner.fixture;

// 1. Import the file
import bh.greenfoot.runner.GreenfootRunner;

/**
 * A sample runner for a greenfoot project.
 */
public class MyRunner extends GreenfootRunner {
    static {
        // 2. Bootstrap the runner class.
        GreenfootRunner.mainClass = MyRunner.class;
    }

    @Override
    protected Configuration getConfiguration() {
        // 3. Prepare the configuration for the runner based on the world class
        return Configuration.forWorld(Garden.class)
                // Set the project name as you wish
                .projectName("Catch the hedghogs");
    }
}
