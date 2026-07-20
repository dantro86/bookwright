package io.bookwright.annotations;

import io.qameta.allure.Owner;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test ownership shorthand: shows up as the Owner label in Allure.
 * Add one such annotation per team member.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Owner("danil")
public @interface OwnerDanil {
}
