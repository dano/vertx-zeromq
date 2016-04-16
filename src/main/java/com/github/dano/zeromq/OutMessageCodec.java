package com.github.dano.zeromq;

import io.vertx.core.buffer.Buffer;

/**
 * A codec used to serlialize OutMessage instances on the Vert.x EventBus.
 */
public class OutMessageCodec extends BaseCodec<OutMessage> {

  @Override
  public void encodeToWire(Buffer buffer, OutMessage obj) {
    int length = obj.isRepliable() ? 3 : 2;
    buffer.appendInt(length);
    addBytes(buffer, obj.getId());
    addBytes(buffer, obj.getMsg());
    if (obj.isRepliable()) {
      addBytes(buffer, obj.getReplyAddress());
    }
  }

  @Override
  public OutMessage decodeFromWire(int pos, Buffer buffer) {
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
      // Get the reply address, if present.
      int addressLength = buffer.getInt(pos);
      pos +=4;
      byte[] replyAddress = buffer.getBytes(pos, addressLength);

      return new OutMessage(id, msg, replyAddress);
    } else {
      return new OutMessage(id, msg);
    }
  }
}
