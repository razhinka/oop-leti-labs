package com.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Модульные тесты для сервиса управления врачами {@link DoctorService} (Лабораторная работа № 9).
 * Проверяет добавление, удаление, редактирование, фильтрацию и взаимодействие с репозиторием через Mockito.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    private DoctorService service;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Mock
    private Repository<Doctor> mockRepository;

    private Doctor doctor1;
    private Doctor doctor2;
    private Doctor doctor3;

    /**
     * Подготавливает тестовые данные перед выполнением каждого тестового метода.
     *
     * @throws Exception при ошибке разбора времени
     */
    @BeforeEach
    public void setUp() throws Exception {
        doctor1 = new Doctor(
                "Быков Андрей Евгеньевич",
                "Терапевт",
                "125",
                timeFormat.parse("08:00"),
                timeFormat.parse("14:00")
        );
        doctor2 = new Doctor(
                "Купитман Иван Натанович",
                "Уролог",
                "39",
                timeFormat.parse("10:00"),
                timeFormat.parse("18:00")
        );
        doctor3 = new Doctor(
                "Черноус Варвара Николаевна",
                "Педиатр",
                "105",
                timeFormat.parse("08:30"),
                timeFormat.parse("15:30")
        );

        List<Doctor> initial = new ArrayList<>();
        initial.add(doctor1);
        initial.add(doctor2);
        service = new DoctorService(initial);
    }

    /**
     * Проверяет корректное добавление нового врача в список.
     *
     * @throws Exception при ошибке валидации
     */
    @Test
    @DisplayName("Успешное добавление врача в коллекцию")
    public void testAddDoctorSuccess() throws Exception {
        assertEquals(2, service.size(), "Исходный размер списка должен быть равен 2");

        service.addDoctor(doctor3);

        assertEquals(3, service.size(), "Размер списка должен увеличиться до 3");
        assertTrue(service.getDoctors().contains(doctor3), "Список должен содержать добавленного врача");
        assertEquals(doctor3, service.getDoctors().get(2), "Добавленный врач должен находиться в конце списка");
    }

    /**
     * Проверяет запрет добавления некорректного или null объекта врача.
     */
    @Test
    @DisplayName("Ошибка валидации при добавлении некорректного врача")
    public void testAddInvalidDoctorThrowsException() {
        assertThrows(InvalidDataException.class, () -> service.addDoctor(null),
                "Добавление null должно вызывать InvalidDataException");

        Doctor invalidDoctor = new Doctor("", "Терапевт", "101", new java.util.Date(), new java.util.Date());
        assertThrows(InvalidDataException.class, () -> service.addDoctor(invalidDoctor),
                "Добавление врача с пустым ФИО должно вызывать InvalidDataException");
    }

    /**
     * Проверяет успешное удаление врача по корректному индексу.
     */
    @Test
    @DisplayName("Успешное удаление врача по индексу")
    public void testRemoveDoctorByIndex() {
        Doctor removed = service.removeDoctor(0);

        assertEquals(doctor1, removed, "Удаленный объект должен совпадать с исходным");
        assertEquals(1, service.size(), "Размер списка должен уменьшиться до 1");
        assertEquals(doctor2, service.getDoctors().get(0), "Оставшийся элемент должен сместиться на индекс 0");
    }

    /**
     * Проверяет генерацию исключения при удалении по невалидному индексу.
     */
    @Test
    @DisplayName("Исключение при выходе индекса за границы при удалении")
    public void testRemoveDoctorByInvalidIndexThrowsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> service.removeDoctor(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> service.removeDoctor(10));
    }

    /**
     * Проверяет удаление врача по ссылке на объект.
     */
    @Test
    @DisplayName("Удаление врача по совпадению объекта")
    public void testRemoveDoctorByObject() {
        boolean removedExisting = service.removeDoctor(doctor1);
        assertTrue(removedExisting, "Удаление существующего элемента должно возвращать true");
        assertEquals(1, service.size(), "Размер списка должен стать 1");

        boolean removedNonExisting = service.removeDoctor(doctor3);
        assertFalse(removedNonExisting, "Удаление отсутствующего элемента должно возвращать false");
        assertEquals(1, service.size(), "Размер списка не должен измениться");
    }

    /**
     * Проверяет успешное редактирование записи врача по индексу.
     *
     * @throws Exception при ошибке валидации
     */
    @Test
    @DisplayName("Успешное редактирование данных врача по индексу")
    public void testUpdateDoctorSuccess() throws Exception {
        Doctor updated = new Doctor(
                "Быков Андрей Евгеньевич",
                "Главврач",
                "200",
                timeFormat.parse("09:00"),
                timeFormat.parse("16:00")
        );

        service.updateDoctor(0, updated);

        assertEquals(2, service.size(), "Размер списка не должен измениться");
        assertEquals("Главврач", service.getDoctors().get(0).specialty(), "Специальность должна обновиться");
        assertEquals("200", service.getDoctors().get(0).room(), "Кабинет должен обновиться");
    }

    /**
     * Проверяет фильтрацию записей по специальности и поисковой строке.
     *
     * @throws Exception при ошибке добавления
     */
    @Test
    @DisplayName("Фильтрация списка врачей по специальности и подстроке поиска")
    public void testFilterDoctors() throws Exception {
        service.addDoctor(doctor3);

        // Фильтр только по специальности
        List<Doctor> therapists = service.filter("Терапевт", null);
        assertEquals(1, therapists.size());
        assertEquals("Быков Андрей Евгеньевич", therapists.get(0).fullName());

        // Фильтр по подстроке ФИО
        List<Doctor> byName = service.filter("Все специализации", "варвара");
        assertEquals(1, byName.size());
        assertEquals("Черноус Варвара Николаевна", byName.get(0).fullName());

        // Фильтр по кабинету
        List<Doctor> byRoom = service.filter(null, "39");
        assertEquals(1, byRoom.size());
        assertEquals("Купитман Иван Натанович", byRoom.get(0).fullName());

        // Фильтр без совпадений
        List<Doctor> emptyResult = service.filter("Окулист", null);
        assertTrue(emptyResult.isEmpty());
    }

    /**
     * Проверяет корректное делегирование сохранения данных репозиторию с помощью Mockito.
     *
     * @throws Exception при ошибке сохранения
     */
    @Test
    @DisplayName("Проверка взаимодействия с репозиторием при сохранении (Mockito verify)")
    public void testSaveDelegatesToRepositoryUsingMockito() throws Exception {
        File dummyFile = new File("test_doctors.xml");

        service.save(mockRepository, dummyFile);

        // Проверяем, что метод save репозитория был вызван ровно один раз с текущим списком и файлом
        verify(mockRepository, times(1)).save(eq(service.getDoctors()), eq(dummyFile));
    }

    /**
     * Проверяет корректное делегирование загрузки данных репозиторию с помощью Mockito.
     *
     * @throws Exception при ошибке загрузки
     */
    @Test
    @DisplayName("Проверка взаимодействия с репозиторием при загрузке (Mockito when/verify)")
    public void testLoadDelegatesToRepositoryUsingMockito() throws Exception {
        File dummyFile = new File("test_doctors.xml");
        List<Doctor> mockedData = List.of(doctor3);

        when(mockRepository.load(dummyFile)).thenReturn(mockedData);

        service.load(mockRepository, dummyFile);

        verify(mockRepository, times(1)).load(dummyFile);
        assertEquals(1, service.size(), "Сервис должен обновить список данными из репозитория");
        assertEquals("Черноус Варвара Николаевна", service.getDoctors().get(0).fullName());
    }
}
