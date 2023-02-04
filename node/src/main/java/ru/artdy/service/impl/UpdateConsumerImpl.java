package ru.artdy.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.artdy.service.AnswerProducer;
import ru.artdy.service.UpdateConsumer;

import static ru.artdy.model.RabbitQueue.*;

@Log4j
@Service
public class UpdateConsumerImpl implements UpdateConsumer {
    private final AnswerProducer answerProducer;

    public UpdateConsumerImpl(AnswerProducer answerProducer) {
        this.answerProducer = answerProducer;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.debug("NODE: Text message is received.");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Hello from NODE!");
        answerProducer.produceAnswer(sendMessage);
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocMessageUpdates(Update update) {
        log.debug("NODE: Document message is received.");
    }

    @Override
    @RabbitListener(queues = PHOTO_MESSAGE_UPDATE)
    public void consumePhotoMessageUpdates(Update update) {
        log.debug("NODE: Photo message is received.");
    }
}
