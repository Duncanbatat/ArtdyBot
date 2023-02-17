package ru.artdy.service;


import org.telegram.telegrambots.meta.api.objects.Message;
import ru.artdy.entity.AppDocument;
import ru.artdy.entity.AppPhoto;

public interface FileService {
    AppDocument processDoc(Message message);
    AppPhoto processPhoto(Message message);
}
