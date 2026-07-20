package io.bookwright.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.microsoft.playwright.Page;
import io.bookwright.config.Configs;
import io.bookwright.config.MainConfig;
import io.bookwright.ui.BrowserManager;

public class UiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MainConfig.class).toInstance(Configs.main());
    }

    @Provides
    Page page() {
        return BrowserManager.page();
    }
}
