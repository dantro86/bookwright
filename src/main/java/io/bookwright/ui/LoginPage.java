package io.bookwright.ui;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.bookwright.config.MainConfig;

public class LoginPage {

    private final Page page;
    private final MainConfig config;

    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;

    @Inject
    public LoginPage(Page page, MainConfig config) {
        this.page = page;
        this.config = config;
        this.usernameInput = page.locator("[data-test=username]");
        this.passwordInput = page.locator("[data-test=password]");
        this.loginButton = page.locator("[data-test=login-button]");
        this.errorMessage = page.locator("[data-test=error]");
    }

    public void open() {
        page.navigate(config.uiBaseUrl());
    }

    public void login(String user, String password) {
        usernameInput.fill(user);
        passwordInput.fill(password);
        loginButton.click();
    }

    public Locator errorMessage() {
        return errorMessage;
    }
}
