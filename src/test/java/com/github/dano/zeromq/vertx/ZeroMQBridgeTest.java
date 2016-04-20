package com.github.dano.zeromq.vertx;

import org.junit.Before;
import org.junit.Test;
import org.zeromq.ZMQ;

import io.vertx.core.Vertx;

import static com.github.dano.zeromq.vertx.ZeroMQBridge.REGISTER;
import static com.github.dano.zeromq.vertx.ZeroMQBridge.UNREGISTER;
import static org.junit.Assert.assertEquals;

/**
 * Test the ZeroMQBridge
 */
public class ZeroMQBridgeTest {
  public static final String ADDRESS = "tcp://localhost:5558";
  private Vertx vertx;

  @Before
  public void before() {
    vertx = Vertx.vertx();
  }
  @Test
  public void testRegisterAndUnregister() {
    final String testChannel = "testChannel";

    ZeroMQBridge bridge = new ZeroMQBridge(ADDRESS, vertx);
    bridge.start();
    ZMQ.Context ctx = ZMQ.context(1);
    ZMQ.Socket listener = ctx.socket(ZMQ.DEALER);
    listener.connect(ADDRESS);
    listener.send((REGISTER + testChannel).getBytes());
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    final String msg = "testMsg";
    final ZMQ.Socket client = ctx.socket(ZMQ.DEALER);
    client.connect(ADDRESS);
    client.send(testChannel.getBytes(), ZMQ.SNDMORE);
    client.send(msg.getBytes(), 0);

    byte[] response = listener.recv();
    byte[] replyChannel = listener.recv();
    assertEquals(msg, new String(response));

    listener.send((UNREGISTER + testChannel).getBytes());
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    client.send(testChannel.getBytes(), ZMQ.SNDMORE);
    client.send(msg.getBytes(), 0);

    response = client.recv();
    assertEquals("NO_HANDLERS", new String(response));
    bridge.stop();

  }

}
