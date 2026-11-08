package com.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Сервисный класс бизнес-логики для управления записями врачей регистратуры поликлиники.
 * Обеспечивает операции добавления, удаления, редактирования, фильтрации
 * и взаимодействия с репозиториями сохранения данных.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class DoctorService {

    private static final Logger logger = LogManager.getLogger(DoctorService.class);
    private final List<Doctor> doctors;

    /**
     * Создает экземпляр сервиса с пустым списком врачей.
     */
    public DoctorService() {
        this.doctors = new ArrayList<>();
    }

    /**
     * Создает экземпляр сервиса с предварительно заполненным списком врачей.
     *
     * @param initialDoctors начальный список врачей
     */
    public DoctorService(List<Doctor> initialDoctors) {
        this.doctors = new ArrayList<>(initialDoctors != null ? initialDoctors : Collections.emptyList());
    }

    /**
     * Добавляет новую запись о враче после обязательной валидации.
     *
     * @param doctor объект врача для добавления
     * @throws InvalidDataException если данные врача не прошли валидацию
     */
    public void addDoctor(Doctor doctor) throws InvalidDataException {
        DoctorValidator.validate(doctor);
        doctors.add(doctor);
        logger.info("Врач успешно добавлен в реестр: {}", doctor.fullName());
    }

    /**
     * Удаляет врача по указанному индексу в списке.
     *
     * @param index индекс удаляемой записи
     * @return удаленный объект {@link Doctor}
     * @throws IndexOutOfBoundsException если индекс выходит за границы диапазона списка
     */
    public Doctor removeDoctor(int index) {
        if (index < 0 || index >= doctors.size()) {
            logger.warn("Попытка удаления врача по некорректному индексу: {}", index);
            throw new IndexOutOfBoundsException("Индекс записи выходит за допустимые границы: " + index);
        }
        Doctor removed = doctors.remove(index);
        logger.info("Удален врач по индексу {}: {}", index, removed.fullName());
        return removed;
    }

    /**
     * Удаляет указанный объект врача из списка.
     *
     * @param doctor объект врача для удаления
     * @return true, если запись была найдена и удалена; false в противном случае
     */
    public boolean removeDoctor(Doctor doctor) {
        boolean removed = doctors.remove(doctor);
        if (removed && doctor != null) {
            logger.info("Удален врач из реестра: {}", doctor.fullName());
        } else {
            logger.warn("Попытка удаления отсутствующего врача: {}", doctor);
        }
        return removed;
    }

    /**
     * Обновляет данные врача по указанному индексу после валидации.
     *
     * @param index         индекс обновляемой записи
     * @param updatedDoctor новые данные врача
     * @throws IndexOutOfBoundsException если индекс выходит за границы диапазона
     * @throws InvalidDataException      если новые данные не прошли валидацию
     */
    public void updateDoctor(int index, Doctor updatedDoctor) throws InvalidDataException {
        if (index < 0 || index >= doctors.size()) {
            logger.warn("Попытка обновления врача по некорректному индексу: {}", index);
            throw new IndexOutOfBoundsException("Индекс записи выходит за допустимые границы: " + index);
        }
        DoctorValidator.validate(updatedDoctor);
        doctors.set(index, updatedDoctor);
        logger.info("Данные врача по индексу {} успешно обновлены: {}", index, updatedDoctor.fullName());
    }

    /**
     * Возвращает неизменяемый снимок текущего списка врачей.
     *
     * @return список объектов {@link Doctor}
     */
    public List<Doctor> getDoctors() {
        return Collections.unmodifiableList(new ArrayList<>(doctors));
    }

    /**
     * Возвращает количество врачей в списке.
     *
     * @return размер списка
     */
    public int size() {
        return doctors.size();
    }

    /**
     * Очищает список врачей.
     */
    public void clear() {
        int count = doctors.size();
        doctors.clear();
        logger.info("Реестр врачей очищен (удалено {} записей)", count);
    }

    /**
     * Заменяет текущий список врачей переданной коллекцией.
     *
     * @param newDoctors новый список врачей
     */
    public void setDoctors(List<Doctor> newDoctors) {
        doctors.clear();
        if (newDoctors != null) {
            doctors.addAll(newDoctors);
        }
        logger.debug("Установлен новый список врачей: {} записей", doctors.size());
    }

    /**
     * Фильтрует список врачей по заданной специальности и текстовому запросу (ФИО или кабинет).
     *
     * @param specialty требуемая специальность (или "Все специализации" / null для пропуска фильтра)
     * @param query     подстрока для поиска по ФИО или кабинету (без учета регистра)
     * @return отфильтрованный список врачей
     */
    public List<Doctor> filter(String specialty, String query) {
        logger.debug("Фильтрация врачей: специальность='{}', поисковый запрос='{}'", specialty, query);
        List<Doctor> result = new ArrayList<>();
        String normalizedQuery = query != null ? query.trim().toLowerCase() : "";
        boolean filterSpecialty = specialty != null && !specialty.trim().isEmpty() && !"Все специализации".equalsIgnoreCase(specialty.trim());

        for (Doctor doc : doctors) {
            boolean matchesSpecialty = !filterSpecialty || doc.specialty().equalsIgnoreCase(specialty.trim());
            boolean matchesQuery = normalizedQuery.isEmpty()
                    || doc.fullName().toLowerCase().contains(normalizedQuery)
                    || doc.room().toLowerCase().contains(normalizedQuery);

            if (matchesSpecialty && matchesQuery) {
                result.add(doc);
            }
        }
        logger.debug("Результат фильтрации: найдено {} записей из {}", result.size(), doctors.size());
        return result;
    }

    /**
     * Загружает данные из файла через указанный репозиторий и обновляет внутренний список.
     *
     * @param repository реализация репозитория (XML, CSV и др.)
     * @param file       исходный файл данных
     * @throws Exception при ошибке чтения файла
     */
    public void load(Repository<Doctor> repository, File file) throws Exception {
        if (repository == null) {
            logger.error("Ошибка загрузки: репозиторий равен null");
            throw new IllegalArgumentException("Репозиторий не может быть null.");
        }
        logger.info("Загрузка данных из файла {} через {}", file != null ? file.getName() : "null", repository.getClass().getSimpleName());
        List<Doctor> loaded = repository.load(file);
        setDoctors(loaded);
        logger.info("Успешно загружено {} врачей", loaded != null ? loaded.size() : 0);
    }

    /**
     * Сохраняет текущий список врачей в файл через указанный репозиторий.
     *
     * @param repository реализация репозитория (XML, CSV и др.)
     * @param file       целевой файл для сохранения
     * @throws Exception при ошибке записи файла
     */
    public void save(Repository<Doctor> repository, File file) throws Exception {
        if (repository == null) {
            logger.error("Ошибка сохранения: репозиторий равен null");
            throw new IllegalArgumentException("Репозиторий не может быть null.");
        }
        logger.info("Сохранение {} врачей в файл {} через {}", doctors.size(), file != null ? file.getName() : "null", repository.getClass().getSimpleName());
        repository.save(doctors, file);
        logger.info("Данные успешно сохранены в файл: {}", file != null ? file.getName() : "null");
    }
}
