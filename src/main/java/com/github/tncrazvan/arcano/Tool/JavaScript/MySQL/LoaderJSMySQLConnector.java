package com.github.tncrazvan.arcano.Tool.JavaScript.MySQL;

import static com.github.tncrazvan.arcano.Common.logger;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import com.google.gson.JsonObject;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.logging.Level;

/**
 *
 * @author Administrator
 */
public interface LoaderJSMySQLConnector {
    public class JSMySQLConnector implements Function<Object, LoaderJSMySQLConnection.JSMySQLConnection>,JsonTools{
        public LoaderJSMySQLConnection.JSMySQLConnection mysql = null;
        @Override
        public LoaderJSMySQLConnection.JSMySQLConnection apply(Object arg) {
            JsonObject details = toJsonObject(arg);
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser(details.get("username").getAsString());
            dataSource.setPassword(details.get("password").getAsString());
            dataSource.setServerName(details.get("hostname").getAsString());
            try {
                if(mysql != null && !mysql.get().isClosed()){
                    mysql.get().close();
                }
                if(mysql == null)
                    mysql = new LoaderJSMySQLConnection.JSMySQLConnection();
                mysql.set(dataSource.getConnection());
                return mysql;
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
}
