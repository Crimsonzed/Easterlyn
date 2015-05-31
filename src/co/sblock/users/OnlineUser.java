package co.sblock.users;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.effects.FXManager;
import co.sblock.effects.fx.SblockFX;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.Icon;
import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.Log;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.inventory.InventoryManager;
import co.sblock.utilities.progression.ServerMode;
import co.sblock.utilities.spectator.Spectators;

/**
 * Represents a Player currently logged into the game.
 * 
 * @author Jikoo
 */
public class OnlineUser extends OfflineUser {

	private final Map<String, SblockFX> effectsList;

	private Location serverDisableTeleport;
	private boolean isServer;

	protected OnlineUser(UUID userID, String ip, YamlConfiguration yaml, Location previousLocation,
			Set<Integer> programs, String currentChannel, Set<String> listening) {
		super(userID, ip, yaml, previousLocation, programs, currentChannel, listening);
		effectsList = new HashMap<>();
		isServer = false;
	}

	@Override
	public Player getPlayer() {
		return Bukkit.getPlayer(getUUID());
	}

	@Override
	public String getDisplayName() {
		return getPlayer().getDisplayName();
	}

	@Override
	public void updateFlight() {
		new BukkitRunnable() {
			@Override
			public void run() {
				boolean allowFlight = getPlayer() != null && (getPlayer().getWorld().getName().equals("Derspit")
						|| getPlayer().getGameMode() == GameMode.CREATIVE
						|| getPlayer().getGameMode() == GameMode.SPECTATOR
						|| isServer || Spectators.getInstance().isSpectator(getUUID()));
				if (getPlayer() != null) {
					getPlayer().setAllowFlight(allowFlight);
					getPlayer().setFlying(allowFlight);
				}
				getYamlConfiguration().set("flying", allowFlight);
			}
		}.runTask(Sblock.getInstance());
	}

