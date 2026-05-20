package dev.jolkert.namecolor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class NameColor implements ModInitializer {
	public static Logger LOGGER = LoggerFactory.getLogger("name_color");
	private static HashMap<UUID, Integer> colorMap = new HashMap<>();

	@Override
	public void onInitialize() {
		colorMap = readColorFile();
		CommandRegistrationCallback.EVENT.register((dispatcher, __, ___) -> {
			dispatcher.register(
					CommandManager.literal("color")
							.requires(Permissions.require("namecolor.command.color", 4))
							.then(
									CommandManager.argument("color", StringArgumentType.greedyString())
											.executes(ctx -> {
												ServerPlayerEntity player = ctx.getSource().getPlayer();
												if (player == null) {
													ctx.getSource().sendMessage(
															Text.literal("Command can only be run by players"));
													return 0;
												}
												try {
													String colorString = ctx.getArgument("color", String.class);
													if (colorString.startsWith("#")) {
														colorString = colorString.substring(1);
													}

													int color = Integer.parseInt(colorString, 16);
													NameColor.setPlayerColor(player.getUuid(), color);
													ctx.getSource().sendMessage(
															Text.literal("Changed chat color to ")
																	.append(Text.literal(
																			"#" + Integer.toString(color, 16))
																			.withColor(color)));
													updatePlayerListName(player);
													return Command.SINGLE_SUCCESS;
												} catch (NumberFormatException e) {
													ctx.getSource().sendError(Text.literal("Invalid color"));
													return 0;
												}
											}))
							.then(
									CommandManager.literal("clear")
											.requires(Permissions.require("namecolor.command.clear", 4))
											.executes(ctx -> {
												ServerPlayerEntity player = ctx.getSource().getPlayer();
												if (player == null) {
													ctx.getSource().sendMessage(
															Text.literal("Command can only be run by players"));
													return 0;
												}

												NameColor.clearPlayerColor(player.getUuid());
												ctx.getSource().sendMessage(Text.literal("Cleared chat color"));
												updatePlayerListName(player);
												return Command.SINGLE_SUCCESS;
											})));
		});
	}

	private void updatePlayerListName(ServerPlayerEntity player) {
		PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
		Objects.requireNonNull(player.getEntityWorld().getServer()).getPlayerManager().sendToAll(packet);
	}

	public static void setPlayerColor(UUID uuid, Integer color) {
		if (color != null) {
			colorMap.put(uuid, color);
			writeColorFile(colorMap);
		}
	}

	public static void clearPlayerColor(UUID uuid) {
		colorMap.remove(uuid);
		writeColorFile(colorMap);
	}

	private static final Path COLOR_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("player_colors.json");

	private static HashMap<UUID, Integer> readColorFile() {

		try {
			if (Files.notExists(COLOR_FILE_PATH)) {
				HashMap<UUID, Integer> newMap = new HashMap<>();
				writeColorFile(newMap);
				return newMap;
			}

			JsonObject root = JsonParser.parseString(Files.readString(COLOR_FILE_PATH)).getAsJsonObject();
			Map<String, JsonElement> rawMap = root.asMap();

			HashMap<UUID, Integer> newMap = new HashMap<>(rawMap.size());
			for (Map.Entry<String, JsonElement> entry : rawMap.entrySet()) {
				UUID uuid = tryParseUuid(entry.getKey());
				Integer color = tryGetInt(entry.getValue());

				if (uuid != null && color != null) {
					newMap.put(uuid, color);
				}
			}

			return newMap;
		} catch (IOException e) {
			NameColor.LOGGER.error("Failed to read player colors file!", e);
			return new HashMap<>();
		}
	}

	private static void writeColorFile(HashMap<UUID, Integer> map) {
		JsonObject root = new JsonObject();
		for (Map.Entry<UUID, Integer> entry : map.entrySet()) {
			root.addProperty(entry.getKey().toString(), entry.getValue());
		}

		try {
			Files.writeString(COLOR_FILE_PATH, new GsonBuilder().setPrettyPrinting().create().toJson(root));
		} catch (IOException e) {
			NameColor.LOGGER.error("Failed to write player colors file!", e);
		}
	}

	private static UUID tryParseUuid(String string) {
		try {
			return UUID.fromString(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static Integer tryGetInt(JsonElement element) {
		try {
			return element.getAsInt();
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	public static int getNameColor(UUID uuid) {
		return colorMap.getOrDefault(uuid, -1);
	}
}
