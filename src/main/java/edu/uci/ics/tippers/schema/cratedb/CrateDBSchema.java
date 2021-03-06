package edu.uci.ics.tippers.schema.cratedb;

import com.ibatis.common.jdbc.ScriptRunner;
import edu.uci.ics.tippers.common.Database;
import edu.uci.ics.tippers.connection.cratedb.CrateDBConnectionManager;
import edu.uci.ics.tippers.exception.BenchmarkException;
import edu.uci.ics.tippers.schema.BaseSchema;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

public class CrateDBSchema extends BaseSchema{

    private Connection connection;
    private String CREATE_FORMAT = "cratedb/schema/mapping%s/create.sql";
    private String DROP_FORMAT = "cratedb/schema/mapping%s/drop.sql";

    public CrateDBSchema(int mapping, String dataDir) {
        super(mapping, dataDir);
        connection = CrateDBConnectionManager.getInstance().getConnection();
    }

    @Override
    public Database getDatabase() {
        return Database.CRATEDB;
    }

    private void runScript(String fileName) throws BenchmarkException {
        ScriptRunner sr = new ScriptRunner(connection, false, true);
        sr.setLogWriter(null);
        Reader reader;
        try {
            InputStream inputStream = CrateDBSchema.class.getClassLoader().getResourceAsStream(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            sr.runScript(reader);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new BenchmarkException("Error Running SQL script");
        }
    }


    @Override
    public void createSchema() throws BenchmarkException {
        switch (mapping) {
            case 1:
            case 2:
                runScript(String.format(CREATE_FORMAT, mapping));
                break;
            default:
                throw new BenchmarkException("No Such Mapping");
        }
    }

    @Override
    public void dropSchema() throws BenchmarkException {
        switch (mapping) {
            case 1:
            case 2:
                runScript(String.format(DROP_FORMAT, mapping));
                break;
            default:
                throw new BenchmarkException("No Such Mapping");
        }
    }
}
