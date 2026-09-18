package io.bookwright.junit;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks a test parameter as an immutable scenario fixture resolved by the test runtime. */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface TestFixture {}
