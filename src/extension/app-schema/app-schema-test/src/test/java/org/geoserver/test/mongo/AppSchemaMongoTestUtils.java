package org.geoserver.test.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

final class AppSchemaMongoTestUtils {

    private static String APP_SCHEMA_MONGO_TESTS_DB = "APP_SCHEMA_MONGO_TESTS_DB";

    private AppSchemaMongoTestUtils() {
    }

    static boolean connected() {
        try {
            return doWorkWithResult(mongoClient -> mongoClient.getDatabase(APP_SCHEMA_MONGO_TESTS_DB) != null);
        } catch (Exception exception) {
            return false;
        }
    }

    static void dropDatabase() {
        doWork(mongoClient -> mongoClient.getDatabase(APP_SCHEMA_MONGO_TESTS_DB).drop());
    }

    static String readResourceContent(String resourcePath) {
        URL resource = AppSchemaMongoTestUtils.class.getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new RuntimeException(String.format("Resource path '%s' not found.", resourcePath));
        }
        try (InputStream input = resource.openStream();
             InputStreamReader inputReader = new InputStreamReader(input);
             BufferedReader buffer = new BufferedReader(inputReader)) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch (Exception exception) {
            throw new RuntimeException(String.format("Error reading resource '%s' content.", resourcePath));
        }
    }

    static void insertJson(String json, String geometryProperty, String collectionName) {
        doWork(mongoClient -> {
            Document document = Document.parse(json);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(APP_SCHEMA_MONGO_TESTS_DB);
            MongoCollection collection = mongoDatabase.getCollection(collectionName);
            collection.insertOne(document);
            collection.createIndex(new BasicDBObject(geometryProperty, "2dsphere"));
        });
    }

    static void doWork(Consumer<MongoClient> work) {
        doWorkWithResult(mongoClient -> {
            work.accept(mongoClient);
            return null;
        });
    }

    static <T> T doWorkWithResult(Function<MongoClient, T> work) {
        String host = getConfigPropertyValue("mongo.host", "localhost");
        int port = getConfigPropertyValue("mongo.port", 27017, Integer::parseInt);
        MongoClient mongoClient = new MongoClient(host, port);
        try {
            return work.apply(mongoClient);
        } finally {
            mongoClient.close();
        }
    }

    private static String getConfigPropertyValue(String propertyName, String defaultValue) {
        return getConfigPropertyValue(propertyName, defaultValue, Function.identity());
    }

    private static <T> T getConfigPropertyValue(String propertyName, T defaultValue, Function<String, T> parser) {
        String propertyValue = System.getenv(propertyName);
        if (propertyValue == null) {
            propertyValue = System.getProperty(propertyName);
        }
        if (propertyValue == null) {
            return defaultValue;
        }
        try {
            return parser.apply(propertyValue);
        } catch (Exception exception) {
            throw new RuntimeException(String.format("Error parsing configuration property '%s' value '%s'.",
                    propertyName, propertyValue), exception);
        }
    }
}
