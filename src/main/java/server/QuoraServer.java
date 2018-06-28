package server;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class QuoraServer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1));
        vertx.deployVerticle(QuoraAPI.class.getName(), new DeploymentOptions().setInstances(1));
    }
}
