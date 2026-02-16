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
package org.spongepowered.asm.transformer;

import org.spongepowered.asm.mixin.throwables.MixinError;
import org.spongepowered.asm.service.ISyntheticClassInfo;
import org.spongepowered.asm.service.ISyntheticClassRegistry;
import space.vectrix.ignite.experimental.hytale.util.IgniteClassDefiner;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SyntheticClassRegistry implements ISyntheticClassRegistry {
  private final Map<String, ISyntheticClassInfo> classes = new HashMap<>();

  /* package */ SyntheticClassRegistry() {
  }

  @Override
  public ISyntheticClassInfo findSyntheticClass(final String name) {
    if(name == null) return null;

    final String internalName = name.replace('.', '/');
    return this.classes.get(internalName);
  }

  /**
   * (Overrides)
   */
  /* package */ void registerSyntheticClass(final ISyntheticClassInfo classInfo) {
    final String name = classInfo.getName();
    final String internalName = name.replace('.', '/');

    final ISyntheticClassInfo other = this.classes.get(internalName);
    if(other != null) {
      if(classInfo == other) return;

      throw new MixinError(String.format(
        "Synthetic class with name %s was already registered by %s. Duplicate being registered by %s.",
        internalName,
        other.getMixin(),
        classInfo.getMixin()
      ));
    }

    this.classes.put(internalName, classInfo);

    // Define the synthetic class into the class loader.
    IgniteClassDefiner.defineSyntheticClass(internalName);
  }
}
