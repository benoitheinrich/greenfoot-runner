package bh.greenfoot.runner.fixture;

import bh.greenfoot.runner.GreenfootRunner;

/**
 * A sample runner for a greenfoot project.
 */
public class MyRunner extends GreenfootRunner {
    static {
        GreenfootRunner.mainClass = MyRunner.class;
    }

    public MyRunner() {
        System.out.println("Creating my runner");
    }

    @Override
    protected Configuration getConfiguration() {
        return Configuration.prepare()
                .projectName("Catch the hedghogs")
                .worldClass(Garden.class);
    }
}
