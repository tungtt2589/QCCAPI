package server;

import SqlConnector.ConnectDatabase;
import SqlConnector.SqlCommonDb;
import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import utils.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class QuoraAPI extends AbstractVerticle {
    ConnectDatabase database;
    public static int port = 7070;
    SqlCommonDb db;
    @Override
    public void start() throws Exception {
        super.start();
        database = new ConnectDatabase();
        db = new SqlCommonDb();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route(HttpMethod.POST,"/login").handler(this::login);
        router.route(HttpMethod.POST,"/getAllTopic").handler(this::getTopic);
        router.route(HttpMethod.POST,"/getAllQuestion").handler(this::getAllQuestion);
        router.route(HttpMethod.POST,"/createQuestion").handler(this::createQuestion);
        router.route(HttpMethod.POST,"/createAnswer").handler(this::createAnswer);
        router.route(HttpMethod.POST,"/getAllAnswer").handler(this::getAllAnswer);
        vertx.createHttpServer().requestHandler(router::accept).listen(port);
        System.out.println("running mother fucker");
    }

    public void login(RoutingContext r){
        JsonObject json = r.getBodyAsJson();
        String user_name = json.getString("username");
        String pwd = json.getString("password");
        StringBuilder query = new StringBuilder("SELECT * from user where username = ?;");
        ArrayList<Object> params = new ArrayList<>();
        params.add(user_name);
        try {

            List<User> result = database.executeQuery(query, params, User.class, db.connectDb());

            if(result.size() == 1 && pwd.equals(result.get(0).getPassword())){
                DataResponse d = new DataResponse();
                d.setStatus(1);
                d.setData(result.get(0));
                sendResponse(r, d);
            }
            else {
                Message m = new Message();
                m.setMessage("Invalid user or wrong pwd!!");
                m.setStatus(0);
                sendResponse(r, m);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            sendResponse(r, "");
        }
    }

    public void createQuestion(RoutingContext r){
        JsonObject jsonObject = r.getBodyAsJson();
        jsonObject.put("created_time",new Timestamp(System.currentTimeMillis()).toString());
        try{
            int res = database.autoInsert("question",jsonObject,db.connectDb());
            sendResponse(r, new Status(res));
        }catch (Exception e){
            e.printStackTrace();
            sendResponse(r, -1);
        }
    }

    public void createAnswer(RoutingContext r){
        JsonObject jsonObject = r.getBodyAsJson();
        jsonObject.put("created_time",new Timestamp(System.currentTimeMillis()).toString());
        try{
            int res = database.autoInsert("answer",jsonObject,db.connectDb());
            sendResponse(r, new Status(res));
        }catch (Exception e){
            e.printStackTrace();
            sendResponse(r, -1);
        }
    }


    public void getTopic(RoutingContext r){
        try {
            List<Topic> res = database.selectQuery(r.getBodyAsJson(), "topic", Topic.class, db.connectDb(), null);
            sendData(res, r);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getAllQuestion(RoutingContext r){
        try {
            List<Question> res = database.selectQuery(r.getBodyAsJson(), "question", Question.class, db.connectDb(), null);
            sendData(res, r);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getAllAnswer(RoutingContext r){
        try {
            List<Answer> res = database.selectQuery(r.getBodyAsJson(), "answer", Answer.class, db.connectDb(), null);
            sendData(res, r);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendResponse(RoutingContext r, Object rs){
        HttpServerResponse response = r.response();
        response.putHeader("content-type", "application/json; charset=utf-8");
        response.end(new Gson().toJson(rs));
    }

    public <T> void sendData(List<T> res, RoutingContext r){
        DataResponse data = new DataResponse();
        if(res.size() > 0){
            data.setStatus(1);
            data.setData(res);
        }
        else{
            data.setData(new ArrayList<>());
            data.setStatus(0);
        }
        sendResponse(r, data);
    }



}
