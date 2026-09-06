package org.example.block2.service;

import org.example.block2.data.PurchaseRecordData;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service for generating CSV reports.
 */
@Service
public class CsvReportService {
    /**
     * Generates CSV content from purchase records.
     *
     * @param records purchase records
     * @return CSV content
     */
    public byte[] generatePurchaseRecordsReport(
            List<PurchaseRecordData> records) {

        StringBuilder csv = new StringBuilder();

        csv.append("id;orderId;materialId;materialName;quantity\n");

        for (PurchaseRecordData record : records) {
            csv.append(record.getId()).append(";");
            csv.append(record.getOrderId()).append(";");
            csv.append(record.getMaterial().getId()).append(";");
            csv.append(escape(record.getMaterial().getName())).append(";");
            String quantity = record.getQuantity() != null
                    ? record.getQuantity().toString().replace('.', ',')
                    : "";
            csv.append(quantity).append("\n");
        }

        byte[] contentBytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] result = new byte[bom.length + contentBytes.length];

        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(contentBytes, 0, result, bom.length, contentBytes.length);

        return result;
    }

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
