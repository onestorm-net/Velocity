/*
 * Copyright (C) 2019-2021 Velocity Contributors
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

package com.velocitypowered.proxy.protocol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class PluginMessageUtilTest {

  @Test
  void transformLegacyToModernChannelWorksWithModern() {
    assertEquals("minecraft:brand", PluginMessageUtil
        .transformLegacyToModernChannel("minecraft:brand"));
    assertEquals("velocity:test", PluginMessageUtil
        .transformLegacyToModernChannel("velocity:test"));
  }

  @Test
  void transformLegacyToModernChannelRewritesSpecialCases() {
    assertEquals("minecraft:brand", PluginMessageUtil
        .transformLegacyToModernChannel("MC|Brand"));
    assertEquals("minecraft:register", PluginMessageUtil
        .transformLegacyToModernChannel("REGISTER"));
    assertEquals("minecraft:unregister", PluginMessageUtil
        .transformLegacyToModernChannel("UNREGISTER"));
    assertEquals("bungeecord:main", PluginMessageUtil
        .transformLegacyToModernChannel("BungeeCord"));
  }

  @Test
  void transformLegacyToModernChannelRewritesGeneral() {
    assertEquals("legacy:example", PluginMessageUtil
        .transformLegacyToModernChannel("Example"));
    assertEquals("legacy:pskeepalive", PluginMessageUtil
        .transformLegacyToModernChannel("PS|KeepAlive"));
  }

  private static PluginMessagePacket brandPacket(String channel, String brand) {
    ByteBuf buf = Unpooled.buffer();
    ProtocolUtils.writeString(buf, brand);
    return new PluginMessagePacket(channel, buf);
  }

  private static String rewrite(String backendBrand, ProtocolVersion version, String template) {
    PluginMessagePacket original = brandPacket("minecraft:brand", backendBrand);
    PluginMessagePacket rewritten = PluginMessageUtil.rewriteMinecraftBrand(
        original, version, template);
    try {
      return ProtocolUtils.readString(rewritten.content().slice());
    } finally {
      original.release();
      rewritten.release();
    }
  }

  @Test
  void rewriteMinecraftBrandReplacesPlaceholder() {
    assertEquals("Paper (Velocity)", rewrite("Paper",
        ProtocolVersion.MAXIMUM_VERSION, "%backend_brand% (Velocity)"));
  }

  @Test
  void rewriteMinecraftBrandReplacesEveryPlaceholder() {
    assertEquals("Paper / Paper", rewrite("Paper",
        ProtocolVersion.MAXIMUM_VERSION, "%backend_brand% / %backend_brand%"));
  }

  @Test
  void rewriteMinecraftBrandUsesTemplateWithoutPlaceholderAsIs() {
    assertEquals("My Network", rewrite("Paper",
        ProtocolVersion.MAXIMUM_VERSION, "My Network"));
  }

  @Test
  void rewriteMinecraftBrandWritesLegacyFormatFor17Clients() {
    PluginMessagePacket original = brandPacket("MC|Brand", "vanilla");
    PluginMessagePacket rewritten = PluginMessageUtil.rewriteMinecraftBrand(
        original, ProtocolVersion.MINECRAFT_1_7_2, "%backend_brand% (Velocity)");
    try {
      assertEquals("MC|Brand", rewritten.getChannel());
      // 1.7 brand messages have no length prefix
      assertEquals("vanilla (Velocity)",
          rewritten.content().toString(StandardCharsets.UTF_8));
    } finally {
      original.release();
      rewritten.release();
    }
  }

  @Test
  void rewriteMinecraftBrandRejectsNonBrandMessages() {
    PluginMessagePacket other = brandPacket("velocity:test", "Paper");
    try {
      assertThrows(IllegalArgumentException.class, () -> PluginMessageUtil
          .rewriteMinecraftBrand(other, ProtocolVersion.MAXIMUM_VERSION, "%backend_brand%"));
    } finally {
      other.release();
    }
  }
}
