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

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import java.io.IOException;
import java.lang.reflect.Method;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.tinylog.Logger;
import space.vectrix.ignite.experimental.hytale.mod.ModLoader;

public final class Ember {
  public static final Ember INSTANCE = new Ember();

  private boolean initialized = false;

  private @UnknownNullability EmberTransformer transformer;

  public EmberTransformer transformer() {
    if(this.transformer == null) throw new IllegalStateException("Unable to retrieve EmberTransformer as Ember#bootstrap has not been called!");
    return this.transformer;
  }

  public void bootstrap() {
    // Check if we have already been bootstrapped.
    if(this.initialized) throw new UnsupportedOperationException("Unable to call Ember#bootstrap more than once!");
    this.initialized = true;

    this.transformer = new EmberTransformer();

    // Start the mixin boostrap.
    MixinBootstrap.init();

    // Add the configuration for access wideners and mixins to the mod loader.
    try {
      final ClassLoader loader = EmberEnvironment.INSTANCE.find(EmberEnvironment.Stage.EARLY_PLUGIN);
      if(loader == null) throw new IllegalStateException("Unable to locate early plugin loader!");

      ModLoader.load(loader);
    } catch(final IOException exception) {
      Logger.error(exception, "Unable to load mixin or access widener configurations, due to an error: ");
    }

    // Complete the mixin bootstrap.
    this.completeMixinBootstrap();

    // Start the mixin extras bootstrap.
    MixinExtrasBootstrap.init();
  }

  private void completeMixinBootstrap() {
    // Move to the default phase.
    try {
      final Method method = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
      method.setAccessible(true);
      method.invoke(null, MixinEnvironment.Phase.INIT);
      method.invoke(null, MixinEnvironment.Phase.DEFAULT);
    } catch(final Exception exception) {
      Logger.error(exception, "Failed to complete mixin bootstrap!");
    }

    // Prepare the transformers now mixin is in the correct state.
    for(final TransformerService transformer : this.transformer.getAll()) {
      transformer.prepare();
    }
  }
}
