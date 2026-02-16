import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  id("ignite.base-conventions")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.licenser.spotless")
  id("com.gradleup.shadow")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

dependencies {
  checkstyle(libs.stylecheck)
  compileOnly(libs.jetbrainsAnnotations)
  compileOnly(libs.jspecify)
  compileOnly(libs.hytale)
}

spotless {
  java {
    importOrderFile(rootProject.file(".spotless/vectrix.importorder"))
    applyCommon()
  }

  kotlin {
    applyCommon()
  }
}

tasks.getByName<ShadowJar>("shadowJar") {
  mergeServiceFiles()
}
