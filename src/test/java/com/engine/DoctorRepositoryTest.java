package com.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для проверки репозиториев {@link XmlDoctorRepository} и {@link CsvDoctorRepository}.
 * Проверяет полный цикл сериализации и десериализации данных с использованием временных файлов JUnit 5.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class DoctorRepositoryTest {

    @TempDir
    Path tempDir;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private List<Doctor> sampleDoctors;

    /**
     * Создает тестовый набор данных перед каждым тестом.
     *
     * @throws Exception при ошибке парсинга времени
     */
    @BeforeEach
    public void setUp() throws Exception {
        sampleDoctors = new ArrayList<>();
        sampleDoctors.add(new Doctor(
                "Быков Андрей Евгеньевич",
                "Терапевт",
                "125",
                timeFormat.parse("08:00"),
                timeFormat.parse("14:00")
        ));
        sampleDoctors.add(new Doctor(
                "Купитман Иван Натанович",
                "Уролог",
                "39",
                timeFormat.parse("10:00"),
                timeFormat.parse("18:00")
        ));
    }

    /**
     * Проверяет сохранение и последующую загрузку списка врачей через {@link XmlDoctorRepository}.
     *
     * @throws Exception при ошибке работы с XML
     */
    @Test
    @DisplayName("Цикл сохранения и загрузки через XmlDoctorRepository")
    public void testXmlDoctorRepositorySaveAndLoad() throws Exception {
        File xmlFile = tempDir.resolve("doctors_test.xml").toFile();
        XmlDoctorRepository repo = new XmlDoctorRepository();

        repo.save(sampleDoctors, xmlFile);
        assertTrue(xmlFile.exists(), "XML-файл должен быть создан на диске");
        assertTrue(xmlFile.length() > 0, "Размер XML-файла должен быть больше 0");

        List<Doctor> loaded = repo.load(xmlFile);

        assertEquals(sampleDoctors.size(), loaded.size(), "Количество загруженных записей должно совпадать");
        for (int i = 0; i < sampleDoctors.size(); i++) {
            Doctor expected = sampleDoctors.get(i);
            Doctor actual = loaded.get(i);
            assertEquals(expected.fullName(), actual.fullName());
            assertEquals(expected.specialty(), actual.specialty());
            assertEquals(expected.room(), actual.room());
            assertEquals(timeFormat.format(expected.startTime()), timeFormat.format(actual.startTime()));
            assertEquals(timeFormat.format(expected.endTime()), timeFormat.format(actual.endTime()));
        }
    }

    /**
     * Проверяет сохранение и последующую загрузку списка врачей через {@link CsvDoctorRepository}.
     *
     * @throws Exception при ошибке работы с CSV
     */
    @Test
    @DisplayName("Цикл сохранения и загрузки через CsvDoctorRepository")
    public void testCsvDoctorRepositorySaveAndLoad() throws Exception {
        File csvFile = tempDir.resolve("doctors_test.csv").toFile();
        CsvDoctorRepository repo = new CsvDoctorRepository();

        repo.save(sampleDoctors, csvFile);
        assertTrue(csvFile.exists(), "CSV-файл должен быть создан на диске");
        assertTrue(csvFile.length() > 0, "Размер CSV-файла должен быть больше 0");

        List<Doctor> loaded = repo.load(csvFile);

        assertEquals(sampleDoctors.size(), loaded.size(), "Количество загруженных записей должно совпадать");
        for (int i = 0; i < sampleDoctors.size(); i++) {
            Doctor expected = sampleDoctors.get(i);
            Doctor actual = loaded.get(i);
            assertEquals(expected.fullName(), actual.fullName());
            assertEquals(expected.specialty(), actual.specialty());
            assertEquals(expected.room(), actual.room());
            assertEquals(timeFormat.format(expected.startTime()), timeFormat.format(actual.startTime()));
            assertEquals(timeFormat.format(expected.endTime()), timeFormat.format(actual.endTime()));
        }
    }

    /**
     * Проверяет корректное возвращение пустого списка при попытке чтения несуществующего файла.
     *
     * @throws Exception при ошибке чтения
     */
    @Test
    @DisplayName("Загрузка из несуществующего файла возвращает пустой список")
    public void testLoadFromNonExistentFile() throws Exception {
        File nonExistentXml = tempDir.resolve("absent.xml").toFile();
        File nonExistentCsv = tempDir.resolve("absent.csv").toFile();

        assertTrue(new XmlDoctorRepository().load(nonExistentXml).isEmpty(),
                "Чтение отсутствующего XML должно возвращать пустой список");
        assertTrue(new CsvDoctorRepository().load(nonExistentCsv).isEmpty(),
                "Чтение отсутствующего CSV должно возвращать пустой список");
    }
}
