package io.bookwright.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bookwright.db.BookingDao;
import io.bookwright.db.DbPool;
import io.bookwright.db.RoomDao;
import io.bookwright.teardown.TeardownStorage;

public class DbModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TeardownStorage.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    BookingDao bookingDao() {
        return DbPool.jdbi().onDemand(BookingDao.class);
    }

    @Provides
    @Singleton
    RoomDao roomDao() {
        return DbPool.jdbi().onDemand(RoomDao.class);
    }
}
