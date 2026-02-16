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
package space.vectrix.ignite.experimental.hytale.ember.transformer;

import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.ISyntheticClassRegistry;
import org.spongepowered.asm.transformers.MixinClassReader;
import space.vectrix.ignite.experimental.hytale.ember.TransformPhase;
import space.vectrix.ignite.experimental.hytale.ember.TransformerService;
import space.vectrix.ignite.experimental.hytale.util.IgniteConstants;

public final class EmberMixinTransformer implements TransformerService {
  private @Nullable IMixinTransformerFactory factory;

  private @UnknownNullability IMixinTransformer transformer;
  private @UnknownNullability ISyntheticClassRegistry registry;

  public void offer(final IMixinTransformerFactory factory) {
    this.factory = factory;
  }

  @Override
  public void prepare() {
    if(this.factory == null) throw new IllegalStateException("Unable to prepare mixin transformer, as the factory is not available!");
    this.transformer = this.factory.createTransformer();
    this.registry = this.transformer.getExtensions().getSyntheticClassRegistry();
  }

  @Override
  public int priority(final TransformPhase phase) {
    // We don't want mixin trying to transform targets it's already
    // transforming.
    if(phase == TransformPhase.MIXIN) return -1;
    // This prioritizes mixin in the middle of the transformation
    // pipeline.
    return 50;
  }

  @Override
  public boolean shouldTransform(final Type type, final ClassNode node) {
    // We allow mixin to decide whether something should be transformed now.
    return true;
  }

  @Override
  public @Nullable ClassNode transform(final Type type, final ClassNode node, final TransformPhase phase) {
    if(this.shouldGenerateClass(type)) {
      return this.generateClass(type, node) ? node : null;
    }

    return this.transformer.transformClass(MixinEnvironment.getCurrentEnvironment(), type.getClassName(), node) ? node : null;
  }

  public ClassNode classNode(final String canonicalName, final String internalName, final byte[] input, final int readerFlags) throws ClassNotFoundException {
    if(input.length != 0) {
      final ClassReader reader = new MixinClassReader(input, canonicalName);
      final ClassNode node = new ClassNode(IgniteConstants.ASM_VERSION);
      reader.accept(node, readerFlags);
      return node;
    }

    final Type type = Type.getObjectType(internalName);
    if(this.shouldGenerateClass(type)) {
      final ClassNode node = new ClassNode(IgniteConstants.ASM_VERSION);
      if(this.generateClass(type, node)) return node;
    }

    throw new ClassNotFoundException(canonicalName);
  }

  private boolean shouldGenerateClass(final Type type) {
    return this.registry.findSyntheticClass(type.getClassName()) != null;
  }

  private boolean generateClass(final Type type, final ClassNode node) {
    return this.transformer.generateClass(MixinEnvironment.getCurrentEnvironment(), type.getClassName(), node);
  }
}
