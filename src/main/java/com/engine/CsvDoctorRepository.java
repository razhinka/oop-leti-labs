package com.engine;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация репозитория для сущности Doctor в формате CSV.
 */
public class CsvDoctorRepository implements Repository<Doctor> {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public void save(List<Doctor> items, File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) { // Использование PrintWriter и FileWriter
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

    @Override
    public List<Doctor> load(File file) throws Exception {
        List<Doctor> result = new ArrayList<>();
        if (!file.exists()) return result; // Проверка существования файла

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) { // Использование BufferedReader и FileReader
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
     * Экранирование значений для CSV.
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Парсинг строки CSV с поддержкой кавычек.
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