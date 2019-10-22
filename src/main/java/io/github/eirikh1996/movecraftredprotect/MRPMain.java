package io.github.eirikh1996.movecraftredprotect;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MRPMain extends JavaPlugin implements Listener {
    private RedProtect redProtectPlugin;
    private Movecraft movecraftPlugin;

    private final String ALLOW_CRAFTS_FLAG = "AllowCrafts";

    private static MRPMain instance;

    public void onEnable(){
        UpdateManager.initialize();
        loadConfig();
        if (!I18nSupport.initialize()){
            return;
        }
        Plugin mPlug = getServer().getPluginManager().getPlugin("Movecraft");
        if (mPlug instanceof Movecraft){
            movecraftPlugin = (Movecraft) mPlug;
        }
        Plugin rpPlug = getServer().getPluginManager().getPlugin("RedProtect");
        if (rpPlug instanceof RedProtect){
            redProtectPlugin = (RedProtect) rpPlug;
        }
        if (movecraftPlugin == null || !movecraftPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Movecraft not found or disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (redProtectPlugin == null || !redProtectPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - RedProtect not found or disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getRedProtectPlugin().getAPI().addFlag(ALLOW_CRAFTS_FLAG, false, false);
        getServer().getPluginManager().registerEvents(this, this);
        UpdateManager.getInstance().start();
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event){
        if (event.getCraft().getSinking()){
            return;
        }
        //Check the current region the craft is in
        Region current = null;
        for (MovecraftLocation ml : event.getOldHitBox()){
            Region test = getRedProtectPlugin().getAPI().getRegion(ml.toBukkit(event.getCraft().getW()));
            if (test == null){
                continue;
            }
            current = test;
            break;
        }
        if (current != null){
            if (Settings.preventCraftExitOnNoExit && !current.canExit(event.getCraft().getNotificationPlayer())){
                event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed not allowed to exit"));
                event.setCancelled(true);
                return;
            }
        }
        //Check the region the craft will enter
        Region destination = null;
        for (MovecraftLocation ml : event.getNewHitBox()){
            Region test = getRedProtectPlugin().getAPI().getRegion(ml.toBukkit(event.getCraft().getW()));
            if (test == null){
                continue;
            }
            destination = test;
            break;
        }
        if (current != null && current != destination &&
                Settings.preventCraftExitOnNoExit && !current.canExit(event.getCraft().getNotificationPlayer())){
                event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed not allowed to exit"));
                event.setCancelled(true);
                return;

        }
        if (destination == null){
            return;
        }
        if (Settings.blockMoveOnNoBuild) {
            if (!destination.canBuild(event.getCraft().getNotificationPlayer()) && !destination.getFlagBool(ALLOW_CRAFTS_FLAG))
            {
                event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed Build not allowed"));
                event.setCancelled(true);
            }
        } else {
            if (!destination.getFlagBool(ALLOW_CRAFTS_FLAG)){
                event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed Crafts not allowed"));
                event.setCancelled(true);
            }
        }


    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event){
        if (event.getCraft().getSinking()){
            return;
        }
        //Check the current region the craft is in
        Region current = null;
        for (MovecraftLocation ml : event.getOldHitBox()){
            Region test = getRedProtectPlugin().getAPI().getRegion(ml.toBukkit(event.getCraft().getW()));
            if (test == null){
                continue;
            }
            current = test;
            break;
        }
        //Check the region the craft will enter
        Region destination = null;
        for (MovecraftLocation ml : event.getNewHitBox()){
            Region test = getRedProtectPlugin().getAPI().getRegion(ml.toBukkit(event.getCraft().getW()));
            if (test == null){
                continue;
            }
            destination = test;
            break;
        }
        if (current != null && current != destination &&
                Settings.preventCraftExitOnNoExit && !current.canExit(event.getCraft().getNotificationPlayer())){
            event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed not allowed to exit"));
            event.setCancelled(true);
            return;

        }
        if (destination == null){
            return;
        }
        if (Settings.blockMoveOnNoBuild) {
            if (!destination.canBuild(event.getCraft().getNotificationPlayer()) && !destination.getFlagBool(ALLOW_CRAFTS_FLAG))
            {
                event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed Build not allowed"));
                event.setCancelled(true);
            }
        } else {
            if (!destination.getFlagBool(ALLOW_CRAFTS_FLAG)){
                event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed Crafts not allowed"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCraftSink(CraftSinkEvent event) {
        Region region = null;
        Craft craft = event.getCraft();
        for (MovecraftLocation ml : craft.getHitBox()){
            Region test = getRedProtectPlugin().getAPI().getRegion(ml.toBukkit(craft.getW()));
            if (test == null){
                continue;
            }
            region = test;
            break;
        }
        if (region != null && !region.getFlagBool("pvp") && Settings.blockSinkOnNoPvP) {
            if (craft.getNotificationPlayer() != null) {
                craft.getNotificationPlayer().sendMessage(I18nSupport.getInternationalisedString("Sink - PvP is disabled"));
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if (!event.getPlayer().hasPermission("mrp.update")){
            return;
        }
        UpdateManager updateManager = UpdateManager.getInstance();
        if (updateManager.checkUpdate(updateManager.getCurrentVersion()) <= updateManager.getCurrentVersion()){
            return;
        }
        event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Update - Update available") + "https://dev.bukkit.org/projects/movecraft-redprotect/files");
    }

    private void loadConfig(){
        saveDefaultConfig();
        Settings.locale = getConfig().getString("locale", "en");
        Settings.preventCraftExitOnNoExit = getConfig().getBoolean("preventCraftExitOnNoExit", false);
        Settings.blockMoveOnNoBuild = getConfig().getBoolean("blockMoveOnNoBuild", true);
        Settings.blockSinkOnNoPvP = getConfig().getBoolean("blockSinkOnNoPvP", true);
        final String[] LOCALES = {"en", "no"};
        for (String locale : LOCALES){
            String fileName = "localisation/lang_" + locale + ".properties";
            File lf = new File(getDataFolder().getAbsolutePath() + fileName);
            if (lf.exists()){
                continue;
            }
            saveResource(fileName, false);
        }
    }

    public static synchronized MRPMain getInstance() {
        return instance;
    }

    public Movecraft getMovecraftPlugin() {
        return movecraftPlugin;
    }

    public RedProtect getRedProtectPlugin() {
        return redProtectPlugin;
    }
}
