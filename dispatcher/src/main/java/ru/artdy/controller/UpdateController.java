package ru.artdy.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.artdy.service.AnswerConsumer;
import ru.artdy.service.UpdateProducer;
import ru.artdy.utils.MessageUtils;

import static ru.artdy.model.RabbitQueue.*;

@Log4j
@Controller
public class UpdateController {
    private final UpdateProducer updateProducer;
    private final AnswerConsumer answerConsumer;
    private final MessageUtils messageUtils;

    public UpdateController(UpdateProducer updateProducer, AnswerConsumer answerConsumer, MessageUtils messageUtils) {
        this.updateProducer = updateProducer;
        this.answerConsumer = answerConsumer;
        this.messageUtils = messageUtils;
    }


    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null.");
            return;
        }

        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.error("Received unsupported message type: " + update + ".");
        }
    }

    private void distributeMessageByType(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            processTextMessage(update);
        } else if (message.hasDocument()) {
            processDocMessage(update);
        } else if (message.hasPhoto()) {
            processPhotoMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.GenerateSendMessageWithText(update,
                "Unsupported message type!");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        answerConsumer.consume(sendMessage);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
    }
}
