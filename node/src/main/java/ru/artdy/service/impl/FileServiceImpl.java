package ru.artdy.service.impl;

import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.artdy.entity.AppDocument;
import ru.artdy.entity.BinaryContent;
import ru.artdy.exceprions.UploadFileException;
import ru.artdy.repository.AppDocumentRepository;
import ru.artdy.repository.BinaryContentRepository;
import ru.artdy.service.FileService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    @Value("${bot.token}")
    private String token;
    @Value("${service.file_info_uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private final AppDocumentRepository appDocumentRepository;
    private final BinaryContentRepository binaryContentRepository;

    public FileServiceImpl(AppDocumentRepository appDocumentRepository, BinaryContentRepository binaryContentRepository) {
        this.appDocumentRepository = appDocumentRepository;
        this.binaryContentRepository = binaryContentRepository;
    }

    @Override
    public AppDocument processDoc(Message message) {
        String fileId = message.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            String filePath = String.valueOf(jsonObject.getJSONObject("result").getString("file_path"));
            byte[] fileBytes = downloadFile(filePath);
            BinaryContent transientBinaryContent = BinaryContent.builder()
                    .fileAsByteArray(fileBytes)
                    .build();
            BinaryContent persistentBinaryContent = binaryContentRepository.save(transientBinaryContent);
            Document document = message.getDocument();
            AppDocument appDocument = buildTransientAppDoc(document, persistentBinaryContent);
            return appDocumentRepository.save(appDocument);
        } else {
            throw new UploadFileException("Bad response from Telegram service:" + response);
        }
    }

    private AppDocument buildTransientAppDoc(Document document, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(document.getFileId())
                .docName(document.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(document.getMimeType())
                .fileSize(document.getFileSize())
                .build();
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(new HttpHeaders());

         return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token,
                fileId
        );
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL url;
        try {
            url = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }

        //TODO оптимизация
        try(InputStream is = url.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}