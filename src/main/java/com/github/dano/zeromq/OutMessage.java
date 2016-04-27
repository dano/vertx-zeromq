/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq;

import org.zeromq.ZMQ;

/**
 * An outgoing Message.
 */
public interface OutMessage {

  /**
   * Get the reply address. Could be null.
   *
   * @return The reply address, or null.
   */
  byte[] getReplyAddress();

  /**
   * Get the message contents.
   *
   * @return The message.
   */
  byte[] getMsg();

  /**
   * Returns true if this OutMessageImpl has a reply address.
   * @return true if the OutMessageImpl has a replay address, otherwise false.
   */
  boolean isRepliable();

  /**
   * Serialize the InMessage and send it out over a ZMQ.Socket.
   *
   * @param socket The socket to send the InMessage to.
   */
  void sendMessage(ZMQ.Socket socket);

  /**
   * Get the id of the InMessage.
   *
   * @return The id.
   */
  byte[] getId();
}
