package io.bookwright.junit;

import io.bookwright.util.TestData;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides isolated reproducible test data and publishes the replay command to Allure. */
public class TestDataExtension implements BeforeEachCallback, BeforeTestExecutionCallback, ParameterResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataExtension.class);
    private static final long RUN_SEED = TestSeeds.resolveRunSeed();

    @Override
    public void beforeEach(ExtensionContext context) {
        getOrCreate(context);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        TestData data = getOrCreate(context);
        String selector = context.getRequiredTestClass().getName() + "." + context.getRequiredTestMethod().getName();
        String command = "./gradlew test -Dtest.seed=%d --tests \"%s\"".formatted(RUN_SEED, selector);

        Allure.parameter("run.seed", RUN_SEED);
        Allure.parameter("test.seed", data.testSeed());
        Allure.addAttachment("Reproduce test data", "text/plain", command);
        LOG.info("Test data seed: run={}, test={}, replay={}", RUN_SEED, data.testSeed(), command);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == TestData.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return getOrCreate(extensionContext);
    }

    public static TestData getOrCreate(ExtensionContext context) {
        ExtensionContext.Store store = NamespaceRegistry.methodStore(context);
        return store.getOrComputeIfAbsent(
                NamespaceRegistry.TEST_DATA_KEY,
                ignored -> create(context),
                TestData.class);
    }

    private static TestData create(ExtensionContext context) {
        String testId = stableTestId(context);
        return new TestData(RUN_SEED, TestSeeds.deriveTestSeed(RUN_SEED, testId), testId);
    }

    private static String stableTestId(ExtensionContext context) {
        return context.getUniqueId();
    }
}
