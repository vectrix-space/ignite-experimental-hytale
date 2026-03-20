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
package space.vectrix.ignite.experimental.hytale.ember;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.tinylog.Logger;
import space.vectrix.ignite.experimental.hytale.util.IgniteConstants;

public final class EmberTransformer {
  private static final List<String> EXCLUDED_RESOURCES = List.of(
    "org/tinylog/",
    "org/objectweb/asm/",
    "org/spongepowered/asm/",
    "org/spongepowered/include/",
    "org/spongepowered/tools/",
    "com/llamalad7/mixinextras/",
    "net/fabricmc/accesswidener/"
  );

  private final Map<Class<? extends TransformerService>, TransformerService> transformers = new IdentityHashMap<>();

  /* package */ EmberTransformer() {
    final ServiceLoader<TransformerService> serviceLoader = ServiceLoader.load(TransformerService.class, Ember.class.getClassLoader());
    for(final TransformerService service : serviceLoader) {
      this.transformers.put(service.getClass(), service);
    }
  }

  public byte[] apply(final String className, final byte[] input, final TransformPhase phase) {
    final String internalName = className.replace('.', '/');

    // If we need to generate synthetic classes, skip the excluded resources.
    if(phase != TransformPhase.GENERATION) {
      for(final String excludeResource : EmberTransformer.EXCLUDED_RESOURCES) {
        if(internalName.startsWith(excludeResource)) return input;
      }
    }

    ClassNode node = new ClassNode(IgniteConstants.ASM_VERSION);

    final Type type = Type.getObjectType(internalName);
    if(input.length > 0) {
      final ClassReader reader = new ClassReader(input);
      reader.accept(node, 0);
    } else {
      node.name = type.getInternalName();
      node.version = MixinEnvironment.getCompatibilityLevel().getClassVersion();
      node.superName = "java/lang/Object";
    }

    final List<TransformerService> transformers = this.order(phase);

    boolean transformed = false;
    for(final TransformerService service : transformers) {
      try {
        // If the transformer should not transform the class, skip it.
        if(!service.shouldTransform(type, node)) continue;

        // Attempt to transform the class.
        final ClassNode transformedNode = service.transform(type, node, phase);
        if(transformedNode != null) {
          node = transformedNode;
          transformed = true;
        }
      } catch(final Throwable throwable) {
        Logger.error(throwable, "Failed to transform {} with {}", type.getClassName(), service.getClass().getName());
      }
    }

    if(!transformed) return input;

    final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    node.accept(writer);

    return writer.toByteArray();
  }

  public <T extends TransformerService> @Nullable T get(final Class<T> transformer) {
    return transformer.cast(this.transformers.get(transformer));
  }

  public Collection<TransformerService> getAll() {
    return Collections.unmodifiableCollection(this.transformers.values());
  }

  private List<TransformerService> order(final TransformPhase phase) {
    return this.transformers.values().stream()

      // Filter out transformers that do not apply to the given phase.
      .filter(value -> value.priority(phase) != -1)

      .sorted((first, second) -> {
        final int firstPriority = first.priority(phase);
        final int secondPriority = second.priority(phase);
        return Integer.compare(firstPriority, secondPriority);
      })

      .toList();
  }
}
