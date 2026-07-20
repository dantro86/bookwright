package io.bookwright.annotations;

/**
 * Tag values in one place so gradle {@code -DincludeTags=...} and annotations
 * never drift apart.
 */
public final class TestTags {

    public static final String SMOKE = "smoke";
    public static final String REGRESSION = "regression";
    public static final String API = "api";
    public static final String UI = "ui";
    public static final String DB = "db";

    private TestTags() {
    }
}
