package com.amadornes.modcast.bot.discord

import akka.actor.Actor
import com.amadornes.modcast.bot.helpers.ShutdownHelper
import com.amadornes.modcast.bot.{Actors, Configuration}
import grizzled.slf4j.Logging
import sx.blah.discord.api.{ClientBuilder, IDiscordClient}
import sx.blah.discord.handle.impl.events.{MessageReceivedEvent, ReadyEvent}
import sx.blah.discord.handle.obj.IGuild

import scala.collection.JavaConversions._

/**
  * Created by rewbycraft on 11/2/16.
  */
class DiscordActor extends Actor with Logging {
	val eventHandler = new DiscordEventHandler(self)
	implicit var client: IDiscordClient = null
	val prefix = Configuration.config.getString("discord.prefix")
	val guildFilter = Configuration.config.getStringList("discord.guildfilter")
	
	def receive: Receive = {
		case DiscordMessages.Startup() =>
			info("Creating discord client...")
			client = new ClientBuilder().withToken(Configuration.config.getString("discord.token")).build()
			client.login()
			ShutdownHelper.registerHandler(() => {
				client.logout()
			})
			client.getDispatcher.registerListener(eventHandler)
		
		case message: ReadyEvent =>
			Actors.discord.commands.commandHandlers.foreach(_ ! CommandMessages.Startup(client))
			info("Discord interface is ready to process!")
			if (client.getGuilds.size() > 0) {
				info("Guilds:")
				for (guild: IGuild <- client.getGuilds)
					info(f" - '${guild.getName}' (${guild.getID}) (Active: ${if (guildFilter.contains(guild.getID)) "Yes" else "No"})")
			}
			Actors.discord.notifier ! DiscordNotifier.Setup(client)
		
		case message: MessageReceivedEvent =>
			//Ignore unwanted guilds
			if (message.getMessage.getChannel.isPrivate || guildFilter.contains(message.getMessage.getGuild.getID)) {
				
				val parts = message.getMessage.getContent.split(" ")
				if (parts(0).equals(s"<@${client.getOurUser.getID}>") || parts(0).startsWith(prefix) || message.getMessage.getChannel.isPrivate) {
					val arguments = parts.slice(if (parts(0).equals(s"<@${client.getOurUser.getID}>")) 2 else 1, parts.length)
					val command = {
						if (parts(0).equals(s"<@${client.getOurUser.getID}>"))
							parts(1)
						else if (message.getMessage.getChannel.isPrivate)
							parts(0)
						else
							parts(0).substring(1)
					}
					
					Actors.discord.commands.commandDispatcher ! CommandMessages.HandleCommand(
						message.getMessage.getChannel,
						message.getMessage.getAuthor,
						command,
						arguments
					)
				}
			}
	}
}
