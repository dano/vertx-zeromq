package com.github.dano.zeromq;

/**
 * PayloadFactory interface. Used to create a Payload.
 * @param <T> The type of the Payload returned by this factory.
 */
public interface PayloadFactory<T> {
  /**
   * Build a new Payload from a byte array.
   *
   * @param msg The byte array to construct the Payload from.
   * @return A Payload.
   */
  Payload fromBytes(byte[] msg);

  /**
   * The type of Payload returned from this factory.
   *
   * @return The Type of the Payload.
   */
  Class<T> getPayloadType();
}
