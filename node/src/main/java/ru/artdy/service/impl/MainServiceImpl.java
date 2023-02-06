package ru.artdy.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.artdy.entity.AppUser;
import ru.artdy.entity.RawData;
import ru.artdy.entity.enums.UserState;
import ru.artdy.repository.AppUserRepository;
import ru.artdy.repository.RawDataRepository;
import ru.artdy.service.AnswerProducer;
import ru.artdy.service.MainService;

import static ru.artdy.service.enums.ServiceCommands.*;
import static ru.artdy.entity.enums.UserState.*;

@Log4j
@Service
public class MainServiceImpl implements MainService {
    private final AnswerProducer answerProducer;
    private final RawDataRepository rawDataRepository;
    private final AppUserRepository appUserRepository;

    public MainServiceImpl(AnswerProducer answerProducer,
                           RawDataRepository rawDataRepository,
                           AppUserRepository appUserRepository) {
        this.answerProducer = answerProducer;
        this.rawDataRepository = rawDataRepository;
        this.appUserRepository = appUserRepository;
    }


    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getUserState();
        String text = update.getMessage().getText();
        String output = "";

        if (CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = serviceCommandProcess(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO добавить проверку емейла
        } else {
            log.error("Unknown user state: " + userState);
            output = "Unknown user state: " + userState + ". Enter /cancel and try again!";
        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSaveContent(chatId, appUser)) {
            //TODO реализовать логику загрузки файлов
            return;
        }
        final String answer = "You document successfully added! Download link: https://google.com";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowToSaveContent(Long chatId, AppUser appUser) {
        if (!appUser.getIsActive()) {
            final String answer = "Register or activate your account for uploading content.";
            sendAnswer(answer, chatId);
            return true;
        } else if (!BASIC_STATE.equals(appUser.getUserState())) {
            final String answer = "Cancel current command for uploading content.";
            sendAnswer(answer, chatId);
            return true;
        }
        return false;
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSaveContent(chatId, appUser )) {
            //TODO реализовать логику загрузки фото
            return;
        }
        final String answer = "You photo successfully added! Download link: https://google.com";
        sendAnswer(answer, chatId);
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        answerProducer.produceAnswer(sendMessage);
    }

    private String serviceCommandProcess(AppUser appUser, String text) {
        if (REGISTRATION.equals(text)) {
            //TODO добавить регистрацию
            return "Registration is not supported yet!";
        } else if (HELP.equals(text)) {
            return help();
        } else if (START.equals(text)) {
            return "Hi! Type /help for available commands list!";
        } else {
            return "Unsupported command! Type /help for available commands list!";
        }
    }

    private String help() {
        return null;
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserRepository.save(appUser);
        return "Command cancelled.";
    }

    private AppUser findOrSaveAppUser(Update update) {
        Message updateMessage = update.getMessage();
        User telegramUser = updateMessage.getFrom();

        AppUser persistentAppUser = appUserRepository.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO доделать регистрацию
                    .isActive(true)
                    .userState(UserState.BASIC_STATE)
                    .build();
            return appUserRepository.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataRepository.save(rawData);
    }
}
