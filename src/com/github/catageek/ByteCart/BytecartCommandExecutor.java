package com.github.catageek.ByteCart;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;

import com.github.catageek.ByteCart.EventManagement.ByteCartInventoryListener;
import com.github.catageek.ByteCart.Routing.Address;
import com.github.catageek.ByteCart.Routing.AddressFactory;
import com.github.catageek.ByteCart.Routing.AddressRouted;
import com.github.catageek.ByteCart.Routing.AddressString;
import com.github.catageek.ByteCart.Routing.Updater;
import com.github.catageek.ByteCart.Signs.BC7010;
import com.github.catageek.ByteCart.Signs.BC7011;

public class BytecartCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("mego")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
			} else {
				Player player = (Player) sender;

				if(args.length == 0 || args.length >= 3 || !AddressString.isAddress(args[0]))
					return false;

				Address address = new AddressString(args[0]);

				if (args.length == 2 && args[1].equalsIgnoreCase("train"))
					address.setTrain(true);

				(new BC7010(player.getLocation().getBlock(), player)).setAddress(address);
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("sendto")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
			} else {
				Player player = (Player) sender;

				if(args.length == 0 || args.length >= 3 || !AddressString.isAddress(args[0]))
					return false;

				Address address = new AddressString(args[0]);

				if (args.length == 2 && args[1].equalsIgnoreCase("train"))
					address.setTrain(true);

				final class Execute implements ModifiableRunnable<Inventory> {

					private final Player player;
					private final Address address;
					private Inventory inventory;


					public Execute(Player player, Address address) {
						this.player = player;
						this.address = address;
					}

					public void run() {
						if ((new BC7011(player.getLocation().getBlock(), ((StorageMinecart) inventory.getHolder()))).setAddress(address)) {
							player.sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.YELLOW + ByteCart.myPlugin.getConfig().getString("Info.SetAddress"));
							player.sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.YELLOW + ByteCart.myPlugin.getConfig().getString("Info.GetTTL") + AddressFactory.<AddressRouted>getAddress(inventory).getTTL());
						}
						else
							player.sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.RED + ByteCart.myPlugin.getConfig().getString("Error.SetAddress") );

					}


					/**
					 * @param inventory 
					 * @param inventory the inventory to set
					 */

					@Override
					public void SetParam(Inventory inventory) {
						this.inventory = inventory;
					}

				}


				player.sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.YELLOW + ByteCart.myPlugin.getConfig().getString("Info.RightClickCart") );
				new ByteCartInventoryListener(ByteCart.myPlugin, player, new Execute(player, address));
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("bcupdater")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
			} else {
				Player player = (Player) sender;

				int region = 0;

				if(args.length == 0 || args.length > 3 || !Updater.Level.isMember(args[0].toLowerCase()))
					return false;

				if (args.length == 1 && ! args[0].equalsIgnoreCase("backbone")
						&& ! args[0].equalsIgnoreCase("reset_backbone"))
					return false;

				if (args[0].equalsIgnoreCase("region") || args[0].equalsIgnoreCase("local")
						|| args[0].equalsIgnoreCase("reset_region")
						|| args[0].equalsIgnoreCase("reset_local")) {
					region = Integer.parseInt(args[1]);
					if (region < 1 || region > 53)
						return false;
				}
				else
					return false;			

				final class Execute implements ModifiableRunnable<Inventory> {

					private final Player player;
					private final Updater.Level level;
					private final int region;
					private Inventory inventory;


					public Execute(Player player, Updater.Level level, int region) {
						this.player = player;
						this.level = level;
						this.region = region;
					}

					public void run() {
						int id = ((StorageMinecart) inventory.getHolder()).getEntityId();
						ByteCart.myPlugin.getUm().addUpdater(id, level, region);
						player.sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.RED + ByteCart.myPlugin.getConfig().getString("Info.SetUpdater") );

					}


					/**
					 * @param inventory 
					 * @param inventory the inventory to set
					 */

					@Override
					public void SetParam(Inventory inventory) {
						this.inventory = inventory;
					}

				}

				player.sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.YELLOW + ByteCart.myPlugin.getConfig().getString("Info.RightClickCart") );
				new ByteCartInventoryListener(ByteCart.myPlugin, player, new Execute(player, Updater.Level.valueOf(args[0].toUpperCase()), region));
			}
			return true;
		}

		return false;
	}

}