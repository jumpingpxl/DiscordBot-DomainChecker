package de.jumpingpxl.womenareproperties.domainchecker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.jumpingpxl.womenareproperties.domainchecker.listener.GuildMessageReceivedListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DomainChecker {

	private final SimpleDateFormat loggingDateFormat = new SimpleDateFormat("HH:mm:ss");
	private final JsonObject config;
	private JDA jda;
	private TextChannel textChannel;

	public DomainChecker() {
		log("Starting...");

		log("Loading Configuration...");
		config = getJsonObjectFromResources("config.json");

		log("Starting the Bot...");
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(config.get("token").getAsString())
					.setEnableShutdownHook(true)
					.setAutoReconnect(true)
					.setStatus(OnlineStatus.ONLINE)
					.addEventListener(new GuildMessageReceivedListener(this), new ListenerAdapter() {
						@Override
						public void onReady(ReadyEvent event) {
							textChannel = event.getJDA().getTextChannelById(
									config.get("channelId").getAsString());
							log("Successfully Started the Bot.");
						}
					})
					.buildBlocking();
		} catch (LoginException | InterruptedException e) {
			log("An Error Occurred While Starting the Bot...");
			e.printStackTrace();
			log("Aborting...");
			System.exit(0);
		}
	}

	public void log(String value) {
		System.out.println(loggingDateFormat.format(new Date()) + " " + value);
	}

	private JsonObject getJsonObjectFromResources(String fileName) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
				StandardCharsets.UTF_8);
		return (JsonObject) new JsonParser().parse(inputStreamReader);
	}

	public JsonObject getJsonObjectFromUrl(String domain) throws IOException, JsonSyntaxException {
		InputStreamReader inputStreamReader;

		URLConnection urlConnection = new URL(config.get("request")
				.getAsString()
				.replace("{0}", domain)).openConnection();
		urlConnection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 " + "Firefox/56.0");
		urlConnection.setReadTimeout(5000);
		urlConnection.setConnectTimeout(5000);
		urlConnection.connect();
		inputStreamReader = new InputStreamReader(urlConnection.getInputStream(),
				StandardCharsets.UTF_8);

		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String line;
		StringBuilder contents = new StringBuilder();
		while ((line = bufferedReader.readLine()) != null) {
			contents.append(contents.toString().equals("") ? "" : "\n").append(line);
		}

		return (JsonObject) new JsonParser().parse(contents.toString());
	}

	public JsonObject getConfig() {
		return config;
	}

	public TextChannel getTextChannel() {
		return textChannel;
	}
}
