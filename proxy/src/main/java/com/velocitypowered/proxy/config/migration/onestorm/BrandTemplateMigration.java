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
import org.apache.logging.log4j.Logger;

/**
 * Creation of the configuration option "brand-template".
 */
public final class BrandTemplateMigration implements StormConfigurationMigration {
  @Override
  public boolean shouldMigrate(final CommentedFileConfig config) {
    return stormConfigVersion(config) < 1;
  }

  @Override
  public void migrate(final CommentedFileConfig config, final Logger logger) {
    config.set("brand-template", config.getOrElse("brand-template", "%backend_brand% (Velocity)"));
    config.setComment("brand-template", """
            The brand shown to clients in the F3 debug screen.
            Use %backend_brand% to insert the brand sent by the backend server.""");
    config.set(VERSION_KEY, 1);
    config.setComment(VERSION_KEY, "Fork config version. Do not change this");
  }
}
