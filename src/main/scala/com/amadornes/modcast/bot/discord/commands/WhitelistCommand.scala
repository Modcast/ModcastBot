package com.amadornes.modcast.bot.discord.commands

import com.amadornes.modcast.bot.database.Permission
import com.amadornes.modcast.bot.discord.{AbstractCommand, TCommandOptions}
import com.amadornes.modcast.bot.helpers.{MCWhitelistHelper, PermissionsHelper}
import sx.blah.discord.handle.obj.IUser

/**
  * Created by rewbycraft on 11/9/16.
  */
class WhitelistCommand extends AbstractCommand[WhitelistCommand.Options] {
	override def handle: Receive = {
		case c@Execute(channel, user, _, arguments) =>
			if (arguments.user == null || arguments.user.getID == user.getID || PermissionsHelper.getUserPermissionLevel(user) == Permission.ADMIN) {
				val affectedUser = if (arguments.user != null) arguments.user else user
				
				try {
					if (arguments.deassociate) {
						MCWhitelistHelper.deassociateMCAccountWithUser(affectedUser)
						c.respond("Okay!")
					}
					else if (arguments.account != null) {
						MCWhitelistHelper.associateMCAccountWithUser(affectedUser, arguments.account)
						c.respond("Okay!")
					} else
						c.respond("You need to specify an account.")
					
				} catch {
					case e: IllegalArgumentException =>
						c.respond("I couldn't find any information on that account.")
				}
			} else
				c.respond("You don't have the required permissions.")
		
	}
	
	override def name: Array[String] = Array("whitelist")
	
	override def parser(command: String): OptionParser = new OptionParser(command) {
		note("Associate an MC account with a user.")
		
		opt[String]('a', "account").text("Account to associate.").action((v, c) => c.copy(account = v)).optional()
		
		opt[IUser]('u', "user").text("User to associate account with. Defaults to you. Using any other user here will require admin permissions.").action((v, c) => c.copy(user = v)).optional()
		
		opt[Unit]('d', "deassociate").text("De-associate the MC account with this user.").action((_, c) => c.copy(deassociate = true))
	}
	
	override def permissionLevel: Permission = Permission.GUEST
}

object WhitelistCommand {
	
	case class Options(user: IUser, account: String, deassociate: Boolean) extends TCommandOptions {
		def this() = this(null, null, false)
	}
	
}
