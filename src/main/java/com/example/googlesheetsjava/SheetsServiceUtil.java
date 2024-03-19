package com.example.googlesheetsjava;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SheetsServiceUtil {
    private static Sheets sheetsService;
    private static final String APPLICATION_NAME = "Google Sheets Example";

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = GoogleAuthorizeUtil.authorize();
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    public static Drive getDriveService() throws GeneralSecurityException, IOException {
        Credential credential = GoogleAuthorizeUtil.authorize();
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT,JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
