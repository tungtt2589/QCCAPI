package SqlConnector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectDatabase {


    public <T> List<T> executeQuery(StringBuilder query, List<Object> params, Class<T> entityClass, Connection conn) {
        List<T> arrayT = new ArrayList<>();
        if (conn == null) {
            return Collections.emptyList();
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(query.toString());

            if (stmt == null) {
                return Collections.emptyList();
            }

            if(params != null){

                for(int i = 0 ; i < params.size(); i++){
                    stmt.setObject(i + 1, params.get(i));
                }
            }

            ResultSet rs = stmt.executeQuery();

            Field[] entityFields = entityClass.getDeclaredFields();

            while (rs.next()){

                JsonObject datasetItem = new JsonObject();
                for(Field f : entityFields){
                    try{
                        String value = rs.getString(f.getName());
                        datasetItem.addProperty(f.getName(), value);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                T item = gson.fromJson(datasetItem, entityClass);
                arrayT.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try{
                conn.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return arrayT;
    }


    public <T> List<T> selectQuery(io.vertx.core.json.JsonObject json, String table, Class<T> entityClass, Connection conn, List<String> op) {
        List<T> arrayT = new ArrayList<>();
        if (conn == null) {
            return Collections.emptyList();
        }

        try {
            StringBuilder query = null;
            if(op != null){
                query = createSelectQuery(table, json, op);
            }
            else {
                query = createSelectQuery(table, json);
            }
            PreparedStatement stmt = conn.prepareStatement(query.toString());
            if (stmt == null) {
                return Collections.emptyList();
            }

            int i = 1;
            for(String str : json.fieldNames()){
                stmt.setObject(i, json.getString(str));
                i++;
            }

            ResultSet rs = stmt.executeQuery();
            Field[] entityFields = entityClass.getDeclaredFields();

            while (rs.next()){
                JsonObject datasetItem = new JsonObject();
                for(Field f : entityFields){
                    try{
                        String value = rs.getString(f.getName());
                        datasetItem.addProperty(f.getName(), value);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                T item = gson.fromJson(datasetItem, entityClass);
                arrayT.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try{
                conn.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return arrayT;
    }

    public StringBuilder creteUpdateQuery(String table, String id2update, io.vertx.core.json.JsonObject json){

        String uValue = json.getString(id2update);
        StringBuilder builder = new StringBuilder();
        builder.append("Update " + table + " set ");
        for(String str : json.fieldNames()){
            if(!str.equals(id2update)){
                builder.append(" " + str + " = ? ,");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(" where " + id2update + " = '" + uValue + "';");
        return builder;

    }

    public int updateQuery(String table, String id2update, io.vertx.core.json.JsonObject json, Connection conn){
        try{
            PreparedStatement stmt = conn.prepareStatement(creteUpdateQuery(table, id2update, json).toString());
            if(stmt == null){
                return -1;
            }

            int i = 1;
            for(String str : json.fieldNames()){
                if(!str.equals(id2update)){
                    stmt.setObject(i, json.getString(str));
                    i++;
                }
            }
            return stmt.executeUpdate();
        }
        catch (Exception e){
            e.printStackTrace();
            return  -1;
        }

    }
    public int insertQuery(StringBuilder query, List<Object> params, Connection conn){

        try{
            PreparedStatement stmt = conn.prepareStatement(query.toString());
            if(stmt == null){
                return -1;
            }

            if(params != null){
                for(int i = 0 ; i < params.size() ;i++){
                    stmt.setObject(i + 1, params.get(i));
                }
                return stmt.executeUpdate();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return  -1;
        }
        return 1;
    }

    public int updateQuery(StringBuilder query, List<Object> params, Connection conn) {

        try {
            PreparedStatement stmt = conn.prepareStatement(query.toString());

            if (stmt == null) {
                return -1;
            }
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        return 1;
    }


    public StringBuilder creteInsertQuery(String table, io.vertx.core.json.JsonObject json){
        StringBuilder builder = new StringBuilder();
        builder.append("Insert into " + table + " (");
        for(String str : json.fieldNames()){
            builder.append(str + " ,");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(") Values (");
        for(String str : json.fieldNames()){
            builder.append( "?,");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(");");
        return builder;
    }


    public StringBuilder createSelectQuery(String table, io.vertx.core.json.JsonObject json){
        StringBuilder builder = new StringBuilder();
        builder.append("Select * from " + table);
        if(json.fieldNames().size() != 0){
            builder.append(" where ");
        }
        for(String str : json.fieldNames()){
            builder.append(" " + str + " = ? and");
        }
        if(json.fieldNames().size() != 0){
            builder.delete(builder.length() - 4, builder.length());
        }
        builder.append(";");
        return builder;
    }


    public StringBuilder createSelectQuery(String table, io.vertx.core.json.JsonObject json, List<String> op){
        StringBuilder builder = new StringBuilder();
        builder.append("Select * from " + table);
        if(json.fieldNames().size() != 0){
            builder.append(" where ");
        }
        int i = 0;
        for(String str : json.fieldNames()){
            builder.append(" " + str + " " +op.get(i) + " ? and");
            i++;
        }
        if(json.fieldNames().size() != 0){
            builder.delete(builder.length() - 4, builder.length());
        }
        builder.append(";");
        return builder;
    }

    public int autoInsert(String table, io.vertx.core.json.JsonObject json, Connection conn){

        StringBuilder query = creteInsertQuery(table, json);

        try{
            PreparedStatement stmt = conn.prepareStatement(query.toString());
            if(stmt == null){
                return -1;
            }

            if(json != null){
                int i = 1;
                for(String str : json.fieldNames()){
                    stmt.setObject(i, json.getString(str));
                    i++;
                }
                return stmt.executeUpdate();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return  -1;
        }
        return 1;
    }
}
