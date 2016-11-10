package com.amadornes.modcast.bot.discord.commands

import com.amadornes.modcast.bot.database.Permission
import com.amadornes.modcast.bot.discord.{AbstractCommand, TCommandOptions}
import grizzled.slf4j.Logging

/**
  * Created by rewbycraft on 11/2/16.
  */
class PingCommand extends AbstractCommand[PingCommand.Options] with Logging {
	
	override def name: Array[String] = Array("ping")
	
	override def handle: Receive = {
		case c: Execute =>
			c.respond("Pong!")
	}
	
	override def parser(command: String): OptionParser = new OptionParser(command) {
		note("If I'm still alive, I'll reply!")
	}
	
	override def permissionLevel: Permission = Permission.NONE
}

object PingCommand {
	
	case class Options() extends TCommandOptions
	
}
