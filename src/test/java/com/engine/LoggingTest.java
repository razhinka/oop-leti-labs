package com.engine;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Модульные тесты для проверки корректности конфигурации и функционирования
 * подсистемы протоколирования Apache Log4j (Лабораторная работа № 10).
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
class LoggingTest {

    private static final Logger logger = LogManager.getLogger(LoggingTest.class);

    @Test
    @DisplayName("Проверка инициализации экземпляра Logger через LogManager")
    void testLoggerInitialization() {
        assertNotNull(logger, "Логгер должен быть успешно сконфигурирован и не равен null");
        assertNotNull(logger.getName(), "Имя логгера должно быть заполнено");
    }

    @Test
    @DisplayName("Проверка активности уровня DEBUG для пакета com.engine")
    void testDebugLevelEnabled() {
        assertTrue(logger.isDebugEnabled(), "Для пакета com.engine должен быть активирован уровень DEBUG");
        assertTrue(logger.isInfoEnabled(), "Уровень INFO должен быть активен при включенном DEBUG");
        assertTrue(logger.isWarnEnabled(), "Уровень WARN должен быть активен");
        assertTrue(logger.isErrorEnabled(), "Уровень ERROR должен быть активен");
    }

    @Test
    @DisplayName("Проверка записи сообщений всех стандартных уровней без исключений")
    void testLoggingAllLevels() {
        assertDoesNotThrow(() -> {
            logger.trace("Тестовое сообщение уровня TRACE");
            logger.debug("Тестовое сообщение уровня DEBUG: параметр={}", "проверка");
            logger.info("Тестовое сообщение уровня INFO: успешный запуск теста");
            logger.warn("Тестовое предупреждение уровня WARN: внимание к параметрам");
            logger.error("Тестовая ошибка уровня ERROR: имитация исключения", new RuntimeException("Test Exception"));
        }, "Вызовы методов логирования не должны приводить к исключениям");
    }

    @Test
    @DisplayName("Проверка создания и записи в файл logs/clinic.log")
    void testLogFileCreationAndWriting() {
        String testMarker = "TEST_LOG_ENTRY_" + System.currentTimeMillis();
        logger.info("Контрольная запись в лог-файл: {}", testMarker);

        File logFile = new File("logs/clinic.log");
        assertTrue(logFile.exists(), "Файл логов 'logs/clinic.log' должен существовать на диске");
        assertTrue(logFile.length() > 0, "Файл логов не должен быть пустым");

        assertDoesNotThrow(() -> {
            List<String> lines = Files.readAllLines(logFile.toPath());
            assertFalse(lines.isEmpty(), "В файле логов должны присутствовать строки");
            boolean found = lines.stream().anyMatch(l -> l.contains(testMarker));
            assertTrue(found, "Контрольная запись маркера должна присутствовать в лог-файле");
        });
    }
}
