package io.bookwright.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bookwright.api.AuthApi;
import io.bookwright.api.BookingApi;
import io.bookwright.api.RetrofitFactory;
import io.bookwright.config.Configs;
import io.bookwright.config.MainConfig;
import io.bookwright.teardown.TeardownStorage;
import retrofit2.Retrofit;

public class ApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MainConfig.class).toInstance(Configs.main());
        bind(TeardownStorage.class).in(Singleton.class);
        bind(io.bookwright.steps.AuthApiSteps.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    Retrofit retrofit(MainConfig config) {
        return RetrofitFactory.create(config.apiBaseUrl());
    }

    @Provides
    @Singleton
    AuthApi authApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }

    @Provides
    @Singleton
    BookingApi bookingApi(Retrofit retrofit) {
        return retrofit.create(BookingApi.class);
    }
}
