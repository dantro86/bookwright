package io.bookwright.junit;

import io.bookwright.api.model.UserCredentials;
import io.bookwright.api.model.UserProfile;
import io.bookwright.api.model.UserSession;

public record TestUser(
    UserFixtureMode mode, UserCredentials credentials, UserProfile profile, UserSession session) {}
