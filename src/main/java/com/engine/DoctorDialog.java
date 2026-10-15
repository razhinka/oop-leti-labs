package com.engine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DoctorDialog extends JDialog {
    private final JTextField nameField = new JTextField(15);
    private final JTextField specField = new JTextField(15);
    private final JTextField roomField = new JTextField(5);
    private final JTextField startField = new JTextField(5);
    private final JTextField endField = new JTextField(5);

    private boolean confirmed = false;
    private Doctor doctor;

    public DoctorDialog(JFrame owner) {
        this(owner, null);
    }

    public DoctorDialog(JFrame owner, Doctor existingDoctor) {
        super(owner, existingDoctor == null ? "Ввод данных нового врача" : "Редактирование данных врача", true);

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

        // Предзаполнение данных, если передана существующая запись
        if (existingDoctor != null) {
            nameField.setText(existingDoctor.fullName());
            specField.setText(existingDoctor.specialty());
            roomField.setText(existingDoctor.room());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            startField.setText(sdf.format(existingDoctor.startTime()));
            endField.setText(sdf.format(existingDoctor.endTime()));
        }

        JButton btnSave = new JButton("Сохранить");
        JButton btnCancel = new JButton("Отмена");

        btnSave.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String spec = specField.getText().trim();
                String room = roomField.getText().trim();

                if (name.isEmpty() || spec.isEmpty() || room.isEmpty()) {
                    throw new Exception("Все текстовые поля (ФИО, Специализация, Кабинет) обязательны для заполнения.");
                }

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                sdf.setLenient(false);
                Date startTime = sdf.parse(startField.getText().trim());
                Date endTime = sdf.parse(endField.getText().trim());

                if (!startTime.before(endTime)) {
                    throw new Exception("Время начала смены должно быть строго раньше времени её окончания.");
                }

                doctor = new Doctor(name, spec, room, startTime, endTime);
                confirmed = true;
                dispose();

            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Некорректный формат времени. Ожидается ЧЧ:ММ (например, 08:00).",
                        "Ошибка ввода",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
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