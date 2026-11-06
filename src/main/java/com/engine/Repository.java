package com.engine;

import java.io.File;
import java.util.List;

/**
 * Обобщенный интерфейс репозитория для сохранения и загрузки коллекций сущностей.
 * Реализует паттерн Data Access Object (DAO) / Repository для абстрагирования источников данных.
 *
 * @param <T> тип сохраняемой сущности
 * @author Ражин Захар 5312
 * @version 1.0
 */
public interface Repository<T> {

    /**
     * Сохраняет список сущностей в целевой файл на диске.
     *
     * @param items список объектов для сохранения
     * @param file  целевой файл для записи
     * @throws Exception при ошибках ввода-вывода или сериализации
     */
    void save(List<T> items, File file) throws Exception;

    /**
     * Загружает список сущностей из указанного файла.
     *
     * @param file исходный файл для чтения данных
     * @return список прочитанных объектов
     * @throws Exception при ошибках ввода-вывода или повреждении формата
     */
    List<T> load(File file) throws Exception;
}
