/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.catpaw.Tool.Database;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

public class NamedPreparedStatement extends PreparedStatementImplementation {

    protected enum FormatType {

        NULL, BOOLEAN, BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BIGDECIMAL, STRING, STRINGLIST, DATE, TIME, TIMESTAMP
    }

    protected String originalSQL;
    protected final List<String> lstParameters;
    protected PrintStream ps;
    
    /*
    public static NamedPreparedStatement create(Connection conn, String sql) throws SQLException {
        return create(conn, sql, System.out);
    }
    
    public static NamedPreparedStatement create(Connection conn, String sql, OutputStream os) throws SQLException {
        return create(conn, sql, new PrintStream(os));
    }
    
    public static NamedPreparedStatement create(Connection conn, String sql, PrintStream os) throws SQLException {
        return new NamedPreparedStatement(conn.prepareStatement(sql), sql,os);
    }*/

    protected NamedPreparedStatement(String originalSQL) {
        this(originalSQL, System.out);
    }
    
    protected NamedPreparedStatement(String originalSQL, PrintStream ps) {
        this.originalSQL = originalSQL.trim();
        this.lstParameters = new ArrayList<>();
        this.ps = ps;
    }

    protected Collection<Integer> getParameterIndexes(String parameter) {
        Collection<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < lstParameters.size(); i++) {
            if (lstParameters.get(i).equalsIgnoreCase(parameter)) {
                indexes.add(i + 1);
            }
        }
        if (indexes.isEmpty()) {
            throw new IllegalArgumentException(String.format("SQL statement doesn't contain the parameter '%s'",
                    parameter));
        }
        return indexes;
    }

    public void setNull(String parameter, int sqlType) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setNull(i, sqlType);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((String) null, FormatType.NULL)));
        }
    }

    public void setBoolean(String parameter, boolean x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setBoolean(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Boolean) x, FormatType.BOOLEAN)));
        }
    }

    public void setByte(String parameter, byte x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setByte(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Byte) x, FormatType.BYTE)));
        }
    }

    public void setShort(String parameter, short x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setShort(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Short) x, FormatType.SHORT)));
        }
    }

    public void setInt(String parameter, int x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setInt(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Integer) x, FormatType.INTEGER)));
        }
    }

    public void setLong(String parameter, long x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setLong(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Long) x, FormatType.LONG)));
        }
    }

    public void setFloat(String parameter, float x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setFloat(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Float) x, FormatType.FLOAT)));
        }
    }

    public void setDouble(String parameter, double x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setDouble(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Double) x, FormatType.DOUBLE)));
        }
    }

    public void setBigDecimal(String parameter, BigDecimal x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setBigDecimal(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((BigDecimal) x, FormatType.BIGDECIMAL)));
        }
    }

    public void setString(String parameter, String x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setString(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((String) x, FormatType.STRING)));
        }
    }

    public void setBytes(String parameter, byte[] x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setBytes(i, x);
            String fval = "";
            for (int j = 0; j < x.length; j++) {
                fval += (char) x[j] + ",";
            }
            if (fval.endsWith(",")) {
                fval = fval.substring(0, fval.length() - 1);
            }
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((String) fval, FormatType.STRING)));
        }
    }

    public void setDate(String parameter, Date x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setDate(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Date) x, FormatType.DATE)));
        }
    }

    public void setTime(String parameter, Time x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setTime(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Time) x, FormatType.TIME)));
        }
    }

    public void setTimestamp(String parameter, Timestamp x) throws SQLException {
        for (Integer i : getParameterIndexes(parameter)) {
            getPreparedStatement().setTimestamp(i, x);
            this.originalSQL = this.originalSQL.replaceFirst("(?i):" + parameter, Matcher.quoteReplacement(format((Timestamp) x, FormatType.TIMESTAMP)));
        }
    }

    public String getQuery() {
        return this.originalSQL.trim();
    }
    
    protected String format(Object o, FormatType type) {
        String returnParam = "";
        try {
            switch (type) {
                case NULL:
                    returnParam = "NULL";
                    break;
                case BIGDECIMAL:
                    returnParam = ((o == null) ? "NULL" : "'" + ((BigDecimal) o).toString() + "'");
                    break;
                case BOOLEAN:
                    returnParam = ((o == null) ? "NULL" : "'" + ((Objects.equals((Boolean) o, Boolean.TRUE)) ? "1" : "0") + "'");
                    break;
                case BYTE:
                    returnParam = ((o == null) ? "NULL" : "'" + ((Byte) o).intValue() + "'");
                    break;
                case DATE:
                    returnParam = ((o == null) ? "NULL" : "'" + new SimpleDateFormat("yyyy-MM-dd").format((Date) o) + "'");
                    break;
                case DOUBLE:
                    returnParam = ((o == null) ? "NULL" : "'" + ((Double) o).toString() + "'");
                    break;
                case FLOAT:
                    returnParam = ((o == null) ? "NULL" : "'" + ((Float) o).toString() + "'");
                    break;
                case INTEGER:
                    returnParam = ((o == null) ? "NULL" : "'" + ((Integer) o).toString() + "'");
                    break;
                case LONG:
                    returnParam = ((o == null) ? "NULL" : "'" + ((Long) o).toString() + "'");
                    break;
                case SHORT:
                    returnParam = ((o == null) ? "NULL" : "'" + ((Short) o).toString() + "'");
                    break;
                case STRING:
                    returnParam = ((o == null) ? "NULL" : "'" + o.toString() + "'");
                    break;
                case STRINGLIST:
                    returnParam = ((o == null) ? "NULL" : "'" + o.toString() + "'");
                    break;
                case TIME:
                    returnParam = ((o == null) ? "NULL" : "'" + new SimpleDateFormat("hh:mm:ss a").format(o) + "'");
                    break;
                case TIMESTAMP:
                    returnParam = ((o == null) ? "NULL" : "'" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(o) + "'");
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace(ps);
        }
        return returnParam.trim();
    }
}
