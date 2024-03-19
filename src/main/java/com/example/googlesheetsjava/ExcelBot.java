package com.example.googlesheetsjava;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Component
public class ExcelBot extends AbilityBot {
    private static String botToken;
    private final ResponseHandler responseHandler;
    public ExcelBot(Environment env) {
        super(env.getProperty("bot_token"), "HCIExcelBot");
        responseHandler = new ResponseHandler(silent, db);
    }

    @Override
    public long creatorId() {
        return 1L;
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name(Constants.TEXT_BEGIN)
                .info(Constants.START_DESCRIPTION)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> responseHandler.replyToStart(ctx.chatId()))
                .build();
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action = (abilityBot, upd) -> {
            try {
                responseHandler.replyToButtons(getChatId(upd), upd.getMessage());
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        };
        return Reply.of(action, Flag.TEXT, upd -> responseHandler.userIsActive(getChatId(upd)));
    }
}
