package edu.uci.ics.tippers.query.cratedb;

import edu.uci.ics.tippers.common.Database;
import edu.uci.ics.tippers.common.constants.Constants;
import edu.uci.ics.tippers.connection.cratedb.CrateDBConnectionManager;
import edu.uci.ics.tippers.exception.BenchmarkException;
import edu.uci.ics.tippers.query.BaseQueryManager;
import edu.uci.ics.tippers.query.postgresql.PgSQLQueryManager;

import java.sql.*;
import java.time.Duration;
import java.util.Date;
import java.util.List;

public class CrateDBQueryManager extends BaseQueryManager {

    private PgSQLQueryManager externalQueryManager;
    private Connection connection;

    public CrateDBQueryManager(int mapping, String queriesDir, String outputDir, boolean writeOutput, long timeout) {
        super(mapping, queriesDir, outputDir, writeOutput, timeout);
        connection = CrateDBConnectionManager.getInstance().getConnection();
        externalQueryManager = new PgSQLQueryManager(mapping, queriesDir, outputDir, writeOutput, timeout, connection);
    }

    @Override
    public Database getDatabase() {
        return Database.CRATEDB;
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public Duration runQuery1(String sensorId) throws BenchmarkException {
        switch (mapping) {
            case 1:
                return externalQueryManager.runQuery1(sensorId);
            default:
                throw new BenchmarkException("Error Running Query 1 on CrateDB");
        }
    }

    @Override
    public Duration runQuery2(String sensorTypeName, List<String> locationIds) throws BenchmarkException {
        switch (mapping) {
            case 1:
                String query = "SELECT sen.name FROM SENSOR sen, SENSOR_TYPE st, SENSOR_COVERAGE sc, " +
                        "COVERAGE_INFRASTRUCTURE ci WHERE sen.SENSOR_TYPE_ID=st.id AND st.name=? " +
                        "AND sen.COVERAGE_ID=sc.id AND sc.id=ci.id AND ci.INFRASTRUCTURE_ID=ANY(?)";
                try {
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setString(1, sensorTypeName);

                    Array locationsArray = connection.createArrayOf("string", locationIds.toArray());
                    stmt.setArray(2, locationsArray);
                    return externalQueryManager.runTimedQuery(stmt, 2);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new BenchmarkException("Error Running Query");
                }
            default:
                throw new BenchmarkException("No Such Mapping");
        }
    }

    @Override
    public Duration runQuery3(String sensorId, Date startTime, Date endTime) throws BenchmarkException {
        switch (mapping) {
            case 1:
                return externalQueryManager.runQuery3(sensorId, startTime, endTime);
            default:
                throw new BenchmarkException("Error Running Query 3 on CrateDB");
        }
    }

    @Override
    public Duration runQuery4(List<String> sensorIds, Date startTime, Date endTime) throws BenchmarkException {
        switch (mapping) {
            case 1:
                String query = "SELECT timeStamp, payload FROM OBSERVATION WHERE timestamp>? AND timestamp<? " +
                        "AND SENSOR_ID = ANY(?)";
                try {
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setTimestamp(1, new Timestamp(startTime.getTime()));
                    stmt.setTimestamp(2, new Timestamp(endTime.getTime()));

                    Array sensorIdArray = connection.createArrayOf("string", sensorIds.toArray());
                    stmt.setArray(3, sensorIdArray);

                    return externalQueryManager.runTimedQuery(stmt, 4);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new BenchmarkException("Error Running Query");
                }
            default:
                throw new BenchmarkException("No Such Mapping");
        }
    }

    @Override
    public Duration runQuery5(String sensorTypeName, Date startTime, Date endTime, String payloadAttribute,
                              Object startPayloadValue, Object endPayloadValue) throws BenchmarkException {
        switch (mapping) {
            case 1:
                return externalQueryManager.runQuery5(sensorTypeName, startTime, endTime, payloadAttribute,
                        startPayloadValue, endPayloadValue);
            default:
                throw new BenchmarkException("No Such Mapping");
        }
    }

    @Override
    public Duration runQuery6(List<String> sensorIds, Date startTime, Date endTime) throws BenchmarkException {
        switch (mapping) {
            case 1:
                String query = "SELECT obs.sensor_id, avg(counts) FROM " +
                        "(SELECT sensor_id, date_trunc('day', timestamp), " +
                        "count(*) as counts " +
                        "FROM OBSERVATION WHERE timestamp>? AND timestamp<? " +
                        "AND SENSOR_ID = ANY(?) GROUP BY sensor_id, date_trunc('day', timestamp)) " +
                        "AS obs GROUP BY sensor_id";
                try {
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setTimestamp(1, new Timestamp(startTime.getTime()));
                    stmt.setTimestamp(2, new Timestamp(endTime.getTime()));

                    Array sensorIdArray = connection.createArrayOf("string", sensorIds.toArray());
                    stmt.setArray(3, sensorIdArray);

                    return externalQueryManager.runTimedQuery(stmt, 6);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new BenchmarkException("Error Running Query");
                }
            default:
                throw new BenchmarkException("No Such Mapping");
        }
    }
}
