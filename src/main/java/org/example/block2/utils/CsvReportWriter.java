package org.example.block2.utils;

import org.example.block2.data.PurchaseRecordData;

import java.util.List;

/**
 * Utility for generating CSV reports.
 */
public class CsvReportWriter {
    private CsvReportWriter() {
    }

    /**
     * Creates CSV content from purchase records.
     *
     * @param records purchase records
     * @return CSV content
     */
    public static String write(List<PurchaseRecordData> records) {

        StringBuilder csv = new StringBuilder();

        csv.append("id,orderId,materialId,materialName,quantity\n");

        for (PurchaseRecordData record : records) {
            csv.append(record.getId()).append(",");
            csv.append(record.getOrderId()).append(",");
            csv.append(record.getMaterial().getId()).append(",");
            csv.append(escape(record.getMaterial().getName())).append(",");
            csv.append(record.getQuantity()).append("\n");
        }

        return csv.toString();
    }

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")) {

            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
