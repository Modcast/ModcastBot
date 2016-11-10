package com.amadornes.modcast.bot

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.amadornes.modcast.bot.discord.commands._
import com.amadornes.modcast.bot.discord.{CommandDispatcher, DiscordActor, DiscordNotifier}
import com.amadornes.modcast.bot.helpers.ShutdownHelper
import com.amadornes.modcast.bot.servers.MCWhitelistServer
import grizzled.slf4j.Logging

/**
  * Created by rewbycraft on 11/2/16.
  */
object Actors extends Logging {
	implicit val system = ActorSystem()
	implicit val materializer = ActorMaterializer()
	
	ShutdownHelper.registerHandler(() => {
		info("Shutting down actor system...")
		system.terminate()
	})
	
	object servers {
		val MCWhitelistServer = system.actorOf(Props[MCWhitelistServer])
	}
	
	object discord {
		val actor = system.actorOf(Props[DiscordActor])
		val notifier = system.actorOf(Props[DiscordNotifier])
		
		object commands {
			val commandDispatcher = system.actorOf(Props[CommandDispatcher])
			val commandHandlers = Array(
				system.actorOf(Props[PingCommand]),
				system.actorOf(Props[WhoamiCommand]),
				system.actorOf(Props[GuestCommand]),
				system.actorOf(Props[StreamCommand]),
				system.actorOf(Props[ShutdownCommand]),
				system.actorOf(Props[WhitelistCommand]),
				system.actorOf(Props[UserCommand])
			)
		}
		
	}
	
}
