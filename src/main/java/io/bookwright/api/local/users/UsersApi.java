package io.bookwright.api.local.users;

import io.bookwright.api.model.UserProfile;
import io.bookwright.api.model.UserRegistration;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UsersApi {

  @POST("api/users")
  Call<UserProfile> register(@Body UserRegistration registration);

  @DELETE("api/users/me")
  Call<Void> deleteCurrentUser(@Header("Authorization") String bearerToken);
}
