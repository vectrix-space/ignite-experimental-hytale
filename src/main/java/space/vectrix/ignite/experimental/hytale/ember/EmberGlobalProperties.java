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

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Map;

public final class EmberGlobalProperties implements IGlobalPropertyService {
  private final Map<String, IPropertyKey> keys = new HashMap<>();
  private final Map<IPropertyKey, Object> values = new HashMap<>();

  @Override
  public IPropertyKey resolveKey(final String name) {
    if(name.isBlank()) {
      throw new IllegalArgumentException("Property names must not be null or empty!");
    }

    return this.keys.computeIfAbsent(name, Key::new);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getProperty(final IPropertyKey key) {
    return (T) this.values.get(key);
  }

  @Override
  public void setProperty(final IPropertyKey key, final Object value) {
    this.values.put(key, value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getProperty(final IPropertyKey key, final T defaultValue) {
    return (T) this.values.getOrDefault(key, defaultValue);
  }

  @Override
  public String getPropertyString(final IPropertyKey key, final String defaultValue) {
    return this.getProperty(key, defaultValue);
  }

  public record Key(String name) implements IPropertyKey {
  }
}
