package uk.co.kieraan.dispenseit;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DispenseIt extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        final File check = new File(this.getDataFolder(), "config.yml");
        if (!check.exists()) {
            this.saveDefaultConfig();
            this.reloadConfig();
        }

        this.getServer().getPluginManager().registerEvents(this, this);

        this.getLogger().info("Loaded " + this.getDescription().getName() + " v" + this.getDescription().getVersion());
        if (this.getConfig().getBoolean("logStartup")) {
            logStartup();
        }

    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled " + this.getDescription().getName() + " v" + this.getDescription().getVersion());
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        String materialName = event.getItem().getType().toString();
        Location loc = event.getBlock().getLocation();

        if (this.getConfig().getString("consoleLog").equalsIgnoreCase("ALL")) {
            this.getLogger().info(materialName + " was just dispensed. (X:" + loc.getBlockX() + ", Y:" + loc.getBlockY() + ", Z:" + loc.getBlockZ() + ")");
        }

        if (this.getConfig().getBoolean("deny." + materialName)) {
            event.setCancelled(true);
            if (this.getConfig().getString("consoleLog").equalsIgnoreCase("BLOCKED")) {
                this.getLogger().info("Stopped " + materialName + " from being dispensed. (X:" + loc.getBlockX() + ", Y:" + loc.getBlockY() + ", Z:" + loc.getBlockZ() + ")");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material block = event.getBlock().getType();
        Location loc = event.getBlock().getLocation();

        if (this.getConfig().getBoolean("usePermission")) {
            if (block == Material.DISPENSER) {
                if (!player.hasPermission("dispenseit.place")) {
                    player.sendMessage(ChatColor.RED + "You cannot place that block!");
                    if (this.getConfig().getString("consoleLog").equalsIgnoreCase("BLOCKED") || this.getConfig().getString("consoleLog").equalsIgnoreCase("ALL")) {
                        this.getLogger().info("Blocked " + player.getName() + " from placing a dispenser (X:" + loc.getBlockX() + ", Y:" + loc.getBlockY() + ", Z:" + loc.getBlockZ() + ")");
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    public void logStartup() {
        try {
            String pluginName = this.getDescription().getName();
            String pluginVersion = this.getDescription().getVersion();
            URL url = new URL("http://kieraan.co.uk/plugins/collectData.php?name=" + pluginName + "&version=" + pluginVersion);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("RECEIVED")) {
                    this.getLogger().info("Startup logged.");
                } else {
                    this.getLogger().severe("Failed to log plugin startup. (Webserver error.)");
                }
            }
        } catch (Exception e) {
            this.getLogger().severe("Failed to log plugin startup. (Connection error.)");
        }
    }

}
