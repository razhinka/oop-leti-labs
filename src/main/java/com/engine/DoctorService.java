package com.engine;

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
            throw new IndexOutOfBoundsException("Индекс записи выходит за допустимые границы: " + index);
        }
        return doctors.remove(index);
    }

    /**
     * Удаляет указанный объект врача из списка.
     *
     * @param doctor объект врача для удаления
     * @return true, если запись была найдена и удалена; false в противном случае
     */
    public boolean removeDoctor(Doctor doctor) {
        return doctors.remove(doctor);
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
            throw new IndexOutOfBoundsException("Индекс записи выходит за допустимые границы: " + index);
        }
        DoctorValidator.validate(updatedDoctor);
        doctors.set(index, updatedDoctor);
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
        doctors.clear();
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
    }

    /**
     * Фильтрует список врачей по заданной специальности и текстовому запросу (ФИО или кабинет).
     *
     * @param specialty требуемая специальность (или "Все специализации" / null для пропуска фильтра)
     * @param query     подстрока для поиска по ФИО или кабинету (без учета регистра)
     * @return отфильтрованный список врачей
     */
    public List<Doctor> filter(String specialty, String query) {
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
            throw new IllegalArgumentException("Репозиторий не может быть null.");
        }
        List<Doctor> loaded = repository.load(file);
        setDoctors(loaded);
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
            throw new IllegalArgumentException("Репозиторий не может быть null.");
        }
        repository.save(doctors, file);
    }
}
