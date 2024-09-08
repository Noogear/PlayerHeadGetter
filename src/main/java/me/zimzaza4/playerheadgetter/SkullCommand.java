package me.zimzaza4.playerheadgetter;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBTList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SkullCommand implements CommandExecutor {
    public static final Set<String> textures = new HashSet<>();
    public static final Set<Player> users = new HashSet<>();

    public static String getTexture(ItemStack item) {
        return NBT.modifyComponents(item, nbt -> {
            ReadableNBT skullOwnerCompound = nbt.getCompound("minecraft:profile");
            if (skullOwnerCompound == null) {
                return null;
            }
            ReadableNBTList<ReadWriteNBT> skullOwnerPropertiesCompound = skullOwnerCompound.getCompoundList("properties");
            for (ReadWriteNBT property : skullOwnerPropertiesCompound) {
                if (Objects.equals(property.getString("name"), "textures") && property.getString("value") != null) {
                    return property.getString("value");
                }
            }
            return null;
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("skull.get")) {
            if (!(sender instanceof Player)) {
                return false;
            }
            switch (args[0]) {
                case "hand":
                    if (((Player) sender).getInventory().getItemInMainHand().getItemMeta() instanceof SkullMeta) {
                        ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
                        String texture = getTexture(item);
                        if (texture != null) {
                            textures.add(texture);
                        }
                    }
                    break;
                case "test":
                    if (((Player) sender).getInventory().getItemInMainHand().getItemMeta() instanceof SkullMeta) {
                        ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
                        String texture = getTexture(item);
                        if (texture != null) {
                            sender.sendMessage(texture);
                        }
                    }
                    break;
                case "save":
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            File f = new File(PlayerHeadGetter.plugin.getDataFolder(), "heads.yml");
                            PlayerHeadGetter.plugin.getDataFolder().mkdirs();
                            if (!f.exists()) {
                                try {
                                    f.createNewFile();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            } else {
                                textures.addAll(YamlConfiguration.loadConfiguration(f).getStringList("textures"));
                            }
                            FileConfiguration cfg = new YamlConfiguration();
                            List<String> strings = new ArrayList<>(textures);
                            cfg.set("textures", strings);
                            try {
                                cfg.save(f);
                                sender.sendMessage("已保存");
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(PlayerHeadGetter.plugin);
                    break;
                case "inventory":
                    Player player = (Player) sender;
                    if (users.contains(player)) {
                        users.remove(player);
                        sender.sendMessage("OFF");
                    } else {
                        sender.sendMessage("ON");
                        users.add(player);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!users.contains(player)) {
                                    this.cancel();
                                }
                                if (!player.isOnline()) {
                                    this.cancel();
                                }
                                for (ItemStack item : player.getOpenInventory().getTopInventory()) {
                                    if (item != null) {
                                        if (item.getItemMeta() instanceof SkullMeta) {
                                            String texture = getTexture(item);
                                            if (texture != null) {
                                                textures.add(texture);
                                                sender.sendMessage(texture);
                                            }
                                        }
                                    }
                                }
                            }
                        }.runTaskTimer(PlayerHeadGetter.plugin, 10, 10);
                    }
                    break;
            }
        }
        return true;
    }
}
