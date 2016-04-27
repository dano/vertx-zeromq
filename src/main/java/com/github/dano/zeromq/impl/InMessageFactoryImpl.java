/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq.impl;

import com.github.dano.zeromq.InMessage;
import com.github.dano.zeromq.InMessageFactory;
import org.zeromq.ZMQ;

/**
 * TODO comment me
 */
public class InMessageFactoryImpl implements InMessageFactory {

  /**
   * Create a InMessageImpl from a ZMQ.Socket. This could result in a InMessageImpl
   * with type CONTROL or MESSAGE, depending on what's in the Socket.
   *
   * @param socket The socket to read the InMessageImpl from.
   * @return The InMessageImpl.
   */
  @Override
  public InMessage fromSocket(ZMQ.Socket socket) {
    byte[] id = socket.recv(0);
    if (!socket.hasReceiveMore()) {
      return new InMessageImpl(id, null);
    }
    // Frame two might be the message, or the reply address.
    byte[] frame2 = socket.recv();
    if (socket.hasReceiveMore()) {
      return new InMessageImpl(id, frame2, new PayloadImpl(socket.recv(0)));
    } else {
      return new InMessageImpl(id, new PayloadImpl((frame2)));
    }
  }
}
