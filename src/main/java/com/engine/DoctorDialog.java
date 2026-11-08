package com.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;

/**
 * Модальное диалоговое окно для добавления новой записи о враче
 * или редактирования параметров существующего специалиста.
 * Содержит поля ввода ФИО, специальности, кабинета и временного интервала смены.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class DoctorDialog extends JDialog {

    private static final Logger logger = LogManager.getLogger(DoctorDialog.class);

    /** Текстовое поле для ввода ФИО врача. */
    private final JTextField nameField = new JTextField(15);
    /** Текстовое поле для ввода специализации. */
    private final JTextField specField = new JTextField(15);
    /** Текстовое поле для ввода номера кабинета. */
    private final JTextField roomField = new JTextField(5);
    /** Текстовое поле для ввода времени начала смены. */
    private final JTextField startField = new JTextField(5);
    /** Текстовое поле для ввода времени окончания смены. */
    private final JTextField endField = new JTextField(5);

    /** Флаг подтверждения сохранения формы. */
    private boolean confirmed = false;
    /** Созданный или отредактированный объект Doctor. */
    private Doctor doctor;

    /**
     * Конструктор для создания новой записи о враче.
     *
     * @param owner родительское окно JFrame
     */
    public DoctorDialog(JFrame owner) {
        this(owner, null);
    }

    /**
     * Конструктор для добавления или редактирования врача.
     *
     * @param owner          родительское окно JFrame
     * @param existingDoctor существующая запись о враче для предзаполнения полей (или null для новой записи)
     */
    public DoctorDialog(JFrame owner, Doctor existingDoctor) {
        super(owner, existingDoctor == null ? "Ввод данных нового врача" : "Редактирование данных врача", true);
        logger.debug("Открытие диалогового окна врача: режим={}", existingDoctor == null ? "создание" : "редактирование");

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
            logger.debug("Пользователь нажал 'Сохранить' в диалоге врача");
            try {
                doctor = DoctorValidator.validate(
                        nameField.getText(),
                        specField.getText(),
                        roomField.getText(),
                        startField.getText(),
                        endField.getText()
                );
                confirmed = true;
                logger.debug("Данные врача в диалоге успешно проверены: {}", doctor.fullName());
                dispose();
            } catch (InvalidDataException ex) {
                logger.warn("Ошибка валидации в диалоговом окне: {}", ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Ошибка данных",
                        JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                logger.error("Непредвиденная ошибка в диалоговом окне: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(this,
                        "Непредвиденная ошибка: " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> {
            logger.debug("Пользователь отменил диалог ввода врача");
            dispose();
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnCancel);

        add(panel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Проверяет, была ли форма подтверждена пользователем нажатием кнопки "Сохранить".
     *
     * @return true, если данные валидны и сохранены; false в противном случае
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Возвращает созданный или отредактированный объект Doctor.
     *
     * @return сформированный объект Doctor или null, если форма была отменена
     */
    public Doctor getDoctor() {
        return doctor;
    }
}
