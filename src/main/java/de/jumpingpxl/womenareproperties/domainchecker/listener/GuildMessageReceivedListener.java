package de.jumpingpxl.womenareproperties.domainchecker.listener;

import com.google.gson.JsonObject;
import de.jumpingpxl.womenareproperties.domainchecker.DomainChecker;
import de.jumpingpxl.womenareproperties.domainchecker.util.ScheduledTask;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GuildMessageReceivedListener extends ListenerAdapter {

	private final DomainChecker domainChecker;

	public GuildMessageReceivedListener(DomainChecker domainChecker) {
		this.domainChecker = domainChecker;
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot() || !event.getChannel().getId().equals(
				domainChecker.getTextChannel().getId())) {
			return;
		}

		String message = event.getMessage().getContentRaw();
		if (!message.contains(".") || message.contains(" ")) {
			delete(event);
			return;
		}

		new Thread(() -> {
			try {
				JsonObject jsonObject = domainChecker.getJsonObjectFromUrl(message);
				domainChecker.log("debug -> " + jsonObject);

				if (jsonObject.has("domain")) {
					jsonObject = (JsonObject) jsonObject.get("domain");
					if (!jsonObject.get("available").getAsBoolean()) {
						delete(event);
					}
				} else if (jsonObject.has("error")) {
					delete(event);
				}
			} catch (IOException e) {
				e.printStackTrace();
				domainChecker.log(
						"An Error Occurred While Loading the Information for \"" + message + "\"");
			}
		}).start();
	}

	private void delete(GuildMessageReceivedEvent event) {
		event.getMessage().delete().queue();
		event.getMessage().getChannel().sendMessage(
				domainChecker.getConfig().get("messageOnDelete").getAsString()).queue(
				success -> new ScheduledTask(() -> success.delete().queue()).delay(
						domainChecker.getConfig().get("deleteMessageAfter").getAsInt(), TimeUnit.SECONDS));
	}
}
