package com.teamcity.ui.pages.setup;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.teamcity.ui.pages.BasePage;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class UnauthorizedAgentsPage extends BasePage {

    private static final Duration LONG_WAITING = Duration.ofMinutes(3);
    private final SelenideElement authorizeAgentButton = $(".AuthorizeAgent__authorizeAgent--Xr > button");
    private final SelenideElement authorizeAgentModalWindowButton = $(".CommonForm__button--Nb");

    public UnauthorizedAgentsPage() {
        authorizeAgentButton.shouldBe(visible, LONG_WAITING);
    }

    public static UnauthorizedAgentsPage open() {
        return Selenide.open("/agents/unauthorized", UnauthorizedAgentsPage.class);
    }

    public UnauthorizedAgentsPage authorizeAgent() {
        authorizeAgentButton.click();
        authorizeAgentModalWindowButton.shouldBe(visible, BASE_WAITING).click();
        return this;
    }

}
