package com.engine;

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
 * Реализация репозитория для сущности Doctor в формате XML.
 */
public class XmlDoctorRepository implements Repository<Doctor> {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public void save(List<Doctor> items, File file) throws Exception {
        // Создание фабрики и DOM-парсера
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.newDocument();

        Element root = doc.createElement("doctors");
        doc.appendChild(root);

        for (Doctor doctor : items) {
            Element docElement = doc.createElement("doctor");

            docElement.setAttribute("fullName", doctor.fullName());
            docElement.setAttribute("specialty", doctor.specialty());
            docElement.setAttribute("room", doctor.room());
            docElement.setAttribute("startTime", timeFormat.format(doctor.startTime()));
            docElement.setAttribute("endTime", timeFormat.format(doctor.endTime()));

            root.appendChild(docElement);
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
    }

    @Override
    public List<Doctor> load(File file) throws Exception {
        List<Doctor> result = new ArrayList<>();
        if (!file.exists()) {
            return result;
        }
        // Создание парсера и чтение документа из файла
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nlDoctors = doc.getElementsByTagName("doctor");

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
        }

        return result;
    }
}