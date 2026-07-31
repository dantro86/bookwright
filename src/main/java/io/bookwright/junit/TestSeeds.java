package io.bookwright.junit;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** Resolves the run seed and derives schedule-independent seeds for individual tests. */
public final class TestSeeds {

    public static final String SYSTEM_PROPERTY = "test.seed";
    public static final String ENVIRONMENT_VARIABLE = "TEST_SEED";

    private TestSeeds() {
    }

    public static long resolveRunSeed() {
        String configured = System.getProperty(SYSTEM_PROPERTY);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(ENVIRONMENT_VARIABLE);
        }
        if (configured == null || configured.isBlank()) {
            return new SecureRandom().nextLong();
        }
        return parse(configured);
    }

    static long parse(String configured) {
        try {
            return Long.parseLong(configured.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Test seed must be a signed 64-bit integer, but was: " + configured, exception);
        }
    }

    public static long deriveTestSeed(long runSeed, String testId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(ByteBuffer.allocate(Long.BYTES).putLong(runSeed).array());
            digest.update(testId.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest.digest()).getLong();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
