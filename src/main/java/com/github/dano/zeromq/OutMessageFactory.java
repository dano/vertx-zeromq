/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq;

import org.zeromq.ZMQ;

/**
 * A Factory for building an OutMessages.
 */
public interface OutMessageFactory {

  OutMessage fromSocket(ZMQ.Socket socket);

  OutMessage fromIdMsg(byte[] id, byte[] msg);

  OutMessage fromIdMsgAddress(byte[] id, byte[] msg, byte[] address);
}
