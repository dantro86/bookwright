package io.bookwright.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Declares test data setup: {@code @Preconditions({AUTH_TOKEN, BOOKING_EXISTS})}.
 * Executed by {@link PreconditionProvider} right before the test body,
 * each precondition wrapped in its own Allure step.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ExtendWith(PreconditionProvider.class)
public @interface Preconditions {

    Precondition[] value();
}
