package ru.artdy.service;


import org.telegram.telegrambots.meta.api.objects.Message;
import ru.artdy.entity.AppDocument;

public interface FileService {
    AppDocument processDoc(Message message);
}
