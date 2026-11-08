package com.uprfvx.random.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GitUtil {

    private GitUtil() {}

    public static Optional<String> getCurrentCommitHash() {
        try {
            return Optional.of(executeGitCommand("rev-parse", "HEAD").trim());
        } catch (InterruptedException | IOException | ExecutionException | TimeoutException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static boolean hasUncommittedChanges() {
        try {
            return !executeGitCommand("status", "--porcelain").isBlank();
        } catch (InterruptedException | IOException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

    private static String executeGitCommand(String... arguments)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String[] command = new String[arguments.length + 1];
        command[0] = "git";
        System.arraycopy(arguments, 0, command, 1, arguments.length);
        Process process = new ProcessBuilder(command).start();
        CompletableFuture<String> outputFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return new String(
                                process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        boolean completed = process.waitFor(10, TimeUnit.SECONDS);
        String output = outputFuture.get(10, TimeUnit.SECONDS);

        if (!completed) {
            process.destroyForcibly();
            throw new IOException("Git command timed out. Command: " + String.join(" ", command));
        }

        if (process.exitValue() != 0) {
            throw new IOException(
                    "Git command failed with exit code "
                            + process.exitValue()
                            + ". Command: "
                            + String.join(" ", command));
        }

        return output;
    }
}
