/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq.impl;

import com.github.dano.zeromq.Payload;

/**
 * A Payload implementation.
 */
public class PayloadImpl implements Payload {

  private final byte[] msg;

  public PayloadImpl(byte[] msg)  {
    this.msg = msg;
  }

  public byte[] getMsg() {
    return msg;
  }
}
