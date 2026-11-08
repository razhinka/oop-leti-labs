package com.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация репозитория для сущности Doctor с использованием текстового формата CSV.
 * Для потоковой записи и чтения задействованы классы {@link PrintWriter}, {@link FileWriter},
 * {@link BufferedReader} и {@link FileReader}.
 * Все операции протоколируются с помощью Log4j.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class CsvDoctorRepository implements Repository<Doctor> {

    private static final Logger logger = LogManager.getLogger(CsvDoctorRepository.class);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Создает новый экземпляр репозитория CSV-данных.
     */
    public CsvDoctorRepository() {
    }

    /**
     * Сохраняет список врачей в CSV-файл с экранированием специальных символов.
     *
     * @param items список врачей для сохранения
     * @param file  целевой файл CSV
     * @throws Exception при ошибке ввода-вывода
     */
    @Override
    public void save(List<Doctor> items, File file) throws Exception {
        logger.info("Начало сохранения списка врачей в CSV-файл: {} (всего записей: {})",
                file != null ? file.getAbsolutePath() : "null", items != null ? items.size() : 0);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("ФИО,Специализация,Кабинет,Начало смены,Конец смены");
            if (items != null) {
                for (Doctor doc : items) {
                    writer.print(escapeCSV(doc.fullName()) + ",");
                    writer.print(escapeCSV(doc.specialty()) + ",");
                    writer.print(escapeCSV(doc.room()) + ",");
                    writer.print(escapeCSV(timeFormat.format(doc.startTime())) + ",");
                    writer.println(escapeCSV(timeFormat.format(doc.endTime())));
                    logger.debug("Записана строка CSV для врача: {}", doc.fullName());
                }
            }
            logger.info("CSV-файл успешно сохранен: {}", file != null ? file.getAbsolutePath() : "null");
        } catch (Exception e) {
            logger.error("Ошибка при сохранении данных в CSV-файл {}: {}",
                    file != null ? file.getAbsolutePath() : "null", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Считывает данные врачей из CSV-файла с пропуском строки заголовка.
     *
     * @param file исходный файл CSV
     * @return список прочитанных объектов Doctor
     * @throws Exception при повреждении структуры файла или неверном формате даты
     */
    @Override
    public List<Doctor> load(File file) throws Exception {
        logger.info("Начало загрузки списка врачей из CSV-файла: {}", file != null ? file.getAbsolutePath() : "null");
        List<Doctor> result = new ArrayList<>();
        if (file == null || !file.exists()) {
            logger.warn("CSV-файл не существует или не указан: {}", file != null ? file.getAbsolutePath() : "null");
            return result;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue; // пропуск заголовка
                }
                String[] parts = parseCSVLine(line);
                if (parts.length == 5) {
                    result.add(new Doctor(
                            parts[0],
                            parts[1],
                            parts[2],
                            timeFormat.parse(parts[3]),
                            timeFormat.parse(parts[4])
                    ));
                    logger.debug("Прочитана запись врача из CSV: {} ({})", parts[0], parts[1]);
                } else {
                    logger.warn("Пропущена некорректная строка CSV (неверное число колонок {}): {}", parts.length, line);
                }
            }
            logger.info("Загрузка CSV завершена успешно. Загружено врачей: {}", result.size());
        } catch (ParseException e) {
            logger.error("Ошибка разбора времени в CSV-файле {}: {}", file.getAbsolutePath(), e.getMessage(), e);
            throw new RuntimeException("При обработке времени произошла ошибка", e);
        } catch (Exception e) {
            logger.error("Ошибка при чтении CSV-файла {}: {}", file.getAbsolutePath(), e.getMessage(), e);
            throw e;
        }
        return result;
    }

    /**
     * Экранирование строковых значений для безопасного сохранения в CSV.
     *
     * @param value исходное строковое значение
     * @return экранированная строка
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Разбор строки формата CSV с учетом закавыченных полей и разделителей-запятых.
     *
     * @param line строка CSV
     * @return массив значений колонок
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
