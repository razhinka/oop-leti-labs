package com.engine;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Класс ClinicRegistryGUI реализует графический интерфейс
 * регистратуры поликлиники с обработкой событий кнопок.
 *
 * @author Ражин Захар С. (кафедра ВТ)
 * @version 1.9
 */
public class ClinicRegistryGUI {

    private final JFrame mainWindow;
    private final DefaultTableModel model;
    private final JTable doctorsTable;
    private JScrollPane scrollPane;

    private JToolBar toolBar;
    private final JButton btnAddDoctor;
    private final JButton btnDeleteDoctor;

    private final List<Doctor> doctorsList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public ClinicRegistryGUI() {
        mainWindow = new JFrame("Регистратура поликлиники - Управление расписанием");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(800, 400);
        mainWindow.setLocationRelativeTo(null);

        toolBar = new JToolBar("Управление записями");
        btnAddDoctor = new JButton("Добавить врача");
        btnDeleteDoctor = new JButton("Удалить");

        toolBar.add(btnAddDoctor);
        toolBar.add(btnDeleteDoctor);

        String[] columnNames = {"ФИО врача", "Специализация", "Кабинет", "Часы приёма"};

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        doctorsTable = new JTable(model);
        scrollPane = new JScrollPane(doctorsTable);

        updateTableModel();
        setupListeners();

        mainWindow.setLayout(new BorderLayout(5, 5));
        mainWindow.add(toolBar, BorderLayout.NORTH);
        mainWindow.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Метод настройки обработчиков событий (слушателей) для элементов управления.
     */
    private void setupListeners() {
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

        btnDeleteDoctor.addActionListener(e -> {
            int selectedRow = doctorsTable.getSelectedRow();
            if (selectedRow != -1) {
                int choice = JOptionPane.showConfirmDialog(
                        mainWindow,
                        "Удалить выбранного врача из расписания?",
                        "Подтверждение",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    doctorsList.remove(selectedRow);
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
    }

    private void updateTableModel() {
        model.setRowCount(0);
        for (Doctor doc : doctorsList) {
            String hours = timeFormat.format(doc.startTime()) + " - " + timeFormat.format(doc.endTime());
            model.addRow(new Object[]{
                    doc.fullName(),
                    doc.specialty(),
                    doc.room(),
                    hours
            });
        }
    }

    public void show() {
        mainWindow.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClinicRegistryGUI().show());
    }

    /**
     * Внутренний класс для отображения формы ввода данных.
     */
    private class DoctorDialog extends JDialog {
        private final JTextField nameField = new JTextField(15);
        private final JTextField specField = new JTextField(15);
        private final JTextField roomField = new JTextField(5);
        private final JTextField startField = new JTextField(5);
        private final JTextField endField = new JTextField(5);

        private boolean confirmed = false;
        private Doctor doctor;

        public DoctorDialog(JFrame owner) {
            super(owner, "Ввод данных нового врача", true);

            JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            panel.add(new JLabel("ФИО:"));
            panel.add(nameField);
            panel.add(new JLabel("Специализация:"));
            panel.add(specField);
            panel.add(new JLabel("Кабинет:"));
            panel.add(roomField);
            panel.add(new JLabel("Начало смены (HH:mm):"));
            panel.add(startField);
            panel.add(new JLabel("Конец смены (HH:mm):"));
            panel.add(endField);

            JButton btnSave = new JButton("Сохранить");
            JButton btnCancel = new JButton("Отмена");

            btnSave.addActionListener(e -> {
                try {
                    String name = nameField.getText().trim();
                    String spec = specField.getText().trim();
                    String room = roomField.getText().trim();

                    if (name.isEmpty() || spec.isEmpty() || room.isEmpty()) {
                        throw new InvalidDataException("Все текстовые поля (ФИО, Специализация, Кабинет) обязательны для заполнения.");
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    sdf.setLenient(false);
                    Date startTime = sdf.parse(startField.getText().trim());
                    Date endTime = sdf.parse(endField.getText().trim());

                    if (!startTime.before(endTime)) {
                        throw new InvalidDataException("Время начала смены должно быть строго раньше времени её окончания.");
                    }

                    doctor = new Doctor(name, spec, room, startTime, endTime);
                    confirmed = true;
                    dispose();

                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Некорректный формат времени. Ожидается ЧЧ:ММ (например, 08:00).",
                            "Ошибка ввода",
                            JOptionPane.ERROR_MESSAGE);
                } catch (InvalidDataException ex) {
                    JOptionPane.showMessageDialog(this,
                            ex.getMessage(),
                            "Ошибка данных",
                            JOptionPane.WARNING_MESSAGE);
                }
            });

            btnCancel.addActionListener(e -> dispose());

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.add(btnSave);
            buttonsPanel.add(btnCancel);

            add(panel, BorderLayout.CENTER);
            add(buttonsPanel, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(owner);
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public Doctor getDoctor() {
            return doctor;
        }
    }
}