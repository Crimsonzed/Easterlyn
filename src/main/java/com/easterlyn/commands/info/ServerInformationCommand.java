package com.easterlyn.commands.info;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

/**
 * EasterlynCommand for obtaining more detailed server information. Writes to plugins/Easterlyn/report.txt
 * 
 * @author Jikoo
 */
public class ServerInformationCommand extends EasterlynAsynchronousCommand {

	public ServerInformationCommand(Easterlyn plugin) {
		super(plugin, "serverinfo");
		this.setDescription("Detailed info about the server or a world. Requires file access.");
		this.setUsage("/serverinfo [world] [chunk|entity|tile]");
		this.setPermissionLevel(UserRank.DANGER_DANGER_HIGH_VOLTAGE);
		this.setPermissionMessage("You're playing on Easterlyn! What a shock.");
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		final List<World> worlds;
		if (args.length == 0) {
			// Minimal risk calling this async, not worried.
			worlds = Bukkit.getWorlds();
		} else {
			final World world = Bukkit.getWorld(args[0]);
			if (world == null) {
				sender.sendMessage(Language.getColor("bad") + "No such world " + args[0] + " loaded!");
				return true;
			}
			worlds = new ArrayList<>();
			worlds.add(world);
		}
		final File file = new File(getPlugin().getDataFolder(), "report.txt");
		// Again, minimal risk calling async
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			sender.sendMessage(Language.getColor("bad") + "IOException creating report.txt");
			return true;
		}
		StringBuilder sb = new StringBuilder();
		// This is slightly risky
		for (World world : worlds) {
			sb.append(world.getName()).append(":\n chunks:\n");
			for (Chunk chunk : world.getLoadedChunks()) {
				try {
					sb.append("  ").append(chunk.getX()).append('_').append(chunk.getZ()).append(":\n");
					if (chunk.getEntities().length > 0) {
						sb.append("   entities:\n");
						for (Entity entity : chunk.getEntities()) {
							sb.append("    ").append(entity.getClass().getName()).append('@')
									.append(entity.getWorld().getName()).append(':')
									.append(entity.getLocation().getBlockX()).append(',')
									.append(entity.getLocation().getBlockY()).append(',')
									.append(entity.getLocation().getBlockZ()).append('\n');
						}
					}
					if (chunk.getTileEntities().length > 0) {
						sb.append("   tiles:\n");
						for (BlockState tile : chunk.getTileEntities()) {
							sb.append("    ").append(tile.getClass().getName()).append('@')
									.append(tile.getX()).append(',').append(tile.getY())
									.append(',').append(tile.getZ()).append('\n');
						}
					}
				} catch (Exception e) {
					// Chunk has probably been unloaded. If this actually happens I'd like to know.
					e.printStackTrace();
				}
			}
		}
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(sb.toString());
		} catch (IOException e) {
			sender.sendMessage(Language.getColor("bad") + "IOException creating report.txt");
			return true;
		}
		sender.sendMessage(Language.getColor("good") + "Report written to plugins/Easterlyn/report.txt");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
