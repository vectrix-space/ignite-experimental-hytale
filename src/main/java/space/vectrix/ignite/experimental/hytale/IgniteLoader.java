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
package space.vectrix.ignite.experimental.hytale;

import com.hypixel.hytale.plugin.early.ClassTransformer;
import com.hypixel.hytale.plugin.early.EarlyPluginLoader;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.tinylog.Logger;
import space.vectrix.ignite.experimental.hytale.ember.Ember;
import space.vectrix.ignite.experimental.hytale.ember.EmberEnvironment;
import space.vectrix.ignite.experimental.hytale.ember.EmberMixinBootstrap;
import space.vectrix.ignite.experimental.hytale.ember.EmberMixinService;
import space.vectrix.ignite.experimental.hytale.ember.TransformPhase;
import space.vectrix.ignite.experimental.hytale.mod.ModCandidate;
import space.vectrix.ignite.experimental.hytale.mod.ModLoader;
import space.vectrix.ignite.experimental.hytale.mod.ModLocator;

@SuppressWarnings("unused")
public final class IgniteLoader implements ClassTransformer {
  public IgniteLoader() {
    try {
      Logger.info("[IgniteLoader] Setting up environment...");

      // Ensure the system loader has been added to the environment.
      final Path targetPath = Paths.get(EarlyPluginLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      EmberEnvironment.INSTANCE.add(EmberEnvironment.Stage.SYSTEM, EarlyPluginLoader.class.getClassLoader());

      // Ensure the early plugin loader has been added to the environment.
      final Path loaderPath = Paths.get(IgniteLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      EmberEnvironment.INSTANCE.add(EmberEnvironment.Stage.EARLY_PLUGIN, IgniteLoader.class.getClassLoader());

      // Find the root game directory.
      final Path gamePath = targetPath.getParent();

      // Add this mod path to the mod loader, before we create the loader.
      try {
        final Path modDirectory = gamePath.resolve("./mods");

        final ClassLoader loader = EmberEnvironment.INSTANCE.find(EmberEnvironment.Stage.EARLY_PLUGIN);
        if(loader == null) throw new IllegalStateException("Unable to locate early plugin loader!");

        final List<ModCandidate> candidates = ModLocator.locate(List.of(modDirectory), loader);
        final String candidatePaths = candidates.stream()
          .map(candidate -> candidate.path().toString())
          .collect(Collectors.joining(File.pathSeparator, File.pathSeparator, ""));

        // Append the loader jar to the java.class.path system property.
        final String currentClassPath = System.getProperty("java.class.path", "");
        final String newClassPath = currentClassPath.isEmpty()
          ? loaderPath + candidatePaths
          : currentClassPath + File.pathSeparator + loaderPath + candidatePaths;

        System.setProperty("java.class.path", newClassPath);

        ModLoader.add(candidates);
      } catch(final Throwable exception) {
        throw new IllegalStateException("Unable to add ignite as a mod candidate!", exception);
      }

      System.setProperty("java.util.logging.manager", "com.hypixel.hytale.logger.backend.HytaleLogManager");
      System.setProperty("mixin.bootstrapService", EmberMixinBootstrap.class.getName());
      System.setProperty("mixin.service", EmberMixinService.class.getName());

      Logger.info("[IgniteLoader] Starting loader...");

      // Bootstrap the transformer system.
      Ember.INSTANCE.bootstrap();
    } catch(final Exception exception) {
      Logger.error(exception, "[IgniteLoader] Unable to load due to an error: ");
    }
  }

  @Override
  public int priority() {
    // Apply this transformer after most others.
    return -50;
  }

  @Override
  public byte@Nullable [] transform(final String name, final String path, final byte[] bytes) {
    try {
      // Ensure the game stage has been added to the environment.
      EmberEnvironment.INSTANCE.add(EmberEnvironment.Stage.GAME, Thread.currentThread().getContextClassLoader());

      // Transform the target class to apply modifications.
      return Ember.INSTANCE.transformer().apply(name, bytes, TransformPhase.INITIALIZE);
    } catch(final Exception exception) {
      Logger.error(exception, "[IgniteLoader] Unable to transform class '{}' at '{}' due to an error: ", name, path);
    }

    return bytes;
  }
}
