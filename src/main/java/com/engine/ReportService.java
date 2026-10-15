package com.engine;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;

import java.io.File;
import java.util.HashMap;

/**
 * Сервисный класс для построения отчетов в форматах PDF и HTML
 * на основе XML-файла данных и шаблона JasperReports.
 *
 * @author Ражин Захар С. (кафедра ВТ)
 * @version 1.0
 */
public class ReportService {

    /**
     * Формирует отчет в формате PDF или HTML на основе переданных параметров.
     *
     * @param datasource имя XML-файла с данными
     * @param xpath      выражение XPath для выборки узлов из XML-файла
     * @param template   имя jrxml-файла шаблона отчета
     * @param resultpath имя выходного файла отчета (.pdf или .html)
     * @throws JRException при ошибке чтения XML, компиляции шаблона или выгрузки отчета
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