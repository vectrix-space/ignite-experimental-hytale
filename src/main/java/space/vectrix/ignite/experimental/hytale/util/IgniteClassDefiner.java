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
package space.vectrix.ignite.experimental.hytale.util;

import org.jspecify.annotations.Nullable;
import org.tinylog.Logger;
import space.vectrix.ignite.experimental.hytale.ember.Ember;
import space.vectrix.ignite.experimental.hytale.ember.EmberEnvironment;
import space.vectrix.ignite.experimental.hytale.ember.TransformPhase;

import java.lang.invoke.MethodHandles;

public final class IgniteClassDefiner {
  private static final byte[] EMPTY_CLASS = new byte[0];

  public static void defineClass(final String canonicalName, final byte[] output) {
    final ClassLoader loader = EmberEnvironment.INSTANCE.find(EmberEnvironment.Stage.GAME);
    if(loader == null) {
      Logger.error("Unable to acquire class loader to define class: {}", canonicalName);
      return;
    }

    // Find the host class to define this synthetic class next to.
    final Class<?> hostClass = IgniteClassDefiner.findAttachClass(canonicalName, loader);
    if(hostClass == null) {
      Logger.error("Unable to locate host class for: {}", canonicalName);
      return;
    }

    // Define the synthetic class into the class loader.
    try {
      final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(hostClass, MethodHandles.lookup());
      lookup.defineClass(output);
    } catch (IllegalAccessException | IllegalArgumentException exception) {
      Logger.error(exception, "Unable to define synthetic class inside class loader: ");
    }
  }

  public static void defineSyntheticClass(final String internalName) {
    final String canonicalName = internalName.replace('/', '.');

    final ClassLoader loader = EmberEnvironment.INSTANCE.find(EmberEnvironment.Stage.GAME);
    if(loader == null) {
      Logger.error("Unable to acquire class loader to define class: {}", canonicalName);
      return;
    }

    // Find the host class to define this synthetic class next to.
    final Class<?> hostClass = IgniteClassDefiner.findAttachClass(canonicalName, loader);
    if(hostClass == null) {
      Logger.error("Unable to locate host class for: {}", canonicalName);
      return;
    }

    // Generate the synthetic class by applying the transformer.
    final byte[] output = Ember.INSTANCE.transformer().apply(
      internalName,
      IgniteClassDefiner.EMPTY_CLASS,
      TransformPhase.GENERATION
    );

    // Define the synthetic class into the class loader.
    try {
      final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(hostClass, MethodHandles.lookup());
      lookup.defineClass(output);
    } catch (IllegalAccessException | IllegalArgumentException exception) {
      Logger.error(exception, "Unable to define synthetic class inside class loader: ");
    }
  }

  private static @Nullable Class<?> findAttachClass(final String canonicalName, final ClassLoader loader) {
    String packageName = canonicalName;
    int lastDot; Class<?> candidate;

    while((lastDot = packageName.lastIndexOf('.')) > 0) {
      packageName = packageName.substring(0, lastDot);

      try {
        candidate = Class.forName(packageName + ".package-info", false, loader);
      } catch(final ClassNotFoundException ignored) {
        continue;
      }

      return candidate;
    }

    return null;
  }

  private IgniteClassDefiner() {
  }
}
