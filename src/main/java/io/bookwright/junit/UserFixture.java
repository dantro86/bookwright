package io.bookwright.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/** Provides either a newly registered isolated user or the configured reusable user. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ExtendWith(UserFixtureExtension.class)
public @interface UserFixture {
  UserFixtureMode value() default UserFixtureMode.NEW;
}
