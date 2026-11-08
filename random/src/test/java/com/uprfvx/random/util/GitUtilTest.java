package com.uprfvx.random.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GitUtilTest {

    @Test
    public void testGetCurrentCommitHash() {
        GitUtil.getCurrentCommitHash()
                .ifPresent(hash ->
                        assertTrue(
                                hash.matches("[0-9a-f]+"),
                                () -> "Commit hash matches pattern\nHash: " + hash));
    }

    @Test
    public void testHasUncommittedChanges() {
        assertDoesNotThrow(GitUtil::hasUncommittedChanges);
    }
}
