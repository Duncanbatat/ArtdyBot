package ru.artdy.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artdy.dto.MailParams;
import ru.artdy.service.MailSenderService;

@RequestMapping("/mail")
@RestController
public class MailController {
    private final MailSenderService mailSenderService;

    public MailController(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendActivationMail(@RequestBody MailParams mailParams) {
        //TODO сделать перехват исключений с помощью аннотации ControllerAdvise
        mailSenderService.send(mailParams);
        return ResponseEntity.ok().build();
    }
}
