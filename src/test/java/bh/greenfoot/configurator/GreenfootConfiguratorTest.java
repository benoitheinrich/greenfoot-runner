package bh.greenfoot.configurator;

import javaslang.Function2;
import javaslang.collection.List;
import javaslang.concurrent.Future;
import javaslang.control.Try;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import static java.lang.Character.LINE_SEPARATOR;
import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class GreenfootConfiguratorTest {

    @Test
    public void testConfigurator() throws Throwable {
        final File tmp = File.createTempFile("GreenfootConfiguratorTest", "");

        ConversationChecker.from(new GreenfootConfigurator())
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
                .check();

        assertThat(tmp.toPath().resolve("My Projects").resolve("Trial Project").toFile())
                .exists()
                .isDirectory();
    }

    private static class ConversationChecker {
        private final GreenfootConfigurator configurator;

        private List<Function2<InputStream, OutputStreamWriter, Try<Void>>> functions = List.empty();
        private int position = 0;
        private String output = "";

        private ConversationChecker(final GreenfootConfigurator configurator) throws IOException {
            this.configurator = configurator;
        }

        public static ConversationChecker from(final GreenfootConfigurator greenfootConfigurator) throws IOException {
            return new ConversationChecker(greenfootConfigurator);
        }

        public ConversationChecker expect(final String str) {
            functions = functions.append((consoleOutput, commandWriter) -> Try.run(() -> {
                ensureOutput(str, consoleOutput);
                if (output.startsWith(str)) {
                    System.out.println(">> " + str);
                } else {
                    System.out.println("!! " + str);
                }
                assertThat(output).startsWith(str);
                // Clear output as soon as matched
                clearConsumedOutput(str);
            }));
            return this;
        }

        public ConversationChecker send(final String command) {
            functions = functions.append((consoleOutput, commandWriter) -> Try.run(() -> {
                System.out.println("<< " + command);
                commandWriter.write(command);
                commandWriter.write(LINE_SEPARATOR);
                commandWriter.flush();
            }));
            return this;
        }

        public void check() throws Throwable {
            // Command line
            final PipedOutputStream commandLine = new PipedOutputStream();
            final PipedInputStream in = new PipedInputStream(commandLine);
            final OutputStreamWriter commandWriter = new OutputStreamWriter(commandLine);

            // Console output
            final PipedOutputStream outputStream = new PipedOutputStream();
            final PrintStream out = new PrintStream(outputStream, true);
            final InputStream consoleOutput = new PipedInputStream(outputStream);

            final Future<Try<Void>> run = Future.of(() -> Try.run(() -> configurator.start(in, out)));
            Thread.sleep(100);
            final Try<Void> eval = functions.foldLeft(Try.success((Void) null), (r, f) -> r.isFailure() ? r : f.apply(consoleOutput, commandWriter));
            if (eval.isFailure()) {
                throw eval.failed().get();
            }
        }

        private void ensureOutput(final String str, final InputStream consoleOutput) throws IOException, InterruptedException {
            if (output.length() < str.length()) {
                output += new String(readAtLeast(consoleOutput, str.length()), Charset.defaultCharset());
            }
        }

        private void clearConsumedOutput(final String str) {
            if (str.length() < output.length()) {
                output = output.substring(str.length() + 1);
            } else {
                output = "";
            }
        }

        private byte[] readAtLeast(final InputStream consoleOutput, final int minLength) throws IOException, InterruptedException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            long start = System.currentTimeMillis();
            byte[] data = new byte[4096];
            int totalRead = 0;
            while (!isTimeout(start, 1000) && totalRead < minLength) {
                if (!hasDataAvailable(consoleOutput)) {
                    Thread.sleep(100);
                } else {
                    int nRead = consoleOutput.read(data, 0, data.length);
                    if (nRead != -1) {
                        totalRead += nRead;
                        buffer.write(data, 0, nRead);
                    } else {
                        break;
                    }
                }
            }

            buffer.flush();

            return buffer.toByteArray();
        }

        private boolean isTimeout(final long start, final int timeout) {
            return start + timeout < System.currentTimeMillis();
        }

        private boolean hasDataAvailable(final InputStream consoleOutput) throws IOException {
            return consoleOutput.available() > 0;
        }
    }
}