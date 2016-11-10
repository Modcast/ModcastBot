package com.amadornes.modcast.bot.discord

import akka.actor.{Actor, ActorRef}
import com.amadornes.modcast.bot.database.Permission
import com.amadornes.modcast.bot.discord.CommandMessages.{DisplayHelp, HandleCommand, Register}
import com.amadornes.modcast.bot.helpers.PermissionsHelper

import scala.collection.mutable

/**
  * Created by rewbycraft on 11/2/16.
  */
class CommandDispatcher extends Actor {
	val commands: mutable.Map[String, (ActorRef, Permission)] = new mutable.HashMap[String, (ActorRef, Permission)]()
	
	def receive: Receive = {
		case command: HandleCommand =>
			if (command.command == "help") {
				if (command.arguments.length == 0) {
					if (!command.channel.isPrivate)
						command.respond("I'll PM you my list of commands!")
					
					val pmChannel = command.user.getOrCreatePMChannel()
					var message = "Welcome to ModBot 0.0.1-ALPHA\nAvailable commands:\n"
					
					val userLevel = PermissionsHelper.getUserPermissionLevel(command.user)
					for ((command, (actor, level)) <- commands; if level.getLevel <= userLevel.getLevel)
						message += " - " + command + "\n"
					
					pmChannel.sendMessage(message + "You can use \"help <command>\" to get more information on a command.")
				} else {
					val handler = commands.get(command.arguments(0))
					if (handler.isEmpty) {
						command.respond(s"I'm sorry! But I don't know '${command.arguments(0)}'!")
					} else {
						if (!command.channel.isPrivate)
							command.respond(s"I'll PM you instructions on '${command.arguments(0)}'!")
						
						val channel = command.user.getOrCreatePMChannel()
						handler.get._1 ! DisplayHelp(channel, command.arguments(0))
					}
				}
			} else {
				val handler = commands.get(command.command)
				if (handler.isDefined)
					if (handler.get._2.getLevel <= PermissionsHelper.getUserPermissionLevel(command.user).getLevel)
						handler.get._1 ! command
					else
						command.respond(s"I'm sorry! But you don't have permission to use '${command.command}'!")
				else
					command.respond(s"I'm sorry! But I don't know how to '${command.command}'!")
			}
		case Register(name, level) =>
			commands += (name -> (sender(), level))
	}
}

