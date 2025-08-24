package com.mobile.test.config;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

public class AppiumConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppiumConfig.class);
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = AppiumConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("설정 파일 로드 완료");
            }
        } catch (IOException e) {
            logger.error("설정 파일 로드 실패", e);
        }
    }

    public static AppiumDriver createDriver(String platform) {
        // 서버 연결 확인 (옵션)
        if (!isAppiumServerRunning()) {
            logger.warn("⚠️ Appium 서버 연결을 확인할 수 없지만 계속 진행합니다...");
        }

        try {
            String serverUrl = properties.getProperty("appium.server.url", "http://127.0.0.1:4723");

            // Appium 버전에 따른 URL 자동 설정
            if (!serverUrl.contains("/wd/hub")) {
                // Appium 2.x 스타일
                logger.info("Appium 2.x URL 사용: {}", serverUrl);
            } else {
                // Appium 1.x 스타일
                logger.info("Appium 1.x URL 사용: {}", serverUrl);
            }

            URL appiumServerUrl = new URL(serverUrl);

            logger.info("===== Appium 연결 정보 =====");
            logger.info("서버 URL: {}", appiumServerUrl);
            logger.info("플랫폼: {}", platform);
            logger.info("===========================");

            AppiumDriver driver = null;

            if (platform.toLowerCase().equals("android")) {
                UiAutomator2Options options = getAndroidOptions();
                logger.info("Android 드라이버 생성 시도...");

                try {
                    // 먼저 설정된 URL로 시도
                    driver = new AndroidDriver(appiumServerUrl, options);
                } catch (Exception e) {
                    logger.warn("기본 URL 실패, 대체 URL 시도...");

                    // URL에 /wd/hub가 없으면 추가해서 시도
                    if (!serverUrl.contains("/wd/hub")) {
                        URL alternativeUrl = new URL(serverUrl + "/wd/hub");
                        logger.info("대체 URL: {}", alternativeUrl);
                        driver = new AndroidDriver(alternativeUrl, options);
                    } else {
                        // /wd/hub를 제거하고 시도
                        String baseUrl = serverUrl.replace("/wd/hub", "");
                        URL alternativeUrl = new URL(baseUrl);
                        logger.info("대체 URL: {}", alternativeUrl);
                        driver = new AndroidDriver(alternativeUrl, options);
                    }
                }
            } else if (platform.toLowerCase().equals("ios")) {
                XCUITestOptions options = getIOSOptions();
                logger.info("iOS 드라이버 생성 시도...");
                driver = new IOSDriver(appiumServerUrl, options);
            } else {
                throw new IllegalArgumentException("지원하지 않는 플랫폼: " + platform);
            }

            // 기본 대기 시간 설정
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            logger.info("✅ Appium 드라이버 생성 성공! Session ID: {}", driver.getSessionId());
            return driver;

        } catch (MalformedURLException e) {
            logger.error("잘못된 Appium 서버 URL: {}", e.getMessage());
            throw new RuntimeException("잘못된 Appium 서버 URL", e);
        } catch (Exception e) {
            logger.error("❌ 드라이버 생성 실패: {}", e.getMessage());
            logger.error("===== 디버깅 정보 =====");
            logger.error("1. Appium 서버 실행 확인:");
            logger.error("   터미널에서: appium");
            logger.error("2. 서버 상태 확인:");
            logger.error("   브라우저에서: http://127.0.0.1:4723/status");
            logger.error("3. Android 디바이스 확인:");
            logger.error("   터미널에서: adb devices");
            logger.error("4. 드라이버 설치 확인:");
            logger.error("   터미널에서: appium driver list --installed");
            logger.error("======================");
            throw new RuntimeException("드라이버 생성 실패", e);
        }
    }

    /**
     * Appium 서버가 실행 중인지 확인
     */
    private static boolean isAppiumServerRunning() {
        try {
            URL url = new URL("http://127.0.0.1:4723/status");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            int responseCode = connection.getResponseCode();
            boolean isRunning = responseCode == 200;

            if (isRunning) {
                logger.info("✅ Appium 서버가 실행 중입니다 (http://127.0.0.1:4723)");
            } else {
                logger.error("❌ Appium 서버 응답 코드: {}", responseCode);
            }

            return isRunning;
        } catch (Exception e) {
            logger.error("❌ Appium 서버에 연결할 수 없습니다: {}", e.getMessage());
            return false;
        }
    }

    private static UiAutomator2Options getAndroidOptions() {
        UiAutomator2Options options = new UiAutomator2Options();

        // 플랫폼명 명시적 설정 (Appium 2.x 필수)
        options.setPlatformName("Android");

        // 디바이스 설정
        String deviceName = properties.getProperty("android.device.name", "HA1Z1NS7");
        options.setDeviceName(deviceName);
        logger.info("Android 디바이스: {}", deviceName);

        // 앱 설정 (APK 경로 또는 패키지명)
        String appPath = properties.getProperty("android.app.path");
        logger.info("#### appPath: {}", appPath);
        if (appPath != null && !appPath.isEmpty()) {
            File appFile = new File(System.getProperty("user.dir"), appPath);
            if (appFile.exists()) {
                options.setApp(appFile.getAbsolutePath());
                logger.info("APK 경로: {}", appFile.getAbsolutePath());
            } else {
                logger.warn("APK 파일을 찾을 수 없음: {}", appFile.getAbsolutePath());
            }
        }

        // 이미 설치된 앱을 사용하는 경우
        String appPackage = properties.getProperty("android.app.package");
        String appActivity = properties.getProperty("android.app.activity");
        if (appPackage != null && appActivity != null) {
            options.setAppPackage(appPackage);
            options.setAppActivity(appActivity);
            logger.info("앱 패키지: {}, 액티비티: {}", appPackage, appActivity);
        }

        // 추가 옵션
        options.setAutoGrantPermissions(true);
        options.setNoReset(false);

        // Appium 2.x 호환성을 위한 설정
        options.setAutomationName("UiAutomator2");
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        return options;
    }

    private static XCUITestOptions getIOSOptions() {
        XCUITestOptions options = new XCUITestOptions();

        // 플랫폼명 명시적 설정
        options.setPlatformName("iOS");

        // 디바이스 설정
        String deviceName = properties.getProperty("ios.device.name", "iPhone 15");
        String platformVersion = properties.getProperty("ios.platform.version", "17.0");
        options.setDeviceName(deviceName);
        options.setPlatformVersion(platformVersion);
        logger.info("iOS 디바이스: {}, 버전: {}", deviceName, platformVersion);

        // 앱 설정
        String appPath = properties.getProperty("ios.app.path");
        if (appPath != null && !appPath.isEmpty()) {
            File appFile = new File(System.getProperty("user.dir"), appPath);
            if (appFile.exists()) {
                options.setApp(appFile.getAbsolutePath());
                logger.info("App 경로: {}", appFile.getAbsolutePath());
            }
        }

        // Bundle ID로 이미 설치된 앱 실행
        String bundleId = properties.getProperty("ios.bundle.id");
        if (bundleId != null && !bundleId.isEmpty()) {
            options.setBundleId(bundleId);
            logger.info("Bundle ID: {}", bundleId);
        }

        // 추가 옵션
        options.setNoReset(false);
        options.setAutomationName("XCUITest");
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        return options;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}