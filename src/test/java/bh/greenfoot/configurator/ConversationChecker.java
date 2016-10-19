package bh.greenfoot.configurator;

import javaslang.CheckedFunction2;
import javaslang.Function3;
import javaslang.collection.List;
import javaslang.concurrent.Future;
import javaslang.control.Try;
import javaslang.control.Try.CheckedSupplier;
import org.fest.assertions.core.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import static java.lang.Character.LINE_SEPARATOR;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * A utility class which allow interactions with functions which read data from an {@link InputStream} and write to an
 * {@link OutputStream}.
 * <p>
 * The utility allow assertion of what's written to the output stream using {@link #expect(String)} and
 * {@link #expect(Supplier)} methods.
 * The utility allow interacting with the function by sending data using the {@link #send(String)} method.
 * <p>
 * All assertions and interactions must be setup before performing the {@link #check(CheckedFunction2)}
 */
public final class ConversationChecker {
    private static final int DEFAULT_TIMEOUT = 1000;
    private final List<Function3<String, InputStream, OutputStreamWriter, Try<String>>> functions;
    private final int timeoutMillis;

    private ConversationChecker(final int timeoutMillis, final List<Function3<String, InputStream, OutputStreamWriter, Try<String>>> functions) {
        this.timeoutMillis = timeoutMillis;
        this.functions = functions;
    }

    public static ConversationChecker create() throws IOException {
        return new ConversationChecker(DEFAULT_TIMEOUT, List.empty());
    }

    public ConversationChecker expect(final String str) {
        return new ConversationChecker(timeoutMillis, functions.append((bufferOutput, consoleOutput, commandWriter) -> Try.of(() -> {
            final String output = ensureOutput(bufferOutput, str, consoleOutput);
            if (output.startsWith(str)) {
                System.out.println(">> " + str);
            } else {
                System.out.println("!! " + str);
            }
            assertThat(output).startsWith(str);
            // Clear output as soon as matched
            return clearConsumedOutput(output, str);
        })));
    }

    public ConversationChecker expect(final Supplier<Assert<?, ?>> assertSupplier) {
        return new ConversationChecker(timeoutMillis, functions.append((bufferOutput, consoleOutput, commandWriter) -> Try.of(() -> {
            assertSupplier.get();
            return bufferOutput;
        })));
    }

    public ConversationChecker send(final String command) {
        return new ConversationChecker(timeoutMillis, functions.append((bufferOutput, consoleOutput, commandWriter) -> Try.of(() -> {
            System.out.println("<< " + command);
            commandWriter.write(command);
            commandWriter.write(LINE_SEPARATOR);
            commandWriter.flush();
            return bufferOutput;
        })));
    }

    public ConversationChecker withTimeout(final int millis) {
        return new ConversationChecker(millis, functions);
    }

    public void check(final CheckedFunction2<InputStream, OutputStream, Try<Void>> function) throws Throwable {
        // Command line
        final PipedOutputStream commandLine = new PipedOutputStream();
        final PipedInputStream in = new PipedInputStream(commandLine);
        final OutputStreamWriter commandWriter = new OutputStreamWriter(commandLine);

        // Console output
        final PipedOutputStream out = new PipedOutputStream();
        final InputStream consoleOutput = new PipedInputStream(out);

        final Future<Try<Void>> run = futureTimeout(timeoutMillis, () -> function.apply(in, out));
        // Give time for the thread to start
        Thread.sleep(100);
        final Try<String> eval = functions.foldLeft(Try.success(""), (r, f) -> r.isFailure() ? r : f.apply(r.get(), consoleOutput, commandWriter));
        if (eval.isFailure()) {
            if (run.isCompleted() && run.get().isFailure()) {
                System.out.println("!! Command execution failed:");
                run.get().failed().get().printStackTrace();
            }
            throw eval.failed().get();
        } else if (run.get().isFailure()) {
            System.out.println("!! Command execution failed:");
            throw run.get().failed().get();
        }
    }

    private Future<Try<Void>> futureTimeout(final int millis, final CheckedSupplier<Try<Void>> function) {
        final Future<Try<Void>> fTimeout = Future.of(() -> {
            Thread.sleep(millis);
            return Try.failure(new RuntimeException("timeout"));
        });

        final Future<Try<Void>> fRun = Future.of(function);
        return Future.firstCompletedOf(List.of(fTimeout, fRun));
    }

    private String ensureOutput(final String bufferOutput, final String str, final InputStream consoleOutput) throws IOException, InterruptedException {
        if (bufferOutput.length() < str.length()) {
            return bufferOutput + new String(readAtLeast(consoleOutput, str.length()), Charset.defaultCharset());
        }
        return bufferOutput;
    }

    private String clearConsumedOutput(final String output, final String str) {
        if (str.length() < output.length()) {
            return output.substring(str.length() + 1);
        } else {
            return "";
        }
    }

    private static byte[] readAtLeast(final InputStream consoleOutput, final int minLength) throws IOException, InterruptedException {
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

    private static boolean isTimeout(final long start, final int timeout) {
        return start + timeout < System.currentTimeMillis();
    }

    private static boolean hasDataAvailable(final InputStream consoleOutput) throws IOException {
        return consoleOutput.available() > 0;
    }
}
