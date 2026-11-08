package com.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для проверки класса {@link DoctorValidator} (Лабораторная работа № 9).
 * Проверяет корректность валидации обязательных полей, формата времени и логики рабочих интервалов.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class DoctorValidatorTest {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Проверяет успешную валидацию и создание объекта Doctor при корректных входных параметрах.
     *
     * @throws Exception при непредвиденных ошибках
     */
    @Test
    @DisplayName("Успешное создание объекта Doctor при валидных входных данных")
    public void testValidDoctorCreation() throws Exception {
        Doctor doc = DoctorValidator.validate(
                "Иванов Иван Иванович",
                "Терапевт",
                "101",
                "08:00",
                "14:00"
        );

        assertNotNull(doc, "Созданный объект не должен быть null");
        assertEquals("Иванов Иван Иванович", doc.fullName());
        assertEquals("Терапевт", doc.specialty());
        assertEquals("101", doc.room());
        assertEquals("08:00", timeFormat.format(doc.startTime()));
        assertEquals("14:00", timeFormat.format(doc.endTime()));
    }

    /**
     * Проверяет генерацию исключения при пустом или null ФИО врача.
     *
     * @param invalidName невалидное значение ФИО
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Ошибка валидации при пустом или некорректном ФИО")
    public void testEmptyFullNameThrowsException(String invalidName) {
        InvalidDataException ex = assertThrows(InvalidDataException.class, () ->
                DoctorValidator.validate(invalidName, "Терапевт", "101", "08:00", "14:00")
        );
        assertTrue(ex.getMessage().contains("ФИО"), "Сообщение должно указывать на ошибку в ФИО");
    }

    /**
     * Проверяет генерацию исключения при пустой или null специальности врача.
     *
     * @param invalidSpecialty невалидная специальность
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Ошибка валидации при пустой специальности")
    public void testEmptySpecialtyThrowsException(String invalidSpecialty) {
        InvalidDataException ex = assertThrows(InvalidDataException.class, () ->
                DoctorValidator.validate("Иванов Иван", invalidSpecialty, "101", "08:00", "14:00")
        );
        assertTrue(ex.getMessage().contains("Специализация"), "Сообщение должно указывать на ошибку в специальности");
    }

    /**
     * Проверяет генерацию исключения при пустом или null номере кабинета.
     *
     * @param invalidRoom невалидный номер кабинета
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Ошибка валидации при пустом номере кабинета")
    public void testEmptyRoomThrowsException(String invalidRoom) {
        InvalidDataException ex = assertThrows(InvalidDataException.class, () ->
                DoctorValidator.validate("Иванов Иван", "Терапевт", invalidRoom, "08:00", "14:00")
        );
        assertTrue(ex.getMessage().contains("кабинета"), "Сообщение должно указывать на ошибку в номере кабинета");
    }

    /**
     * Проверяет генерацию исключения при неверном формате строки времени.
     *
     * @param invalidTime невалидная строка времени
     */
    @ParameterizedTest
    @ValueSource(strings = {"25:00", "08:65", "abc", "12-00", "8:0", ""})
    @DisplayName("Ошибка валидации при неверном формате времени")
    public void testInvalidTimeFormatThrowsException(String invalidTime) {
        assertThrows(InvalidDataException.class, () ->
                DoctorValidator.validate("Иванов Иван", "Терапевт", "101", invalidTime, "14:00")
        );
        assertThrows(InvalidDataException.class, () ->
                DoctorValidator.validate("Иванов Иван", "Терапевт", "101", "08:00", invalidTime)
        );
    }

    /**
     * Проверяет генерацию исключения, если время начала смены совпадает с временем окончания.
     */
    @Test
    @DisplayName("Ошибка валидации: время начала равно времени окончания смены")
    public void testStartTimeEqualsEndTimeThrowsException() {
        InvalidDataException ex = assertThrows(InvalidDataException.class, () ->
                DoctorValidator.validate("Иванов Иван", "Терапевт", "101", "09:00", "09:00")
        );
        assertTrue(ex.getMessage().contains("раньше"), "Сообщение должно требовать строгого предшествования времени начала");
    }

    /**
     * Проверяет генерацию исключения, если время начала смены позже времени окончания.
     */
    @Test
    @DisplayName("Ошибка валидации: время начала смены позже времени окончания")
    public void testStartTimeAfterEndTimeThrowsException() {
        InvalidDataException ex = assertThrows(InvalidDataException.class, () ->
                DoctorValidator.validate("Иванов Иван", "Терапевт", "101", "17:00", "09:00")
        );
        assertTrue(ex.getMessage().contains("раньше"), "Сообщение должно требовать строгого предшествования времени начала");
    }

    /**
     * Проверяет валидацию готового объекта Doctor.
     *
     * @throws Exception при непредвиденных ошибках
     */
    @Test
    @DisplayName("Валидация объекта Doctor методом DoctorValidator.validate(Doctor)")
    public void testValidateDoctorObject() throws Exception {
        Date start = timeFormat.parse("08:30");
        Date end = timeFormat.parse("15:00");
        Doctor validDoctor = new Doctor("Петров Петр", "Хирург", "205", start, end);

        assertDoesNotThrow(() -> DoctorValidator.validate(validDoctor),
                "Валидный объект Doctor не должен вызывать исключений");

        assertThrows(InvalidDataException.class, () -> DoctorValidator.validate((Doctor) null),
                "null-объект должен вызывать исключение");

        Doctor invalidTimeDoctor = new Doctor("Петров Петр", "Хирург", "205", end, start);
        assertThrows(InvalidDataException.class, () -> DoctorValidator.validate(invalidTimeDoctor),
                "Объект с некорректным временем должен вызывать исключение");
    }
}
