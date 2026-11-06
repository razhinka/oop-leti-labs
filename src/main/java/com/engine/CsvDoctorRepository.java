package com.engine;

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
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class CsvDoctorRepository implements Repository<Doctor> {

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
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("ФИО,Специализация,Кабинет,Начало смены,Конец смены");
            for (Doctor doc : items) {
                writer.print(escapeCSV(doc.fullName()) + ",");
                writer.print(escapeCSV(doc.specialty()) + ",");
                writer.print(escapeCSV(doc.room()) + ",");
                writer.print(escapeCSV(timeFormat.format(doc.startTime())) + ",");
                writer.println(escapeCSV(timeFormat.format(doc.endTime())));
            }
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
        List<Doctor> result = new ArrayList<>();
        if (!file.exists()) {
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
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException("При обработке времени произошла ошибка", e);
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
