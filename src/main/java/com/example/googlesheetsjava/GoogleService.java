package com.example.googlesheetsjava;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class GoogleService {
    private static String getTemporaryLink(Drive driveService, File file) throws IOException {
        // Lấy thông tin về file
        String fileId = file.getId();

        // Tạo yêu cầu để lấy đường dẫn tạm thời
        PermissionList permissions = driveService.permissions().list(fileId).execute();
        List<Permission> permissionList = permissions.getPermissions();
        String permissionId = null;
        for (Permission permission : permissionList) {
            if (permission.getType().equals("anyone") && permission.getRole().equals("reader")) {
                permissionId = permission.getId();
                break;
            }
        }
        if (permissionId != null) {
            // Tạo yêu cầu để lấy đường dẫn tạm thời
            Drive.Files.Get request = driveService.files().get(fileId);
            request.setFields("webViewLink");

            // Thực hiện yêu cầu để lấy đường dẫn tạm thời
            File updatedFile = request.execute();
            String temporaryLink = updatedFile.getWebViewLink();

            return temporaryLink;
        } else {
            throw new IOException("File is not publicly accessible.");
        }
    }
    private static void updatePermission(Drive driveService,String fileId) throws IOException {
        List<Permission> permissionList = driveService.permissions().list(fileId).execute().getPermissions();

        String permissionId = null;

        // Tìm quyền truy cập public của file (nếu có)
        for (Permission permission : permissionList) {
            if ("anyone".equals(permission.getType()) && "reader".equals(permission.getRole())) {
                permissionId = permission.getId();
                break;
            }
        }

        // Nếu không tìm thấy quyền truy cập public, thêm quyền truy cập mới
        if (permissionId == null) {
            Permission newPermission = new Permission();
            newPermission.setType("anyone");
            newPermission.setRole("reader");

            // Tạo yêu cầu để thêm quyền truy cập
            driveService.permissions().create(fileId, newPermission).execute();
            System.out.println("Updated file permissions to public access.");
        } else {
            System.out.println("File already has public access.");
        }
    }
    public static String generateTemporaryLink(String nameSheet)
            throws IOException, GeneralSecurityException, URISyntaxException {
        Spreadsheet spreadsheet = SheetsServiceUtil.getSheetsService().spreadsheets().get(KeyboardFactory.displayedSpreadsheetId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        int id = 0;
        for(Sheet sheet : sheets){
            if(sheet.getProperties().getTitle().equals(nameSheet)){
                id = sheet.getProperties().getSheetId();
            }
        }
        String temporaryLinkExport = "https://docs.google.com/spreadsheets/d/" + KeyboardFactory.displayedSpreadsheetId + "/export?format=csv&gid=" + id;
        String temporaryLinkView = "https://docs.google.com/spreadsheets/d/" + KeyboardFactory.displayedSpreadsheetId + "/edit#gid=" + id;
        openHomePage(temporaryLinkExport);
        openHomePage(temporaryLinkView);
        return temporaryLinkView;
    }
    private static void openHomePage(String link) throws IOException {
        Runtime rt = Runtime.getRuntime();
        rt.exec("rundll32 url.dll,FileProtocolHandler " + link);
    }
    public static List<Sheet> getAllIdSheet(String SPREADSHEET_ID) throws IOException, GeneralSecurityException {
        Spreadsheet spreadsheet = SheetsServiceUtil.getSheetsService().spreadsheets().get(SPREADSHEET_ID).execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        return sheets;
    }

}
