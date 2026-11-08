package com.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Сервис многопоточной обработки данных регистратуры поликлиники (Лабораторная работа № 8 и № 10).
 * Организует строго синхронизированную работу трех независимых потоков исполнения:
 * <ul>
 *     <li><b>Поток 1 (Loader):</b> считывает список врачей из исходного XML-файла и передает его в общую модель данных;</li>
 *     <li><b>Поток 2 (Editor):</b> ожидает завершения чтения, вносит изменения в коллекцию данных и сохраняет результат в отдельный XML-файл;</li>
 *     <li><b>Поток 3 (Reporter):</b> ожидает сохранения отредактированного XML-файла и компилирует на его основе HTML-отчет через JasperReports.</li>
 * </ul>
 * Синхронизация очередности выполнения потоков реализована с помощью примитивов {@link CountDownLatch}.
 * Все этапы и межпоточные события протоколируются через Apache Log4j (уровни DEBUG, INFO, WARN, ERROR).
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class MultithreadingService {

    private static final Logger logger = LogManager.getLogger(MultithreadingService.class);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Конструктор по умолчанию для сервиса многопоточной обработки.
     */
    public MultithreadingService() {
    }

    /**
     * Интерфейс обратного вызова для отслеживания стадий работы потоков
     * и безопасного обновления пользовательского интерфейса Swing.
     */
    public interface MultithreadCallback {

        /**
         * Вызывается при успешной загрузке данных Потоком 1.
         *
         * @param loadedDoctors прочитанный список врачей
         */
        void onLoaded(List<Doctor> loadedDoctors);

        /**
         * Вызывается при завершении редактирования и сохранения XML-файла Потоком 2.
         *
         * @param editedDoctors обновленный список врачей
         */
        void onEdited(List<Doctor> editedDoctors);

        /**
         * Вызывается при успешном формировании HTML-отчета Потоком 3.
         *
         * @param reportFile итоговый сгенерированный HTML-файл
         */
        void onCompleted(File reportFile);

        /**
         * Вызывается при возникновении ошибки на любом из этапов.
         *
         * @param threadNumber номер потока, в котором возникла ошибка (1, 2 или 3)
         * @param stage        краткое текстовое описание текущего этапа
         * @param error        перехваченное исключение
         */
        void onError(int threadNumber, String stage, Throwable error);
    }

    /**
     * Создает исходный XML-файл с демонстрационными данными, если файл отсутствует на диске.
     *
     * @param sourceXml целевой файл для сохранения исходных данных
     * @throws Exception при ошибке формирования данных или записи XML
     */
    public void prepareDefaultSourceXmlIfAbsent(File sourceXml) throws Exception {
        if (!sourceXml.exists()) {
            logger.warn("Исходный XML-файл не найден: {}. Создается демонстрационный набор данных...", sourceXml.getAbsolutePath());
            List<Doctor> initialData = new ArrayList<>();
            initialData.add(new Doctor("Быков Андрей Евгеньевич", "Терапевт", "125",
                    timeFormat.parse("08:00"), timeFormat.parse("14:00")));
            initialData.add(new Doctor("Купитман Иван Натанович", "Уролог", "39",
                    timeFormat.parse("10:00"), timeFormat.parse("18:00")));
            initialData.add(new Doctor("Черноус Варвара Николаевна", "Педиатр", "105",
                    timeFormat.parse("08:30"), timeFormat.parse("15:30")));
            new XmlDoctorRepository().save(initialData, sourceXml);
            logger.info("Демонстрационный XML-файл успешно подготовлен: {}", sourceXml.getName());
        }
    }

    /**
     * Запускает три строго синхронизированных потока в асинхронном режиме.
     * Метод возвращает управление немедленно, передавая события в указанный callback.
     *
     * @param sourceXml    исходный XML-файл с расписанием
     * @param editedXml    целевой XML-файл для сохранения отредактированных данных
     * @param reportHtml   выходной HTML-файл отчета
     * @param templatePath путь к файлу шаблона отчета JasperReports (.jrxml)
     * @param targetList   общая коллекция записей (например, модель формы)
     * @param callback     обработчик событий жизненного цикла пайплайна
     */
    public void runPipelineAsync(
            File sourceXml,
            File editedXml,
            File reportHtml,
            String templatePath,
            List<Doctor> targetList,
            MultithreadCallback callback
    ) {
        logger.info("Инициализация асинхронного запуска многопоточного пайплайна (Лаб. 8, Лаб. 10)...");
        startThreads(sourceXml, editedXml, reportHtml, templatePath, targetList, callback, false);
    }

    /**
     * Запускает три строго синхронизированных потока в синхронном режиме
     * и ожидает завершения работы всех трех потоков.
     *
     * @param sourceXml    исходный XML-файл с расписанием
     * @param editedXml    целевой XML-файл для сохранения отредактированных данных
     * @param reportHtml   выходной HTML-файл отчета
     * @param templatePath путь к файлу шаблона отчета JasperReports (.jrxml)
     * @param targetList   общая коллекция записей (может быть пустым списком)
     * @param callback     обработчик событий жизненного цикла пайплайна (может быть null)
     */
    public void runPipelineSync(
            File sourceXml,
            File editedXml,
            File reportHtml,
            String templatePath,
            List<Doctor> targetList,
            MultithreadCallback callback
    ) {
        logger.info("Инициализация синхронного запуска многопоточного пайплайна...");
        startThreads(sourceXml, editedXml, reportHtml, templatePath, targetList, callback, true);
    }

    /**
     * Внутренний метод конструирования, синхронизации и запуска трех рабочих потоков.
     *
     * @param sourceXml    исходный XML-файл с расписанием
     * @param editedXml    целевой XML-файл для сохранения отредактированных данных
     * @param reportHtml   выходной HTML-файл отчета
     * @param templatePath путь к jrxml-шаблону
     * @param targetList   синхронизируемый список врачей
     * @param callback     слушатель событий
     * @param waitToFinish признак ожидания завершения всех потоков (join)
     */
    private void startThreads(
            File sourceXml,
            File editedXml,
            File reportHtml,
            String templatePath,
            List<Doctor> targetList,
            MultithreadCallback callback,
            boolean waitToFinish
    ) {
        try {
            prepareDefaultSourceXmlIfAbsent(sourceXml);
        } catch (Exception e) {
            logger.error("Ошибка при подготовке исходного XML-файла: {}", e.getMessage(), e);
            if (callback != null) {
                callback.onError(1, "Подготовка исходного XML", e);
            }
            return;
        }

        CountDownLatch loadCompletedLatch = new CountDownLatch(1);
        CountDownLatch editCompletedLatch = new CountDownLatch(1);

        // Поток 1: Загрузка данных из исходного XML-файла
        Thread loadThread = new Thread(() -> {
            try {
                logger.info("[Поток 1] Старт загрузки данных из файла: {}", sourceXml.getName());
                Repository<Doctor> repo = new XmlDoctorRepository();
                List<Doctor> loaded = repo.load(sourceXml);

                synchronized (targetList) {
                    targetList.clear();
                    targetList.addAll(loaded);
                }

                logger.debug("[Поток 1] Прочитано {} записей из исходного файла, имитация I/O задержки (300 мс)...", loaded.size());
                Thread.sleep(300);
                logger.info("[Поток 1] Загрузка успешно завершена, записей: {}", loaded.size());

                if (callback != null) {
                    callback.onLoaded(new ArrayList<>(loaded));
                }
            } catch (Throwable ex) {
                logger.error("[Поток 1] Ошибка загрузки данных из XML: {}", ex.getMessage(), ex);
                if (callback != null) {
                    callback.onError(1, "Загрузка данных из XML", ex);
                }
            } finally {
                logger.debug("[Поток 1] Сброс защелки loadCompletedLatch...");
                loadCompletedLatch.countDown();
            }
        }, "Loader-Thread-1");

        // Поток 2: Редактирование данных и сохранение нового XML-файла
        Thread editThread = new Thread(() -> {
            try {
                logger.debug("[Поток 2] Ожидание завершения загрузки первым потоком (loadCompletedLatch.await)...");
                loadCompletedLatch.await();

                logger.info("[Поток 2] Начало модификации данных...");
                List<Doctor> snapshot;
                synchronized (targetList) {
                    if (!targetList.isEmpty()) {
                        Doctor first = targetList.getFirst();
                        Doctor updatedFirst = new Doctor(
                                first.fullName(),
                                first.specialty(),
                                first.room() + " (обновл.)",
                                first.startTime(),
                                first.endTime()
                        );
                        targetList.set(0, updatedFirst);
                        logger.debug("[Поток 2] Обновлен кабинет первой записи: '{}' -> '{}'", first.room(), updatedFirst.room());
                    }

                    Doctor newDoctor = new Doctor(
                            "Павлова Ирина Алексеевна",
                            "Хирург",
                            "301",
                            timeFormat.parse("09:00"),
                            timeFormat.parse("17:00")
                    );
                    targetList.add(newDoctor);
                    logger.debug("[Поток 2] Добавлена новая запись: {} ({}, каб. {})", newDoctor.fullName(), newDoctor.specialty(), newDoctor.room());

                    snapshot = new ArrayList<>(targetList);
                    new XmlDoctorRepository().save(snapshot, editedXml);
                }

                logger.debug("[Поток 2] Сохранение отредактированного списка ({} записей), задержка I/O (300 мс)...", snapshot.size());
                Thread.sleep(300);
                logger.info("[Поток 2] Отредактированный XML успешно сохранен: {}", editedXml.getName());

                if (callback != null) {
                    callback.onEdited(snapshot);
                }
            } catch (Throwable ex) {
                logger.error("[Поток 2] Ошибка модификации или сохранения данных: {}", ex.getMessage(), ex);
                if (callback != null) {
                    callback.onError(2, "Модификация и выгрузка в XML", ex);
                }
            } finally {
                logger.debug("[Поток 2] Сброс защелки editCompletedLatch...");
                editCompletedLatch.countDown();
            }
        }, "Editor-Thread-2");

        // Поток 3: Построение HTML-отчета через JasperReports
        Thread reportThread = new Thread(() -> {
            try {
                logger.debug("[Поток 3] Ожидание сохранения XML-файла вторым потоком (editCompletedLatch.await)...");
                editCompletedLatch.await();

                logger.info("[Поток 3] Формирование HTML-отчета по файлу: {}", editedXml.getName());
                ReportService.buildReport(
                        editedXml.getAbsolutePath(),
                        "/doctors/doctor",
                        templatePath,
                        reportHtml.getAbsolutePath()
                );

                logger.info("[Поток 3] HTML-отчет успешно сформирован: {}", reportHtml.getAbsolutePath());
                if (callback != null) {
                    callback.onCompleted(reportHtml);
                }
            } catch (Throwable ex) {
                logger.error("[Поток 3] Ошибка построения HTML-отчета: {}", ex.getMessage(), ex);
                if (callback != null) {
                    callback.onError(3, "Генерация HTML-отчета", ex);
                }
            }
        }, "Reporter-Thread-3");

        // Запуск потоков
        logger.debug("Запуск параллельных потоков: Loader, Editor, Reporter...");
        loadThread.start();
        editThread.start();
        reportThread.start();

        if (waitToFinish) {
            try {
                loadThread.join();
                editThread.join();
                reportThread.join();
                logger.info("Все три потока успешно завершили работу (join).");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Ожидание завершения потоков было прервано: {}", e.getMessage());
            }
        }
    }
}
