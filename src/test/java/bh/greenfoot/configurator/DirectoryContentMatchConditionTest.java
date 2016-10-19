package bh.greenfoot.configurator;

import javaslang.control.Try;
import org.fest.assertions.api.FileAssert;
import org.fest.assertions.core.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static bh.greenfoot.configurator.DirectoryContentMatchCondition.sameContentAs;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.not;

/**
 *
 */
public class DirectoryContentMatchConditionTest {
    private Path tmp;
    private Path actualPath;
    private Path expectedPath;

    @Before
    public void init() throws IOException {
        tmp = Files.createTempDirectory("GreenfootConfiguratorTest");
        actualPath = tmp.resolve("actual");
        expectedPath = tmp.resolve("expected");
        Files.createDirectory(actualPath);
        Files.createDirectory(expectedPath);
    }

    @After
    public void cleanup() {
        final File file = tmp.toFile();
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testMatchFileContentFailed() throws IOException {
        write2Files("build.gradle", "Some string", "Some other string");
        write2Files("gradlew", "Some string", "Some string");
        final Try<FileAssert> result = Try.of(() -> assertThat(actualPath.toFile()).has(sameContentAs(expectedPath.toFile())));
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).hasMessageContaining("build.gradle");
        assertThat(result.failed().get()).has(notMessageContaining("buildw"));
    }

    private Condition<? super Throwable> notMessageContaining(final String str) {
        return new Condition<Throwable>() {
            @Override
            public boolean matches(final Throwable value) {
                as("Message shouldn't contain string: " + str);
                return !value.getMessage().contains(str);
            }
        };
    }

    private void write2Files(String file, String content, String otherContent) throws IOException {
        writeFile(actualPath.resolve(file), content);
        writeFile(expectedPath.resolve(file), otherContent);
    }

    private void writeFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes());
    }
}