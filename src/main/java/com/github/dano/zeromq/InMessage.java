package com.github.dano.zeromq;


import org.zeromq.ZMQ;

/**
 * Represents a message received from a 0MQ client.
 */
public class InMessage {
  private byte[] id;
  private byte[] msg;
  private byte[] address;
  private InMessageType type;

  public enum InMessageType {
    CONTROL,
    MESSAGE
  }

  /**
   * Create a InMessage with just an id and msg. This is
   * treated as a "control" message, and isn't forwarded
   * to an EventBus channel.
   *
   * @param id The id
   * @param msg The message.
   */
  public InMessage(byte[] id, byte[] msg) {
    this.id = id;
    this.msg = msg;
    this.address = null;
    type = InMessageType.CONTROL;
  }

  /**
   * Create a InMessage for a standard (non-control) message.
   *
   * @param id The internal ID of the message.
   * @param address The reply address.
   * @param msg The message contents.
   */
  public InMessage(byte[] id, byte[] address, byte[] msg) {
    this.id = id;
    this.address = address;
    this.msg = msg;
    type = InMessageType.MESSAGE;
  }

  /**
   * Create a InMessage from a ZMQ.Socket. This could result in a InMessage
   * with type CONTROL or MESSAGE, depending on what's in the Socket.
   *
   * @param socket The socket to read the InMessage from.
   * @return The InMessage.
   */
  public static InMessage fromSocket(ZMQ.Socket socket) {
    byte[] id = socket.recv(0);
    if (!socket.hasReceiveMore()) {
      return new InMessage(id, null);
    }
    // Frame two might be the message, or the reply address.
    byte[] frame2 = socket.recv();
    if (socket.hasReceiveMore()) {
      return new InMessage(id, frame2, socket.recv(0));
    } else {
      return new InMessage(id, frame2);
    }
  }

  /**
   * Serialize the InMessage and send it out over a ZMQ.Socket.
   *
   * @param socket The socket to send the InMessage to.
   */
  public void sendMessage(ZMQ.Socket socket) {
    socket.send(id, ZMQ.SNDMORE);
    if (!isControl()) {
      socket.send(address, ZMQ.SNDMORE);
    }
    socket.send(msg, 0);
  }

  /**
   * Returns true if this InMessage is InMessageType.CONTROL.
   *
   * @return true if its a CONTROL InMessage, false otherwise.
   */
  public boolean isControl() {
    return type.equals(InMessageType.CONTROL);
  }

  /**
   * Get the id of the InMessage.
   *
   * @return The id.
   */
  public byte[] getId() {
    return id;
  }

  /**
   * Get the message contents of the InMessage.
   *
   * @return The message contents.
   */
  public byte[] getMsg() {
    return msg;
  }

  /**
   * Get the reply address of the InMessage.
   *
   * @return The address the message was sent to. Can be null.
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
    return "InMessage{" +
        "id=" + ((id == null) ? null : new String(id)) +
        ", address=" + ((address == null) ? null : new String(address)) +
        ", msg=" + ((msg == null) ? null : new String(msg)) +
        ", type=" + type +
        '}';
  }
}
