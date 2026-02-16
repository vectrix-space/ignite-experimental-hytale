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

Ignite provides mixin and access widener support for other modders to modify
the Hytale codebase at runtime, without needing to modify the game files.

This mod is intended to address many of the shortcomings of Hyxin, however it
is unable to fix all of them due to Hytale and Java limitations. This version
is intended to work in singleplayer and multiplayer. It is compatible with
existing mods that were designed for Hyxin without needing any changes.

If you are only interested in creating mixins for a server run independently
of the client, I would recommend the [launcher version of Ignite.](https://github.com/vectrix-space/ignite)

## Install

Download the `ignite.jar` from the [releases page](https://github.com/vectrix-space/ignite-experimental-hytale/releases/latest).

Place the `ignite.jar` into the `/earlyplugins` directory (which you may need to manually create next to the `/mods` directory).

If you're still confused, be sure to [ask for help](https://discord.gg/chpEj5UC45).

Any mods that use Ignite should then be placed into the `/earlyplugins` directory.

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
