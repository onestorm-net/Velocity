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

package com.velocitypowered.proxy.config.migration.onestorm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BrandTemplateMigrationTest {

  private static final String OLD_CONFIG = """
      config-version = "2.9"
      bind = "0.0.0.0:25565"

      [advanced]
      compression-threshold = 256
      """;

  @TempDir
  Path dir;

  private CommentedFileConfig load(Path path) {
    CommentedFileConfig config = CommentedFileConfig.builder(path)
        .preserveInsertionOrder()
        .sync()
        .build();
    config.load();
    return config;
  }

  private Path migrate(String contents) throws IOException {
    Path path = dir.resolve("velocity.toml");
    Files.writeString(path, contents);
    try (CommentedFileConfig config = load(path)) {
      BrandTemplateMigration migration = new BrandTemplateMigration();
      assertTrue(migration.shouldMigrate(config));
      migration.migrate(config, LogManager.getLogger(BrandTemplateMigrationTest.class));
      config.save();
    }
    return path;
  }

  @Test
  void addsBrandTemplateAndVersionAtTopLevel() throws IOException {
    Path path = migrate(OLD_CONFIG);

    try (CommentedFileConfig config = load(path)) {
      assertEquals("%backend_brand% (Velocity)", config.get("brand-template"));
      assertEquals(1, config.getInt(StormConfigurationMigration.VERSION_KEY));
      assertFalse(config.contains("advanced.brand-template"));
      assertFalse(config.contains("advanced." + StormConfigurationMigration.VERSION_KEY));
      // upstream settings are left alone
      assertEquals("2.9", config.get("config-version"));
      assertEquals(256, config.getInt("advanced.compression-threshold"));
      assertFalse(new BrandTemplateMigration().shouldMigrate(config));
    }
  }

  @Test
  void keepsExistingBrandTemplate() throws IOException {
    Path path = migrate("brand-template = \"Custom\"\n" + OLD_CONFIG);

    try (CommentedFileConfig config = load(path)) {
      assertEquals("Custom", config.get("brand-template"));
    }
  }

  @Test
  void defaultConfigIsAlreadyUpToDate() throws IOException {
    Path path = dir.resolve("velocity.toml");
    try (var in = BrandTemplateMigrationTest.class.getClassLoader()
        .getResourceAsStream("default-velocity.toml")) {
      Files.copy(in, path);
    }

    try (CommentedFileConfig config = load(path)) {
      assertFalse(new BrandTemplateMigration().shouldMigrate(config));
    }
  }
}
