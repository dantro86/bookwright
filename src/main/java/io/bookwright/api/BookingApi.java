package io.bookwright.api;

import io.bookwright.api.model.Booking;
import io.bookwright.api.model.BookingId;
import io.bookwright.api.model.CreatedBooking;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * restful-booker booking CRUD. Write operations authenticate
 * with a {@code Cookie: token=<token>} header.
 */
public interface BookingApi {

    @GET("booking")
    Call<List<BookingId>> getBookingIds();

    @GET("booking")
    Call<List<BookingId>> findBookingIds(@Query("firstname") String firstname, @Query("lastname") String lastname);

    @GET("booking/{id}")
    Call<Booking> getBooking(@Path("id") int id);

    @POST("booking")
    Call<CreatedBooking> createBooking(@Body Booking booking);

    @PUT("booking/{id}")
    Call<Booking> updateBooking(@Path("id") int id, @Body Booking booking, @Header("Cookie") String tokenCookie);

    @PATCH("booking/{id}")
    Call<Booking> partialUpdateBooking(@Path("id") int id, @Body Booking partial, @Header("Cookie") String tokenCookie);

    @DELETE("booking/{id}")
    Call<Void> deleteBooking(@Path("id") int id, @Header("Cookie") String tokenCookie);
}
