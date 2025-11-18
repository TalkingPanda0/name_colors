package dev.jolkert.namecolor.mixin;

import com.mojang.authlib.GameProfile;
import dev.jolkert.namecolor.NameColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;
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


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends PlayerEntity
{
    @Shadow
    @Final
    private MinecraftServer server;


    public ServerPlayerMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "getPlayerListName", at=@At(value = "RETURN"),cancellable = true)
    private void modifyPlayerName(CallbackInfoReturnable<Text> cir) {

        if(cir.getReturnValue() == null){
            int color = NameColor.getNameColor(this.getUuid());
            if(color == -1) return;
            Text displayName = Objects.requireNonNull(this.getDisplayName()).copy().setStyle(Style.EMPTY.withColor(color));

            cir.setReturnValue(displayName);
        }
    }

}
