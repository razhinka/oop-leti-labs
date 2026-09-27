package com.engine;

/**
 * Пользовательское исключение для контроля некорректного ввода данных.
 */
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}