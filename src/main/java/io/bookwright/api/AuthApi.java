package io.bookwright.api;

import io.bookwright.api.model.AuthRequest;
import io.bookwright.api.model.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("auth")
    Call<AuthResponse> createToken(@Body AuthRequest request);

    @GET("ping")
    Call<Void> ping();
}
