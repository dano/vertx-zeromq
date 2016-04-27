package com.github.dano.zeromq;

import org.zeromq.ZMQ;

/**
 * Used to send a response to a message once its been processed. The
 * message will be packaged into an OutMessageImpl and sent to the blocking
 * 0MQ thread via a ZMQ PUSH Socket.
 */
public class MessageResponder {
  private final byte[] id;
  private final String mAddress;
  private final ZMQ.Context ctx;
  private final OutMessageFactory outMessageFactory;

  /**
   * Create a MessageResponder. Note that the PUSH socket can't be
   * create in the constructor, because the MessageResponder isn't
   * constructed in the same thread the reply will actually be sent
   * from.
   *
   * @param id The socket id associated with this message.
   * @param ctx The ZMQ context to use for socket creation.
   * @param mAddress The address to connect ZMQ response sockets to.
   * @param outMessageFactory A factory for creating OutMessages.
   */
  public MessageResponder(byte[] id, ZMQ.Context ctx, String mAddress,
                          OutMessageFactory outMessageFactory) {
    this.id = id;
    this.ctx = ctx;
    this.mAddress = mAddress;
    this.outMessageFactory = outMessageFactory;
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
  public void respond(Payload msg) {
    ZMQ.Socket sock = getReplySocket();
    try {
      outMessageFactory.fromIdMsg(id, msg).sendMessage(sock);
    } finally {
      sock.close();
    }
  }

  /**
   * Respond with a message body and a reply address.
   *
   * @param payload The message body.
   * @param address The reply address.
   */
  public void respond(Payload payload, byte[] address) {
    ZMQ.Socket sock = getReplySocket();
    try {
      outMessageFactory.fromIdMsgAddress(id, payload, address).sendMessage(sock);
    } finally {
      sock.close();
    }
  }

  /**
   * Create the PUSH socket used to send the response
   * back to the blocking 0MQ thread.
   *
   * @return The response socket.
   */
  private ZMQ.Socket getReplySocket() {
    ZMQ.Socket sock = ctx.socket(ZMQ.PUSH);
    sock.connect(mAddress);
    return sock;
  }
}
