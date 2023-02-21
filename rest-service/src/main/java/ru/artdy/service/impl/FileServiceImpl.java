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
import ru.artdy.utils.CryptoTool;

import java.io.File;
import java.io.IOException;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    private final AppDocumentRepository appDocumentRepository;
    private final AppPhotoRepository appPhotoRepository;

    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentRepository appDocumentRepository,
                           AppPhotoRepository appPhotoRepository,
                           CryptoTool cryptoTool) {
        this.appDocumentRepository = appDocumentRepository;
        this.appPhotoRepository = appPhotoRepository;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument getDocument(String hash) {
        Long id = cryptoTool.idOf(hash);
        if (id == null) {
            return null;
        }
        return appDocumentRepository.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String hash) {
        Long id = cryptoTool.idOf(hash);
        if (id == null) {
            return null;
        }
        return appPhotoRepository.findById(id).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            //TODO добавить генерацию случайного имени файла
            File tempFile = File.createTempFile("tempFile", ".bin");
            tempFile.deleteOnExit();
            FileUtils.writeByteArrayToFile(tempFile, binaryContent.getFileAsByteArray());
            //TODO проверить корректность удаления временного файла
            //tempFile.delete();
            return new FileSystemResource(tempFile);
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }
}
