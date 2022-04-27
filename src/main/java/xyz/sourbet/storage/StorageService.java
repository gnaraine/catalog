package xyz.sourbet.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String storeAndGetIds(MultipartFile file);

    void delete(String id);

    byte[] findById(String id);

}
