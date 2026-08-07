package io.bookwright.api.restfulbooker.health;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HealthApi {

  @GET("ping")
  Call<Void> ping();
}
