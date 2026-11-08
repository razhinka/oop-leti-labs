package com.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Валидатор данных сущности Doctor.
 * Выполняет строгую проверку корректности текстовых полей и временных интервалов смены врача.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class DoctorValidator {

    private static final Logger logger = LogManager.getLogger(DoctorValidator.class);
    private static final String TIME_FORMAT_PATTERN = "HH:mm";

    /**
     * Приватный конструктор для предотвращения создания экземпляров утилитного класса.
     */
    private DoctorValidator() {
    }

    /**
     * Выполняет валидацию строковых параметров и конструирует валидный объект {@link Doctor}.
     *
     * @param fullName     ФИО врача (не может быть пустым)
     * @param specialty    специальность врача (не может быть пустой)
     * @param room         номер кабинета (не может быть пустым)
     * @param startTimeStr время начала смены в формате "HH:mm"
     * @param endTimeStr   время окончания смены в формате "HH:mm"
     * @return сформированный и провалидированный объект {@link Doctor}
     * @throws InvalidDataException при наличии пустых полей или если начало смены не предшествует ее концу
     */
    public static Doctor validate(
            String fullName,
            String specialty,
            String room,
            String startTimeStr,
            String endTimeStr
    ) throws InvalidDataException {
        logger.debug("Валидация строковых параметров врача: ФИО='{}', специальность='{}', кабинет='{}', интервал={}-{}",
                fullName, specialty, room, startTimeStr, endTimeStr);

        if (fullName == null || fullName.trim().isEmpty()) {
            logger.warn("Ошибка валидации: не указано ФИО врача");
            throw new InvalidDataException("ФИО врача обязательно для заполнения.");
        }
        if (specialty == null || specialty.trim().isEmpty()) {
            logger.warn("Ошибка валидации: не указана специальность врача");
            throw new InvalidDataException("Специализация врача обязательна для заполнения.");
        }
        if (room == null || room.trim().isEmpty()) {
            logger.warn("Ошибка валидации: не указан кабинет врача");
            throw new InvalidDataException("Номер кабинета обязателен для заполнения.");
        }
        if (startTimeStr == null || startTimeStr.trim().isEmpty() || endTimeStr == null || endTimeStr.trim().isEmpty()) {
            logger.warn("Ошибка валидации: не заполнено время смены врача");
            throw new InvalidDataException("Время начала и окончания смены обязательны для заполнения.");
        }

        String startTrimmed = startTimeStr.trim();
        String endTrimmed = endTimeStr.trim();

        String timeRegex = "^([01]\\d|2[0-3]):[0-5]\\d$";
        if (!startTrimmed.matches(timeRegex)) {
            logger.warn("Ошибка валидации: неверный формат времени начала смены '{}'", startTrimmed);
            throw new InvalidDataException("Некорректный формат времени начала смены. Ожидается HH:mm (например, 08:00).");
        }
        if (!endTrimmed.matches(timeRegex)) {
            logger.warn("Ошибка валидации: неверный формат времени окончания смены '{}'", endTrimmed);
            throw new InvalidDataException("Некорректный формат времени окончания смены. Ожидается HH:mm (например, 14:00).");
        }

        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_PATTERN);
        sdf.setLenient(false);

        Date start;
        Date end;
        try {
            start = sdf.parse(startTrimmed);
        } catch (ParseException e) {
            logger.warn("Ошибка валидации: сбой разбора времени начала смены '{}'", startTrimmed);
            throw new InvalidDataException("Некорректный формат времени начала смены. Ожидается HH:mm (например, 08:00).");
        }

        try {
            end = sdf.parse(endTrimmed);
        } catch (ParseException e) {
            logger.warn("Ошибка валидации: сбой разбора времени окончания смены '{}'", endTrimmed);
            throw new InvalidDataException("Некорректный формат времени окончания смены. Ожидается HH:mm (например, 14:00).");
        }

        if (!start.before(end)) {
            logger.warn("Ошибка валидации: время начала смены ({}) не предшествует окончанию ({})", startTrimmed, endTrimmed);
            throw new InvalidDataException("Время начала смены должно быть строго раньше времени её окончания.");
        }

        Doctor doctor = new Doctor(fullName.trim(), specialty.trim(), room.trim(), start, end);
        logger.debug("Валидация строковых параметров успешно завершена: {}", doctor.fullName());
        return doctor;
    }

    /**
     * Проверяет существующий объект {@link Doctor} на корректность данных.
     *
     * @param doctor проверяемый объект врача
     * @throws InvalidDataException если объект null, содержит пустые поля или невалидное время
     */
    public static void validate(Doctor doctor) throws InvalidDataException {
        logger.debug("Валидация объекта врача: {}", doctor);
        if (doctor == null) {
            logger.warn("Ошибка валидации: передан null-объект врача");
            throw new InvalidDataException("Объект врача не может быть null.");
        }
        if (doctor.fullName() == null || doctor.fullName().trim().isEmpty()) {
            logger.warn("Ошибка валидации: у объекта врача пустое ФИО");
            throw new InvalidDataException("ФИО врача обязательно для заполнения.");
        }
        if (doctor.specialty() == null || doctor.specialty().trim().isEmpty()) {
            logger.warn("Ошибка валидации: у объекта врача пустая специальность");
            throw new InvalidDataException("Специализация врача обязательна для заполнения.");
        }
        if (doctor.room() == null || doctor.room().trim().isEmpty()) {
            logger.warn("Ошибка валидации: у объекта врача пустой кабинет");
            throw new InvalidDataException("Номер кабинета обязателен для заполнения.");
        }
        if (doctor.startTime() == null || doctor.endTime() == null) {
            logger.warn("Ошибка валидации: у объекта врача не заданы временные границы смены");
            throw new InvalidDataException("Время начала и окончания смены не могут быть null.");
        }
        if (!doctor.startTime().before(doctor.endTime())) {
            logger.warn("Ошибка валидации: у объекта врача начало смены не раньше окончания");
            throw new InvalidDataException("Время начала смены должно быть строго раньше времени её окончания.");
        }
        logger.debug("Валидация объекта врача завершена успешно: {}", doctor.fullName());
    }
}
