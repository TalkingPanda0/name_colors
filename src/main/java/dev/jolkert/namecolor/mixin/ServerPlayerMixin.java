package dev.jolkert.namecolor.mixin;

import com.mojang.authlib.GameProfile;
import dev.jolkert.namecolor.NameColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayer.class, priority = 1000000) // Forces this mixin to execute last
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, GameProfile profile) {
        super(level, profile);
    }

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void modifyPlayerListName(CallbackInfoReturnable<Component> cir) {
        int color = NameColor.getNameColor(this.getUUID());
        if (color == -1) return;

        if (cir.getReturnValue() == null) {
            // Fall back to our colored display name
            cir.setReturnValue(this.getDisplayName());
        } else {
            // Intercept whatever custom list name other mods created and style it
            Component current = cir.getReturnValue();
            cir.setReturnValue(current.copy().withStyle(current.getStyle().withColor(color)));
        }
    }
}