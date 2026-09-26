//? if >= 26.3 {
package io.github.jumperonjava.jjelytraswap.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC 26.3 polls key states against the raw SDL scancode buffer
 * (InputConstants.isKeyDown → ByteBuffer.get). A negative scancode — e.g. the old
 * GLFW "unbound" sentinel -1 persisted in options.txt by sessions running the
 * unpatched build — makes every screen close crash with IndexOutOfBoundsException
 * in KeyMapping.setAll(). Guard KeyMapping.setKey: remap negative keyboard
 * scancodes to the UNKNOWN key (scancode 0) before assignment.
 */
@Mixin(KeyMapping.class)
public class PoisonKeySanitizerMixin {

    @Inject(method = "setKey", at = @At("HEAD"), cancellable = true)
    public void sanitizeNegativeScancodes(InputConstants.Key key, CallbackInfo ci) {
        if (key.getType() == InputConstants.Type.KEYBOARD && key.getValue() < 0) {
            ((KeyMapping) (Object) this).setKey(InputConstants.UNKNOWN);
            ci.cancel();
        }
    }
}
//? } else {
/*package io.github.jumperonjava.jjelytraswap.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.client.option.KeyBinding.class)
public class PoisonKeySanitizerMixin {
}
*///?}