package edu.uci.ics.tippers.data.mongodb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.uci.ics.tippers.common.DataFiles;
import edu.uci.ics.tippers.common.Database;
import edu.uci.ics.tippers.common.util.BigJsonReader;
import edu.uci.ics.tippers.common.util.Converter;
import edu.uci.ics.tippers.connection.mongodb.DBManager;
import edu.uci.ics.tippers.data.BaseDataUploader;
import edu.uci.ics.tippers.exception.BenchmarkException;
import edu.uci.ics.tippers.model.observation.Observation;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MongoDBDataUploader extends BaseDataUploader{

    private MongoDatabase database;
    private static String datePattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static SimpleDateFormat sdf = new SimpleDateFormat(datePattern);

    public MongoDBDataUploader(int mapping, String dataDir) {
        super(mapping, dataDir);
        database = DBManager.getInstance().getDatabase();
    }

    @Override
    public Database getDatabase() {
        return null;
    }

    @Override
    public Duration addAllData() throws BenchmarkException {
        Instant start = Instant.now();

        addInfrastructureData();
        addUserData();
        addDeviceData();
        addSensorData();
        addObservationData();

        Instant end = Instant.now();
        return Duration.between(start, end);
    }

    private void addDataToCollection(String collectionName, DataFiles dataFile) {
        MongoCollection collection = database.getCollection(collectionName);

        String values = null;
        try {
            values = new String(Files.readAllBytes(Paths.get(dataDir + dataFile.getPath())), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BenchmarkException("Error Reading Data Files");
        }
        List<Document> documents = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(values);
        jsonArray.forEach(e-> {
            Document docToInsert = Document.parse(e.toString());
            documents.add(docToInsert);
        });
        collection.insertMany(documents);
    }

    private void addObservations(String collectionName, DataFiles dataFile) {

        MongoCollection collection = database.getCollection(collectionName);

        BigJsonReader<Observation> reader = new BigJsonReader<>(dataDir + dataFile.getPath(), Observation.class);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JSONObject.class, Converter.<JSONObject>getJSONSerializer())
                .create();
        Observation obs;
        while ((obs = reader.readNext()) != null) {

            Document docToInsert = Document.parse(gson.toJson(obs, Observation.class));
            docToInsert.put("timeStamp", obs.getTimeStamp());
            System.out.println(gson.toJson(obs, Observation.class));
            collection.insertOne(docToInsert);
        }
    }

    @Override
    public void addInfrastructureData() throws BenchmarkException {
        switch (mapping) {
            case 1:
                addDataToCollection("Location", DataFiles.LOCATION);
                addDataToCollection("Region", DataFiles.REGION);
                addDataToCollection("InfrastructureType", DataFiles.INFRA_TYPE);
                addDataToCollection("Infrastructure", DataFiles.INFRA);
                break;
            default:
                throw new BenchmarkException("Error Inserting Data");
        }
    }

    @Override
    public void addUserData() throws BenchmarkException {
        switch (mapping) {
            case 1:
                addDataToCollection("Group", DataFiles.GROUP);
                addDataToCollection("User", DataFiles.USER);
                break;
            default:
                throw new BenchmarkException("Error Inserting Data");
        }
    }

    @Override
    public void addSensorData() throws BenchmarkException {
        switch (mapping) {
            case 1:
                addDataToCollection("ObservationType", DataFiles.OBS_TYPE);
                addDataToCollection("SensorType", DataFiles.SENSOR_TYPE);
                addDataToCollection("Sensor", DataFiles.SENSOR);
                break;
            default:
                throw new BenchmarkException("Error Inserting Data");
        }
    }

    @Override
    public void addDeviceData() throws BenchmarkException {
        switch (mapping) {
            case 1:
                addDataToCollection("PlatformType", DataFiles.PLT_TYPE);
                addDataToCollection("Platform", DataFiles.PLT);
                break;
            default:
                throw new BenchmarkException("Error Inserting Data");
        }
    }

    @Override
    public void addObservationData() throws BenchmarkException {
        switch (mapping) {
            case 1:
                addObservations("Observation", DataFiles.OBS);
                break;
            default:
                throw new BenchmarkException("Error Inserting Data");
        }
    }

    @Override
    public void virtualSensorData() {
        // TODO: Insert Virtual Sensor Data
    }

    @Override
    public void addSemanticObservationData() {
        // TODO: Insert Semantic Observation Data
    }
}
