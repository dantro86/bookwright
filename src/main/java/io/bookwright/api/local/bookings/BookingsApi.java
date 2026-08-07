package io.bookwright.api.local.bookings;

import io.bookwright.api.model.LocalBooking;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BookingsApi {

  @POST("api/bookings")
  Call<LocalBooking> create(@Body LocalBooking booking);

  @GET("api/bookings/{id}")
  Call<LocalBooking> get(@Path("id") int id);

  @DELETE("api/bookings/{id}")
  Call<Void> delete(@Path("id") int id);
}
