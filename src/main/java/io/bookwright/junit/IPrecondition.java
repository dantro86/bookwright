package io.bookwright.junit;

import io.bookwright.steps.ApiSteps;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A single named precondition. Implemented by the {@link Precondition} enum;
 * the interface exists so the provider and tests depend on the contract,
 * not the enum.
 */
public interface IPrecondition {

    String title();

    void execute(ApiSteps api, ExtensionContext.Store store);
}
