package com.engine;

/**
 * Пользовательское проверяемое исключение для валидации некорректных данных врача
 * (например, пустые обязательные поля, логически неверный интервал рабочих часов и т.д.).
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class InvalidDataException extends Exception {

    /**
     * Конструктор с указанием понятного описания причины ошибки валидации.
     *
     * @param message подробное описание выявленной ошибки
     */
    public InvalidDataException(String message) {
        super(message);
    }
}
