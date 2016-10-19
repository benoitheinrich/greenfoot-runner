package bh.greenfoot.configurator;

import javaslang.control.Try;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class GreenfootConfiguratorTest {

    @Test
    public void testConfigurator() throws Throwable {
        final File tmp = File.createTempFile("GreenfootConfiguratorTest", "");

        ConversationChecker.create()
                .expect("Welcome to the Greenfoot Runner configurator!")
                .expect("")
                .expect("Please follow instructions below.")
                .expect("Enter the location to store your new project: ")
                .send("/tmp")
                .expect("This directory already exists, please select a new directory name: ")
                .send("/root/myproject")
                .expect("Directory '/root/myproject' doesn't exist, do you want to create it? (Yes/no): ")
                .send("n")
                .expect("Please select a new location to store your new project: ")
                .send("/root/myproject")
                .expect("Directory '/root/myproject' doesn't exist, do you want to create it? (Yes/no): ")
                .send("y")
                .expect("You don't have write access to create this directory, please select another location: ")
                .send(tmp.getAbsolutePath() + "/My Projects/Trial Project")
                .expect("Directory 'My Projects/' doesn't exist, do you want to create it? (Yes/no) ")
                .send("y")
                .expect("Directory 'My Projects/' created")
                .expect(() -> assertThat(tmp.toPath().resolve("My Projects").resolve("Trial Project").toFile())
                        .exists()
                        .isDirectory())
                .expect("Please enter the name of you project: [Trial Project] ")
                .send("My Trial Project")
                .expect("Please enter an optional group name used by the gradle build: [na] ")
                .send("")
                .expect("Please enter an optiona name for the runner class: [Main] ")
                .send("")
                .expect("Your project has been created, enjoy!")
                .withTimeout(1000)
                .check((in, out) -> Try.run(() -> new GreenfootConfigurator().start(in, new PrintStream(out, true))));
    }

}