package ru.artdy.service;

import org.springframework.core.io.FileSystemResource;
import ru.artdy.entity.AppDocument;
import ru.artdy.entity.AppPhoto;
import ru.artdy.entity.BinaryContent;

public interface FileService {

    AppDocument getDocument(String id);

    AppPhoto getPhoto(String id);

    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
