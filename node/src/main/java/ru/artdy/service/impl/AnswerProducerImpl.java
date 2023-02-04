package ru.artdy.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.artdy.service.AnswerProducer;

import static ru.artdy.model.RabbitQueue.ANSWER_MESSAGE;

@Service
public class AnswerProducerImpl implements AnswerProducer {
    private final RabbitTemplate rabbitTemplate;

    public AnswerProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceAnswer(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage);
    }
}
