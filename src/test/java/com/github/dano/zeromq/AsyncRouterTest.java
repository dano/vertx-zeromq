package com.github.dano.zeromq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Test the AsyncRouter.
 */
@RunWith(VertxUnitRunner.class)
public class AsyncRouterTest {
  private AsyncRouter echo;

  @Before
  public void before() {
    echo = new AsyncRouter("tcp://*:5558", new InMessageFactoryImpl(),
                           new OutMessageFactoryImpl()) {
      @Override
      protected void handleRequest(InMessage message, MessageResponder responder) {
        responder.respond(message.getPayload());
      }
    };
    echo.start();
  }

  @After
  public void after() {
    echo.stop();
  }

  @Test
  public void shouldReceiveAtLeastASingleResponse() throws TimeoutException {
    TestClient client = new TestClient("tcp://localhost:5558",1);
    new Thread(client).start();
    client.waitFor();
    System.out.println("Test done");
  }

  @Test
  public void shouldReceiveCorrelatedResponses() throws TimeoutException {
    TestClient client3 = new TestClient("tcp://localhost:5558",5);
    new Thread(client3).start();

    TestClient client2 = new TestClient("tcp://localhost:5558",5);
    new Thread(client2).start();

    TestClient client1 = new TestClient("tcp://localhost:5558",5);
    new Thread(client1).start();

    client3.waitFor();
    client2.waitFor();
    client1.waitFor();
  }
}
