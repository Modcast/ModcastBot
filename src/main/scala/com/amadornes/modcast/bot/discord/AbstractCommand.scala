package com.amadornes.modcast.bot.discord

import java.lang.reflect.Constructor
import java.text.ParseException

import akka.actor.Actor
import com.amadornes.modcast.bot.Actors
import com.amadornes.modcast.bot.database.Permission
import scopt.Read
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.{IChannel, IUser}

import scala.reflect._


trait TCommandOptions

/**
  * Created by rewbycraft on 11/4/16.
  */
abstract class AbstractCommand[OptionType <: TCommandOptions : ClassTag] extends Actor {
	
	import CommandMessages._
	
	implicit private var client: IDiscordClient = _
	
	protected def discordClient = client
	
	override def receive: Receive = {
		case Startup(c) =>
			this.client = c
			for (s: String <- name)
				Actors.discord.commands.commandDispatcher ! Register(s, permissionLevel)
		
		case command: HandleCommand =>
			try {
				val args = parser(command.command).parse(command.arguments, newConfig)
				if (args.isDefined)
					handle.apply(Execute(command.channel, command.user, command.command, args.get))
				else
					command.respond("Sorry, but I don't understand. You can use \"!help " + command.command + "\" to get information on how to use that command.")
			} catch {
				case e: Throwable =>
					e.printStackTrace()
					command.respond("Internal error.")
			}
		
		case DisplayHelp(channel, command) =>
			channel.sendMessage("Help for: " + command + "\n" + parser(command).usage)
		
		case msg =>
			handle.apply(msg)
	}
	
	def handle: Receive
	
	def name: Array[String]
	
	def parser(command: String): OptionParser
	
	def newConfig: OptionType = {
		def findGoodConstructor(cz: Class[_]): Constructor[_] = {
			val ctors = cz.getConstructors.sortBy(_.getParameterTypes.length)
			
			if (ctors.head.getParameterTypes.length == 0) {
				// use no arg ctor
				ctors.head
			} else {
				// use primary ctor
				ctors.reverse.head
			}
		}
		
		def getConstructorDefaultArguments(cz: Class[_], ctor: Constructor[_]): Array[AnyRef] = {
			
			val defaultValueMethodNames = ctor.getParameterTypes.zipWithIndex.map {
				valIndex => s"$$lessinit$$greater$$default$$${valIndex._2 + 1}"
			}
			
			try {
				defaultValueMethodNames.map(cz.getMethod(_).invoke(null))
			} catch {
				case ex: NoSuchMethodException =>
					throw new InstantiationException(s"$cz must have a no arg constructor or all args must be defaulted")
			}
		}
		
		val clazz = classTag[OptionType].runtimeClass
		val constructor = findGoodConstructor(clazz)
		val defaultValues = getConstructorDefaultArguments(clazz, constructor)
		constructor.newInstance(defaultValues: _*).asInstanceOf[OptionType]
	}
	
	def permissionLevel: Permission
	
	protected case class Execute(channel: IChannel, user: IUser, command: String, arguments: OptionType) {
		def respond(message: String) = {
			if (channel.isPrivate)
				channel.sendMessage(message)
			else
				channel.sendMessage(f"${user.mention(true)} $message")
		}
	}
	
	protected abstract class OptionParser(command: String) extends scopt.OptionParser[OptionType](command) {
		
		implicit def userRead: Read[IUser] = Read.reads((msg) => {
			val rc: IUser = DiscordUtils.parseUserMention(msg)(client)
			if (rc == null)
				throw new ParseException("No user", -1)
			rc
		})
		
		implicit def channelRead: Read[IChannel] = Read.reads {
			DiscordUtils.parseChannelMention(_)(client)
		}
		
		implicit def permissionRead: Read[Permission] = Read.reads(Permission.parsePermission)
		
		override def showUsageOnError: Boolean = false
		
		override def reportError(msg: String): Unit = {}
		
		override def reportWarning(msg: String): Unit = {}
		
		override def showTryHelp(): Unit = {}
	}
	
}
