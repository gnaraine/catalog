package xyz.sourbet.image;

import java.util.Base64;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import xyz.sourbet.storage.StorageService;

@Repository
public class ImageRepository {

    @Autowired
    StorageService storageService;

    @Autowired
    private MongoDatabaseFactory mongoDatabaseFactory;

    @Bean
    public GridFSBucket getGridFsBucket() {
        MongoDatabase database = mongoDatabaseFactory.getMongoDatabase();
        return GridFSBuckets.create(database);
    }

    public String getImage(String Id) {

        byte[] image = storageService.findById(Id.toString());
        
        return Base64.getEncoder().encodeToString(image);
    }

}
