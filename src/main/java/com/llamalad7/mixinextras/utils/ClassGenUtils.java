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
package com.llamalad7.mixinextras.utils;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import space.vectrix.ignite.experimental.hytale.util.IgniteClassDefiner;

public final class ClassGenUtils {
  private static final Map<String, byte[]> DEFINITIONS = new HashMap<>();

  private static final Definer DEFINER;

  static {
    DEFINER = (canonicalName, bytes, scope) -> {
      // Give the class to Ignite to define on the correct class loader.
      IgniteClassDefiner.defineClass(canonicalName, bytes);
    };
  }

  private ClassGenUtils() {
  }

  public static void defineClass(final ClassNode node, final MethodHandles.Lookup scope) {
    final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    node.accept(writer);

    final String internalName = node.name.replace('.', '/');
    final String canonicalName = internalName.replace('/', '.');
    final byte[] bytes = writer.toByteArray();
    try {
      ClassGenUtils.DEFINER.define(canonicalName, bytes, scope);
    } catch(final Throwable throwable) {
      throw new RuntimeException(
        String.format(
          "Failed to define class %s from %s! Please report to LlamaLad7!",
          internalName, scope
        ),
        throwable
      );
    }

    ClassGenUtils.DEFINITIONS.put(canonicalName, bytes);

    MixinInternals.registerClassInfo(node);
    MixinInternals.getExtensions().export(MixinEnvironment.getCurrentEnvironment(), internalName, false, node);
  }

  public static Map<String, byte[]> getDefinitions() {
    return Collections.unmodifiableMap(ClassGenUtils.DEFINITIONS);
  }

  @FunctionalInterface
  private interface Definer {
    void define(final String name, final byte[] bytes, final MethodHandles.Lookup scope) throws Throwable;
  }
}
