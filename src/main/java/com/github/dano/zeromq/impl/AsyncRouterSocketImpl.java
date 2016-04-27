/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq.impl;

import com.github.dano.zeromq.AsyncRouterSocket;
import com.github.dano.zeromq.InMessage;
import com.github.dano.zeromq.InMessageFactory;
import com.github.dano.zeromq.MessageResponder;
import com.github.dano.zeromq.OutMessageFactory;
import org.zeromq.ZMQ;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

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
public class AsyncRouterSocketImpl implements AsyncRouterSocket {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncRouterSocketImpl.class);

  private String frontendAddress;
  private String backendAddress;
  private boolean running = true;
  private ZMQ.Context ctx;
  private CountDownLatch shutdownLatch = new CountDownLatch(1);
  private final InMessageFactory inMessageFactory;
  private final OutMessageFactory outMessageFactory;
  private final BiConsumer<InMessage, MessageResponder> handleBlockingRequest;

  /**
   * Create a AsyncRouterSocketImpl.
   *
   * @param ctx The ZMQ socket to use.
   * @param frontendAddress The address to use for the frontend.
   * @param backendAddress The address to use for the backend.
   * @param inMessageFactory A factory for creating InMessages.
   * @param outMessageFactory A factory for creating OutMessages.
   * @param handleBlockingRequest The function to use to handle received requests.
   */
  public AsyncRouterSocketImpl(ZMQ.Context ctx, String frontendAddress, String backendAddress,
                           InMessageFactory inMessageFactory,
                           OutMessageFactory outMessageFactory,
                           BiConsumer<InMessage, MessageResponder> handleBlockingRequest) {
    this.frontendAddress = frontendAddress;
    this.backendAddress = backendAddress;
    this.ctx = ctx;
    this.inMessageFactory = inMessageFactory;
    this.outMessageFactory = outMessageFactory;
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
    pull.bind(backendAddress);

    ZMQ.Poller poller = new ZMQ.Poller(2);
    poller.register(server, ZMQ.Poller.POLLIN);
    poller.register(pull, ZMQ.Poller.POLLIN);

    while (running) {
      poller.poll(1000);

      if (poller.pollin(0)) {
        InMessage msg = inMessageFactory.fromSocket(server);
        // Broker it
        handleBlockingRequest.accept(msg, new MessageResponder(msg.getId(), ctx,
            backendAddress, outMessageFactory));
      }

      if (poller.pollin(1)) {
        // receive message
        outMessageFactory.fromSocket(pull).sendMessage(server);
      }
    }
    server.close();
    pull.close();
    shutdownLatch.countDown();
  }

  /**
   * Stop the socket.
   */
  @Override
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
