package com.amadornes.modcast.bot.discord

import akka.actor.Actor
import com.amadornes.modcast.bot.Configuration
import com.amadornes.modcast.bot.helpers.ShutdownHelper
import grizzled.slf4j.Logging
import sx.blah.discord.api.IDiscordClient
import scala.collection.JavaConversions._

import scala.collection.mutable

/**
  * Created by rewbycraft on 11/9/16.
  */
class DiscordNotifier extends Actor with Logging {
	
	var client: IDiscordClient = _
	val queue: mutable.Queue[String] = new mutable.Queue[String]
	
	def processQueue(): Unit =
		try {
			if (client != null) {
				for (channelID <- Configuration.config.getStringList("discord.notifyChannels")) {
					val channel = client.getChannelByID(channelID)
					for (msg <- queue)
						channel.sendMessage(msg)
				}
				queue.clear()
			}
		} catch {
			case t: Throwable =>
				t.printStackTrace()
		}
	
	def sendNotification(msg: String) = {
		queue += msg
		processQueue()
	}
	
	def receive: Receive = {
		case DiscordNotifier.Setup(newClient) =>
			client = newClient
			
			ShutdownHelper.registerHandler(() => {
				info("Notifying everyone of a shutdown.")
				sendNotification("The bot is shutting down...")
			})
			
			processQueue()
		
		case DiscordNotifier.StreamStartNotification(name) =>
			sendNotification(s"$name has started streaming!")
		
		case DiscordNotifier.StreamStopNotification(name) =>
			sendNotification(s"$name has stopped streaming! :<")
	}
}

object DiscordNotifier {
	
	case class Setup(client: IDiscordClient)
	
	case class StreamStartNotification(name: String)
	
	case class StreamStopNotification(name: String)
	
}
