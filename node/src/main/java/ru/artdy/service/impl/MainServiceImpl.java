package ru.artdy.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.artdy.entity.AppDocument;
import ru.artdy.entity.AppPhoto;
import ru.artdy.entity.AppUser;
import ru.artdy.entity.RawData;
import ru.artdy.entity.enums.UserState;
import ru.artdy.exceprions.UploadFileException;
import ru.artdy.repository.AppUserRepository;
import ru.artdy.repository.RawDataRepository;
import ru.artdy.service.AnswerProducer;
import ru.artdy.service.FileService;
import ru.artdy.service.MainService;
import ru.artdy.service.enums.ServiceCommand;

import static ru.artdy.service.enums.ServiceCommand.*;
import static ru.artdy.entity.enums.UserState.*;

@Log4j
@Service
public class MainServiceImpl implements MainService {
    private final AnswerProducer answerProducer;
    private final FileService fileService;
    private final RawDataRepository rawDataRepository;
    private final AppUserRepository appUserRepository;

    public MainServiceImpl(AnswerProducer answerProducer,
                           FileService fileService, RawDataRepository rawDataRepository,
                           AppUserRepository appUserRepository) {
        this.answerProducer = answerProducer;
        this.fileService = fileService;
        this.rawDataRepository = rawDataRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getUserState();
        String textMessage = update.getMessage().getText();
        String output = "";

        ServiceCommand serviceCommand = fromValues(textMessage);
        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = serviceCommandProcess(serviceCommand);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO добавить проверку емейла
        } else {
            log.error("Unknown user state: " + userState);
            output = "Unknown user state: " + userState + ". Enter /cancel and try again!";
        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserRepository.save(appUser);
        return "Command cancelled.";
    }

    private String serviceCommandProcess(ServiceCommand serviceCommand) {
        if (REGISTRATION.equals(serviceCommand)) {
            //TODO добавить регистрацию
            return "Registration is not supported yet!";
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            return "Hi! Type /help for available commands list!";
        } else {
            return "Unsupported command! Type /help for available commands list!";
        }
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSaveContent(chatId, appUser)) {
            return;
        }
        try {
            AppDocument appDocument = fileService.processDoc(update.getMessage());
            //TODO добавить генерацию ссылки для скачивания
            String textAnswer = "Document successfully uploaded! " +
                    "Your downloading link: " +
                    "https://www.google.com";
            sendAnswer(textAnswer, chatId);
        } catch (UploadFileException e) {
            log.error(e);
            String textAnswer = "File uploading failed! Try again later.";
            sendAnswer(textAnswer, chatId);
        }
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
            return;
        }

        try {
            AppPhoto appPhoto = fileService.processPhoto(update.getMessage());
            //TODO добавить генерацию ссылки для скачивания
            String textAnswer = "Photo successfully uploaded! " +
                    "Your downloading link: " +
                    "https://www.google.com";
            sendAnswer(textAnswer, chatId);
        } catch (UploadFileException e) {
            log.error(e);
            String textAnswer = "Photo uploading failed! Try again later.";
            sendAnswer(textAnswer, chatId);
        }
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        answerProducer.produceAnswer(sendMessage);
    }

    private String help() {
        return "Command list:\n" +
                "• \\start\n" +
                "• \\help\n" +
                "• \\registration\n";
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
