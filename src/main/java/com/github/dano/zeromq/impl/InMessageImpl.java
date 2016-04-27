/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq.impl;


import com.github.dano.zeromq.InMessage;
import com.github.dano.zeromq.Payload;
import org.zeromq.ZMQ;

/**
 * Represents a message received from a 0MQ client. There are two
 * flavors of incoming messages:
 * 1) A Control message. This is not a message meant for an Event Bus
 *    channel, but instead is meant for the ZeroMQ bridge itself. The
 *    register and unregister commands are the only control messages,
 *    currently. These messages have only an id and message body.
 * 2) Normal messages. These have an id, a destination address, and a
 *    message body.
 */
public class InMessageImpl implements InMessage {
  private byte[] id;
  private PayloadImpl payload;
  private byte[] address;
  private InMessageType type;

  public enum InMessageType {
    CONTROL,
    MESSAGE
  }

  /**
   * Create a InMessageImpl with just an id and payload. This is
   * treated as a "control" message, and isn't forwarded
   * to an EventBus channel.
   *  @param id The id
   * @param payload The payload.
   */
  public InMessageImpl(byte[] id, PayloadImpl payload) {
    this.id = id;
    this.payload = payload;
    this.address = null;
    type = InMessageType.CONTROL;
  }

  /**
   * Create a InMessageImpl for a standard (non-control) message.
   *  @param id The internal ID of the message.
   * @param address The destination Event Bus address.
   * @param payload The message contents.
   */
  public InMessageImpl(byte[] id, byte[] address, PayloadImpl payload) {
    this.id = id;
    this.address = address;
    this.payload = payload;
    type = InMessageType.MESSAGE;
  }

  /**
   * Serialize the InMessageImpl and send it out over a ZMQ.Socket.
   *
   * @param socket The socket to send the InMessageImpl to.
   */
  public void sendMessage(ZMQ.Socket socket) {
    socket.send(id, ZMQ.SNDMORE);
    if (!isControl()) {
      socket.send(address, ZMQ.SNDMORE);
    }
    socket.send(payload.getMsg(), 0);
  }

  /**
   * Returns true if this InMessageImpl is InMessageType.CONTROL.
   *
   * @return true if its a CONTROL InMessageImpl, false otherwise.
   */
  public boolean isControl() {
    return type.equals(InMessageType.CONTROL);
  }

  /**
   * Get the id of the InMessageImpl.
   *
   * @return The id.
   */
  public byte[] getId() {
    return id;
  }

  /**
   * Get the message contents of the InMessageImpl.
   *
   * @return The message contents.
   */
  public Payload getPayload() {
    return payload;
  }

  @Override
  public byte[] getControlMessage() throws IllegalStateException {
    if (!isControl()) {
      throw new IllegalStateException("Not a control message");
    }
    return payload.getMsg();
  }

  /**
   * Get the destination address of the InMessageImpl.
   *
   * @return The address the message was sent to. Can be null,
   * if the message is a control message.
   */
  public byte[] getAddress() {
    return address;
  }

  /**
   * Get the message destination address as a string.
   *
   * @return The address as a string.
   */
  public String getAddressAsString() {
    return new String(address);
  }

  @Override
  public String toString() {
    return "InMessageImpl{" +
        "id=" + ((id == null) ? null : new String(id)) +
        ", address=" + ((address == null) ? null : new String(address)) +
        ", msg=" + ((payload == null) ? null : new String(payload.getMsg())) +
        ", type=" + type +
        '}';
  }
}
