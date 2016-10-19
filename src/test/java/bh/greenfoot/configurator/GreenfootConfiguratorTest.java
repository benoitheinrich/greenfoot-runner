package bh.greenfoot.configurator;

import javaslang.control.Try;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static bh.greenfoot.configurator.DirectoryContentMatchCondition.sameContentAs;
import static bh.greenfoot.configurator.TestUtils.getTestResourcesPath;
import static org.fest.assertions.api.Assertions.assertThat;

public class GreenfootConfiguratorTest {
    private Path tmp;

    @Before
    public void init() throws IOException {
        tmp = Files.createTempDirectory("GreenfootConfiguratorTest");
    }

    @After
    public void cleanup() {
        final File file = tmp.toFile();
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testConfigurator() throws Throwable {
        final Path projectDir = tmp.resolve("My Projects").resolve("Trial Project");

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
                .send(tmp.toAbsolutePath() + "/My Projects/Trial Project")
                .expect("Directory 'My Projects/' doesn't exist, do you want to create it? (Yes/no) ")
                .send("y")
                .expect("Directory 'My Projects/' created")
                .expect(() -> assertThat(projectDir.toFile())
                        .exists()
                        .isDirectory()
                )
                .expect("Please enter the name of you project: [Trial Project] ")
                .send("My Trial Project")
                .expect("Please enter an optional group name used by the gradle build: [na] ")
                .send("")
                .expect("No group name entered, no group will be setup in gradle build.")
                .expect("Please enter an optional name for the runner class: [Main] ")
                .send("")
                .expect("Using default runner class name: Main")
                //.expect("Generating project structure in {}...", projectDir)
                .expect(() -> assertThat(projectDir.toFile()).has(sameContentAs(getTestResourcesPath("configurator/scenario1"))))
                .expect("Your project has been created, enjoy!")
                .withTimeout(1000)
                .check((in, out) -> Try.run(() -> new GreenfootConfigurator().start(in, new PrintStream(out, true))));
    }
}