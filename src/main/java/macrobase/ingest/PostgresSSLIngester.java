package macrobase.ingest;

import macrobase.conf.ConfigurationException;
import macrobase.conf.MacroBaseConf;

import java.sql.SQLException;
import java.util.Map;

public class PostgresSSLIngester extends PostgresIngester {
    public PostgresSSLIngester(MacroBaseConf conf) throws ConfigurationException, SQLException {
        super(conf);
    }

    @Override
    protected Map<String, String> getJDBCProperties() {
        Map<String, String> properties = super.getJDBCProperties();
        properties.put("ssl", "true");
        properties.put("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
        return properties;
    }
}
