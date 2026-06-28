package dev.jolkert.namecolor.mixin;

import dev.jolkert.namecolor.NameColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Set priority to 1000 (or higher) so it runs strictly after StyledChat's priority 700
@Mixin(value = Player.class, priority = 1000000)
public abstract class PlayerMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void modifyDisplayName(CallbackInfoReturnable<Component> cir) {
        Player player = (Player) (Object) this;
        
        if (!player.level().isClientSide()) {
            int color = NameColor.getNameColor(player.getUUID());
            if (color != -1) {
                // cir.getReturnValue() here already contains the styled name from StyledChat!
                Component original = cir.getReturnValue();
                
                // Copy StyledChat's component and apply your hex color on top of it
                cir.setReturnValue(original.copy().withStyle(original.getStyle().withColor(color)));
            }
        }
    }
}