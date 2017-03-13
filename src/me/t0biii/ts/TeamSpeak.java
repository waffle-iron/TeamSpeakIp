package me.t0biii.ts;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;

import me.t0biii.ts.commands.Ts;
import me.t0biii.ts.commands.TsTapCompleter;
import me.t0biii.ts.listener.PlayerJoin;
import me.t0biii.ts.methods.Metrics;
import me.t0biii.ts.methods.Updater;
import me.t0biii.ts.methods.Updater.UpdateType;
import me.t0biii.ts.methods.files.Cache;
import me.t0biii.ts.methods.files.ConfigManager;
import me.t0biii.ts.methods.files.Filter;
import me.t0biii.ts.methods.files.Messages;

public class TeamSpeak extends JavaPlugin{
	
	/** 
	 * TS3 Prefix and Updater id
	 */
	public String prefix = "[TeamSpeakIP] ";
	public String Prefix = "�8[�6TeamSpeakIP�8] �f"; 
	private int uid = 70774;
	public Updater updater;
	
	
	public static TeamSpeak instance;
	private ConfigManager cm = new ConfigManager(this);
 	public Cache ca = new Cache(this);
 	private Messages ms = new Messages(this);
 	public Filter fi = new Filter(this);
	public Logger log = Bukkit.getLogger();

	/**
	 * TS Api
	 */
	public boolean error = false;   	
	public final TS3Config config = new TS3Config();
	public final TS3Query query = new TS3Query(config);
	public final TS3Api api = query.getApi();
	/**
	 * Load TS3 Login data
	 */
 	String host = getConfig().getString("ts3.ip");
 	int Queryport = getConfig().getInt("ts3.queryport");
 	int ts3port = getConfig().getInt("ts3.port");
	String queryname = getConfig().getString("ts3.querylogin.name");
	String querypw = getConfig().getString("ts3.querylogin.pw");
	String querydisplayname = getConfig().getString("ts3.queryname");
	
	/**
	 * Disable Part
	 */
	@Override
	public void onDisable() {
		if(!error){
			api.logout();
			query.exit();
		}
		log.info(prefix +"Plugin disabeld.");
	}
	
	/**
	 * Enable Part
	 */
	@Override
	public void onEnable() {
		PluginManager pm = Bukkit.getPluginManager();
		instance = this;
	    
		/**
		 * Config load and save
		 */
		cm.loadConfig();
		ms.loadMessages();
      	saveConfig();
    	
      	/**
    	 * TS Command and TapCompleter
    	 */
      	this.getCommand("ts").setExecutor(new Ts(this));
     	this.getCommand("ts").setTabCompleter(new TsTapCompleter());
      	
     	/**
     	 * Events registrieren
     	 */
     	pm.registerEvents(new PlayerJoin(this),this);
     	
     	/**
     	 * TS3 Connect
     	 */
     	try{
		config.setHost(host);
		config.setQueryPort(Queryport);
		config.setDebugLevel(Level.OFF);
		query.connect();
		log.info(prefix+"Connectet to Teamspeak!");
		}catch(Exception e){
			log.info(prefix+"Cant connect to Teamspeak!");
			error = true;
		}
		if(!error){	
			try{
				api.login(queryname, querypw);
				api.selectVirtualServerByPort(ts3port);
				api.setNickname(querydisplayname);
			}catch(Exception e){
				error = true;
				log.info(prefix+"Cant connect to Teamspeak!");
			}						
		} 
		
		/**
		 * Load Configurations
		 */
		if(!error)
     	ca.loadCache(api);
     	fi.loadFilter();
     	
     	/**
     	 * Metrics start
     	 */	
		if(getConfig().getBoolean("options.Metrics")){		
					Metrics metrics = new Metrics(this);
					startBstat(metrics);
		}
		
		/**
         * Updater 
         */
        updater = new Updater(this, uid, getFile(), UpdateType.NO_DOWNLOAD, true);
        
        /**
         * Start Auto Chache
         */
		int interval = 60;
		interval = getConfig().getInt("options.realtime.update");
		if(!error){
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				ca.cachetoconfig(api);
			}
		}, 20L, interval*20L);
		} 
	}
	
	public void startBstat(Metrics bstats){
		bstats.addCustomChart(new Metrics.SimplePie("update-info") {
		public String getValue() {
			String re = getConfig().getString("options.Update-info");
			if(re.equalsIgnoreCase("true") || re.equalsIgnoreCase("false")){
				return re;
			}
				return "unknow";
			}
		});	
	}
}