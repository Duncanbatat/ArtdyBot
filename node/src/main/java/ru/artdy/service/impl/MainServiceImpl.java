package ru.artdy.service.impl;

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

        Message updateMessage = update.getMessage();
        User telegramUser = updateMessage.getFrom();
        AppUser appUser = findOrSaveAppUser(telegramUser);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(updateMessage.getChatId());
        sendMessage.setText("Hello from NODE!");
        answerProducer.produceAnswer(sendMessage);
    }

    private AppUser findOrSaveAppUser(User telegramUser) {
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
