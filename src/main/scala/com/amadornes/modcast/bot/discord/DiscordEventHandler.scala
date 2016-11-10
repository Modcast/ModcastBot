package com.amadornes.modcast.bot.discord

import akka.actor.ActorRef
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.{MessageReceivedEvent, ReadyEvent}

/**
  * Created by rewbycraft on 11/2/16.
  */
class DiscordEventHandler(val actor: ActorRef) {
	@EventSubscriber
	def ready(event: ReadyEvent) = actor ! event
	
	@EventSubscriber
	def message(event: MessageReceivedEvent) = actor ! event
}
