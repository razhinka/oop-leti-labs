package com.engine;

import java.util.Date;

/**
 * Record-класс для хранения данных о враче.
 */
public record Doctor(
        String fullName,
        String specialty,
        String room,
        Date startTime,
        Date endTime
) {}