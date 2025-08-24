package com.mobile.test.tests;

import com.mobile.test.base.BaseTest;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

public class CoupangTest extends BaseTest {

    @Test
    public void testCoupangSearch() {
        // 검색 버튼 찾기
        WebElement searchBtn = driver.findElement(
                AppiumBy.xpath("//android.widget.TextView[@text='검색']")
        );
        searchBtn.click();

        // 검색어 입력
        WebElement searchInput = driver.findElement(
                AppiumBy.className("android.widget.EditText")
        );
        searchInput.sendKeys("노트북   ");

        pause(3);
    }
}