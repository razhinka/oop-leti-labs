package com.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки многопоточного сервиса {@link MultithreadingService} (Лабораторные работы № 8 и № 9).
 * Проверяет корректность синхронизированного выполнения трёх потоков.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class MultithreadingServiceTest {

    @TempDir
    Path tempDir;

    /**
     * Проверяет создание исходного XML-файла по умолчанию, если файл не существует.
     *
     * @throws Exception при ошибке создания файла
     */
    @Test
    @DisplayName("Создание исходного XML-файла по умолчанию при его отсутствии")
    public void testPrepareDefaultSourceXmlIfAbsent() throws Exception {
        File sourceXml = tempDir.resolve("doctors_init.xml").toFile();
        MultithreadingService service = new MultithreadingService();

        assertFalse(sourceXml.exists());
        service.prepareDefaultSourceXmlIfAbsent(sourceXml);
        assertTrue(sourceXml.exists());

        List<Doctor> loaded = new XmlDoctorRepository().load(sourceXml);
        assertEquals(3, loaded.size(), "Файл по умолчанию должен содержать 3 врачей");
    }

    /**
     * Проверяет сквозное синхронизированное выполнение трех потоков в синхронном режиме.
     */
    @Test
    @DisplayName("Синхронное выполнение трёх потоков (чтение, модификация, генерация отчета)")
    public void testRunPipelineSyncExecution() {
        File sourceXml = tempDir.resolve("doctors_src.xml").toFile();
        File editedXml = tempDir.resolve("doctors_edit.xml").toFile();
        File reportHtml = tempDir.resolve("doctors_rep.html").toFile();
        String templatePath = "doctors_report.jrxml";

        MultithreadingService service = new MultithreadingService();
        List<Doctor> sharedList = new ArrayList<>();

        AtomicBoolean loadedCalled = new AtomicBoolean(false);
        AtomicBoolean editedCalled = new AtomicBoolean(false);
        AtomicBoolean completedCalled = new AtomicBoolean(false);

        service.runPipelineSync(
                sourceXml,
                editedXml,
                reportHtml,
                templatePath,
                sharedList,
                new MultithreadingService.MultithreadCallback() {
                    @Override
                    public void onLoaded(List<Doctor> loadedDoctors) {
                        loadedCalled.set(true);
                    }

                    @Override
                    public void onEdited(List<Doctor> editedDoctors) {
                        editedCalled.set(true);
                    }

                    @Override
                    public void onCompleted(File reportFile) {
                        completedCalled.set(true);
                    }

                    @Override
                    public void onError(int threadNumber, String stage, Throwable error) {
                        fail("Ошибка в потоке " + threadNumber + " на этапе " + stage + ": " + error.getMessage());
                    }
                }
        );

        assertTrue(loadedCalled.get(), "Коллбэк onLoaded должен быть вызван");
        assertTrue(editedCalled.get(), "Коллбэк onEdited должен быть вызван");
        assertTrue(completedCalled.get(), "Коллбэк onCompleted должен быть вызван");

        assertTrue(editedXml.exists(), "Отредактированный XML-файл должен быть создан на диске");
        assertTrue(reportHtml.exists(), "HTML-отчет должен быть создан на диске");
        assertTrue(reportHtml.length() > 0, "Размер HTML-отчета должен быть больше 0");
    }
}
