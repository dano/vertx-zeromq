/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq.impl;

import com.github.dano.zeromq.OutMessage;
import com.github.dano.zeromq.OutMessageFactory;
import com.github.dano.zeromq.Payload;
import org.zeromq.ZMQ;

/**
 * TODO comment me
 */
public class OutMessageFactoryImpl implements OutMessageFactory {
  /**
   * Build an outgoing message from a ZMQ.socket. The socket
   * is read until there are no more frames waiting. The
   * method assumes there will be either two or three frames:
   * 1) The socket id
   * 2) The message
   * 3) The reply address (optional)
   *
   * @param socket The socket to read from.
   * @return An instance of this, so the API can be used fluently.
   */
  @Override
  public OutMessage fromSocket(ZMQ.Socket socket) {
    byte[] id = socket.recv(0);
    if (!socket.hasReceiveMore()) {
      return new OutMessageImpl(id, null);
    }
    byte[] msg = socket.recv();
    if (socket.hasReceiveMore()) {
      // There's a reply address, so get that too.
      return new OutMessageImpl(id, new PayloadImpl(msg), socket.recv(0));
    } else {
      return new OutMessageImpl(id, new PayloadImpl(msg));
    }
  }

  @Override
  public OutMessage fromIdMsg(byte[] id, Payload msg) {
    return new OutMessageImpl(id, (PayloadImpl) msg);
  }

  @Override
  public OutMessage fromIdMsgAddress(byte[] id, Payload msg, byte[] address) {
    return new OutMessageImpl(id, (PayloadImpl) msg, address);
  }
}
