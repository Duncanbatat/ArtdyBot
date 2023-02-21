package ru.artdy.service;

import ru.artdy.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
