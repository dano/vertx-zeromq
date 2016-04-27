/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq;

import org.zeromq.ZMQ;

/**
 * InMessage interface.
 */
public interface InMessage {
  /**
   * Returns true if this InMessage is a control message.
   *
   * @return true if its a control message, false otherwise.
   */
  boolean isControl();

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

  /**
   * Get the payload of the InMessage.
   *
   * @return The payload.
   */
  Payload getPayload();

  byte[] getControlMessage() throws IllegalStateException;


  /**
   * Get the destination address of the InMessage.
   *
   * @return The address the message was sent to. Can be null,
   * if the message is a control message.
   */
  byte[] getAddress();

  /**
   * Get the message destination address as a string.
   *
   * @return The address as a string.
   */
  String getAddressAsString();

}
