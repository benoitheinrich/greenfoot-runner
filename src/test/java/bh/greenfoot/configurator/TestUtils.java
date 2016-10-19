package bh.greenfoot.configurator;

import javaslang.control.Option;
import org.junit.Assert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {
    /**
     * @return the src/test/resources directory
     */
    public static Option<Path> getTestResourcesDir() {
        return Option.of(TestUtils.class.getProtectionDomain().getCodeSource())
                .map(src -> Paths.get(src.getLocation().getPath()).getParent().getParent().resolve("resources").resolve("test"));
    }


    public static File getTestResourcesPath(final String s) {
        if (getTestResourcesDir().isEmpty()) {
            Assert.fail("Test Resources path not accessible");
        }
        return getTestResourcesDir().get().resolve(s).toFile();
    }
}
