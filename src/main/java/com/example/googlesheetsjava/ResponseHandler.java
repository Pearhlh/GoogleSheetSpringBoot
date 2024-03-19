package com.example.googlesheetsjava;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Map;
public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;

    public ResponseHandler(SilentSender sender, DBContext db) {
        this.sender = sender;
        chatStates = db.getMap(Constants.CHAT_STATES);
    }

    public void replyToStart(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(Constants.START_TEXT);
        message.setReplyMarkup(KeyboardFactory.getTools());
        sender.execute(message);
        chatStates.put(chatId, UserState.AWAITING_NAME);
    }

    private void replyToName(long chatId, Message message) {
        promptWithKeyboardForState(chatId, "Hello " + message.getText() + ". Can you choose the tool?",
                KeyboardFactory.getPizzaOrDrinkKeyboard(),
                UserState.FOOD_DRINK_SELECTION);
    }

    private void promptWithKeyboardForState(Long chatId, String text, ReplyKeyboard YesOrNo, UserState awaitingReorder) {

        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);

        sendMessage.setText(text);

        sendMessage.setReplyMarkup(YesOrNo);

        sender.execute(sendMessage);

        chatStates.put(chatId, awaitingReorder);
    }
    public void replyToButtons(long chatId, Message message) throws GeneralSecurityException, IOException, URISyntaxException {
        if (message.getText().equalsIgnoreCase("/stop")) {
            stopChat(chatId);
        }

        switch (chatStates.get(chatId)) {
            case AWAITING_NAME -> replyToName(chatId, message);
            case FOOD_DRINK_SELECTION -> replyToFoodDrinkSelection(chatId, message);
            case PIZZA_TOPPINGS -> replyToPizzaToppings(chatId, message);
            case AWAITING_CONFIRMATION -> replyToOrder(chatId, message);
            default -> unexpectedMessage(chatId);
        }
    }
    private void unexpectedMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("I did not expect that.");
        sender.execute(sendMessage);
    }
    private void replyToOrder(long chatId, Message message) throws GeneralSecurityException, IOException, URISyntaxException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(GoogleService.generateTemporaryLink(message.getText()));
       sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
       sender.execute(sendMessage);
//       stopChat(chatId);
    }
    private void replyToFoodDrinkSelection(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setText("Mời bạn điền link google sheet cần export");
        sender.execute(sendMessage);
        chatStates.put(chatId,UserState.PIZZA_TOPPINGS);
    }
    private void replyToPizzaToppings(long chatId, Message message) throws GeneralSecurityException, IOException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Choose table need to export");
        sendMessage.setReplyMarkup(KeyboardFactory.getNameSheet(message.getText()));
        sender.execute(sendMessage);
        chatStates.put(chatId,UserState.AWAITING_CONFIRMATION);
    }
    private void stopChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format("Thank you for your order. See you soon!\n /%s to order again",Constants.TEXT_BEGIN));
        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
    }
    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }
}