package com.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация репозитория для сущности Doctor с использованием формата XML.
 * Для чтения и формирования структуры XML-документа применяется W3C DOM-парсер
 * ({@link DocumentBuilder}, {@link Document}, {@link Transformer}).
 * Все операции протоколируются с помощью Log4j.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class XmlDoctorRepository implements Repository<Doctor> {

    private static final Logger logger = LogManager.getLogger(XmlDoctorRepository.class);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Создает новый экземпляр репозитория XML-данных.
     */
    public XmlDoctorRepository() {
    }

    /**
     * Сериализует список врачей в XML-файл с отступами.
     *
     * @param items список врачей для сохранения
     * @param file  целевой XML-файл
     * @throws Exception при ошибке создания DOM-дерева или трансформации в файл
     */
    @Override
    public void save(List<Doctor> items, File file) throws Exception {
        logger.info("Начало сохранения списка врачей в XML-файл: {} (всего записей: {})",
                file != null ? file.getAbsolutePath() : "null", items != null ? items.size() : 0);

        try {
            // Создание фабрики и DOM-парсера
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.newDocument();

            Element root = doc.createElement("doctors");
            doc.appendChild(root);

            if (items != null) {
                for (Doctor doctor : items) {
                    Element docElement = doc.createElement("doctor");

                    docElement.setAttribute("fullName", doctor.fullName());
                    docElement.setAttribute("specialty", doctor.specialty());
                    docElement.setAttribute("room", doctor.room());
                    docElement.setAttribute("startTime", timeFormat.format(doctor.startTime()));
                    docElement.setAttribute("endTime", timeFormat.format(doctor.endTime()));

                    root.appendChild(docElement);
                    logger.debug("Сформирован XML-элемент: врач '{}', кабинет '{}'", doctor.fullName(), doctor.room());
                }
            }

            // Создание преобразователя документа для записи в файл
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            // Настройка форматирования (отступы) для удобного чтения файла
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // Запись документа в файл
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

            logger.info("XML-файл успешно сохранен: {}", file != null ? file.getAbsolutePath() : "null");
        } catch (Exception e) {
            logger.error("Ошибка при сохранении данных в XML-файл {}: {}",
                    file != null ? file.getAbsolutePath() : "null", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Загружает список врачей из XML-файла с разбором DOM-структуры.
     *
     * @param file исходный XML-файл
     * @return коллекция десериализованных объектов Doctor
     * @throws Exception при ошибке разбора XML или парсинга формата времени
     */
    @Override
    public List<Doctor> load(File file) throws Exception {
        logger.info("Начало загрузки списка врачей из XML-файла: {}", file != null ? file.getAbsolutePath() : "null");
        List<Doctor> result = new ArrayList<>();
        if (file == null || !file.exists()) {
            logger.warn("XML-файл не существует или не указан: {}", file != null ? file.getAbsolutePath() : "null");
            return result;
        }

        try {
            // Создание парсера и чтение документа из файла
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nlDoctors = doc.getElementsByTagName("doctor");
            logger.debug("В XML-файле обнаружено узлов <doctor>: {}", nlDoctors.getLength());

            // Цикл просмотра списка элементов
            for (int i = 0; i < nlDoctors.getLength(); i++) {
                Node elem = nlDoctors.item(i);
                NamedNodeMap attrs = elem.getAttributes();
                // Чтение значений атрибутов по их именам
                String fullName = attrs.getNamedItem("fullName").getNodeValue();
                String specialty = attrs.getNamedItem("specialty").getNodeValue();
                String room = attrs.getNamedItem("room").getNodeValue();
                String startTimeStr = attrs.getNamedItem("startTime").getNodeValue();
                String endTimeStr = attrs.getNamedItem("endTime").getNodeValue();

                result.add(new Doctor(
                        fullName,
                        specialty,
                        room,
                        timeFormat.parse(startTimeStr),
                        timeFormat.parse(endTimeStr)
                ));
                logger.debug("Прочитан врач из XML [{}]: {} ({})", i + 1, fullName, specialty);
            }

            logger.info("Загрузка XML завершена успешно. Загружено врачей: {}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Ошибка при загрузке данных из XML-файла {}: {}", file.getAbsolutePath(), e.getMessage(), e);
            throw e;
        }
    }
}
