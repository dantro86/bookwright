package io.bookwright.junit;

import io.bookwright.steps.ApiSteps;
import io.qameta.allure.Allure;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * Runs {@link Preconditions} declared on the test method (or class) just before
 * the test body. Reuses the class-level Guice injector, so preconditions go
 * through the same steps (and the same LIFO teardown) as the test itself.
 */
public class PreconditionProvider implements BeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Precondition[] preconditions = findAnnotation(context)
                .map(Preconditions::value)
                .orElseThrow(() -> new IllegalStateException(
                        "PreconditionProvider is registered but no @Preconditions annotation found on "
                                + context.getDisplayName()));

        ApiSteps api = StepsParameterResolver.injectorFor(ApiSteps.class, context).getInstance(ApiSteps.class);
        ExtensionContext.Store store = NamespaceRegistry.methodStore(context);

        for (IPrecondition precondition : preconditions) {
            Allure.step("Precondition: " + precondition.title(), () -> precondition.execute(api, store));
        }
    }

    private Optional<Preconditions> findAnnotation(ExtensionContext context) {
        Optional<Preconditions> onMethod = context.getTestMethod()
                .flatMap(m -> AnnotationSupport.findAnnotation((AnnotatedElement) m, Preconditions.class));
        if (onMethod.isPresent()) {
            return onMethod;
        }
        return context.getTestClass()
                .flatMap(c -> AnnotationSupport.findAnnotation(c, Preconditions.class));
    }
}
