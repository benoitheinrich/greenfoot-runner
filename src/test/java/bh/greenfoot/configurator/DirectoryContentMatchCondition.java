package bh.greenfoot.configurator;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.collection.Stream;
import javaslang.control.Try;
import org.fest.assertions.api.FileAssert;
import org.fest.assertions.core.Condition;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class DirectoryContentMatchCondition {
    /**
     * A condition which verify if all the files contained in {@code expectedResourcePath} matches files with same path
     * in the tested file.
     */
    public static Condition<? super File> sameContentAs(final File expectedResourcePath) {
        final Try<Stream<Path>> expectedFiles = Try.of(() -> Files.walk(expectedResourcePath.toPath()).collect(Stream.collector()));
        if (expectedFiles.isFailure()) {
            return fail(expectedFiles.failed().get().getMessage());
        }
        return new Condition<File>() {
            @Override
            public boolean matches(final File actualResourceFile) {
                final Path actualResourcePath = actualResourceFile.toPath();
                final Try<Stream<Path>> actualFiles = Try.of(() -> Files.walk(actualResourcePath).collect(Stream.collector()));
                if (actualFiles.isFailure()) {
                    as("Can't read actual files: " + actualFiles.failed().get().getMessage());
                    return false;
                }

                final Stream<Path> expectedFilesStream = expectedFiles.get();
                final Stream<Path> actualFilesStream = actualFiles.get();

                final Map<String, Path> expectedNames = expectedFilesStream
                        .toMap(p -> Tuple.of(expectedResourcePath.toPath().relativize(p).toString(), p))
                        .filter(s -> !s._1.isEmpty());
                final Map<String, Path> actualNames = actualFilesStream
                        .toMap(p -> Tuple.of(actualResourcePath.relativize(p).toString(), p))
                        .filter(s -> !s._1.isEmpty());

                // Stop if some files are missing
                if (expectedNames.size() != actualNames.size()) {
                    final List<String> missing = expectedNames.keySet().removeAll(actualNames.keySet()).toList().sorted();
                    final List<String> extra = actualNames.keySet().removeAll(expectedNames.keySet()).toList().sorted();
                    as(format("Expected %s files generated but contains %s files (missing: %s, extra: %s)", expectedNames.size(), actualNames.size(), missing, extra));
                    return false;
                }


                final Seq<Tuple2<String, Try<FileAssert>>> failedMatches = expectedNames.map(e -> Tuple.of(e._1, e._2, actualNames.get(e._1).get()))
                        .map(e -> Tuple.of(e._1, Try.of(() -> assertThat(e._3.toFile()).hasContentEqualTo(e._2.toFile()))))
                        .filter(e -> e._2.isFailure());

                if (!failedMatches.isEmpty()) {
                    as("Following file content failed: " + failedMatches.map(Tuple2::_1).sorted().toJavaList());
                    failedMatches.forEach(e -> e._2.failed().get().printStackTrace());
                    return false;
                }
                return true;
            }
        };
    }

    private static Condition<File> fail(final String format) {
        return new Condition<File>(format) {
            @Override
            public boolean matches(final File value) {
                return false;
            }
        };
    }
}
