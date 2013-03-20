package uk.co.kieraan.dispenseit;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.mcstats.MetricsLite;

public class DispenseIt extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        final File check = new File(this.getDataFolder(), "config.yml");
        if (!check.exists()) {
            this.saveDefaultConfig();
            this.reloadConfig();
        }

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Stats no worky :(
        }

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("Loaded " + this.getDescription().getName() + " v" + this.getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled " + this.getDescription().getName() + " v" + this.getDescription().getVersion());
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        String materialName = event.getItem().getType().toString();
        Location loc = event.getBlock().getLocation();
        Material block = event.getBlock().getType();
        String type = "dispensed";

        if (block.equals(Material.DROPPER)) {
            type = "dropped";
        }

        if ((this.getConfig().getBoolean("blacklist.dispenser." + materialName) && type.equals("dispensed")) || (this.getConfig().getBoolean("blacklist.dropper." + materialName) && type.equals("dropped"))) {
            event.setCancelled(true);
            if (this.getConfig().getString("consoleLog").equalsIgnoreCase("BLOCKED") || this.getConfig().getString("consoleLog").equalsIgnoreCase("ALL")) {
                this.getLogger().info("Stopped " + materialName + " from being " + type + ". (X:" + loc.getBlockX() + ", Y:" + loc.getBlockY() + ", Z:" + loc.getBlockZ() + ")");
            }
            return;
        }
        
        if (this.getConfig().getString("consoleLog").equalsIgnoreCase("ALL")) {
            this.getLogger().info(materialName + " was just " + type + ". (X:" + loc.getBlockX() + ", Y:" + loc.getBlockY() + ", Z:" + loc.getBlockZ() + ")");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material block = event.getBlock().getType();
        Location loc = event.getBlock().getLocation();

        if (this.getConfig().getBoolean("usePermission.dispenser")) {
            if (block.equals(Material.DROPPER)) {
                if (!player.hasPermission("dispenseit.place.dispenser")) {
                    player.sendMessage(ChatColor.RED + "You cannot place that block!");
                    if (this.getConfig().getString("consoleLog").equalsIgnoreCase("BLOCKED") || this.getConfig().getString("consoleLog").equalsIgnoreCase("ALL")) {
                        this.getLogger().info("Blocked " + player.getName() + " from placing a dispenser (X:" + loc.getBlockX() + ", Y:" + loc.getBlockY() + ", Z:" + loc.getBlockZ() + ")");
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (this.getConfig().getBoolean("usePermission.dropper")) {
            if (block.equals(Material.DROPPER)) {
                if (!player.hasPermission("dispenseit.place.dropper")) {
                    player.sendMessage(ChatColor.RED + "You cannot place that block!");
                    if (this.getConfig().getString("consoleLog").equalsIgnoreCase("BLOCKED") || this.getConfig().getString("consoleLog").equalsIgnoreCase("ALL")) {
                        this.getLogger().info("Blocked " + player.getName() + " from placing a dropper (X:" + loc.getBlockX() + ", Y:" + loc.getBlockY() + ", Z:" + loc.getBlockZ() + ")");
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
