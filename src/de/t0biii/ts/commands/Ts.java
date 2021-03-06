package de.t0biii.ts.commands;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.t0biii.ts.TeamSpeak;
import de.t0biii.ts.methods.JsonMessage;
import de.t0biii.ts.methods.SendHelp;
import de.t0biii.ts.methods.Updater;
import de.t0biii.ts.methods.Updater.UpdateResult;
import de.t0biii.ts.methods.files.DBManager;
import de.t0biii.ts.methods.files.Filter;
import de.t0biii.ts.methods.files.Messages;


public class Ts implements CommandExecutor{
	
	private static TeamSpeak pl;
	public Ts(TeamSpeak pl){ Ts.pl = pl; }

	static String tsip = "";
	static Messages me = new Messages(pl);
	static Filter fil = new Filter(pl);
	static DBManager db = new DBManager();
	static File file = me.getFile();
	static YamlConfiguration cfg = me.getcfg();	
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{	
		if(pl.getConfig().getInt("ts3.port") == 9987){
			tsip = pl.getConfig().getString("ts3.ip");
		}else{
			 tsip = pl.getConfig().getString("ts3.ip") + ":" + pl.getConfig().getString("ts3.port");
		}
		if(sender instanceof Player)
		{
			final Player p = (Player) sender;
			if(args.length == 1)
            { 
			if(args[0].equalsIgnoreCase("rl"))
			{					
				//RELOAD COMMAND
				if(p.hasPermission("ts.reload"))
				{
				 pl.reloadConfig();
				 prefixsend(p);
				 p.sendMessage("");
				 p.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString("messages.reload")));
       		     p.sendMessage("");
       		     prefixsend(p);		  
				}else
				{
					p.sendMessage(ChatColor.translateAlternateColorCodes('&',  cfg.getString("messages.no-permission")));
				}
				//HELP COMMAND
			}else if(args[0].equalsIgnoreCase("help")){
				SendHelp sh = new SendHelp();
				sh.sendHelp(p);
             /*
              * UPDATE COMMAND    
              */
			}else if(args[0].equalsIgnoreCase("update")){
				if(p.hasPermission("ts.update") || p.isOp()){		
					if(pl.updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE){
						prefixsend(p);
						p.sendMessage("");
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&l"+cfg.getString("messages.update-info")));
						p.sendMessage("�2Download Link:");
						p.sendMessage(ChatColor.BLUE+ pl.updater.getLatestFileLink());
						p.sendMessage("");
						prefixsend(p);
					}else if(pl.updater.getResult() != UpdateResult.UPDATE_AVAILABLE){
						prefixsend(p);
						p.sendMessage("");
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',  cfg.getString("messages.no-update")));
						p.sendMessage("");
						prefixsend(p);
					}
				}		
			}
			//GET IP COMMAND
			else if(args[0].equalsIgnoreCase("getip")){
				prefixsend(p);	
				p.sendMessage("");
				{
					JsonMessage jm = new JsonMessage();
					jm.append("�1�lClick This")
					.setClickAsSuggestCmd(ChatColor.translateAlternateColorCodes('&',  tsip))
					.setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', tsip)).save().send(p);
				}
				p.sendMessage("");
				prefixsend(p);	
			//RL-Filter COMMAND	
			}else if(args[0].equalsIgnoreCase("rl-filter")){
				if(p.isOp() || p.hasPermission("ts.filter")){
				pl.fi.loadFilter();
				prefixsend(p);
				p.sendMessage("");
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString("messages.reloadfilter")));
				p.sendMessage("");
				prefixsend(p);
				}else{
					tsipsend(p);
				}	
			//cache-off COMMAND	
			}else if(args[0].equalsIgnoreCase("cache-off")){
				if(p.isOp() || p.hasPermission("ts.cache")){
					if(!pl.getConfig().getBoolean("options.realtime.activated")){
						pl.getConfig().set("options.realtime.activated", true);
						pl.saveConfig();
						prefixsend(p);
						p.sendMessage("");
						p.sendMessage("�3Live data �2activated. \n�3Cache �cdisabled.");
						p.sendMessage("");
						prefixsend(p);						
					}else{
						prefixsend(p);
						p.sendMessage("");
						p.sendMessage("�3Live data is already �2activated");
						p.sendMessage("");
						prefixsend(p);
					}
				}else{
					tsipsend(p);
				}
			//cache-on COMMAND	
			}else if(args[0].equalsIgnoreCase("cache-on")){
				if(p.isOp() || p.hasPermission("ts.cache")){
					if(pl.getConfig().getBoolean("options.realtime.activated")){
						pl.getConfig().set("options.realtime.activated", false);
						pl.saveConfig();
						prefixsend(p);
						p.sendMessage("");
						p.sendMessage("�3Live data �cdisabled. \n�3Cache �2activated.");
						p.sendMessage("");
						prefixsend(p);		
					}else{
						prefixsend(p);
						p.sendMessage("");
						p.sendMessage("�3Live data is already �cdisabled");
						p.sendMessage("");
						prefixsend(p);
					}
				}else{
					tsipsend(p);
				}
			//cache COMMAND
			}else if(args[0].equalsIgnoreCase("cache")){
				if(p.isOp() || p.hasPermission("ts.cache")){
				 pl.dbupdate();
				 prefixsend(p);
				 {
					 JsonMessage jm = new JsonMessage();
					 jm.append(ChatColor.translateAlternateColorCodes('&', cfg.getString("messages.cachenew")) ).save().send(p);;
				 }
				 prefixsend(p);
				}
			//List COMMAND
			}else if(args[0].equalsIgnoreCase("list")){
				if(pl.error){
					p.sendMessage("�cTeamspeak is unreachable!");
				}else{
					try{
						YamlConfiguration fcfg = fil.getcfg();
						int anzahl = db.getInt("min");
						int max = db.getInt("max");
						List<String> cachelist = db.getArray();
						List<String> filter = fcfg.getStringList("ignore");
						
				if(!pl.getConfig().getBoolean("options.realtime.activated")){ 
					prefixsend(p);	
					p.sendMessage(ChatColor.AQUA+"Teamspeak: "+ tsip + " �cCached");
					p.sendMessage(ChatColor.AQUA+"Online: �2"+ (anzahl) +" of " +max);
					p.sendMessage(ChatColor.AQUA+"List of People: ");
					for(String Users : cachelist){
						if(!filter.contains(Users)){
							p.sendMessage("�2"+Users);
						}
					}
					prefixsend(p);	
					
				}else{
					prefixsend(p);	
					p.sendMessage(ChatColor.AQUA+"Teamspeak: "+ tsip + " �2Realtime");
					p.sendMessage(ChatColor.AQUA+"Online: �2"+ (pl.api.getClients().size()) +" of " + pl.api.getHostInfo().getTotalMaxClients());
					p.sendMessage(ChatColor.AQUA+"List of People:");
					for (Client c : pl.api.getClients()) {
						if(!filter.contains(c.getNickname())){	
							p.sendMessage("�2"+c.getNickname());
						}
					}
					prefixsend(p);
				}
				}catch(Exception e){
					p.sendMessage(ChatColor.AQUA+"Online: �2- of -" );
					p.sendMessage(ChatColor.AQUA+"List of People:");
					p.sendMessage("�4Not enough permissions");
					prefixsend(p);
				}
				}				
			}else{
				tsipsend(p);
			}
            }else 
            {
            	 tsipsend(p);
            } 
			return true;
			} else  {
				sender.sendMessage(cfg.getString("messages.konsole"));
			}
		return false;
	}
	
	public static void tsipsend(Player p){
		prefixsend(p);
 		p.sendMessage("");
 		{
 			JsonMessage jm = new JsonMessage();
 			jm.append(ChatColor.translateAlternateColorCodes('&',  cfg.getString("messages.ts3")))
 			.setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', tsip))
 			.setClickAsSuggestCmd(ChatColor.translateAlternateColorCodes('&',  tsip)).save().send(p);
 		}   
 		p.sendMessage("");
 		prefixsend(p);	
	}
	
	public static void prefixsend(Player p){
		p.sendMessage(ChatColor.YELLOW+"[]================"+ChatColor.GOLD +ChatColor.BOLD +" TeamSpeak "+ChatColor.RESET +ChatColor.YELLOW+"===============[]");
	}
}