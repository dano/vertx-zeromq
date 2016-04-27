/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq.impl;

import com.github.dano.zeromq.OutMessage;
import com.github.dano.zeromq.Payload;
import org.zeromq.ZMQ;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * An outgoing Message.
 */
public class OutMessageImpl implements OutMessage {
  private static final Logger LOG = LoggerFactory.getLogger(OutMessageImpl.class);
  private byte[] id;
  private PayloadImpl msg;
  private byte[] replyAddress;
  private OutMessageType type;

  public enum OutMessageType {
    REPLIABLE,
    NON_REPLIABLE
  }

  /**
   * Create an outgoing message without a reply address.
   *
   * @param id The socket ID.
   * @param msg The message.
   */
  public OutMessageImpl(byte[] id, PayloadImpl msg) {
    this.id = id;
    this.msg = msg;
    type = OutMessageType.NON_REPLIABLE;
  }

  /**
   * Create an outgoing message with a reply address
   *
   * @param id The socket ID.
   * @param msg The message.
   * @param replyAddress The reply address.
   */
  public OutMessageImpl(byte[] id, PayloadImpl msg, byte[] replyAddress) {
    this.id = id;
    this.msg = msg;
    this.replyAddress = replyAddress;
    type = OutMessageType.REPLIABLE;
  }

  /**
   * Get the socket ID
   *
   * @return The soscket ID
   */
  public byte[] getId() {
    return id;
  }

  /**
   * Get the message contents.
   *
   * @return The message.
   */
  public Payload getMsg() {
    return msg;
  }

  /**
   * Get the reply address. Could be null.
   *
   * @return The reply address, or null.
   */
  public byte[] getReplyAddress() {
    return replyAddress;
  }

  /**
   * Returns true if this OutMessageImpl has a reply address.
   * @return true if the OutMessageImpl has a replay address, otherwise false.
   */
  public boolean isRepliable() {
    return type.equals(OutMessageType.REPLIABLE);
  }

  /**
   * Serialize the message and send over a ZMQ.socket.
   * The id, message, and reply address (if present) are each sent in
   * separate frames.
   *
   * @param socket The socket to send the message over.
   */
  public void sendMessage(ZMQ.Socket socket) {
    socket.send(id, ZMQ.SNDMORE);
    socket.send(msg.getMsg(), isRepliable() ? ZMQ.SNDMORE : 0);
    if (isRepliable()) {
      socket.send(replyAddress, 0);
    }
  }
}
