package com.gmail.nossr50;

import com.gmail.nossr50.config.AdvancedConfig;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.config.HiddenConfig;
import com.gmail.nossr50.config.mods.ArmorConfigManager;
import com.gmail.nossr50.config.mods.BlockConfigManager;
import com.gmail.nossr50.config.mods.EntityConfigManager;
import com.gmail.nossr50.config.mods.ToolConfigManager;
import com.gmail.nossr50.config.skills.alchemy.PotionConfig;
import com.gmail.nossr50.config.skills.repair.RepairConfigManager;
import com.gmail.nossr50.config.treasure.TreasureConfig;
import com.gmail.nossr50.database.DatabaseManager;
import com.gmail.nossr50.database.DatabaseManagerFactory;
import com.gmail.nossr50.listeners.*;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.party.PartyManager;
import com.gmail.nossr50.runnables.CheckDateTask;
import com.gmail.nossr50.runnables.SaveTimerTask;
import com.gmail.nossr50.runnables.backups.CleanBackupsTask;
import com.gmail.nossr50.runnables.database.UserPurgeTask;
import com.gmail.nossr50.runnables.party.PartyAutoKickTask;
import com.gmail.nossr50.runnables.player.PowerLevelUpdatingTask;
import com.gmail.nossr50.runnables.skills.BleedTimerTask;
import com.gmail.nossr50.skills.alchemy.Alchemy;
import com.gmail.nossr50.skills.child.ChildConfig;
import com.gmail.nossr50.skills.repair.repairables.Repairable;
import com.gmail.nossr50.skills.repair.repairables.RepairableManager;
import com.gmail.nossr50.skills.repair.repairables.SimpleRepairableManager;
import com.gmail.nossr50.util.*;
import com.gmail.nossr50.util.blockmeta.chunkmeta.ChunkManager;
import com.gmail.nossr50.util.blockmeta.chunkmeta.ChunkManagerFactory;
import com.gmail.nossr50.util.commands.CommandRegistrationManager;
import com.gmail.nossr50.util.experience.FormulaManager;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.scoreboards.ScoreboardManager;
import net.gravitydevelopment.updater.mcmmo.Updater;
import net.gravitydevelopment.updater.mcmmo.Updater.UpdateResult;
import net.gravitydevelopment.updater.mcmmo.Updater.UpdateType;
import net.shatteredlands.shatt.backup.ZipLibrary;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class mcMMO extends JavaPlugin {
    /* Metadata Values */
    public final static String entityMetadataKey = "mcMMO: Spawned Entity";
    public final static String blockMetadataKey = "mcMMO: Piston Tracking";
    public final static String furnaceMetadataKey = "mcMMO: Tracked Furnace";
    public final static String tntMetadataKey = "mcMMO: Tracked TNT";
    public final static String tntsafeMetadataKey = "mcMMO: Safe TNT";
    public final static String customNameKey = "mcMMO: Custom Name";
    public final static String customVisibleKey = "mcMMO: Name Visibility";
    public final static String droppedItemKey = "mcMMO: Tracked Item";
    public final static String infiniteArrowKey = "mcMMO: Infinite Arrow";
    public final static String bowForceKey = "mcMMO: Bow Force";
    public final static String arrowDistanceKey = "mcMMO: Arrow Distance";
    public final static String customDamageKey = "mcMMO: Custom Damage";
    public final static String disarmedItemKey = "mcMMO: Disarmed Item";
    public final static String playerDataKey = "mcMMO: Player Data";
    public static mcMMO p;
    // Jar Stuff
    public static File mcmmo;
    public static FixedMetadataValue metadataValue;
    /* Managers */
    private static ChunkManager placeStore;
    private static RepairableManager repairableManager;
    private static ModManager modManager;
    private static DatabaseManager databaseManager;
    private static FormulaManager formulaManager;
    private static HolidayManager holidayManager;
    /* File Paths */
    private static String mainDirectory;
    private static String flatFileDirectory;
    private static String usersFile;
    private static String modDirectory;
    /* Plugin Checks */
    private static boolean combatTagEnabled;
    private static boolean healthBarPluginEnabled;
    // Config Validation Check
    public boolean noErrorsInConfigFiles = true;
    // Update Check
    private boolean updateAvailable;
    // XP Event Check
    private boolean xpEventEnabled;

    public static String getMainDirectory() {
        return mainDirectory;
    }

    public static String getFlatFileDirectory() {
        return flatFileDirectory;
    }

    public static String getUsersFilePath() {
        return usersFile;
    }

    public static String getModDirectory() {
        return modDirectory;
    }

    public static FormulaManager getFormulaManager() {
        return formulaManager;
    }

    public static HolidayManager getHolidayManager() {
        return holidayManager;
    }

    public static ChunkManager getPlaceStore() {
        return placeStore;
    }

    public static RepairableManager getRepairableManager() {
        return repairableManager;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Deprecated
    public static void setDatabaseManager(DatabaseManager databaseManager) {
        mcMMO.databaseManager = databaseManager;
    }

    public static ModManager getModManager() {
        return modManager;
    }

    public static boolean isCombatTagEnabled() {
        return combatTagEnabled;
    }

    public static boolean isHealthBarPluginEnabled() {
        return healthBarPluginEnabled;
    }

    /**
     * Things to be run when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        try {
            p = this;
            getLogger().setFilter(new LogFilter(this));
            metadataValue = new FixedMetadataValue(this, true);
            PluginManager pluginManager = getServer().getPluginManager();
            combatTagEnabled = pluginManager.getPlugin("CombatTag") != null;
            healthBarPluginEnabled = pluginManager.getPlugin("HealthBar") != null;
            setupFilePaths();
            modManager = new ModManager();
            loadConfigFiles();
            if (!noErrorsInConfigFiles) {
                return;
            }
            if (getServer().getName().equals("MCPC+")) {
                checkModConfigs();
            }
            if (healthBarPluginEnabled) {
                getLogger().info("HealthBar plugin found, mcMMO's healthbars are automatically disabled.");
            }
            if (pluginManager.getPlugin("NoCheatPlus") != null && pluginManager.getPlugin("CompatNoCheatPlus") == null) {
                getLogger().warning("NoCheatPlus plugin found, but CompatNoCheatPlus was not found!");
                getLogger().warning("mcMMO will not work properly alongside NoCheatPlus without CompatNoCheatPlus");
            }
            databaseManager = DatabaseManagerFactory.getDatabaseManager();
            registerEvents();
            registerCustomRecipes();
            PartyManager.loadParties();
            formulaManager = new FormulaManager();
            holidayManager = new HolidayManager();
            for (Player player : getServer().getOnlinePlayers()) {
                UserManager.addUser(player); // In case of reload add all users back into UserManager
                ScoreboardManager.setupPlayer(player);
            }
            debug("Version " + getDescription().getVersion() + " is enabled!");
            scheduleTasks();
            CommandRegistrationManager.registerCommands();
            placeStore = ChunkManagerFactory.getChunkManager(); // Get our ChunkletManager
            checkForUpdates();
            if (Config.getInstance().getPTPCommandWorldPermissions()) {
                Permissions.generateWorldTeleportPermissions();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Things to be run when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        try {
            Alchemy.finishAllBrews();   // Finish all partially complete AlchemyBrewTasks to prevent vanilla brewing continuation on restart
            UserManager.saveAll();      // Make sure to save player information if the server shuts down
            PartyManager.saveParties(); // Save our parties
            ScoreboardManager.teardownAll();
            formulaManager.saveFormula();
            holidayManager.saveAnniversaryFiles();
            placeStore.saveAll();       // Save our metadata
            placeStore.cleanUp();       // Cleanup empty metadata stores
        } catch (NullPointerException e) {
        }

        debug("Canceling all tasks...");
        getServer().getScheduler().cancelTasks(this); // This removes our tasks
        debug("Unregister all events...");
        HandlerList.unregisterAll(this); // Cancel event registrations

        if (Config.getInstance().getBackupsEnabled()) {
            // Remove other tasks BEFORE starting the Backup, or we just cancel it straight away.
            try {
                ZipLibrary.mcMMOBackup();
            } catch (IOException e) {
                getLogger().severe(e.toString());
            } catch (Throwable e) {
                if (e instanceof NoClassDefFoundError) {
                    getLogger().severe("Backup class not found!");
                    getLogger().info("Please do not replace the mcMMO jar while the server is running.");
                } else {
                    getLogger().severe(e.toString());
                }
            }
        }

        debug("Was disabled."); // How informative!
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public boolean isXPEventEnabled() {
        return xpEventEnabled;
    }

    public void setXPEventEnabled(boolean enabled) {
        this.xpEventEnabled = enabled;
    }

    public void toggleXpEventEnabled() {
        xpEventEnabled = !xpEventEnabled;
    }

    public void debug(String message) {
        getLogger().info("[Debug] " + message);
    }

    /**
     * Setup the various storage file paths
     */
    private void setupFilePaths() {
        mcmmo = getFile();
        mainDirectory = getDataFolder().getPath() + File.separator;
        flatFileDirectory = mainDirectory + "flatfile" + File.separator;
        usersFile = flatFileDirectory + "mcmmo.users";
        modDirectory = mainDirectory + "mods" + File.separator;
        fixFilePaths();
    }

    private void fixFilePaths() {
        File oldFlatfilePath = new File(mainDirectory + "FlatFileStuff" + File.separator);
        File oldModPath = new File(mainDirectory + "ModConfigs" + File.separator);

        if (oldFlatfilePath.exists()) {
            if (!oldFlatfilePath.renameTo(new File(flatFileDirectory))) {
                getLogger().warning("Failed to rename FlatFileStuff to flatfile!");
            }
        }

        if (oldModPath.exists()) {
            if (!oldModPath.renameTo(new File(modDirectory))) {
                getLogger().warning("Failed to rename ModConfigs to mods!");
            }
        }

        File oldArmorFile = new File(modDirectory + "armor.yml");
        File oldBlocksFile = new File(modDirectory + "blocks.yml");
        File oldEntitiesFile = new File(modDirectory + "entities.yml");
        File oldToolsFile = new File(modDirectory + "tools.yml");

        if (oldArmorFile.exists()) {
            if (!oldArmorFile.renameTo(new File(modDirectory + "armor.default.yml"))) {
                getLogger().warning("Failed to rename armor.yml to armor.default.yml!");
            }
        }

        if (oldBlocksFile.exists()) {
            if (!oldBlocksFile.renameTo(new File(modDirectory + "blocks.default.yml"))) {
                getLogger().warning("Failed to rename blocks.yml to blocks.default.yml!");
            }
        }

        if (oldEntitiesFile.exists()) {
            if (!oldEntitiesFile.renameTo(new File(modDirectory + "entities.default.yml"))) {
                getLogger().warning("Failed to rename entities.yml to entities.default.yml!");
            }
        }

        if (oldToolsFile.exists()) {
            if (!oldToolsFile.renameTo(new File(modDirectory + "tools.default.yml"))) {
                getLogger().warning("Failed to rename tools.yml to tools.default.yml!");
            }
        }

        File currentFlatfilePath = new File(flatFileDirectory);
        currentFlatfilePath.mkdirs();
    }

    private void checkForUpdates() {
        if (!Config.getInstance().getUpdateCheckEnabled()) {
            return;
        }

        Updater updater = new Updater(this, 31030, mcmmo, UpdateType.NO_DOWNLOAD, false);

        if (updater.getResult() != UpdateResult.UPDATE_AVAILABLE) {
            this.updateAvailable = false;
            return;
        }

        if (updater.getLatestType().equals("beta") && !Config.getInstance().getPreferBeta()) {
            this.updateAvailable = false;
            return;
        }

        this.updateAvailable = true;
        getLogger().info(LocaleLoader.getString("UpdateChecker.Outdated"));
        getLogger().info(LocaleLoader.getString("UpdateChecker.NewAvailable"));
    }

    private void loadConfigFiles() {
        // Force the loading of config files
        TreasureConfig.getInstance();
        HiddenConfig.getInstance();
        AdvancedConfig.getInstance();
        PotionConfig.getInstance();
        new ChildConfig();

        List<Repairable> repairables = new ArrayList<Repairable>();

        if (Config.getInstance().getToolModsEnabled()) {
            new ToolConfigManager(this);
        }

        if (Config.getInstance().getArmorModsEnabled()) {
            new ArmorConfigManager(this);
        }

        if (Config.getInstance().getBlockModsEnabled()) {
            new BlockConfigManager(this);
        }

        if (Config.getInstance().getEntityModsEnabled()) {
            new EntityConfigManager(this);
        }

        // Load repair configs, make manager, and register them at this time
        repairables.addAll(new RepairConfigManager(this).getLoadedRepairables());
        repairables.addAll(modManager.getLoadedRepairables());
        repairableManager = new SimpleRepairableManager(repairables.size());
        repairableManager.registerRepairables(repairables);
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        // Register events
        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new BlockListener(this), this);
        pluginManager.registerEvents(new EntityListener(this), this);
        pluginManager.registerEvents(new InventoryListener(this), this);
        pluginManager.registerEvents(new SelfListener(), this);
        pluginManager.registerEvents(new WorldListener(this), this);
    }

    private void registerCustomRecipes() {
        if (Config.getInstance().getChimaeraEnabled()) {
            getServer().addRecipe(ChimaeraWing.getChimaeraWingRecipe());
        }
    }

    private void scheduleTasks() {
        // Periodic save timer (Saves every 10 minutes by default)
        long saveIntervalTicks = Config.getInstance().getSaveInterval() * 1200;
        new SaveTimerTask().runTaskTimer(this, saveIntervalTicks, saveIntervalTicks);

        // Cleanup the backups folder
        new CleanBackupsTask().runTaskAsynchronously(mcMMO.p);

        // Bleed timer (Runs every two seconds)
        new BleedTimerTask().runTaskTimer(this, 2 * Misc.TICK_CONVERSION_FACTOR, 2 * Misc.TICK_CONVERSION_FACTOR);

        // Old & Powerless User remover
        long purgeIntervalTicks = Config.getInstance().getPurgeInterval() * 60L * 60L * Misc.TICK_CONVERSION_FACTOR;

        if (purgeIntervalTicks == 0) {
            new UserPurgeTask().runTaskLater(this, 2 * Misc.TICK_CONVERSION_FACTOR); // Start 2 seconds after startup.
        } else if (purgeIntervalTicks > 0) {
            new UserPurgeTask().runTaskTimer(this, purgeIntervalTicks, purgeIntervalTicks);
        }

        // Automatically remove old members from parties
        long kickIntervalTicks = Config.getInstance().getAutoPartyKickInterval() * 60L * 60L * Misc.TICK_CONVERSION_FACTOR;

        if (kickIntervalTicks == 0) {
            new PartyAutoKickTask().runTaskLater(this, 2 * Misc.TICK_CONVERSION_FACTOR); // Start 2 seconds after startup.
        } else if (kickIntervalTicks > 0) {
            new PartyAutoKickTask().runTaskTimer(this, kickIntervalTicks, kickIntervalTicks);
        }

        // Update power level tag scoreboards
        new PowerLevelUpdatingTask().runTaskTimer(this, 2 * Misc.TICK_CONVERSION_FACTOR, 2 * Misc.TICK_CONVERSION_FACTOR);

        if (getHolidayManager().nearingAprilFirst()) {
            new CheckDateTask().runTaskTimer(this, 10L * Misc.TICK_CONVERSION_FACTOR, 1L * 60L * 60L * Misc.TICK_CONVERSION_FACTOR);
        }
    }

    private void checkModConfigs() {
        if (!Config.getInstance().getToolModsEnabled()) {
            getLogger().info("MCPC+ implementation found, but the custom tool config for mcMMO is disabled!");
            getLogger().info("To enable, set Mods.Tool_Mods_Enabled to TRUE in config.yml.");
        }

        if (!Config.getInstance().getArmorModsEnabled()) {
            getLogger().info("MCPC+ implementation found, but the custom armor config for mcMMO is disabled!");
            getLogger().info("To enable, set Mods.Armor_Mods_Enabled to TRUE in config.yml.");
        }

        if (!Config.getInstance().getBlockModsEnabled()) {
            getLogger().info("MCPC+ implementation found, but the custom block config for mcMMO is disabled!");
            getLogger().info("To enable, set Mods.Block_Mods_Enabled to TRUE in config.yml.");
        }

        if (!Config.getInstance().getEntityModsEnabled()) {
            getLogger().info("MCPC+ implementation found, but the custom entity config for mcMMO is disabled!");
            getLogger().info("To enable, set Mods.Entity_Mods_Enabled to TRUE in config.yml.");
        }
    }
}
