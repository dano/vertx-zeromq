package com.github.dano.zeromq;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

/**
 * Used to send a response to a message once its been processed.
 */
public class MessageResponder {
  private final byte[] id;
  private final String queueChannel;
  private final EventBus eventBus;

  public MessageResponder(byte[] id, Vertx vertx, String queueChannel) {
    this.id = id;
    this.queueChannel = queueChannel;
    this.eventBus = vertx.eventBus();
  }

  /**
   * Get the ID associated with the message.
   *
   * @return The ID.
   */
  public byte[] getSocketId() {
    return id;
  }

  /**
   * Respond with just a message body.
   *
   * @param msg The message body.
   */
  public void respond(byte[] msg) {
    eventBus.send(queueChannel, new OutMessage(id, msg));
  }

  /**
   * Respond with a message body and a reply address.
   *
   * @param msg The message body.
   * @param address The reply address.
   */
  public void respond(byte[] msg, byte[] address) {
    eventBus.send(queueChannel, new OutMessage(id, msg, address));
  }
}
