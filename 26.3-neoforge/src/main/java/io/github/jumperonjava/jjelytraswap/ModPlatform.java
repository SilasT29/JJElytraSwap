package io.github.jumperonjava.jjelytraswap;

/**
 * This interface allows you to define platform specific code, and call it in a
 * mapping-agnostic way (no Minecraft types leak through the interface).
 */
public interface ModPlatform {
    String getModloader();
    boolean isModLoaded(String modId);
    void registerClientTickEvent(Runnable tick);
    void registerToggleKeybind(String translationKeyName, int defaultKeyId);
    boolean consumeToggleKeybind();
}