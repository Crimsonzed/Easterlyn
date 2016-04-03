package co.sblock.discord;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import co.sblock.discord.abstraction.DiscordCallable;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;

/**
 * A Thread for periodically executing DiscordCallables from the given Queue.
 * 
 * @author Jikoo
 */
public class QueueDrainThread extends Thread {

	private final Discord discord;
	private final Queue<DiscordCallable> queue;
	private final long delay;

	public QueueDrainThread(Discord discord, long delay, String threadName) {
		super(threadName);
		this.discord = discord;
		this.queue = new PriorityBlockingQueue<>();
		this.delay = delay;
	}

	@Override
	public void run() {

		while (discord.isEnabled()) {

			if (!discord.isReady()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
				continue;
			}

			if (queue.isEmpty()) {
				// Sleep for duration specified
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					break;
				}
				continue;
			}

			if (!discord.getClient().isReady()) {
				discord.getLogger().warning("Bot has likely been logged out. Attempting to log back in.");
				try {
					discord.getClient().login();
					Thread.sleep(1000);
					continue;
				} catch (DiscordException de) {
					de.printStackTrace();
					discord.getLogger().severe("Unable to log bot back in. Attempting more drastic measures.");
					discord.disable();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						break;
					}
					discord.enable();
					break;
				} catch (InterruptedException ie) {
					break;
				}
			}

			DiscordCallable callable = queue.remove();
			try {
				callable.call();
			} catch (DiscordException e) {
				try {
					// Currently, Discord4J is firing all exceptions as DiscordExceptions
					// That means we're being rate limited and forcibly logged out because
					// it isn't properly informing us when rate limiting occurs.
					// If we encounter an exception, assume it's rate limiting and pause.
					Thread.sleep(500L);
				} catch (InterruptedException ie) {
					break;
				}
				if (callable.retryOnException()) {
					/*
					 * Rather than skip removal in this case to preserve order, we re-add the
					 * DiscordCallable. This does ruin order in the case of messages sent, however,
					 * sent messages do not currently retry. It also allows us to modify the queue from inside
					 * the callables safely.
					 */
					queue.add(callable);
					// Don't log when retrying, we only retry because of a Discord4J fault generally.
					continue;
				}
				e.printStackTrace();
			} catch (HTTP429Exception e) {
				try {
					// Pause, we're rate limited.
					Thread.sleep(e.getRetryDelay());
				} catch (InterruptedException ie) {
					break;
				}
				continue;
			} catch (Exception e) {
				// Likely permissions, but can be malformed JSON when odd responses are received
				e.printStackTrace();
			}
		}
	}

	public void queue(DiscordCallable callable) {
		this.queue.add(callable);
	}

}
