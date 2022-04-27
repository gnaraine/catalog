package xyz.sourbet.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import xyz.sourbet.image.ImageRepository;

@Service
public class MongoStorageService implements StorageService {

    @Autowired
    ImageRepository imageRepository;

    @Override
    public String storeAndGetIds(MultipartFile file) {

        try {
            if (file.isEmpty()) {
                System.out.println("Failed to store empty file.");
                return null;
            }
            try (InputStream inputStream = file.getInputStream()) {

                String fileName = file.getOriginalFilename();

                GridFSBucket gridFSBucket = imageRepository.getGridFsBucket();

                GridFSUploadOptions options = new GridFSUploadOptions()
                        .chunkSizeBytes(1048576)
                        .metadata(new Document("type", "image"));
                ObjectId fileId = gridFSBucket.uploadFromStream(fileName,
                        inputStream, options);
                System.out.println("The file id of the uploaded file is: " +
                        fileId.toHexString());

                return fileId.toHexString();
            }
        } catch (IOException e) {
            System.out.println("Failed to store file." + e);
        }

        return null;
    }

    public byte[] findById(String Id) {
        GridFSBucket gridFSBucket = imageRepository.getGridFsBucket();

        ObjectId fileId = new ObjectId(Id);
        try (GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(fileId)) {
            int fileLength = (int) downloadStream.getGridFSFile().getLength();

            byte[] bytesToWriteTo = new byte[fileLength];
            downloadStream.read(bytesToWriteTo);

            InputStream is = new ByteArrayInputStream(bytesToWriteTo);

            BufferedImage img = ImageIO.read(is);

            ByteArrayOutputStream bao = new ByteArrayOutputStream();

            ImageIO.write(img, "png", bao);

            if (bao.equals(null)) {
                return null;
            }
            return bao.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        GridFSBucket gridFSBucket = imageRepository.getGridFsBucket();
        ObjectId fileId = new ObjectId(id);
        gridFSBucket.delete(fileId);

    }

}
