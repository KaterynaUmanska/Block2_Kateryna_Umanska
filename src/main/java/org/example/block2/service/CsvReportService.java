package org.example.block2.service;

import org.example.block2.data.PurchaseRecordData;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;

/**
 * Service for generating CSV reports.
 */
@Service
public class CsvReportService {
    /**
     * Writes CSV header.
     *
     * @param writer output writer
     * @throws IOException if writing fails
     */
    public void writeHeader(Writer writer) throws IOException {
        writer.write("\uFEFF");
        writer.write("ID,Order ID,Material,Quantity\n");
    }

    /**
     * Writes one purchase record to CSV.
     *
     * @param writer output writer
     * @param record purchase record
     * @throws IOException if writing fails
     */
    public void writeRecord(
            Writer writer,
            PurchaseRecordData record
    ) throws IOException {

        writer.write(String.format(
                "%d,%s,%s,%.2f\n",
                record.getId(),
                record.getOrderId(),
                record.getMaterial() != null
                        ? escape(record.getMaterial().getName())
                        : "",
                record.getQuantity()
        ));
    }

    /**
     * Escapes CSV values containing special characters.
     *
     * @param value value to escape
     * @return escaped value
     */
    private String escape(String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(";")
                || value.contains("\"")
                || value.contains("\n")) {

            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
