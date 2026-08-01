package io.bookwright.api;

import io.bookwright.api.model.UserCredentials;
import io.bookwright.api.model.UserProfile;
import io.bookwright.api.model.UserRegistration;
import io.bookwright.api.model.UserSession;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LocalUserApi {

  @POST("api/users")
  Call<UserProfile> register(@Body UserRegistration registration);

  @POST("api/auth/sessions")
  Call<UserSession> login(@Body UserCredentials credentials);

  @DELETE("api/users/me")
  Call<Void> deleteCurrentUser(@Header("Authorization") String bearerToken);
}
