package com.example.googlesheetsjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;

@SpringBootApplication
public class GoogleSheetsJavaApplication {

	public static void main(String[] args) throws IOException {
		ConfigurableApplicationContext ctx = SpringApplication.run(GoogleSheetsJavaApplication.class, args);
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(ctx.getBean("excelBot", AbilityBot.class));
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}
}
