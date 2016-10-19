import bh.greenfoot.runner.GreenfootRunner;

/**
 * A sample runner for a greenfoot project.
 */
public class Main extends GreenfootRunner {
    static {
        bootstrap(Main.class,
                Configuration.forWorld(Garden.class)
                        .projectName("${projectDescription}")
        );
    }
}