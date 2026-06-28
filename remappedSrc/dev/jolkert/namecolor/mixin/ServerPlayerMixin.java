package dev.jolkert.namecolor.mixin;

import com.mojang.authlib.GameProfile;
import dev.jolkert.namecolor.NameColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Objects;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player
{
    @Shadow
    @Final
    private MinecraftServer server;


    public ServerPlayerMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "getTabListDisplayName", at=@At(value = "RETURN"),cancellable = true)
    private void modifyPlayerName(CallbackInfoReturnable<Component> cir) {

        if(cir.getReturnValue() == null){
            int color = NameColor.getNameColor(this.getUUID());
            if(color == -1) return;
            Component displayName = Objects.requireNonNull(this.getDisplayName()).copy().setStyle(Style.EMPTY.withColor(color));

            cir.setReturnValue(displayName);
        }
    }

}
