package io.bookwright.junit;

import io.bookwright.api.model.UserCredentials;
import io.bookwright.api.model.UserProfile;
import io.bookwright.api.model.UserRegistration;
import io.bookwright.api.model.UserSession;
import io.bookwright.config.Configs;
import io.bookwright.steps.ApiSteps;
import io.bookwright.util.TestData;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Resolves user identity before parameter resolution creates an authenticated browser context. */
public class UserFixtureExtension implements BeforeEachCallback {

  static final String TEST_USER_KEY = "testUser";

  @Override
  public void beforeEach(ExtensionContext context) {
    UserFixture fixture =
        context
            .getElement()
            .flatMap(
                element -> java.util.Optional.ofNullable(element.getAnnotation(UserFixture.class)))
            .orElseGet(() -> context.getRequiredTestClass().getAnnotation(UserFixture.class));
    if (fixture == null) {
      throw new IllegalStateException("@UserFixture configuration is not available");
    }

    ApiSteps api =
        StepsParameterResolver.injectorFor(ApiSteps.class, context).getInstance(ApiSteps.class);
    TestUser user =
        Allure.step(
            "Fixture: provide %s user".formatted(fixture.value().name().toLowerCase()),
            () -> createUser(fixture.value(), api, context));
    NamespaceRegistry.methodStore(context).put(TEST_USER_KEY, user);
  }

  public static java.util.Optional<TestUser> find(ExtensionContext context) {
    return java.util.Optional.ofNullable(
        NamespaceRegistry.methodStore(context).get(TEST_USER_KEY, TestUser.class));
  }

  public static TestUser require(ExtensionContext context) {
    return find(context)
        .orElseThrow(
            () -> new IllegalStateException("TestUser requires @UserFixture on the test or class"));
  }

  private TestUser createUser(UserFixtureMode mode, ApiSteps api, ExtensionContext context) {
    if (mode == UserFixtureMode.EXISTING) {
      UserCredentials credentials =
          new UserCredentials(
              Configs.main().localExistingUserEmail(), Configs.main().localExistingUserPassword());
      UserSession session = api.local().auth().login(credentials);
      return new TestUser(mode, credentials, session.user(), session);
    }

    TestData data = TestDataExtension.getOrCreate(context);
    UserRegistration registration = data.user();
    UserCredentials credentials =
        new UserCredentials(registration.email(), registration.password());
    UserProfile profile = api.local().users().register(registration);
    UserSession session = api.local().auth().login(credentials);
    api.local().users().registerCleanup(session);
    return new TestUser(mode, credentials, profile, session);
  }
}
