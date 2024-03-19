package com.example.googlesheetsjava;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
public class GoogleSheetsLiveTest {
    private static Sheets sheetsService;
    private static final String SPREADSHEET_ID = "1Gztm9o8JEPibPWEDwH54qBG5kwj51ILDOk_dxK6uTSY";

    @BeforeClass
    public static void setup() throws GeneralSecurityException, IndexOutOfBoundsException, IOException {
        sheetsService = SheetsServiceUtil.getSheetsService();
    }

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

    @Test
    public static void generateTemporaryLink()
            throws IOException, GeneralSecurityException, URISyntaxException {
        // Khởi tạo dịch vụ Google Drive
        Drive driveService = SheetsServiceUtil.getDriveService();

//        Take id
        FileList result = driveService.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
//
//        Update permission for file
        updatePermission(driveService,"1ZuiqGjmOvqw5wt9Fx-ULudcO8GCGGuo8");
        // Lấy thông tin về file
        File file = driveService.files().get("1ZuiqGjmOvqw5wt9Fx-ULudcO8GCGGuo8").execute();
//
//        // Tạo đường dẫn tạm thời
        String temporaryLink = getTemporaryLink(driveService, file);
        Desktop desk = Desktop.getDesktop();
        desk.browse(new URI(temporaryLink));
    }

    @Test
    public void googleDrive() throws GeneralSecurityException, IOException {
        Drive service = SheetsServiceUtil.getDriveService();
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

    @Test
    public void getAllIdSheet() throws IOException {
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        for (Sheet sheet : sheets) {
            System.out.println(sheet.getProperties().getSheetId() + " " + sheet.getProperties().getTitle());
        }
    }

    @Test
    public void getData() throws IOException {
        String range = "Congress Data!A2:E";
        ValueRange response = sheetsService.spreadsheets().values().get(SPREADSHEET_ID, range).execute();
        System.out.println(response.getValues());
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.println("Name, Major");
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.printf("%s, %s\n", row.get(0), row.get(4));
                System.out.println();
            }
        }
    }

    @Test
    public void setColumnsAllOneValue() throws IOException {
        ValueRange body = new ValueRange()
                .setValues(IntStream.range(0, 30).mapToObj((i) -> {
                    List<Object> list = new ArrayList<>();
                    list.add(i);
                    return list;
                }).collect(Collectors.toList()));
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, "E2", body)
                .setValueInputOption("RAW")
                .execute();
    }

    @Test
    public void whenWriteSheet_thenReadSheetOk() throws IOException {
        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList("Tong Viet Hoang", "Male"),
                        Arrays.asList("Dau Quang Hieu", "Male"),
                        Arrays.asList("Le Gia Huy", "Male"),
                        Arrays.asList("Le Thi Thu Ha", "Female"),
                        Arrays.asList("Tran Thi Hoa", "Female"),
                        Arrays.asList("Le Gia Hung", "Male"),
                        Arrays.asList("Le Ngoc Trung", "Male")
                ));
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, "A2", body)
                .setValueInputOption("RAW")
                .execute();
    }

    @Test
    public void writingtoMultipleRanges() throws IOException {
        List<ValueRange> data = new ArrayList<>();
        data.add(new ValueRange()
                .setRange("A9")
                .setValues(List.of(
                        Arrays.asList("Le Gia Huy", "Male Update"))));
        data.add(new ValueRange()
                .setRange("E9")
                .setValues(List.of(
                        Arrays.asList("Math", "PTIT Updatw"))));

        BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
//                User_entered se cho nguoi dung nhap theo dinh dang roi google sheet se tinh toan theo (EX:Sum(A1:A10))
                .setValueInputOption("USER_ENTERED")
                .setData(data);

        BatchUpdateValuesResponse batchResult = sheetsService.spreadsheets().values()
                .batchUpdate(SPREADSHEET_ID, batchBody)
                .execute();
    }

    @Test
    public void appendingDataAfterATable() throws IOException {
        ValueRange appendBody = new ValueRange()
                .setValues(List.of(
                        Arrays.asList("Total", "=SUM(E1:31)")));
        AppendValuesResponse appendResult = sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, "E32", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
    }

    @Test
    public void readingValuesfromaSheet() throws IOException {
        List<String> ranges = Arrays.asList("A1:E1", "E4");
        BatchGetValuesResponse readResult = sheetsService.spreadsheets().values()
                .batchGet(SPREADSHEET_ID)
                .setRanges(ranges)
                .execute();
        System.out.println(readResult.getValueRanges());
    }

    @Test
    public void whenUpdateSpreadSheetTitle_thenOk() throws IOException {
        UpdateSpreadsheetPropertiesRequest updateSpreadSheetRequest
                = new UpdateSpreadsheetPropertiesRequest().setFields("*")
                .setProperties(new SpreadsheetProperties().setTitle("Congress Data"));

        CopyPasteRequest copyRequest = new CopyPasteRequest()
                .setSource(new GridRange().setSheetId(0)
                        .setStartColumnIndex(0).setEndColumnIndex(6)
                        .setStartRowIndex(0).setEndRowIndex(33))
                .setDestination(new GridRange().setSheetId(1189798523)
                        .setStartColumnIndex(0).setEndColumnIndex(6)
                        .setStartRowIndex(0).setEndRowIndex(33))
                .setPasteType("PASTE_VALUES");

        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setCopyPaste(copyRequest));
        requests.add(new Request()
                .setUpdateSpreadsheetProperties(updateSpreadSheetRequest));

        BatchUpdateSpreadsheetRequest body
                = new BatchUpdateSpreadsheetRequest().setRequests(requests);

        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
    }
}
