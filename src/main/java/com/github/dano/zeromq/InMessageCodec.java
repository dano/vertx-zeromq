package com.github.dano.zeromq;

import io.vertx.core.buffer.Buffer;

/**
 * A codec used to serialize InMessage instances on the Vert.x EventBus.
 */
public class InMessageCodec extends BaseCodec<InMessage> {

  @Override
  public void encodeToWire(Buffer buffer, InMessage obj) {
    int length = obj.isControl() ? 2 : 3;
    buffer.appendInt(length);
    addBytes(buffer, obj.getId());
    addBytes(buffer, obj.getMsg());
    if (!obj.isControl()) {
      addBytes(buffer, obj.getAddress());
    }
  }

  @Override
  public InMessage decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    pos += 4;

    // Get ID
    int idLength = buffer.getInt(pos);
    pos += 4;

    byte[] id = buffer.getBytes(pos, idLength);
    pos += idLength;

    // Get the msg
    int msgLength = buffer.getInt(pos);
    pos += 4;

    byte[] msg = buffer.getBytes(pos, msgLength);
    pos += msgLength;

    if (length == 3) {
      // Get the address
      int addressLength = buffer.getInt(pos);
      pos +=4;
      byte[] address = buffer.getBytes(pos, addressLength);

      return new InMessage(id, address, msg);
    } else {
      return new InMessage(id, msg);
    }
  }
}
