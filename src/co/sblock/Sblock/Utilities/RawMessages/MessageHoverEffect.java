package co.sblock.Sblock.Utilities.RawMessages;

/**
 * Adds an effect to a MessageElement's text when a player
 * moves their mouse over the text in chat.
 * <p>
 * This is the only Message portion in which you are encouraged
 * to directly specify ChatColor, if any.
 * 
 * @author Jikoo
 */
public class MessageHoverEffect {
	public enum HoverEffect {
		SHOW_TEXT, SHOW_ITEM, SHOW_ACHIEVEMENT, SHOW_ENTITY;
	}

	private HoverEffect effect;
	private String data;
	public MessageHoverEffect(HoverEffect effect, String data) {
		this.effect = effect;
		this.data = data;
	}

	public String toString() {
		return new StringBuilder(",\"hoverEvent\":{\"action\":\"").append(effect.name().toLowerCase())
				.append("\",\"value\":\"").append(data).append("\"}").toString();
	}
}
