package xyz.mackan.Namie;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Namie extends JavaPlugin {
	public static Logger log;

	public static String dataPath = null;

	public static FileConfiguration config;

	public static boolean doFilter = true;

	@Override
	public void onEnable () {
		log = this.getLogger();
		dataPath = this.getDataFolder().getPath();

		this.getCommand("rename").setExecutor(new RenameCommand());

		log.info("Namie enabled");
	}

	@Override
	public void onDisable () {
		log.info("Namie disabled.");
	}
}
