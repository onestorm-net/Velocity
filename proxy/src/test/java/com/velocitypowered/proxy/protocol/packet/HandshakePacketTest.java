/*
 * Copyright (C) 2026 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.protocol.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

class HandshakePacketTest {

  private static HandshakePacket decode(int protocolVersion) {
    ByteBuf buf = Unpooled.buffer();
    try {
      ProtocolUtils.writeVarInt(buf, protocolVersion);
      ProtocolUtils.writeString(buf, "localhost");
      buf.writeShort(25565);
      ProtocolUtils.writeVarInt(buf, 1); // status
      HandshakePacket packet = new HandshakePacket();
      packet.decode(buf, ProtocolUtils.Direction.SERVERBOUND, ProtocolVersion.MINIMUM_VERSION);
      return packet;
    } finally {
      buf.release();
    }
  }

  @Test
  void knownVersionKeepsRawValue() {
    int protocol = ProtocolVersion.MAXIMUM_VERSION.getProtocol();
    HandshakePacket packet = decode(protocol);

    assertEquals(protocol, packet.getRawProtocolVersion());
    assertEquals(ProtocolVersion.MAXIMUM_VERSION, packet.getProtocolVersion());
  }

  @Test
  void unsupportedVersionKeepsRawValue() {
    int protocol = ProtocolVersion.MAXIMUM_VERSION.getProtocol() + 1000;
    HandshakePacket packet = decode(protocol);

    assertEquals(protocol, packet.getRawProtocolVersion());
    assertEquals(ProtocolVersion.UNKNOWN, packet.getProtocolVersion());
  }

  @Test
  void queryPingVersionKeepsNegativeRawValue() {
    HandshakePacket packet = decode(-1);

    assertEquals(-1, packet.getRawProtocolVersion());
    assertEquals(ProtocolVersion.UNKNOWN, packet.getProtocolVersion());
  }
}
