# JJElytraSwap

A client-side Minecraft mod that automatically swaps your elytra on and off — jump off a ledge and the best elytra from your inventory is equipped; land and your best chestplate goes back on.

This is an updated fork of [JumperOnJava/JJElytraSwap](https://github.com/JumperOnJava/JJElytraSwap) adding support for **Minecraft 26.3 "Wilderness Bound"** (Fabric + NeoForge) — the first unobfuscated Minecraft release.

- Original mod: https://modrinth.com/mod/jjelytraswap
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/autoelytra
- Original author: [JavaJumper](https://github.com/JumperOnJava)

## Supported versions

| Minecraft | Fabric | NeoForge |
|-----------|--------|----------|
| 26.3      | ✅     | ✅       |
| 1.21.3 – 1.21.11 | ✅ | ✅   |

## Building

```bash
./gradlew chiseledBuild                    # all versions, both loaders
./gradlew :26.3-fabric:buildAndCollect     # single version (stonecutter)
cd 26.3-neoforge && ./gradlew build        # 26.3 NeoForge (standalone, loom-no-remap)
```

Built jars land in `build/libs/<mod version>/<loader>/` (stonecutter) and
`26.3-neoforge/build/libs/` (standalone).

> **Note on 26.3:** Minecraft 26.x ships unobfuscated, so there are no yarn/mojmap
> mappings for it. The 26.3-fabric version builds with an identity mappings stub +
> `noIntermediateMappings()`; the 26.3 NeoForge version lives outside the stonecutter
> tree because Architectury Loom's NeoForge path requires the `loom-no-remap` plugin
> variant, which cannot coexist with regular loom in one Kotlin script. Sources for it
> are extracted from the shared `src/` tree by `tools/extract_variant.py`.

## License

LGPLv3 — inherited from the original project.