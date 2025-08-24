package com.mobile.test.base;

import com.mobile.test.config.AppiumConfig;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected AppiumDriver driver;
    protected String platform;

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        logger.info("========================================");
        logger.info("테스트 시작: {}", testInfo.getDisplayName());
        logger.info("========================================");

        // 시스템 속성 또는 기본값에서 플랫폼 가져오기
        platform = System.getProperty("platform",
                AppiumConfig.getProperty("default.platform"));

        if (platform == null || platform.isEmpty()) {
            platform = "android"; // 기본값
        }

        logger.info("테스트 플랫폼: {}", platform);

        // 드라이버 생성
        driver = AppiumConfig.createDriver(platform);

        logger.info("드라이버 초기화 완료");
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        logger.info("테스트 종료: {}", testInfo.getDisplayName());

        if (driver != null) {
            try {
                driver.quit();
                logger.info("드라이버 종료 완료");
            } catch (Exception e) {
                logger.error("드라이버 종료 중 오류 발생", e);
            }
        }

        logger.info("========================================\n");
    }

    /**
     * 테스트 중 일시 정지 (디버깅용)
     */
    protected void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}