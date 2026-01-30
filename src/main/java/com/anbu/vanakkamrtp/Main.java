package com.anbu.vanakkamrtp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class Main extends JavaPlugin {

    private final Random random = new Random();
    private final HashMap<String, Long> rtpCooldown = new HashMap<>();

    private long cooldownTime; // milliseconds
    private int radius;

    private FileConfiguration messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();       // config.yml
        loadMessages();            // messages.yml

        cooldownTime = getConfig().getLong("rtp.cooldown-seconds") * 1000;
        radius = getConfig().getInt("rtp.radius");

        getLogger().info("VanakkamRTP Enabled!");
    }

    // Load messages.yml
    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");

        if (!file.exists()) {
            saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);
    }

    // Get colored message
    private String msg(String path) {
        String text = messages.getString(path, "&cMessage missing: " + path);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("rtp")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(msg("messages.console-only"));
            return true;
        }

        Player player = (Player) sender;
        String name = player.getName();
        long now = System.currentTimeMillis();

        // Cooldown check
        if (rtpCooldown.containsKey(name)) {
            long lastUsed = rtpCooldown.get(name);
            long remaining = (lastUsed + cooldownTime) - now;

            if (remaining > 0) {
                long seconds = remaining / 1000;
                player.sendMessage(
                        msg("messages.cooldown").replace("%time%", String.valueOf(seconds))
                );
                return true;
            }
        }

        // Save cooldown
        rtpCooldown.put(name, now);

        World world = player.getWorld();
        int x = random.nextInt(radius * 2) - radius;
        int z = random.nextInt(radius * 2) - radius;
        int y = world.getHighestBlockYAt(x, z);

        Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
        player.teleport(location);

        player.sendMessage(msg("messages.success"));
        return true;
    }
}
