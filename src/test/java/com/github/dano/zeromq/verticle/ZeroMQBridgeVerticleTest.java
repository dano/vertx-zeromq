package com.github.dano.zeromq.verticle;

import com.github.dano.zeromq.impl.ZeroMQBridge;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeromq.ZMQ;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Test the ZeroMQBridgeVerticle
 */
@RunWith(VertxUnitRunner.class)
public class ZeroMQBridgeVerticleTest {

  private Vertx vertx;
  private JsonObject json;

  @Before
  public void before() {
    vertx = Vertx.vertx();
    json = new JsonObject().put("address", "tcp://*:5558");
  }

  @Test
  public void testZeroMQVerticle(TestContext context) throws Exception {
    final Async async = context.async();

    DeploymentOptions options = new DeploymentOptions()
        .setConfig(json);
    vertx.deployVerticle(ZeroMQBridgeVerticle.class.getName(), options,
         res -> {
          if (res.succeeded()) {
            performClientTests(context);
          } else {
            Assert.fail("Deployment failed " + res.result());
          }
           vertx.undeploy(res.result(), v -> async.complete());
        });
  }

  @Test
  public void testSendToListener(TestContext context) {
    final Async async = context.async();
    DeploymentOptions options = new DeploymentOptions()
        .setConfig(json);
    vertx.deployVerticle(ZeroMQBridgeVerticle.class.getName(), options,
        res -> {
          if (res.succeeded()) {
            vertx.deployVerticle(TestVerticle.class.getName(), res2 -> {
              if (res2.succeeded()) {
                final ZMQ.Context ctx = ZMQ.context(1);
                final ZMQ.Socket client = ctx.socket(ZMQ.DEALER);
                final String address = "tcp://localhost:5558";
                client.connect(address);
                client.send("register:replyHandler".getBytes());
                client.send(TestVerticle.CHANNEL.getBytes(), ZMQ.SNDMORE);
                client.send(new JsonObject()
                    .put("msg", "a message")
                    .put("replyChannel", "replyHandler").encode().getBytes(), 0);
                byte[] reply = client.recv(0);
                context.assertEquals(TestVerticle.REPLY, new String(reply));
                vertx.undeploy(res.result(), v ->
                  vertx.undeploy(res2.result(), v2 -> async.complete())
                );
              } else {
                Assert.fail("Deployment failed " + res2.result());
              }
            });
          } else {
            Assert.fail("Deployment failed " + res.result());
          }
        });

  }

  private static void performClientTests(TestContext context) {
    final String address = "tcp://localhost:5558";
    final ZMQ.Context ctx = ZMQ.context(1);
    final ZMQ.Socket registered = ctx.socket(ZMQ.DEALER);
    final String channelName = "testHandler";
    final String msg = "message 1";
    final String msg2 = "message 2";

    registered.connect(address);
    registered.send((ZeroMQBridge.REGISTER + channelName).getBytes());

    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    final ZMQ.Socket client = ctx.socket(ZMQ.DEALER);
    client.connect(address);
    client.send(channelName.getBytes(), ZMQ.SNDMORE);
    client.send(msg.getBytes(), 0);

    byte[] response = registered.recv();
    byte[] replyaddress = registered.recv();
    context.assertEquals(msg, new String(response));

    registered.send(replyaddress, ZMQ.SNDMORE);
    registered.send(msg2.getBytes(), 0);

    byte[] response3 = client.recv();
    context.assertEquals(msg2, new String(response3));
  }
}
