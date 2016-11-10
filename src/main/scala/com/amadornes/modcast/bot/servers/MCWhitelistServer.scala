package com.amadornes.modcast.bot.servers

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.amadornes.modcast.bot.Configuration
import grizzled.slf4j.Logging

import scala.collection.mutable.ArrayBuffer

/**
  * Created by rewbycraft on 11/9/16.
  */
class MCWhitelistServer extends Actor with Logging {
	
	import Tcp._
	import context.system
	
	IO(Tcp) ! Bind(self, new InetSocketAddress(Configuration.config.getString("mc.host"), Configuration.config.getInt("mc.port")))
	
	val connections = new ArrayBuffer[ActorRef]()
	
	def receive = {
		case str: String =>
		//Ignored
		
		case b@Bound(localAddress) =>
			info("MC Whitelist TCP Server is online and listening.")
		
		case CommandFailed(_: Bind) =>
			error("Bind failed. MC Whitelist Server is not functional.")
			context stop self
		
		case c@Connected(remote, local) =>
			if (Configuration.config.getStringList("mc.acceptedIPs").contains(remote.getAddress.getHostAddress)) {
				sender() ! Register(self)
				connections += sender()
			} else {
				warn(s"Refusing connection from ${remote.getAddress.getHostAddress}")
				sender() ! Close
			}
		
		case MCWhitelistServer.WhitelistUser(userID) =>
			for (connection <- connections)
				connection ! Write(ByteString(s"W$userID\n"))
		case MCWhitelistServer.UnWhitelistUser(userID) =>
			for (connection <- connections)
				connection ! Write(ByteString(s"U$userID\n"))
	}
}

object MCWhitelistServer {
	
	case class WhitelistUser(id: String)
	
	case class UnWhitelistUser(id: String)
	
}
