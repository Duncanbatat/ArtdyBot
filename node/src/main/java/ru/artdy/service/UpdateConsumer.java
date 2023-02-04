package ru.artdy.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateConsumer {

    void consumeTextMessageUpdates(Update update);

    void consumeDocMessageUpdates(Update update);

    void consumePhotoMessageUpdates(Update update);
}
