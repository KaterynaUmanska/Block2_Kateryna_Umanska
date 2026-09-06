package org.example.block2.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dto.PurchaseRecordSaveDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class JsonStreamParser {
    private final ObjectMapper objectMapper;
    private final JsonFactory jsonFactory;

    public JsonStreamParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.jsonFactory = objectMapper.getFactory();
    }

    /**
     * Parses purchase records from JSON input stream.
     *
     * @param inputStream JSON input stream
     * @param consumer consumer for each parsed purchase record
     * @throws IOException if JSON cannot be parsed
     */
    public void parse(
            InputStream inputStream,
            Consumer<PurchaseRecordSaveDto> consumer
    ) throws IOException {

        try (JsonParser parser = jsonFactory.createParser(inputStream)) {

            JsonToken firstToken = parser.nextToken();

            if (firstToken != JsonToken.START_ARRAY) {
                throw new IllegalStateException(
                        "Очікувався JSON-масив"
                );
            }

            while (true) {
                JsonToken token = parser.nextToken();

                if (token == JsonToken.END_ARRAY) {
                    break;
                }

                if (token != JsonToken.START_OBJECT) {
                    throw new IllegalStateException(
                            "Очікувався JSON-об'єкт у масиві"
                    );
                }

                PurchaseRecordSaveDto record =
                        objectMapper.readValue(
                                parser,
                                PurchaseRecordSaveDto.class
                        );

                consumer.accept(record);
            }

            if (parser.nextToken() != null) {
                throw new IllegalStateException(
                        "Після завершення JSON-масиву знайдено додаткові дані"
                );
            }
        }
    }
}
