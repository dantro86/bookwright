package io.bookwright.api.local.auth;

import io.bookwright.api.model.UserCredentials;
import io.bookwright.api.model.UserSession;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

  @POST("api/auth/sessions")
  Call<UserSession> login(@Body UserCredentials credentials);
}
