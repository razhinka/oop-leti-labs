package com.engine;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;

import java.io.File;
import java.util.HashMap;

/**
 * Сервисный класс для генерации отчетов в форматах PDF и HTML
 * на основе XML-источника данных и шаблона JasperReports (.jrxml).
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class ReportService {

    /**
     * Приватный конструктор для предотвращения создания экземпляров утилитного класса.
     */
    private ReportService() {
    }

    /**
     * Формирует отчет в формате PDF или HTML на основе переданных параметров.
     *
     * @param datasource имя XML-файла с исходными данными
     * @param xpath      выражение XPath для выборки узлов из XML-файла (например, "/doctors/doctor")
     * @param template   путь к файлу шаблона отчета (.jrxml)
     * @param resultpath путь к результирующему файлу отчета (.pdf или .html)
     * @throws JRException при ошибке компиляции шаблона, заполнения отчета данными или экспорта
     */
    public static void buildReport(String datasource, String xpath, String template, String resultpath) throws JRException {
        JRDataSource ds = new JRXmlDataSource(new File(datasource), xpath);
        JasperReport report = JasperCompileManager.compileReport(template);
        JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(), ds);

        if (resultpath.toLowerCase().endsWith(".pdf")) {
            JasperExportManager.exportReportToPdfFile(print, resultpath);
        } else if (resultpath.toLowerCase().endsWith(".html")) {
            JasperExportManager.exportReportToHtmlFile(print, resultpath);
        } else {
            throw new IllegalArgumentException("Неподдерживаемый формат файла отчета: " + resultpath);
        }
    }
}
