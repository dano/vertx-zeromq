package com.github.dano.zeromq;

import org.zeromq.ZMQ;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * A broker that has two jobs:
 *
 * 1) Listen on a blocking 0MQ socket for messages from
 * external clients, and forward them on for processing.
 *
 * 2) Listen on a blocking 0MQ socket for responses to
 * messages already received from an external client, and
 * send them back out to the client.
 */
public class AsyncRouterSocket implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncRouterSocket.class);

  private String frontendAddress;
  private String backendAddress;
  private boolean running = true;
  private ZMQ.Context ctx;
  private final Vertx vertx;
  private final String queueChannel;
  private CountDownLatch shutdownLatch = new CountDownLatch(1);
  private final BiConsumer<InMessage, MessageResponder> handleBlockingRequest;

  /**
   * Create a AsyncRouterSocket.
   *
   * @param ctx The ZMQ socket to use.
   * @param frontendAddress The address to use for the frontend.
   * @param backendAddress The address to use for the backend.
   */
  public AsyncRouterSocket(ZMQ.Context ctx, String frontendAddress, String backendAddress,
                           Vertx vertx, String queueChannel,
                           BiConsumer<InMessage, MessageResponder> handleBlockingRequest) {
    this.frontendAddress = frontendAddress;
    this.backendAddress = backendAddress;
    this.ctx = ctx;
    this.vertx = vertx;
    this.queueChannel = queueChannel;
    this.handleBlockingRequest = handleBlockingRequest;
  }

  /**
   * Start up the frontend and backend listeners, and handle requests.
   * Requests sent to the frontend port are forwarded to the Vert.x
   * event bus, and events on the backend port are sent out
   * via 0MQ.
   */
  @Override
  public void run() {
    ZMQ.Socket server = ctx.socket(ZMQ.ROUTER);
    server.bind(frontendAddress);

    ZMQ.Socket pull = ctx.socket(ZMQ.PULL);
    pull.connect(backendAddress);

    ZMQ.Poller poller = new ZMQ.Poller(2);
    poller.register(server, ZMQ.Poller.POLLIN);
    poller.register(pull, ZMQ.Poller.POLLIN);

    while (running) {
      poller.poll(1000);

      if (poller.pollin(0)) {
        InMessage msg = InMessage.fromSocket(server);
        // Broker it
        handleBlockingRequest.accept(msg, new MessageResponder(msg.getId(), vertx, queueChannel));
      }

      if (poller.pollin(1)) {
        // receive message
        OutMessage.fromSocket(pull).sendMessage(server);
      }
    }
    server.close();
    pull.close();
    shutdownLatch.countDown();
  }

  /**
   * Stop the socket.
   */
  public void stop() {
    this.running = false;
    try {
      shutdownLatch.await();
    } catch (InterruptedException e) {
      LOG.error(e);
      throw new RuntimeException(e);
    }
  }
}
