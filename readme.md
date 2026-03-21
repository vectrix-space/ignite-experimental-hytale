<div align="center">
  <img src="./.github/ignite.png" width="250" height="250" alt="Ignite Logo">
  <br/><br/>
  <p><strong><a href="https://github.com/vectrix-space/ignite-experimental-hytale">Ignite Experimental Hytale</a></strong> is a <a href="https://github.com/SpongePowered/Mixin">Mixin</a> loader mod for Hytale.</p>
  <br/>
</div>

<div align="center">

![Build Status](https://github.com/vectrix-space/ignite-experimental-hytale/actions/workflows/build.yml/badge.svg)
[![MIT License](https://img.shields.io/badge/license-MIT-blue)](license.txt)
[![Discord](https://img.shields.io/discord/819522977586348052)](https://discord.gg/chpEj5UC45)

</div>

## Overview

Ignite Experimental Hytale is a mod that provides mixin and access widener support 
for other mods to modify the server at runtime, without needing to patch the
game files.

> [!WARNING]
> This mod is intended to address many of the shortcomings of [Hyxin], however it
> is unable to fix all of them due to Hytale and Java limitations. This version
> is intended to work in singleplayer and multiplayer. It is compatible with
> existing mods that were designed for [Hyxin] without needing any changes.
>
> If you are only interested in creating mixins for a server run independently
> of the client, I would recommend the [launcher version of Ignite](https://github.com/vectrix-space/ignite).

## Install

Download the `ignite.jar` from the [releases page](https://github.com/vectrix-space/ignite-experimental-hytale/releases/latest).

Place the `ignite.jar` into the `/earlyplugins` directory (which you may need to manually create next to the `/mods` directory).

If you're still confused, be sure to [ask for help](https://discord.gg/chpEj5UC45).

Any mods that use Ignite should then be placed into the `/earlyplugins` directory.

## Creating a Mod

The structure of your mod should follow the existing project structure Hytale expects with a `manifest.json`. However, inside the `manifest.json`
you will be able to add your mixins and access widener files under the new Ignite section. For example:

```json
{
  "Group": "...",
  "Name": "...",
  "Version": "...",
  "Description": "...",
  "Ignite": {
    "Mixins": [
      "your_plugin.mixins.json"
    ],
    "Wideners": [
      "your_plugin.accesswidener"
    ]
  }
}
```

Your mixin config files are configured how mixin normally expects. For example:

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.example.mixin.test",
  "plugin": "com.example.mixin.plugin.TestMixinPlugin",
  "target": "@env(DEFAULT)",
  "compatibilityLevel": "JAVA_25",
  "mixins": [
    "TestMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  },
  "overwrites": {
    "requireAnnotations": true
  }
}
```

Your access widener files are configured how access wideners normally expects. For example:

```text
accessWidener v1  named

accessible  class com/example/Example$InnerExample
```

Your mod must then always be placed in the `/earlyplugins` for Ignite to load it.

## Building

__Note:__ If you do not have [Gradle] installed then use `./gradlew` for Unix systems or Git Bash and gradlew.bat for Windows systems in 
place of any 'gradle' command.

In order to build Ignite you simply need to run the `gradle build` command. You can find the compiled JAR file in `./build/libs/` named 
'ignite.jar'.

## Inspiration

This project has many parts inspired by the following projects:

- [Orion]
- [Fabric]
- [Sponge]
- [Velocity]
- [plugin-spi]

[Mixin]: https://github.com/SpongePowered/Mixin
[Access Widener]: https://github.com/FabricMC/access-widener
[Mixin Specification]: https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---The-Mixin-Environment#mixin-configuration-files
[Access Widener Specification]: https://fabricmc.net/wiki/tutorial:accesswideners

[Gradle]: https://www.gradle.org/
[Orion]: https://github.com/OrionMinecraft/Orion
[Fabric]: https://github.com/FabricMC/fabric-loader
[Sponge]: https://github.com/SpongePowered/Sponge
[Velocity]: https://github.com/VelocityPowered/Velocity
[plugin-spi]: https://github.com/SpongePowered/plugin-spi
[Hyxin]: https://github.com/Build-9/Hyxin
