package com.engine;

import java.util.Date;

/**
 * Record-класс для представления сущности врача в регистратуре поликлиники.
 * Содержит полную информацию о враче: ФИО, медицинскую специальность,
 * номер рабочего кабинета, а также время начала и окончания приёма пациентов.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 * @param fullName  фамилия, имя и отчество врача
 * @param specialty медицинская специальность врача (Терапевт, Уролог, Педиатр и др.)
 * @param room      номер кабинета приёма
 * @param startTime время начала рабочей смены приёма
 * @param endTime   время окончания рабочей смены приёма
 */
public record Doctor(
        String fullName,
        String specialty,
        String room,
        Date startTime,
        Date endTime
) {}
