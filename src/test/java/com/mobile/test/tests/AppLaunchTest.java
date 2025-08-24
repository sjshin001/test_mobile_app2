package com.mobile.test.tests;

import com.mobile.test.base.BaseTest;
import com.mobile.test.config.AppiumConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppLaunchTest extends BaseTest {

    @Test
    @DisplayName("앱 실행 테스트")
    public void testAppLaunch() {
        logger.info("앱 실행 확인 중...");

        // 드라이버가 정상적으로 생성되었는지 확인
        assertNotNull(driver, "드라이버가 null입니다");

        // 세션이 생성되었는지 확인
        assertNotNull(driver.getSessionId(), "세션 ID가 null입니다");
        logger.info("현재 세션 ID: {}", driver.getSessionId());

        // 플랫폼 정보 확인
        String currentPlatform = driver.getCapabilities()
                .getPlatformName().toString();
        logger.info("현재 플랫폼: {}", currentPlatform);

        // 앱이 실행되었는지 간단히 확인 (3초 대기)
        pause(3);

        // Android인 경우 추가 확인
        if (platform.equalsIgnoreCase("android")) {
            if (driver instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
                try {
                    String currentPackage = androidDriver.getCurrentPackage();
                    logger.info("현재 실행 중인 패키지: {}", currentPackage);
                    assertNotNull(currentPackage, "패키지명이 null입니다");
                } catch (Exception e) {
                    logger.warn("패키지 정보를 가져올 수 없습니다: {}", e.getMessage());
                }
            }
        }

        logger.info("✅ 앱이 성공적으로 실행되었습니다!");
    }

//    @Test
    @DisplayName("앱 재시작 테스트")
    public void testAppRestart() {
        logger.info("앱 재시작 테스트 시작...");

        // 플랫폼별 앱 재시작 처리
        if (platform.equalsIgnoreCase("android")) {
            testAndroidAppRestart();
        } else if (platform.equalsIgnoreCase("ios")) {
            testIOSAppRestart();
        } else {
            logger.warn("알 수 없는 플랫폼: {}", platform);
        }

        logger.info("✅ 앱 재시작 테스트 성공!");
    }

    private void testAndroidAppRestart() {
        if (driver instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
            // 첫 번째 세션 ID 저장
            String firstSessionId = androidDriver.getSessionId().toString();
            logger.info("첫 번째 세션 ID: {}", firstSessionId);

            String appPackage = getAppIdentifier();

            // 앱 종료
            boolean terminated = androidDriver.terminateApp(appPackage);
            logger.info("앱 종료 완료: {}", terminated);
            pause(2);

            // 앱 재시작
            androidDriver.activateApp(appPackage);
            logger.info("앱 재시작 완료");
            pause(2);

            // 세션이 유지되는지 확인
            String currentSessionId = androidDriver.getSessionId().toString();
            assertEquals(firstSessionId, currentSessionId,
                    "세션 ID가 변경되었습니다");
        }
    }

    private void testIOSAppRestart() {
        if (driver instanceof io.appium.java_client.ios.IOSDriver iosDriver) {
            // 첫 번째 세션 ID 저장
            String firstSessionId = iosDriver.getSessionId().toString();
            logger.info("첫 번째 세션 ID: {}", firstSessionId);

            String bundleId = getAppIdentifier();

            // 앱 종료
            boolean terminated = iosDriver.terminateApp(bundleId);
            logger.info("앱 종료 완료: {}", terminated);
            pause(2);

            // 앱 재시작
            iosDriver.activateApp(bundleId);
            logger.info("앱 재시작 완료");
            pause(2);

            // 세션이 유지되는지 확인
            String currentSessionId = iosDriver.getSessionId().toString();
            assertEquals(firstSessionId, currentSessionId,
                    "세션 ID가 변경되었습니다");
        }
    }

    /**
     * 플랫폼별 앱 식별자 반환
     */
    private String getAppIdentifier() {
        if (platform.equalsIgnoreCase("android")) {
            String packageName = AppiumConfig.getProperty("android.app.package");
            if (packageName != null && !packageName.isEmpty()) {
                return packageName;
            }
            // 기본값 (예: Calculator 앱)
            return "com.android.calculator2";
        } else {
            String bundleId = AppiumConfig.getProperty("ios.bundle.id");
            if (bundleId != null && !bundleId.isEmpty()) {
                return bundleId;
            }
            // 기본값 (예: Settings 앱)
            return "com.apple.Preferences";
        }
    }
}