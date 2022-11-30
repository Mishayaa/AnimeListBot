package io.project.MegaCoolAnimeBot.service;

import io.project.MegaCoolAnimeBot.config.AppConfig;
import io.project.MegaCoolAnimeBot.model.UserEntity;
import io.project.MegaCoolAnimeBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    AppConfig appConfig;
    @Autowired
    private UserRepository userRepository;

    Map<Long, String> listMap = new HashMap<>();

    public TelegramBot() {
    }

    public TelegramBot(AppConfig config) {
        this.appConfig = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/list", "show your anime list"));
        listofCommands.add(new BotCommand("/remove", "remove anime by the name"));
        listofCommands.add(new BotCommand("/roll", "print random number from 1 to 100"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return appConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return appConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        new TelegramBot(appConfig);
        if (update.hasMessage() && update.getMessage().hasText()) {
            StringBuilder animeList = new StringBuilder();

            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {

                case "/start":
                    registerUser(update.getMessage(), chatId);
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/list":
                    UserEntity userEntity = userRepository.findById(update.getMessage().getChatId()).get();
                    sendMessage(chatId, userEntity.getAnimeList());
                    break;
                case "/roll":
                    int a = (int) (Math.random() * (100 - 1 + 1) + 1);
                    sendMessage(chatId, String.valueOf(a));
                    break;
                case "/help":
                    sendMessage(chatId, "To add anime to the list just print it in chat\n" +
                            "To remove anime from the list print \"remove command\"+anime name(no space between command and anime title)");
                default:
                    if (messageText.startsWith("/remove")) {
                        userEntity = userRepository.findById(update.getMessage().getChatId()).get();
                        String srToRemove = messageText.replace("/remove", "");
                        if (listMap.get(chatId).contains(srToRemove)) {
                            listMap.put(chatId, listMap.get(chatId).trim().replace(srToRemove.trim(), ""));
                            userEntity.setAnimeList(listMap.get(chatId));
                            userRepository.save(userEntity);
                        }
                    } else if (update.getMessage().getText().startsWith("/") && !update.getMessage().getText().equals("/help")) {
                        sendMessage(chatId, "Sorry, brother, this command doesn't exist :(");
                    } else {
                        userEntity = userRepository.findById(update.getMessage().getChatId()).get();
                        listMap.put(chatId, listMap.get(chatId) == null ? update.getMessage().getText() + "\n"
                                : listMap.get(chatId) + "\n" + update.getMessage().getText());
                        userEntity.setAnimeList(listMap.get(chatId));
                        userRepository.save(userEntity);
                    }


            }


        }


    }

    public void startCommandReceived(Long chatId, String name) {

        sendMessage(chatId, "Hi " + name + ", as i can see you are real man!\n" +
                "Really glad to see you here");
    }

    private void registerUser(Message msg, Long id) {

        if (userRepository.findById(msg.getChatId()).isEmpty()) {

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            UserEntity user = new UserEntity();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));


            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    public void sendMessage(long chatId, String textToSend) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
