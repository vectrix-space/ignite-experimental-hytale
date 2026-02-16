plugins {
  id("ignite.common-conventions")
}

dependencies {
  api(libs.mixin)
  api(libs.mixinExtras)
  api(libs.accessWidener)

  implementation(libs.asm)
  implementation(libs.asm.analysis)
  implementation(libs.asm.commons)
  implementation(libs.asm.tree)
  implementation(libs.asm.util)

  implementation(libs.tinylog.api)
  implementation(libs.tinylog.impl)
}
