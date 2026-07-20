package io.bookwright.junit;

import io.bookwright.steps.ApiSteps;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Implements {@link WithAuthSession}: fetches the token through the same steps
 * facade as the test and shares it via the method-scoped store.
 */
public class AuthSessionExtension implements BeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        ApiSteps api = StepsParameterResolver.injectorFor(ApiSteps.class, context).getInstance(ApiSteps.class);
        Allure.step("Fixture: authenticate API session", () ->
                NamespaceRegistry.methodStore(context).put(NamespaceRegistry.AUTH_TOKEN_KEY, api.auth().token()));
    }
}
