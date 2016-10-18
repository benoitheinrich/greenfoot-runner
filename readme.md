# Description

This project is an attempt to provide a wrapper around the excellent [greenfoot](www.greenfoot.org) platform.

The goal is to allow developers to use external IDEs like IntelliJ to develop games.

This project doesn't provide as many features as the original greenfoot IDE, but is a nice transition from greenfoot to 
a professional IDE.

# Disclamer

This project isn't supported by any greenfoot team's member and is a personal attempt to allow my son to use IntelliJ 
IDEA to make his learning easier.

# Building runner

Before you can start using this wrapper, you first need to generate the library to be used in your project.
For this you should first checkout this repo and run the command:

```
$ gradle dist
```

This will create the file `build/libs/greenfoot-runner-dist-3.0.4-SNAPSHOT.jar` which contains all the classes required 
to start greenfoot.

# Using the runner

Once the runner is built, you can create your own project and add the `libs/greenfoot-runner-dist-3.0.4-SNAPSHOT.jar` 
to the list of dependencies of your project.

I recommend gradle as the build tool chain as it makes it simpler enough to update build dependencies, without getting
too much in the way of a young developer.

Once you add the runner lib to your project, you can create your own runner class dedicated to that one project.
You can check an example in the tests of this library by checking the
[`MyRunner`](https://github.com/benoitheinrich/greenfoot-runner/blob/master/src/test/java/bh/greenfoot/runner/fixture/MyRunner.java) 
class.
```
// 1. Import the file
import bh.greenfoot.runner.GreenfootRunner;

public class MyRunner extends GreenfootRunner {
    static {
        // 2. Bootstrap the runner class.
        bootstrap(MyRunner.class,
                // 3. Prepare the configuration for the runner based on the world class
                Configuration.forWorld(Garden.class)
                        // Set the project name as you wish
                        .projectName("Catch the hedghogs")
        );
    }
}
```

First thing to do is to import the file so it can be extended by your own runner.

Then you need to bootstrap the runner by indicating the `GreenfootRunner.mainClass`.
This will automatically create the instance of the runner when the application is starting.

Lastly you need to indicate the world class used by your project.
You can also configure additional information like project name, but this is optional.
To see what can be configured you can check the `GreenfootRunner.Configuration` class for the details.

# Example project

An example project can be found there: [greenfoot-runner-example](https://github.com/benoitheinrich/greenfoot-runner-example)
