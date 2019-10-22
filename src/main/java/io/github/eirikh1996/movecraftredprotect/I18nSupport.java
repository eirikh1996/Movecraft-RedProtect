package io.github.eirikh1996.movecraftredprotect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class I18nSupport {
    private static Properties languageFile;
    public static boolean initialize(){
        languageFile = new Properties();
        File file = new File(MRPMain.getInstance().getDataFolder().getAbsolutePath() + "/localisation/lang_" + Settings.locale + ".properties");
        try {
            languageFile.load(new FileInputStream(file));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            MRPMain.getInstance().getServer().getPluginManager().disablePlugin(MRPMain.getInstance());
            return false;
        }
    }

    public static String getInternationalisedString(String key){
        String property = languageFile.getProperty(key);
        return property != null ? property : key;
    }
}
