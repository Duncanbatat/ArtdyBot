package ru.artdy.service.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.artdy.entity.RawData;
import ru.artdy.repository.RawDataRepository;
import ru.artdy.service.AnswerProducer;
import ru.artdy.service.MainService;

@Service
public class MainServiceImpl implements MainService {
    private final AnswerProducer answerProducer;
    private final RawDataRepository rawDataRepository;

    public MainServiceImpl(AnswerProducer answerProducer, RawDataRepository rawDataRepository) {
        this.answerProducer = answerProducer;
        this.rawDataRepository = rawDataRepository;
    }


    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Hello from NODE!");
        answerProducer.produceAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataRepository.save(rawData);
    }
}
