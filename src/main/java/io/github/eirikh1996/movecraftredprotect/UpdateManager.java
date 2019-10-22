package io.github.eirikh1996.movecraftredprotect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateManager extends BukkitRunnable {
    private static UpdateManager instance;
    private boolean running = false;
    //Prevents more than one instance being created
    private UpdateManager(){}

    @Override
    public void run() {
        final double currentVersion = getCurrentVersion();
        final double newVersion = checkUpdate(currentVersion);
        MRPMain.getInstance().getLogger().info(I18nSupport.getInternationalisedString("Update - Checking"));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (newVersion > currentVersion){

                    for (Player p : Bukkit.getOnlinePlayers()){
                        if (!p.hasPermission("mrp.update")){
                            p.sendMessage(I18nSupport.getInternationalisedString("Update - Update available") + "https://dev.bukkit.org/projects/movecraft-redprotect/files");
                        }
                    }
                    return;
                }
                MRPMain.getInstance().getLogger().info(I18nSupport.getInternationalisedString("Update - Up to date"));
            }
        }.runTaskLaterAsynchronously(MRPMain.getInstance(), 100);
    }

    public static void initialize(){
        instance = new UpdateManager();
    }

    public static UpdateManager getInstance(){
        return instance;
    }

    public void start(){
        if (running)
            return;
        runTaskTimerAsynchronously(MRPMain.getInstance(), 0, 1000000);
        running = true;
    }
    public double getCurrentVersion(){
        return Double.parseDouble(MRPMain.getInstance().getDescription().getVersion());
    }

    public double checkUpdate(double currentVersion){
        try {
            URL url = new URL("https://servermods.forgesvc.net/servermods/files?projectids=348956");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "Movecraft-RedProtect Update Checker");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final JSONArray jsonArray = (JSONArray) JSONValue.parse(response);
            if (jsonArray.size() == 0) {
                MRPMain.getInstance().getLogger().warning("No files found, or Feed URL is bad.");
                return currentVersion;
            }
            JSONObject jsonObject = (JSONObject) jsonArray.get(jsonArray.size() - 1);
            String versionName = ((String) jsonObject.get("name"));
            String newVersion = versionName.substring(versionName.lastIndexOf("v") + 1);
            return Double.parseDouble(newVersion);
        } catch (Exception e) {
            e.printStackTrace();
            return currentVersion;
        }
    }
}
