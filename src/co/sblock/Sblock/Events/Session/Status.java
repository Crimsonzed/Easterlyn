package co.sblock.Sblock.Events.Session;

import org.bukkit.ChatColor;

/**
 * Enum representing the status of Minecraft's servers.
 * 
 * @author Jikoo
 */
public enum Status {
	LOGIN("Login servers aren't responding, don't close your client!",
			"Login servers are back up, closing Minecraft is ok!", null),
	SESSION("Session servers aren't responding, don't disconnect!",
			"Session servers are back up, multiplayer will work!",
			"Session servers aren't responding! You might not be able to log in."),
	BOTH("Session and login servers aren't responding, don't disconnect!",
			"Session and login servers are back up! Continue play as usual.",
			"Session servers aren't responding! You might not be able to log in."),
	NEITHER(null, null, null);

	/** The message to broadcast initially. */
	private String announcement;

	/** The message to broadcast when over. */
	private String allClear;

	/** The server list MOTD to set. */
	private String MOTD;

	/**
	 * Constructor for Status.
	 * 
	 * @param announcement the message to broadcast when Status is set
	 * @param allclear the message to broadcast when Status is changed
	 * @param MOTD the MOTD to set in the server list
	 */
	private Status(String announcement, String allclear, String MOTD) {
		this.announcement = announcement;
		this.allClear = allclear;
		this.MOTD = MOTD;
	}

	/**
	 * Check if the Status has a message to broadcast initially.
	 * 
	 * @return true if the message is not null
	 */
	public boolean hasAnnouncement() {
		return announcement != null;
	}

	/** 
	 * Gets the message to broadcast initially.
	 * 
	 * @return String
	 */
	public String getAnnouncement() {
		return announcement;
	}

	/**
	 * Check if the Status has a message to broadcast when over.
	 * 
	 * @return true if the message is not null
	 */
	public boolean hasAllClear() {
		return allClear != null;
	}

	/** 
	 * Gets the message to broadcast when over.
	 * 
	 * @return String
	 */
	public String getAllClear() {
		return allClear;
	}

	/**
	 * Check if the Status has a server list MOTD.
	 * 
	 * @return true if the message is not null
	 */
	public boolean hasMOTDChange() {
		return MOTD != null;
	}

	/** 
	 * Gets the message to set as server list MOTD.
	 * 
	 * @return String
	 */
	public String getMOTDChange() {
		return ChatColor.RED + "[Lil Hal] " + MOTD;
	}
}
