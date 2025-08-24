package com.mobile.test.tests;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionTest.class);

    @Test
    public void testAppiumServerConnection() {
        String serverUrl = "http://127.0.0.1:4723/status";

        try {
            logger.info("Appium 서버 연결 테스트 시작...");
            logger.info("URL: {}", serverUrl);

            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            logger.info("응답 코드: {}", responseCode);

            if (responseCode == 200) {
                logger.info("✅ Appium 서버가 정상적으로 실행 중입니다!");
            } else {
                logger.error("❌ Appium 서버 응답 오류: {}", responseCode);
            }

            assertEquals(200, responseCode, "Appium 서버가 응답하지 않습니다");

        } catch (Exception e) {
            logger.error("❌ Appium 서버에 연결할 수 없습니다!", e);
            logger.error("확인사항:");
            logger.error("1. 터미널에서 'appium' 명령으로 서버를 실행하세요");
            logger.error("2. http://127.0.0.1:4723/status 를 브라우저에서 확인하세요");
            fail("Appium 서버 연결 실패: " + e.getMessage());
        }
    }
}