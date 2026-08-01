package io.bookwright.localapp;

import java.time.Instant;

record UserSessionResponse(String accessToken, Instant expiresAt, UserResponse user) {}
