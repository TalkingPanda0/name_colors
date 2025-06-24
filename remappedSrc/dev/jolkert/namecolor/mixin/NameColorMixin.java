package dev.jolkert.namecolor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.jolkert.namecolor.NameColor;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(MessageType.class)
public class NameColorMixin
{

	@WrapOperation(
			method = "params(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/entity/Entity;)Lnet/minecraft/network/message/MessageType$Parameters;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;getDisplayName()Lnet/minecraft/text/Text;"
			)
	)
	private static Text test(Entity sourceEntity, Operation<Text> o)
	{
		Text original = o.call(sourceEntity);
		int color = NameColor.getNameColor(sourceEntity.getUuid());

		return color != -1 ?
				original.copy().setStyle(Style.EMPTY.withColor(color)) :
				original;
	}
}
