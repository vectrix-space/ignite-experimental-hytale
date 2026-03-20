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

import java.nio.file.Path;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;

public final class EmberMixinContainer extends ContainerHandleVirtual {
  public EmberMixinContainer(final String name) {
    super(name);
  }

  public void addResource(final String name, final Path path) {
    this.add(new ResourceContainer(name, path));
  }

  @Override
  public String toString() {
    return "EmberMixinContainer{name=" + this.getName() + "}";
  }

  /* package */ static class ResourceContainer extends ContainerHandleURI {
    private final String name;
    private final Path path;

    /* package */ ResourceContainer(final String name, final Path path) {
      super(path.toUri());

      this.name = name;
      this.path = path;
    }

    public String name() {
      return this.name;
    }

    public Path path() {
      return this.path;
    }

    @Override
    public String toString() {
      return "ResourceContainer{name=" + this.name + ", path=" + this.path + "}";
    }
  }
}
