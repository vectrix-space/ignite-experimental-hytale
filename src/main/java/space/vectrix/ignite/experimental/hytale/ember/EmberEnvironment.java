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

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.jspecify.annotations.Nullable;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public final class EmberEnvironment {
  public static final EmberEnvironment INSTANCE = new EmberEnvironment();

  private final EnumMap<Stage, ClassLoader> loaders = new EnumMap<>(Stage.class);
  private final LinkedList<LoaderStage> loaderList = new LinkedList<>();

  public boolean add(final Stage stage, final @Nullable ClassLoader loader) {
    requireNonNull(stage, "stage");

    if(loader == null || this.loaders.containsKey(stage)) return false;

    // Check against the previous stages.
    for(final LoaderStage next : this.loaderList) {
      // The stage loader has already been added here.
      if(next.loader() == loader) return false;

      // The parent of an existing loader cannot be registered here.
      ClassLoader parent = next.loader();
      while((parent = parent.getParent()) != null) {
        if(parent == loader) return false;
      }
    }

    // Ensure this stage is not a previous stage.
    this.loaders.put(stage, loader);
    this.loaderList.addLast(new LoaderStage(stage, loader));

    Logger.info("Added environment stage '{}' for loader: {}", stage, loader);
    return true;
  }

  public @Nullable ClassLoader findForClass(final String name) {
    requireNonNull(name, "name");

    final String canonicalName = name.replace('/', '.').replace(".class", "");
    final String resourceName = canonicalName.replace('.', '/').concat(".class");

    return this.findFor(resourceName);
  }

  public @Nullable ClassLoader findFor(final String resourceName) {
    requireNonNull(resourceName, "resourceName");

    final Iterator<LoaderStage> iterator = this.loaderList.descendingIterator();
    while(iterator.hasNext()) {
      final LoaderStage next = iterator.next();
      if(next.stage() == Stage.GAME && resourceName.contains("MixinPlugin")) continue;
      if(next.loader().getResource(resourceName) == null) continue;
      return next.loader();
    }

    return null;
  }

  public @Nullable ClassLoader find(final Stage stage) {
    requireNonNull(stage, "stage");

    final ClassLoader loader = this.loaders.get(stage);
    if(loader != null) return loader;

    final Iterator<LoaderStage> iterator = this.loaderList.descendingIterator();
    while(iterator.hasNext()) {
      final LoaderStage next = iterator.next();
      if(next.stage().priority() <= stage.priority()) continue;
      return next.loader();
    }

    return null;
  }

  public @Nullable InputStream findStream(final String resource) {
    requireNonNull(resource, "resource");

    final Iterator<LoaderStage> iterator = this.loaderList.descendingIterator();

    InputStream stream;
    while(iterator.hasNext()) {
      final LoaderStage loaderStage = iterator.next();
      final ClassLoader loader = loaderStage.loader();

      if((stream = loader.getResourceAsStream(resource)) != null) {
        return stream;
      }
    }

    return null;
  }

  private record LoaderStage(Stage stage, ClassLoader loader) {
  }

  public enum Stage {
    SYSTEM(0),
    EARLY_PLUGIN(1),
    GAME(2);

    private final int priority;

    /* package */ Stage(final int priority) {
      this.priority = priority;
    }

    public int priority() {
      return this.priority;
    }
  }
}
