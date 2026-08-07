package io.bookwright.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.bookwright.api.RetrofitFactory;
import io.bookwright.api.local.users.UsersApi;
import io.bookwright.api.restfulbooker.health.HealthApi;
import io.bookwright.config.Configs;
import io.bookwright.config.MainConfig;
import io.bookwright.teardown.TeardownStorage;
import retrofit2.Retrofit;

public class ApiModule extends AbstractModule {

  private final TeardownStorage teardownStorage;

  public ApiModule(TeardownStorage teardownStorage) {
    this.teardownStorage = teardownStorage;
  }

  @Override
  protected void configure() {
    bind(MainConfig.class).toInstance(Configs.main());
    bind(TeardownStorage.class).toInstance(teardownStorage);
    bind(io.bookwright.steps.restfulbooker.auth.AuthSteps.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  @Named("restfulBooker")
  Retrofit restfulBookerRetrofit(MainConfig config) {
    return RetrofitFactory.create(config.apiBaseUrl());
  }

  @Provides
  @Singleton
  @Named("local")
  Retrofit localRetrofit(MainConfig config) {
    return RetrofitFactory.create(config.localBookingBaseUrl());
  }

  @Provides
  @Singleton
  io.bookwright.api.restfulbooker.auth.AuthApi restfulBookerAuthApi(
      @Named("restfulBooker") Retrofit retrofit) {
    return retrofit.create(io.bookwright.api.restfulbooker.auth.AuthApi.class);
  }

  @Provides
  @Singleton
  HealthApi healthApi(@Named("restfulBooker") Retrofit retrofit) {
    return retrofit.create(HealthApi.class);
  }

  @Provides
  @Singleton
  io.bookwright.api.restfulbooker.bookings.BookingsApi restfulBookerBookingsApi(
      @Named("restfulBooker") Retrofit retrofit) {
    return retrofit.create(io.bookwright.api.restfulbooker.bookings.BookingsApi.class);
  }

  @Provides
  @Singleton
  io.bookwright.api.local.auth.AuthApi localAuthApi(@Named("local") Retrofit retrofit) {
    return retrofit.create(io.bookwright.api.local.auth.AuthApi.class);
  }

  @Provides
  @Singleton
  UsersApi usersApi(@Named("local") Retrofit retrofit) {
    return retrofit.create(UsersApi.class);
  }

  @Provides
  @Singleton
  io.bookwright.api.local.bookings.BookingsApi localBookingsApi(@Named("local") Retrofit retrofit) {
    return retrofit.create(io.bookwright.api.local.bookings.BookingsApi.class);
  }
}
