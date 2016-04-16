package com.github.dano.zeromq;

import org.zeromq.ZMQ;

import java.util.Optional;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Listens for messages on an EventBus queue channel,
 * and forwards them on to the 0mq background thread
 * for sending back out the 0mq pipe.
 */
public class QueueListeningPublishSocket {
  private static final Logger LOG = LoggerFactory.getLogger(QueueListeningPublishSocket.class);

  private final String address;
  private final ZMQ.Context ctx;
  private final Vertx vertx;
  private final String queueChannel;
  private MessageConsumer<OutMessage> consumer;
  private ZMQ.Socket push;

  /**
   * Create and start the listener, and create
   * the 0MQ PUSH socket used to forward messages..
   *
   * @param ctx The 0MQ Context.
   * @param address The address to forward requests to.
   * @param vertx THe vertx instance.
   * @param queueChannel The EventBus address to listen on.
   */
  public QueueListeningPublishSocket(ZMQ.Context ctx, String address, Vertx vertx,
                                     String queueChannel) {
    this.vertx = vertx;
    this.address = address;
    this.ctx = ctx;
    this.queueChannel = queueChannel;
    listen();
  }

  /**
   * Listen for messages on the Vert.x event bus,
   * and forward them to the ZeroMQ thread.
   */
  private void listen() {
    push = ctx.socket(ZMQ.PUSH);
    push.setLinger(5000);
    push.setSndHWM(0);
    push.bind(address);

    consumer = vertx.eventBus().consumer(queueChannel, msg ->
        Optional.ofNullable(msg.body())
            .ifPresent(response -> response.sendMessage(push)));
  }

  /**
   * Stop the EventBus consumer, and close the PUSH socket.
   */
  public void stop() {
    consumer.unregister();
    push.close();
  }
}
