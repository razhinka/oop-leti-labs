package com.engine;

import net.sf.jasperreports.engine.JRException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Класс ClinicRegistryGUI реализует графический интерфейс пользователя
 * регистратуры поликлиники с поддержкой сериализации в XML, фильтрации записей,
 * формирования отчетов в JasperReports и синхронизированной многопоточной обработки данных.
 * Все взаимодействия и события интерфейса протоколируются с помощью Log4j.
 *
 * @author Ражин Захар 5312
 * @version 3.0
 */
public class ClinicRegistryGUI {

    private static final Logger logger = LogManager.getLogger(ClinicRegistryGUI.class);

    private final JFrame mainWindow;
    private final DefaultTableModel model;
    private final JTable doctorsTable;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JButton btnAddDoctor;
    private final JButton btnEditDoctor;
    private final JButton btnDeleteDoctor;
    private final JButton btnSave;
    private final JButton btnLoad;
    private final JButton btnReportPdf;
    private final JButton btnReportHtml;
    private final JButton btnRunThreads;

    // Компоненты панели поиска и фильтрации
    private final JComboBox<String> specialtyFilter;
    private final JTextField doctorSearchField;
    private final JButton btnSearch;
    private final JButton btnResetSearch;

    private final List<Doctor> doctorsList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Конструктор главного окна приложения.
     * Выполняет компоновку элементов интерфейса, настройку таблицы и панели фильтров.
     */
    public ClinicRegistryGUI() {
        logger.info("Инициализация графического интерфейса ClinicRegistryGUI...");
        mainWindow = new JFrame("Регистратура поликлиники - Управление расписанием");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(980, 480);
        mainWindow.setLocationRelativeTo(null);

        // Верхняя панель инструментов
        JToolBar toolBar = new JToolBar("Управление записями");
        btnAddDoctor = new JButton("Добавить врача");
        btnEditDoctor = new JButton("Редактировать");
        btnDeleteDoctor = new JButton("Удалить");
        btnLoad = new JButton("Загрузить из XML");
        btnSave = new JButton("Сохранить в XML");
        btnReportPdf = new JButton("Отчет PDF");
        btnReportHtml = new JButton("Отчет HTML");
        btnRunThreads = new JButton("Потоки (Лаб. 8)");

        toolBar.add(btnAddDoctor);
        toolBar.add(btnEditDoctor);
        toolBar.add(btnDeleteDoctor);
        toolBar.addSeparator();
        toolBar.add(btnLoad);
        toolBar.add(btnSave);
        toolBar.addSeparator();
        toolBar.add(btnReportPdf);
        toolBar.add(btnReportHtml);
        toolBar.addSeparator();
        toolBar.add(btnRunThreads);

        // Таблица и модель данных
        String[] columnNames = {"ФИО врача", "Специализация", "Кабинет", "Часы приёма"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        doctorsTable = new JTable(model);
        sorter = new TableRowSorter<>(model);
        doctorsTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(doctorsTable);

        // Нижняя панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        specialtyFilter = new JComboBox<>(new String[]{"Все специализации"});
        doctorSearchField = new JTextField(20);
        btnSearch = new JButton("Поиск");
        btnResetSearch = new JButton("Сброс");

        searchPanel.add(new JLabel("Специализация:"));
        searchPanel.add(specialtyFilter);
        searchPanel.add(new JLabel("ФИО / Кабинет:"));
        searchPanel.add(doctorSearchField);
        searchPanel.add(btnSearch);
        searchPanel.add(btnResetSearch);

        updateTableModel();
        setupListeners();

        mainWindow.setLayout(new BorderLayout(5, 5));
        mainWindow.add(toolBar, BorderLayout.NORTH);
        mainWindow.add(scrollPane, BorderLayout.CENTER);
        mainWindow.add(searchPanel, BorderLayout.SOUTH);
        logger.info("Компоновка компонентов GUI успешно завершена");
    }

    /**
     * Настраивает слушателей событий для интерактивных компонентов интерфейса.
     */
    private void setupListeners() {
        // Добавление врача
        btnAddDoctor.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Добавить врача'");
            DoctorDialog dialog = new DoctorDialog(mainWindow);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                doctorsList.add(dialog.getDoctor());
                logger.info("В список GUI добавлен врач: {}", dialog.getDoctor().fullName());
                updateTableModel();
                JOptionPane.showMessageDialog(mainWindow,
                        "Запись о враче успешно добавлена.",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Редактирование выбранного врача
        btnEditDoctor.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Редактировать'");
            int selectedViewRow = doctorsTable.getSelectedRow();
            if (selectedViewRow != -1) {
                int modelRow = doctorsTable.convertRowIndexToModel(selectedViewRow);
                Doctor currentDoctor = doctorsList.get(modelRow);
                logger.debug("Открытие формы редактирования для врача: {}", currentDoctor.fullName());

                DoctorDialog dialog = new DoctorDialog(mainWindow, currentDoctor);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    doctorsList.set(modelRow, dialog.getDoctor());
                    logger.info("Врач на позиции {} обновлен: {}", modelRow, dialog.getDoctor().fullName());
                    updateTableModel();
                    JOptionPane.showMessageDialog(mainWindow,
                            "Данные врача успешно обновлены.",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                logger.warn("Попытка редактирования без выбора записи в таблице");
                JOptionPane.showMessageDialog(mainWindow,
                        "Пожалуйста, выберите запись в таблице для редактирования.",
                        "Предупреждение",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Удаление врача
        btnDeleteDoctor.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Удалить'");
            int selectedViewRow = doctorsTable.getSelectedRow();
            if (selectedViewRow != -1) {
                int choice = JOptionPane.showConfirmDialog(
                        mainWindow,
                        "Удалить выбранного врача из расписания?",
                        "Подтверждение",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    int modelRow = doctorsTable.convertRowIndexToModel(selectedViewRow);
                    Doctor removed = doctorsList.remove(modelRow);
                    logger.info("Удален врач из списка GUI: {}", removed != null ? removed.fullName() : "null");
                    updateTableModel();
                    JOptionPane.showMessageDialog(mainWindow,
                            "Врач удален.",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                logger.warn("Попытка удаления без выбора записи в таблице");
                JOptionPane.showMessageDialog(mainWindow,
                        "Пожалуйста, выберите запись в таблице.",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Загрузка из XML
        btnLoad.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Загрузить из XML'");
            FileDialog fd = new FileDialog(mainWindow, "Загрузить расписание из XML", FileDialog.LOAD);
            fd.setFile("*.xml");
            fd.setVisible(true);
            if (fd.getFile() != null) {
                File file = new File(fd.getDirectory(), fd.getFile());
                try {
                    Repository<Doctor> repo = new XmlDoctorRepository();
                    doctorsList.clear();
                    doctorsList.addAll(repo.load(file));
                    updateTableModel();
                    logger.info("Данные успешно загружены из XML в форму GUI: {}", file.getAbsolutePath());
                    JOptionPane.showMessageDialog(mainWindow,
                            "Данные успешно загружены из XML.",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    logger.error("Ошибка при чтении XML: {}", ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(mainWindow,
                            "Ошибка при чтении XML: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Сохранение в XML
        btnSave.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Сохранить в XML'");
            FileDialog fd = new FileDialog(mainWindow, "Сохранить расписание в XML", FileDialog.SAVE);
            fd.setFile("doctors.xml");
            fd.setVisible(true);
            if (fd.getFile() != null) {
                File file = new File(fd.getDirectory(), fd.getFile());
                try {
                    Repository<Doctor> repo = new XmlDoctorRepository();
                    repo.save(doctorsList, file);
                    logger.info("Данные GUI успешно сохранены в XML: {}", file.getAbsolutePath());
                    JOptionPane.showMessageDialog(mainWindow,
                            "Данные успешно сохранены в XML.",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    logger.error("Ошибка при сохранении XML: {}", ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(mainWindow,
                            "Ошибка при сохранении XML: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Генерация отчетов в PDF и HTML
        btnReportPdf.addActionListener(e -> generateReport("pdf"));
        btnReportHtml.addActionListener(e -> generateReport("html"));

        // Запуск трех синхронизированных потоков (Лабораторная работа № 8)
        btnRunThreads.addActionListener(e -> startParallelThreads());

        // Слушатели нижнего бара поиска
        btnSearch.addActionListener(e -> applyFilter());
        doctorSearchField.addActionListener(e -> applyFilter());

        btnResetSearch.addActionListener(e -> {
            logger.debug("Сброс фильтрации таблицы");
            specialtyFilter.setSelectedIndex(0);
            doctorSearchField.setText("");
            sorter.setRowFilter(null);
        });
    }

    /**
     * Организует параллельную работу трех синхронизированных потоков через {@link MultithreadingService}:
     * 1-й поток загружает данные из XML-файла в экранную форму;
     * 2-й поток ожидает завершения загрузки, редактирует данные и сохраняет отдельный XML-файл;
     * 3-й поток ожидает записи отредактированного XML-файла и строит HTML-отчет.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void startParallelThreads() {
        logger.info("Запуск многопоточного пайплайна (Лаб. 8/10)...");
        File sourceXml = new File("doctors.xml");
        File editedXml = new File("doctors_edited.xml");
        File reportHtml = new File("doctors_report_multithread.html");
        String templatePath = "doctors_report.jrxml";

        MultithreadingService service = new MultithreadingService();
        service.runPipelineAsync(
                sourceXml,
                editedXml,
                reportHtml,
                templatePath,
                doctorsList,
                new MultithreadingService.MultithreadCallback() {
                    @Override
                    public void onLoaded(List<Doctor> loadedDoctors) {
                        logger.debug("Callback onLoaded: обновление GUI таблицы");
                        SwingUtilities.invokeLater(ClinicRegistryGUI.this::updateTableModel);
                    }

                    @Override
                    public void onEdited(List<Doctor> editedDoctors) {
                        logger.debug("Callback onEdited: обновление GUI таблицы");
                        SwingUtilities.invokeLater(ClinicRegistryGUI.this::updateTableModel);
                    }

                    @Override
                    public void onCompleted(File reportFile) {
                        logger.info("Callback onCompleted: многопоточный пайплайн успешно завершен");
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                mainWindow,
                                "Многопоточная обработка успешно завершена!\n\n" +
                                        "1. Поток 1 загрузил исходный файл: " + sourceXml.getName() + "\n" +
                                        "2. Поток 2 обновил данные и создал: " + editedXml.getName() + "\n" +
                                        "3. Поток 3 сформировал HTML-отчет: " + reportHtml.getName(),
                                "Результат работы потоков (Лаб. 8)",
                                JOptionPane.INFORMATION_MESSAGE
                        ));
                    }

                    @Override
                    public void onError(int threadNumber, String stage, Throwable error) {
                        logger.error("Callback onError: сбой в потоке {} на этапе {}: {}", threadNumber, stage, error.getMessage(), error);
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                mainWindow,
                                "Ошибка в Потоке " + threadNumber + " (" + stage + "): " + error.getMessage(),
                                "Ошибка многопоточности",
                                JOptionPane.ERROR_MESSAGE
                        ));
                    }
                }
        );
    }

    /**
     * Выполняет фильтрацию записей таблицы по выбранной специализации
     * и тексту из поисковой строки.
     */
    private void applyFilter() {
        String selectedSpecialty = (String) specialtyFilter.getSelectedItem();
        String searchText = doctorSearchField.getText().trim();
        logger.debug("Применение фильтрации в GUI: специальность='{}', поиск='{}'", selectedSpecialty, searchText);

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (selectedSpecialty != null && !"Все специализации".equals(selectedSpecialty)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedSpecialty) + "$", 1));
        }

        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 0, 2));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    /**
     * Вызывает диалог сохранения и формирует отчет в выбранном формате (PDF или HTML).
     *
     * @param format расширение отчета ("pdf" или "html")
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void generateReport(String format) {
        logger.info("Запрос генерации отчета в формате {}", format.toUpperCase());
        if (doctorsList.isEmpty()) {
            logger.warn("Попытка генерации отчета при пустом списке врачей");
            JOptionPane.showMessageDialog(mainWindow,
                    "Таблица пуста. Добавьте или загрузите записи перед созданием отчета.",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        FileDialog fd = new FileDialog(mainWindow, "Сохранить отчет (" + format.toUpperCase() + ")", FileDialog.SAVE);
        fd.setFile("doctors_report." + format);
        fd.setVisible(true);

        if (fd.getFile() != null) {
            File reportFile = new File(fd.getDirectory(), fd.getFile());
            File tempXml = new File("doctors_temp_datasource.xml");
            try {
                new XmlDoctorRepository().save(doctorsList, tempXml);

                String templateName = "doctors_report.jrxml";
                ReportService.buildReport(
                        tempXml.getAbsolutePath(),
                        "/doctors/doctor",
                        templateName,
                        reportFile.getAbsolutePath()
                );

                logger.info("Отчет успешно сохранен в: {}", reportFile.getAbsolutePath());
                JOptionPane.showMessageDialog(mainWindow,
                        "Отчет успешно сформирован:\n" + reportFile.getAbsolutePath(),
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (JRException ex) {
                logger.error("Ошибка JasperReports: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(mainWindow,
                        "Ошибка JasperReports при формировании отчета: " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                logger.error("Ошибка при подготовке отчета: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(mainWindow,
                        "Ошибка при подготовке данных отчета: " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Синхронизирует модель таблицы и список доступных специализаций в нижнем баре
     * с текущей коллекцией объектов Doctor.
     */
    private void updateTableModel() {
        model.setRowCount(0);
        String currentSelection = (String) specialtyFilter.getSelectedItem();

        specialtyFilter.removeAllItems();
        specialtyFilter.addItem("Все специализации");

        List<String> addedSpecialties = new ArrayList<>();

        for (Doctor doc : doctorsList) {
            String hours = timeFormat.format(doc.startTime()) + " - " + timeFormat.format(doc.endTime());
            model.addRow(new Object[]{
                    doc.fullName(),
                    doc.specialty(),
                    doc.room(),
                    hours
            });

            if (!addedSpecialties.contains(doc.specialty())) {
                addedSpecialties.add(doc.specialty());
                specialtyFilter.addItem(doc.specialty());
            }
        }

        if (currentSelection != null && addedSpecialties.contains(currentSelection)) {
            specialtyFilter.setSelectedItem(currentSelection);
        }
        logger.debug("Модель таблицы обновлена. Строк: {}, Специализаций: {}", doctorsList.size(), addedSpecialties.size());
    }

    /**
     * Отображает главное окно программы.
     */
    public void show() {
        mainWindow.setVisible(true);
    }

    /**
     * Точка входа в приложение.
     * При передаче аргумента {@code --run-threads} выполняет синхронный запуск
     * трех потоков для проверки работы без открытия GUI.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        logger.info("Запуск приложения 'Регистратура поликлиники'");
        if (args.length > 0 && "--run-threads".equals(args[0])) {
            logger.info("Режим аргументов командной строки: запуск трех потоков (--run-threads)");
            System.out.println("Выполнение многопоточной обработки данных (Лабораторная работа № 8)...");
            MultithreadingService service = new MultithreadingService();
            File sourceXml = new File("doctors.xml");
            File editedXml = new File("doctors_edited.xml");
            File reportHtml = new File("doctors_report_multithread.html");
            String templatePath = "doctors_report.jrxml";
            service.runPipelineSync(sourceXml, editedXml, reportHtml, templatePath, new ArrayList<>(), null);
            System.out.println("Многопоточная обработка успешно завершена.");
            return;
        }
        SwingUtilities.invokeLater(() -> new ClinicRegistryGUI().show());
    }
}
