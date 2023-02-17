package ru.artdy.service.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.artdy.controller.BotController;
import ru.artdy.service.AnswerConsumer;

import static ru.artdy.model.RabbitQueue.ANSWER_MESSAGE;

@Service
public class AnswerConsumerImpl implements AnswerConsumer {
    private final BotController botController;

    public AnswerConsumerImpl(BotController botController) {
        this.botController = botController;
    }


    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
         botController.sendAnswerMessage(sendMessage);
    }
}
