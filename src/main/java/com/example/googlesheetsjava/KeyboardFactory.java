package com.example.googlesheetsjava;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {
    static String displayedSpreadsheetId;
    public static ReplyKeyboard getPizzaToppingsKeyboard() {

        KeyboardRow row = new KeyboardRow();

        row.add("Margherita");
        row.add("Anchovies");

        row.add("Pepperoni");

        return new ReplyKeyboardMarkup(List.of(row));

    }

    public static ReplyKeyboard getYesOrNo() {

        KeyboardRow row = new KeyboardRow();

        row.add("Yes");

        row.add("No");

        return new ReplyKeyboardMarkup(List.of(row));


    }

    public static ReplyKeyboard getNameSheet(String link) throws GeneralSecurityException, IOException {
        String[] parts = link.split("/");
        String SPREADSHEET_ID = parts[parts.length - 2];
        displayedSpreadsheetId = SPREADSHEET_ID;
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();
        int count = 0;

        for (Sheet sheet : GoogleService.getAllIdSheet(SPREADSHEET_ID)) {
            currentRow.add(sheet.getProperties().getTitle());
            count++;

            if (count == 3) {
                rows.add(currentRow);
                currentRow = new KeyboardRow();
                count = 0;
            }
        }

        // Kiểm tra xem có phần tử cuối cùng trong currentRow không đủ 3 phần tử
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        return new ReplyKeyboardMarkup(rows);
    }
    public static ReplyKeyboard getPizzaOrDrinkKeyboard(){

        KeyboardRow row = new KeyboardRow();

        row.add("Export Sheet");
        return new ReplyKeyboardMarkup(List.of(row));

    }

    public static ReplyKeyboard getTools() {

        KeyboardRow row = new KeyboardRow();

        row.add("Google Sheet");
        row.add("Google Docs");
        return new ReplyKeyboardMarkup(List.of(row));

    }

}
