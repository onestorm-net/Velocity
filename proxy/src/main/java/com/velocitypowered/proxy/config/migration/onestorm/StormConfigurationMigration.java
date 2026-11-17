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

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.io.IOException;
import org.apache.logging.log4j.Logger;

/**
 * Configuration migration for options specific to this fork.
 *
 * <p>These migrations track their own version in {@value #VERSION_KEY}, separate from
 * upstream's {@code config-version}, so the two version numbers never conflict.
 */
public sealed interface StormConfigurationMigration
        permits BrandTemplateMigration {

  /**
   * The configuration key holding the fork configuration version.
   */
  String VERSION_KEY = "onestorm-config-version";

  boolean shouldMigrate(CommentedFileConfig config);

  void migrate(CommentedFileConfig config, Logger logger) throws IOException;

  /**
   * Gets the fork configuration version.
   *
   * @param config the configuration.
   * @return the fork configuration version, or 0 if it is not set
   */
  default int stormConfigVersion(CommentedFileConfig config) {
    return config.getIntOrElse(VERSION_KEY, 0);
  }
}
