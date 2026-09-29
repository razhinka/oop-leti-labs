package com.engine;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Обобщенный интерфейс репозитория для сохранения и загрузки сущностей.
 *
 * @param <T> тип сохраняемой сущности
 * @author Ражин Захар С. (кафедра ВТ)
 */
public interface Repository<T> {

    /**
     * Сохраняет список сущностей в целевой файл.
     *
     * @param items список объектов для сохранения
     * @throws IOException при ошибках записи
     */
    void save(List<T> items, File file) throws Exception;

    /**
     * Загружает список сущностей из указанного файла.
     *
     * @return список прочитанных объектов
     * @throws IOException при ошибках чтения или повреждении формата
     */
    List<T> load(File file) throws Exception;
}