/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq;

import org.zeromq.ZMQ;

/**
 * A Factory for building an InMessages.
 */
public interface InMessageFactory {
  InMessage fromSocket(ZMQ.Socket socket);
}
