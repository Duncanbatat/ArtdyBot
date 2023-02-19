package ru.artdy.service.impl;

import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.artdy.entity.AppDocument;
import ru.artdy.entity.AppPhoto;
import ru.artdy.entity.BinaryContent;
import ru.artdy.exceprions.UploadFileException;
import ru.artdy.repository.AppDocumentRepository;
import ru.artdy.repository.AppPhotoRepository;
import ru.artdy.repository.BinaryContentRepository;
import ru.artdy.service.FileService;
import ru.artdy.service.enums.LinkType;
import ru.artdy.utils.CryptoTool;

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
    @Value("${rest_service.link}")
    private String restServiceLink;

    private final AppDocumentRepository appDocumentRepository;
    private final AppPhotoRepository appPhotoRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentRepository appDocumentRepository,
                           AppPhotoRepository appPhotoRepository,
                           BinaryContentRepository binaryContentRepository, CryptoTool cryptoTool) {
        this.appDocumentRepository = appDocumentRepository;
        this.appPhotoRepository = appPhotoRepository;
        this.binaryContentRepository = binaryContentRepository;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument processDoc(Message message) {
        Document document = message.getDocument();
        String fileId = message.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            AppDocument transientAppDoc = buildTransientAppDoc(document, persistentBinaryContent);
            return appDocumentRepository.save(transientAppDoc);
        } else {
            throw new UploadFileException("Bad response from Telegram service:" + response);
        }
    }

    @Override
    public AppPhoto processPhoto(Message message) {
        int size = message.getPhoto().size();
        int photoIndex = size > 1 ? size - 1 : 0;
        PhotoSize photoSize = message.getPhoto().get(photoIndex);
        String fileId = photoSize.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            AppPhoto transientAppPhoto = buildTransientAppPhoto(photoSize, persistentBinaryContent);
            return appPhotoRepository.save(transientAppPhoto);
        } else {
            throw new UploadFileException("Bad response from Telegram service:" + response);
        }
    }

    @Override
    public String generateLink(Long id, LinkType linkType) {
        String hash = cryptoTool.hashOf(id);
        return "http://" +
                restServiceLink + "/" +
                linkType.toString() +
                "?id=" + hash;
    }

    private AppPhoto buildTransientAppPhoto(PhotoSize photoSize, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramFileId(photoSize.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(photoSize.getFileSize())
                .build();
    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        String filePath = getFilePath(response);
        byte[] fileBytes = downloadFile(filePath);
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsByteArray(fileBytes)
                .build();
        return binaryContentRepository.save(transientBinaryContent);
    }

    private static String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        String filePath = String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
        return filePath;
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
        try (InputStream is = url.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
