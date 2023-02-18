package ru.artdy.service.impl;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import ru.artdy.entity.AppDocument;
import ru.artdy.entity.AppPhoto;
import ru.artdy.entity.BinaryContent;
import ru.artdy.repository.AppDocumentRepository;
import ru.artdy.repository.AppPhotoRepository;
import ru.artdy.service.FileService;

import java.io.File;
import java.io.IOException;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    private final AppDocumentRepository appDocumentRepository;
    private final AppPhotoRepository appPhotoRepository;

    public FileServiceImpl(AppDocumentRepository appDocumentRepository, AppPhotoRepository appPhotoRepository) {
        this.appDocumentRepository = appDocumentRepository;
        this.appPhotoRepository = appPhotoRepository;
    }

    @Override
    public AppDocument getDocument(String encodedId) {
        //TODO добавить дешифрование
        long id = Long.parseLong(encodedId);
        return appDocumentRepository.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String encodedId) {
        long id = Long.parseLong(encodedId);
        return appPhotoRepository.findById(id).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            //TODO добавить генерацию случайного имени файла
            File tempFile = File.createTempFile("tempFile", ".bin");
            tempFile.deleteOnExit();
            FileUtils.writeByteArrayToFile(tempFile, binaryContent.getFileAsByteArray());
            return new FileSystemResource(tempFile);
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }
}
