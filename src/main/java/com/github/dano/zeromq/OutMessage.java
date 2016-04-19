package com.github.dano.zeromq;

import org.zeromq.ZMQ;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * An outgoing Message.
 */
public class OutMessage {
  private static final Logger LOG = LoggerFactory.getLogger(OutMessage.class);
  private byte[] id;
  private byte[] msg;
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
  public OutMessage(byte[] id, byte[] msg) {
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
  public OutMessage(byte[] id, byte[] msg, byte[] replyAddress) {
    this.id = id;
    this.msg = msg;
    this.replyAddress = replyAddress;
    type = OutMessageType.REPLIABLE;
  }

  /**
   * Build an outoing message from a ZMQ.socket. The socket
   * is read until there are no more frames waiting. The
   * method assumes there will be either two or three frames:
   * 1) The socket id
   * 2) The message
   * 3) The reply address (optional)
   *
   * @param socket The socket to read from.
   * @return
   */
  public static OutMessage fromSocket(ZMQ.Socket socket) {
    byte[] id = socket.recv(0);
    if (!socket.hasReceiveMore()) {
      return new OutMessage(id, null);
    }
    byte[] msg = socket.recv();
    if (socket.hasReceiveMore()) {
      // There's a reply address, so get that too.
      return new OutMessage(id, msg, socket.recv(0));
    } else {
      return new OutMessage(id, msg);
    }
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
   *  @return The message.
   */
  public byte[] getMsg() {
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
   * Returns true if this OutMessage has a reply address.
   * @return
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
    socket.send(msg, isRepliable() ? ZMQ.SNDMORE : 0);
    if (isRepliable()) {
      socket.send(replyAddress, 0);
    }
  }
}
