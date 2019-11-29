package com.github.tncrazvan.arcano.Tool.JavaScript.MySQL;

import static com.github.tncrazvan.arcano.Common.js;
import com.github.tncrazvan.arcano.Tool.Database.Query;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.JSObject;

/**
 *
 * @author Administrator
 */
public interface LoaderJSMySQLConnection {
    public class JSMySQLConnection implements JsonTools{
        private Connection connection;
        public void set(final Connection connection) {
            this.connection = connection;
        }

        public Connection get() {
            return connection;
        }

        public void close() throws SQLException {
            connection.close();
        }

        public void database(final String dbname) throws SQLException {
            connection.setCatalog(dbname);
        }

        private Query query;

        public void prepare(final String value) throws SQLException {
            query = new Query(connection, value);
        }

        public void bindString(final String key, final String value) throws SQLException {
            query.setString(key, value);
        }

        public JSObject execute() throws SQLException, ScriptException {
            final ResultSet result = query.executeQuery();
            final ResultSetMetaData metadata = result.getMetaData();
            final ArrayList<Object> output = new ArrayList<>();
            while (result.next()) {
                final JsonObject o = new JsonObject();
                for (int i = 1, length = metadata.getColumnCount(); i <= length; i++) {
                    final String colname = metadata.getColumnName(i);
                    final int type = metadata.getColumnType(i);
                    switch (type) {
                    case java.sql.Types.BINARY:
                    case java.sql.Types.CLOB:
                    case java.sql.Types.BLOB:
                    case java.sql.Types.VARBINARY:
                    case java.sql.Types.NVARCHAR:
                    case java.sql.Types.NCHAR:
                    case java.sql.Types.DATE:
                    case java.sql.Types.CHAR:
                    case java.sql.Types.VARCHAR:
                    default:
                        o.addProperty(colname, result.getString(colname));
                        break;
                    case java.sql.Types.NUMERIC:
                    case java.sql.Types.SMALLINT:
                    case java.sql.Types.INTEGER:
                    case java.sql.Types.BIT:
                    case java.sql.Types.BIGINT:
                        o.addProperty(colname, result.getInt(colname));
                        break;
                    case java.sql.Types.FLOAT:
                        o.addProperty(colname, result.getFloat(colname));
                        break;
                    case java.sql.Types.DECIMAL:
                    case java.sql.Types.DOUBLE:
                        o.addProperty(colname, result.getDouble(colname));
                        break;
                    case java.sql.Types.BOOLEAN:
                        o.addProperty(colname, result.getBoolean(colname));
                        break;
                    }
                }
                output.add(o);
            }
            js.put("$tmp", output);
            final JSObject obj = (JSObject) js.eval("JSON.parse($tmp)");
            return obj;
        }
    }
}