	@Override
	public String getTimePlayed() {
		long time = getPlayer().getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK);
		long days = time / (24 * 60 * 60 * 20);
		time -= days * 24 * 60 * 60 * 20;
		long hours = time / (60 * 60 * 20);
		time -= hours * 60 * 60 * 20;
		time = time / (60 * 20);
		DecimalFormat decimalFormat = new DecimalFormat("00");
		return days + " days, " + decimalFormat.format(hours) + ':' + decimalFormat.format(time);
	}

	@Override
	public Location getCurrentLocation() {
		return getPlayer().getLocation();
	}

	@Override
	public Region getCurrentRegion() {
		Region region = Region.getRegion(getPlayer().getWorld().getName());
		if (region.isDream()) {
			return getDreamPlanet();
		}
		return region;
	}

	@Override
	public void updateCurrentRegion(Region newRegion) {
		Region oldRegion = super.getCurrentRegion();
		if (newRegion.isDream()) {
			getPlayer().setPlayerTime(newRegion == Region.DERSE ? 18000L : 6000L, false);
		} else {
			getPlayer().resetPlayerTime();
		}
		if (oldRegion != null && newRegion == oldRegion) {
			if (!getListening().contains(oldRegion.getChannelName())) {
				Channel channel = ChannelManager.getChannelManager().getChannel(oldRegion.getChannelName());
				addListening(channel);
			}
			return;
		}
		if (currentChannel == null || oldRegion != null && currentChannel.equals(oldRegion.getChannelName())) {
			currentChannel = newRegion.getChannelName();
		}
		if (oldRegion != null && !oldRegion.getChannelName().equals(newRegion.getChannelName())) {
			removeListening(oldRegion.getChannelName());
		}
		if (!getListening().contains(newRegion.getChannelName())) {
			addListening(ChannelManager.getChannelManager().getChannel(newRegion.getChannelName()));
		}
		if (oldRegion == null || !oldRegion.getResourcePackURL().equals(newRegion.getResourcePackURL())) {
			getPlayer().setResourcePack(newRegion.getResourcePackURL());
		}
		setCurrentRegion(newRegion);
	}

	/**
	 * Check if the User is in server mode.
	 * 
	 * @return true if the User is in server mode
	 */
	public boolean isServer() {
		return this.isServer;
	}

	public void startServerMode() {
		Player p = this.getPlayer();
		if (Spectators.getInstance().isSpectator(getUUID())) {
			Spectators.getInstance().removeSpectator(p);
		}
		if (getClient() == null) {
			p.sendMessage(Color.BAD + "You must have a client to enter server mode!"
					+ "+\nAsk someone with " + Color.COMMAND + "/requestclient <player>");
			return;
		}
		OnlineUser u = Users.getOnlineUser(getClient());
		if (u == null) {
			p.sendMessage(Color.BAD + "You should wait for your client before progressing!");
			return;
		}
		if (!u.getPrograms().contains(Icon.SBURBCLIENT.getProgramID())) {
			p.sendMessage(Color.BAD + u.getPlayerName() + " does not have the Sburb Client installed!");
			return;
		}
		Machine m = Machines.getInstance().getComputer(getClient());
		if (m == null) {
			p.sendMessage(Color.BAD + u.getPlayerName() + " has not placed their computer in their house!");
			return;
		}
		this.serverDisableTeleport = p.getLocation();
		if (!Machines.getInstance().isByComputer(u.getPlayer(), 25)) {
			p.teleport(m.getKey());
		} else {
			p.teleport(u.getPlayer());
		}
		this.isServer = true;
		this.updateFlight();
		p.setNoDamageTicks(Integer.MAX_VALUE);
		InventoryManager.storeAndClearInventory(p);
		p.getInventory().addItem(MachineType.COMPUTER.getUniqueDrop());
		p.getInventory().addItem(MachineType.CRUXTRUDER.getUniqueDrop());
		p.getInventory().addItem(MachineType.PUNCH_DESIGNIX.getUniqueDrop());
		p.getInventory().addItem(MachineType.TOTEM_LATHE.getUniqueDrop());
		p.getInventory().addItem(MachineType.ALCHEMITER.getUniqueDrop());
		for (Material mat : ServerMode.getInstance().getApprovedSet()) {
			p.getInventory().addItem(new ItemStack(mat));
		}
		p.sendMessage(Color.GOOD + "Server mode enabled!");
	}

	public void stopServerMode() {
		if (Bukkit.getOfflinePlayer(getClient()).isOnline()) {
			Player clientPlayer = Bukkit.getPlayer(getClient());
			for (ItemStack is : getPlayer().getInventory()) {
				if (Captcha.isPunch(is)) {
					clientPlayer.getWorld().dropItem(clientPlayer.getLocation(), is).setPickupDelay(0);
					break;
				}
			}
		}
		this.isServer = false;
		this.updateFlight();
		Player p = this.getPlayer();
		p.teleport(serverDisableTeleport);
		p.setFallDistance(0);
		p.setNoDamageTicks(0);
		InventoryManager.restoreInventory(p);
		p.sendMessage(Color.GOOD + "Server program closed!");
	}

	@Override
	public void sendMessage(String message) {
		Player player = this.getPlayer();
		if (player == null) {
			return;
		}
		player.sendMessage(message);
	}

	@Override
	public void setCurrentChannel(Channel c) {
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel("null"));
			return;
		}
		if (c.isBanned(this)) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), c.getName()));
			return;
		}
		if (!c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c.getName()));
			return;
		}
		currentChannel = c.getName();
		if (!this.getListening().contains(c.getName())) {
			this.addListening(c);
		} else {
			this.sendMessage(ChatMsgs.onChannelSetCurrent(c.getName()));
		}
	}

	@Override
	public boolean addListening(Channel channel) {
		if (channel == null) {
			return false;
		}
		if (channel.isBanned(this)) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), channel.getName()));
			return false;
		}
		if (!channel.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(channel.getName()));
			return false;
		}
		if (!this.getListening().contains(channel)) {
			this.getListening().add(channel.getName());
		}
		if (!channel.getListening().contains(getUUID())) {
			channel.addListening(getUUID());
			this.getListening().add(channel.getName());
			channel.sendMessage(ChatMsgs.onChannelJoin(this, channel));
			return true;
		} else {
			this.sendMessage(ChatMsgs.errorAlreadyListening(channel.getName()));
			return false;
		}
	}

	@Override
	public void handleLoginChannelJoins() {
		for (Iterator<String> iterator = this.getListening().iterator(); iterator.hasNext();) {
			Channel c = ChannelManager.getChannelManager().getChannel(iterator.next());
			if (c != null && !c.isBanned(this) && (c.getAccess() != AccessLevel.PRIVATE || c.isApproved(this))) {
				this.getListening().add(c.getName());
				c.addListening(this.getUUID());
			} else {
				iterator.remove();
			}
		}
		if (this.getPlayer().hasPermission("sblock.felt") && !this.getListening().contains("@")) {
			this.getListening().add("@");
			ChannelManager.getChannelManager().getChannel("@").addListening(this.getUUID());
		}
		String base = new StringBuilder(Color.GOOD_PLAYER.toString()).append(this.getDisplayName())
				.append(Color.GOOD).append(" began pestering <>").append(Color.GOOD)
				.append(" at ").append(new SimpleDateFormat("HH:mm").format(new Date())).toString();
		// Heavy loopage ensues
		for (OfflineUser u : Users.getUsers()) {
			if (!u.isOnline() || !(u instanceof OnlineUser)) {
				continue;
			}
			StringBuilder matches = new StringBuilder();
			for (String s : this.getListening()) {
				if (u.getListening().contains(s)) {
					matches.append(Color.GOOD_EMPHASIS).append(s).append(Color.GOOD).append(", ");
				}
			}
			String message;
			if (matches.length() > 0) {
				matches.replace(matches.length() - 3, matches.length() - 1, "");
				StringBuilder msg = new StringBuilder(base.replace("<>", matches.toString()));
				int comma = msg.lastIndexOf(",");
				if (comma != -1) {
					if (comma == msg.indexOf(",")) {
						msg.replace(comma, comma + 1, " and");
					} else {
						msg.insert(comma + 1, " and");
					}
				}
				message = msg.toString();
			} else {
				message = base.replace(" <>", "");
			}
			u.sendMessage(message);
		}

		Log.anonymousInfo(base.toString().replace("<>", StringUtils.join(getListening(), ", ")));
	}

	@Override
	public void removeListening(String channelName) {
		Channel c = ChannelManager.getChannelManager().getChannel(channelName);
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel(channelName));
			this.getListening().remove(channelName);
			return;
		}
		if (this.getListening().remove(channelName)) {
			c.removeNick(this, false);
			c.sendMessage(ChatMsgs.onChannelLeave(this, c));
			c.removeListening(this.getUUID());
			if (this.currentChannel != null && channelName.equals(this.getCurrentChannel().getName())) {
				this.currentChannel = null;
			}
		} else {
			this.sendMessage(Color.BAD + "You are not listening to " + Color.BAD_EMPHASIS + channelName);
		}
	}

	@Override
	public boolean getComputerAccess() {
		if (!Chat.getComputerRequired()) {
			// Overrides the computer limitation for pre-Entry shenanigans
			return true;
		}
		return getEffects().containsKey("Computer") || Machines.getInstance().isByComputer(this.getPlayer(), 10);
	}

	/**
	 * Gets the user's current Effects
	 * 
	 * @return the Map of effects
	 */
	public Map<String, SblockFX> getEffects() {
		return this.effectsList;
	}

	/**
	 * Set the user's current Effects. Will overlay existing map.
	 * 
	 * @param effects the map of all Effects to add
	 */
	public void setAllEffects(HashMap<String, SblockFX> effects) {
		removeAllEffects();
		for (SblockFX entry : effects.values()) {
			this.effectsList.put(entry.getCanonicalName(), entry);
			if (entry.isPassive()) {
				entry.applyEffect(this, null);
			}
		}
	}

	/**
	 * Removes all Effects from the user and cancels the Effect
	 */
	public void removeAllEffects() {
		for (SblockFX effect : effectsList.values()) {
			effect.removeEffect(this);
		}
		this.effectsList.clear();
	}

	/**
	 * Add a new effect to the user's current Effects. If the effect is already present, increases
	 * the strength by 1.
	 * 
	 * @param effect the Effect to add
	 */
	public void addEffect(SblockFX effect) {
		if (this.effectsList.containsKey(effect.getCanonicalName())) {
			effect.setMultiplier(this.effectsList.get(effect.getCanonicalName()).getMultiplier()
					+ effect.getMultiplier());
			this.effectsList.put(effect.getCanonicalName(), effect);
		} else {
			this.effectsList.put(effect.getCanonicalName(), effect);
		}
		if (effect.isPassive()) {
			effect.applyEffect(this, null);
		}
	}

	/**
	 * Reduces the multiplier on an effect
	 * 
	 * @param effect The effect to change
	 * @param reduction The amount to reduce the multiplier by
	 */
	public void reduceEffect(SblockFX effect, Integer reduction) {
		if (this.effectsList.containsKey(effect.getCanonicalName())) {
			if (this.effectsList.get(effect.getCanonicalName()).getMultiplier() - reduction > 0) {
				effect.setMultiplier(effect.getMultiplier() - reduction);
				if (effect.isPassive()) {
					effect.applyEffect(this, null);
				}
			} else {
				this.effectsList.remove(effect.getCanonicalName());
				effect.removeEffect(this);
			}
		}
	}
	
	public void applyGodtierPassiveEffect() {
		if(this.getProgression().value() >= ProgressionState.GODTIER.value()) {
			SblockFX passive = null;
			switch(this.getUserAspect()) {
			case BLOOD:
				break;
			case BREATH:
				break;
			case DOOM:
				break;
			case HEART:
				try {
					passive = FXManager.getInstance().getValidEffects().get("ABSORPTION").newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					Sblock.getLog().info("Passive Heart Effect Exception");
					e.printStackTrace();
				}
				break;
			case HOPE:
				break;
			case LIFE:
				break;
			case LIGHT:
				break;
			case MIND:
				break;
			case RAGE:
				break;
			case SPACE:
				break;
			case TIME:
				break;
			case VOID:
				break;
			default:
				break;
			}
			if(passive != null)	passive.applyEffect(this, null);
		}
	}
	
	public void applyGodtierActiveEffect() {
		if(this.getProgression().value() >= ProgressionState.GODTIER.value()) {
			SblockFX active = null;
			switch(this.getUserAspect()) {
			case BLOOD:
				break;
			case BREATH:
				break;
			case DOOM:
				break;
			case HEART:
				try {
					active = FXManager.getInstance().getValidEffects().get("FXGodtierHeartActive").newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					Sblock.getLog().info("Active Heart Effect Exception");
					e.printStackTrace();
				}
				break;
			case HOPE:
				break;
			case LIFE:
				break;
			case LIGHT:
				break;
			case MIND:
				break;
			case RAGE:
				break;
			case SPACE:
				break;
			case TIME:
				break;
			case VOID:
				break;
			default:
				break;
			}
			if(active != null)	active.applyEffect(this, null);
		}
	}

	@Override
	public OnlineUser getOnlineUser() {
		return this;
	}

	@Override
	public void save() {
		if (this.isOnline()) {
			super.save();
		} else {
			Users.getInstance().getLogger().warning("Online user did not unload for " + getUUID());
			OfflineUser.fromOnline(this).save();
		}
	}
}
