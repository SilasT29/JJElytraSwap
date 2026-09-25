# JJElytraSwap 2.4.0

- Added support for Minecraft 26.3 "Wilderness Bound" (Fabric + NeoForge)
  - 26.3 is the first unobfuscated Minecraft release: the mod now builds without mappings for 26.x
  - Mixin retargeted to the new glide mechanics (`LocalPlayer.aiStep` / `tryToStartFallFlying`)
- Updated Fabric loader to 0.19.5, Fabric API to 0.161.0+26.3, ModMenu to 21.0.0, NeoForge to 26.3.0.22-beta
- Java 25 for 26.3 builds; Java 21 for 1.20.5+ builds (unchanged behaviour for older versions)