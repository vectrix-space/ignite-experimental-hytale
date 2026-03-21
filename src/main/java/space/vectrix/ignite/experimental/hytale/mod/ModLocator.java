/*
 * This file is part of Ignite Experimental Hytale, licensed under the MIT License
 * (MIT).
 *
 * Copyright (c) vectrix.space <https://vectrix.space/>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package space.vectrix.ignite.experimental.hytale.mod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.tinylog.Logger;
import space.vectrix.ignite.experimental.hytale.mod.config.HyxinConfig;
import space.vectrix.ignite.experimental.hytale.mod.config.IgniteConfig;
import space.vectrix.ignite.experimental.hytale.mod.config.PluginManifest;
import space.vectrix.ignite.experimental.hytale.util.IgniteConstants;

public final class ModLocator {
  private static final Gson GSON = new GsonBuilder().create();

  public static List<ModCandidate> locate(final List<Path> directories, final ClassLoader loader) throws IOException {
    final List<ModCandidate> candidates = new ArrayList<>();

    for(final Path search : directories) {
      try(final Stream<Path> paths = Files.walk(search)) {
        final List<Path> possibleCandidates = paths
          .filter(Files::isRegularFile)
          .filter(file -> file.toFile().getPath().endsWith(".jar"))
          .peek(path -> Logger.debug("Found possible mod candidate: {}", path))
          .toList();

        for(final Path possibleCandidate : possibleCandidates) {
          try(final FileSystem fileSystem = FileSystems.newFileSystem(possibleCandidate, loader)) {
            final Path manifestPath = fileSystem.getPath(IgniteConstants.MANIFEST_JSON);
            if(!Files.exists(manifestPath)) continue;

            final PluginManifest config;
            try(final InputStreamReader reader = new InputStreamReader(Files.newInputStream(manifestPath))) {
              config = GSON.fromJson(reader, PluginManifest.class);
            }

            final ModCandidate candidate = new ModCandidate(config.name(), possibleCandidate);

            final IgniteConfig igniteConfig = config.ignite();
            if(igniteConfig != null) {
              for(final String mixin : igniteConfig.mixins()) {
                candidate.mixin(mixin);
              }

              for(final String widener : igniteConfig.wideners()) {
                candidate.widener(widener);
              }
            }

            final HyxinConfig hyxinConfig = config.hyxin();
            if(hyxinConfig != null) {
              for(final String mixin : hyxinConfig.configs()) {
                candidate.mixin(mixin);
              }
            }

            candidates.add(candidate);
          }
        }
      }
    }

    return candidates;
  }

  private ModLocator() {
  }
}
