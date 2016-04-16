package com.github.dano.zeromq.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Created by dan on 4/16/16.
 */
public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Future<Void> startFuture) {
    vertx.deployVerticle(ZeroMQBridgeVerticle.class.getCanonicalName(),
        new DeploymentOptions()
            .setConfig(new JsonObject().put("address", "tcp://localhost:5558"))
    );
  }
}
