package com.s3hospitals.feedback.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.s3hospitals.feedback.model.RequestParams;
import com.s3hospitals.feedback.utility.ExcelGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class GoogleFormResponseService {

    private static Logger logger = LoggerFactory.getLogger(GoogleFormResponseService.class);

    private static final String APPLICATION_NAME = "Google Sheets API Java";
    private static final String SPREADSHEET_ID = "1mGas0-O0ejy-tlprK-3x9wqWBUPp1IoyuGXldqx19nw";
    private static final String RANGE = "'Form Responses 1'"; // Define the range

    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        String jsonBase64 = System.getenv("SERVICE_ACCOUNT_KEY");
        String jsonContent = "";
        if (jsonBase64 != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(jsonBase64);
            jsonContent = new String(decodedBytes, StandardCharsets.UTF_8);
        }
        InputStream serviceAccount = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccount);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        return new Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public byte[] readGoogleSheet(RequestParams requestParams) throws IOException, GeneralSecurityException {
        Sheets sheetsService = getSheetsService();
        ValueRange response = sheetsService.spreadsheets().values().get(SPREADSHEET_ID, RANGE).execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return new byte[] {};
        } else {
            String duration = requestParams.getSelectValue();
            LocalDate fromDate = LocalDate.now();
            LocalDate toDate = LocalDate.now();
            if ("all".equals(duration)) {
                fromDate = LocalDate.now().minusYears(20);
                toDate = LocalDate.now();
            } else if ("last month".equalsIgnoreCase(duration)) {
                fromDate = LocalDate.now().minusMonths(1);
                toDate = LocalDate.now();
            } else if ("last 3 months".equalsIgnoreCase(duration)) {
                fromDate = LocalDate.now().minusMonths(3);
                toDate = LocalDate.now();
            } else if ("last 6 months".equalsIgnoreCase(duration)) {
                fromDate = LocalDate.now().minusMonths(6);
                toDate = LocalDate.now();
            } else if ("last year".equalsIgnoreCase(duration)) {
                fromDate = LocalDate.now().minusYears(1);
                toDate = LocalDate.now();
            } else if ("custom date range".equalsIgnoreCase(duration)) {
                fromDate = LocalDate.parse(requestParams.getFromDate());
                toDate = LocalDate.parse(requestParams.getToDate());
            }
            List<List<Object>> filteredList = new ArrayList<>();
            for (List<Object> row : values) {
                String timeStampField = (String) row.get(0);
                if ("Timestamp".equalsIgnoreCase(timeStampField)) {
                    filteredList.add(row);
                    continue;
                }
                List<String> dateFormats = Arrays.asList("M/d/yyyy", "MM/dd/yyyy", "MM/d/yyyy", "M/dd/yyyy");
                String inputDate = timeStampField.split(" ")[0]; // Example input

                LocalDate parsedDate = parseDate(inputDate, dateFormats);
                if(parsedDate == null) {
                    continue;
                }
                if ((parsedDate.isAfter(fromDate) && parsedDate.isBefore(toDate)) || parsedDate.isEqual(fromDate) || parsedDate.isEqual(toDate)) {
                    filteredList.add(row);
                }
            }
            logger.info("{} feedbacks found for the requested range of dates.", filteredList.size());
            return ExcelGenerator.generateExcelBytes(filteredList);
        }
    }

    public static LocalDate parseDate(String date, List<String> formats) {
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(date, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }
        return null; // No valid format found
    }

}
