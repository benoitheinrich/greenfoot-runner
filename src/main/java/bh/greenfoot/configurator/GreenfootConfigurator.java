package bh.greenfoot.configurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Used to initialise new projects based on {@code GreenfootRunner} from command line.
 * <p>
 * This generates the base directory structure based on gradle tool chain.
 * <p>
 * Here is an example conversation:
 * Enter the location to store your new project: /some/path
 * This directory already exists, please select a new directory name: /some/other/path
 * Directory '/some/other' doesn't exist, do you want to create it? (Yes/no) n
 * Please select a new location to store your new project: /some/other/path
 * Directory '/some/other' doesn't exist, do you want to create it? (Yes/no) Y
 * You don't have write access to create this directory, please select another location: My Projects/Trial Project
 * Directory 'My Projects/' doesn't exist, do you want to create it? (Yes/no) Y
 */
public class GreenfootConfigurator {
    public static void main(final String[] args) {
        try {
            new GreenfootConfigurator().start(System.in, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void start(final InputStream in, final PrintStream out) throws IOException {
        out.println("Welcome to the Greenfoot Runner configurator!");
        out.println("");
        out.println("Please follow instructions below.");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        final File projectPath = readProjectPath(reader, out);
        //final String projectName;
        //final String projectDescription;
        //final String groupName;
        //final String runnerName;
    }

    private File readProjectPath(final BufferedReader scanner, final PrintStream out) throws IOException {
        out.print("Enter the location to store your new project: ");
        final File projectPath = acceptProjectPath(scanner, out);
        final File parentPath = acceptParentPath(scanner, out);
/*
        while (!projectPath.getParentFile().toFile().exists()) {

        }
*/
        return projectPath;
    }

    private File acceptProjectPath(final BufferedReader scanner, final PrintStream out) throws IOException {
        File projectPath = new File(scanner.readLine());
        while (projectPath.exists()) {
            out.print("This directory already exists, please select a new directory name: ");
            projectPath = new File(scanner.readLine());
        }
        return projectPath;
    }

    private File acceptParentPath(final BufferedReader scanner, final PrintStream out) throws IOException {
        File parentPath = new File(scanner.readLine());
        while (parentPath.exists()) {
            out.print("This directory already exists, please select a new directory name: ");
            parentPath = new File(scanner.readLine());
        }
        return parentPath;
    }
}
