package io.bookwright.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * User fixture: obtains an explicit API auth session before the test and stores it in the
 * method-scoped store ({@code store.authSession()} in the test). The lean analog of a per-test user
 * fixture — restful-booker has one admin user, so the "user" boils down to an authenticated
 * session.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ExtendWith(AuthSessionExtension.class)
public @interface WithAuthSession {}
