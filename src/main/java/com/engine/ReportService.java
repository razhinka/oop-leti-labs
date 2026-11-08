package com.engine;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;

/**
 * Сервисный класс для генерации отчетов в форматах PDF и HTML
 * на основе XML-источника данных и шаблона JasperReports (.jrxml).
 * Все этапы формирования отчетов логируются через Apache Log4j.
 *
 * @author Ражин Захар 5312
 * @version 1.0
 */
public class ReportService {

    private static final Logger logger = LogManager.getLogger(ReportService.class);

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
        logger.info("Начало построения отчета JasperReports: template='{}', result='{}'", template, resultpath);
        logger.debug("Параметры отчета: источник='{}', xpath='{}'", datasource, xpath);

        try {
            JRDataSource ds = new JRXmlDataSource(new File(datasource), xpath);
            logger.debug("XML-источник данных успешно инициализирован");

            JasperReport report = JasperCompileManager.compileReport(template);
            logger.debug("Шаблон отчета успешно скомпилирован");

            JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(), ds);
            logger.debug("Отчет успешно заполнен данными");

            if (resultpath.toLowerCase().endsWith(".pdf")) {
                JasperExportManager.exportReportToPdfFile(print, resultpath);
                logger.info("Отчет успешно экспортирован в PDF-файл: {}", resultpath);
            } else if (resultpath.toLowerCase().endsWith(".html")) {
                JasperExportManager.exportReportToHtmlFile(print, resultpath);
                logger.info("Отчет успешно экспортирован в HTML-файл: {}", resultpath);
            } else {
                logger.error("Попытка экспорта отчета в неподдерживаемый формат: {}", resultpath);
                throw new IllegalArgumentException("Неподдерживаемый формат файла отчета: " + resultpath);
            }
        } catch (JRException e) {
            logger.error("Ошибка при генерации отчета JasperReports '{}': {}", resultpath, e.getMessage(), e);
            throw e;
        }
    }
}
