package com.amadornes.modcast.bot.discord.commands

import com.amadornes.modcast.bot.database.Permission
import com.amadornes.modcast.bot.discord.{AbstractCommand, TCommandOptions}
import com.amadornes.modcast.bot.helpers.ShutdownHelper

/**
  * Created by rewbycraft on 11/9/16.
  */
class ShutdownCommand extends AbstractCommand[ShutdownCommand.Options]{
	override def handle: Receive = {
		case c @ Execute(channel, user, _, _) =>
			c.respond("Ok! I'll go bye bye now!")
			ShutdownHelper.shutdown()
	}
	
	override def name: Array[String] = Array("shutdown")
	
	override def parser(command: String): OptionParser = new OptionParser(command) {
		note("Shut down the bot.")
	}
	
	override def permissionLevel: Permission = Permission.ADMIN
}

object ShutdownCommand {
	case class Options() extends TCommandOptions
}