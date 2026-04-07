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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IAdviceProvider;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IFeatureValidator;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.util.Constants;
import org.spongepowered.asm.util.ReEntranceLock;
import space.vectrix.ignite.experimental.hytale.ember.transformer.EmberMixinTransformer;

public final class EmberMixinService implements IMixinService, IClassProvider, IClassBytecodeProvider {
  private final EmberMixinContainer container;
  private final ReEntranceLock lock;

  public EmberMixinService() {
    this.container = new EmberMixinContainer(this.getName());
    this.lock = new ReEntranceLock(1);
  }

  //<editor-fold desc="IMixinService">
  @Override
  public String getName() {
    return "Ember/Ignite";
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void prepare() {
  }

  @Override
  public MixinEnvironment.Phase getInitialPhase() {
    return MixinEnvironment.Phase.PREINIT;
  }

  @Override
  public void offer(final IMixinInternal internal) {
    if(internal instanceof final IMixinTransformerFactory factory) {
      final EmberMixinTransformer transformer = Ember.INSTANCE.transformer().get(EmberMixinTransformer.class);
      if(transformer == null) return;

      transformer.offer(factory);
    }
  }

  @Override
  public void init() {
  }

  @Override
  public void beginPhase() {
  }

  @Override
  public void checkEnv(final Object bootSource) {
  }

  @Override
  public String getSideName() {
    return Constants.SIDE_SERVER;
  }

  @Override
  public ILogger getLogger(final String name) {
    return EmberMixinLogger.get(name);
  }

  @Override
  public ReEntranceLock getReEntranceLock() {
    return this.lock;
  }

  @Override
  public IClassProvider getClassProvider() {
    return this;
  }

  @Override
  public IClassBytecodeProvider getBytecodeProvider() {
    return this;
  }

  @Override
  public @Nullable ITransformerProvider getTransformerProvider() {
    return null;
  }

  @Override
  public @Nullable IClassTracker getClassTracker() {
    return null;
  }

  @Override
  public @Nullable IMixinAuditTrail getAuditTrail() {
    return null;
  }

  @Override
  public IFeatureValidator getFeatureValidator() {
    return IFeatureValidator.ALLOW_ALL;
  }

  @Override
  public IAdviceProvider getAdviceProvider() {
    return IAdviceProvider.GENERIC;
  }

  @Override
  public Collection<String> getPlatformAgents() {
    return List.of();
  }

  @Override
  public IContainerHandle getPrimaryContainer() {
    return this.container;
  }

  @Override
  public Collection<IContainerHandle> getMixinContainers() {
    return List.of();
  }

  @Override
  public @Nullable InputStream getResourceAsStream(final String name) {
    return EmberEnvironment.INSTANCE.findStream(name);
  }

  @Override
  public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
    return MixinEnvironment.CompatibilityLevel.JAVA_25;
  }

  @Override
  public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
    return MixinEnvironment.CompatibilityLevel.JAVA_25;
  }
  //</editor-fold>

  //<editor-fold desc="IClassProvider">
  @Override
  public URL[] getClassPath() {
    return new URL[0];
  }

  @Override
  public Class<?> findClass(final String name) throws ClassNotFoundException {
    return this.findClass(name, true);
  }

  @Override
  public Class<?> findClass(final String name, final boolean initialize) throws ClassNotFoundException {
    return Class.forName(name, initialize, EmberEnvironment.INSTANCE.findForClass(name));
  }

  @Override
  public Class<?> findAgentClass(final String name, final boolean initialize) throws ClassNotFoundException {
    return this.findClass(name, initialize);
  }
  //</editor-fold>

  //<editor-fold desc="IClassBytecodeProvider">
  @Override
  public ClassNode getClassNode(final String name) throws ClassNotFoundException, IOException {
    return this.getClassNode(name, true);
  }

  @Override
  public ClassNode getClassNode(final String name, final boolean transform) throws ClassNotFoundException, IOException {
    return this.getClassNode(name, transform, 0);
  }

  @Override
  public ClassNode getClassNode(final String name, final boolean transform, final int readerFlags) throws ClassNotFoundException, IOException {
    if(!transform) throw new IllegalStateException("ClassNodes must always be provided transformed!");

    final EmberTransformer transformer = Ember.INSTANCE.transformer();

    final EmberMixinTransformer mixinTransformer = transformer.get(EmberMixinTransformer.class);
    if(mixinTransformer == null) throw new ClassNotFoundException("Unable to retrieve the mixin transformer!");

    final String canonicalName = name.replace('/', '.');
    final String internalName = name.replace('.', '/');
    final String resourceName = internalName.concat(".class");

    final byte[] bytes;
    try(final InputStream stream = EmberEnvironment.INSTANCE.findStream(resourceName)) {
      bytes = stream != null ? stream.readAllBytes() : new byte[0];
    }

    return mixinTransformer.classNode(canonicalName, internalName, bytes, readerFlags);
  }
  //</editor-fold>
}
