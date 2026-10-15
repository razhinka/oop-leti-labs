package com.engine;

import net.sf.jasperreports.engine.JRException;

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
 * Класс ClinicRegistryGUI реализует графический интерфейс
 * регистратуры поликлиники с поддержкой XML, фильтрации записей и генерации отчетов.
 *
 * @author Ражин Захар С. (кафедра ВТ)
 * @version 2.2
 */
public class ClinicRegistryGUI {

    private final JFrame mainWindow;
    private final DefaultTableModel model;
    private final JTable doctorsTable;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JButton btnAddDoctor;
    private final JButton btnDeleteDoctor;
    private final JButton btnSave;
    private final JButton btnLoad;
    private final JButton btnReportPdf;
    private final JButton btnReportHtml;

    // Компоненты нижнего бара поиска
    private final JComboBox<String> specialtyFilter;
    private final JTextField doctorSearchField;
    private final JButton btnSearch;
    private final JButton btnResetSearch;

    private final List<Doctor> doctorsList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Конструктор главного окна приложения.
     * Инициализирует компоненты интерфейса, таблицу, сортировщик и нижнюю панель поиска.
     */
    public ClinicRegistryGUI() {
        mainWindow = new JFrame("Регистратура поликлиники - Управление расписанием");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(900, 450);
        mainWindow.setLocationRelativeTo(null);

        // Верхняя панель инструментов (локальная переменная)
        JToolBar toolBar = new JToolBar("Управление записями");
        btnAddDoctor = new JButton("Добавить врача");
        btnDeleteDoctor = new JButton("Удалить");
        btnLoad = new JButton("Загрузить из XML");
        btnSave = new JButton("Сохранить в XML");
        btnReportPdf = new JButton("Отчет PDF");
        btnReportHtml = new JButton("Отчет HTML");

        toolBar.add(btnAddDoctor);
        toolBar.add(btnDeleteDoctor);
        toolBar.addSeparator();
        toolBar.add(btnLoad);
        toolBar.add(btnSave);
        toolBar.addSeparator();
        toolBar.add(btnReportPdf);
        toolBar.add(btnReportHtml);

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

        // Нижняя панель поиска (локальная переменная)
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
    }

    /**
     * Настраивает слушателей событий для кнопок панели инструментов и нижней панели поиска.
     */
    private void setupListeners() {
        // Добавление врача
        btnAddDoctor.addActionListener(e -> {
            DoctorDialog dialog = new DoctorDialog(mainWindow);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                doctorsList.add(dialog.getDoctor());
                updateTableModel();
                JOptionPane.showMessageDialog(mainWindow,
                        "Запись о враче успешно добавлена.",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Удаление врача (с учетом активного фильтра поиска)
        btnDeleteDoctor.addActionListener(e -> {
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
                    doctorsList.remove(modelRow);
                    updateTableModel();
                    JOptionPane.showMessageDialog(mainWindow,
                            "Врач удален.",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(mainWindow,
                        "Пожалуйста, выберите запись в таблице.",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Загрузка из XML
        btnLoad.addActionListener(e -> {
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
                    JOptionPane.showMessageDialog(mainWindow,
                            "Данные успешно загружены из XML.",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainWindow,
                            "Ошибка при чтении XML: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Сохранение в XML
        btnSave.addActionListener(e -> {
            FileDialog fd = new FileDialog(mainWindow, "Сохранить расписание в XML", FileDialog.SAVE);
            fd.setFile("doctors.xml");
            fd.setVisible(true);
            if (fd.getFile() != null) {
                File file = new File(fd.getDirectory(), fd.getFile());
                try {
                    Repository<Doctor> repo = new XmlDoctorRepository();
                    repo.save(doctorsList, file);
                    JOptionPane.showMessageDialog(mainWindow,
                            "Данные успешно сохранены в XML.",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
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

        // Слушатели нижнего бара поиска
        btnSearch.addActionListener(e -> applyFilter());
        doctorSearchField.addActionListener(e -> applyFilter()); // Поиск по нажатию Enter

        btnResetSearch.addActionListener(e -> {
            specialtyFilter.setSelectedIndex(0);
            doctorSearchField.setText("");
            sorter.setRowFilter(null);
        });
    }

    /**
     * Выполняет фильтрацию записей таблицы по выбранной специализации
     * и тексту из поисковой строки.
     */
    private void applyFilter() {
        String selectedSpecialty = (String) specialtyFilter.getSelectedItem();
        String searchText = doctorSearchField.getText().trim();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Фильтр по специализации (колонка с индексом 1)
        if (selectedSpecialty != null && !"Все специализации".equals(selectedSpecialty)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedSpecialty) + "$", 1));
        }

        // Фильтр по текстовому полю (ищет совпадения в ФИО или номере кабинета без учета регистра)
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
        if (doctorsList.isEmpty()) {
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
                // Сохраняем актуальные данные в XML-файл источника
                new XmlDoctorRepository().save(doctorsList, tempXml);

                String templateName = "doctors_report.jrxml";
                ReportService.buildReport(
                        tempXml.getAbsolutePath(),
                        "/doctors/doctor",
                        templateName,
                        reportFile.getAbsolutePath()
                );

                JOptionPane.showMessageDialog(mainWindow,
                        "Отчет успешно сформирован:\n" + reportFile.getAbsolutePath(),
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (JRException ex) {
                JOptionPane.showMessageDialog(mainWindow,
                        "Ошибка JasperReports при формировании отчета: " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
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
    }

    /**
     * Отображает главное окно программы.
     */
    public void show() {
        mainWindow.setVisible(true);
    }

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClinicRegistryGUI().show());
    }
}