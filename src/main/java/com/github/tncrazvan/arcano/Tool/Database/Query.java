package com.github.tncrazvan.arcano.Tool.Database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Administrator
 */
public final class Query extends NamedPreparedStatement{
    private Connection con;
    
    public Query(Connection con, String value) throws SQLException{
        super(value);
        connect(con);
    }
    
    public Query(String value){
        super(value);
    }
    
    public void connect(Connection con) throws SQLException{
        this.con = con;
        setPreparedStatement(this.con.prepareStatement(originalSQL));
    }
}
