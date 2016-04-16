package com.github.dano.zeromq;

import org.zeromq.ZMQ;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by dan on 4/17/16.
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
  public OutMessage(byte[] id, byte[] msg) {
    this.id = id;
    this.msg = msg;
    type = OutMessageType.NON_REPLIABLE;
  }

  public OutMessage(byte[] id, byte[] msg, byte[] replyAddress) {
    this.id = id;
    this.msg = msg;
    this.replyAddress = replyAddress;
    type = OutMessageType.REPLIABLE;
  }

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

  public byte[] getId() {
    return id;
  }

  public byte[] getMsg() {
    return msg;
  }

  public byte[] getReplyAddress() {
    return replyAddress;
  }

  public boolean isRepliable() {
    return type.equals(OutMessageType.REPLIABLE);
  }

  public void sendMessage(ZMQ.Socket socket) {
    socket.send(id, ZMQ.SNDMORE);
    socket.send(msg, isRepliable() ? ZMQ.SNDMORE : 0);
    if (isRepliable()) {
      socket.send(replyAddress, 0);
    }
  }
}
