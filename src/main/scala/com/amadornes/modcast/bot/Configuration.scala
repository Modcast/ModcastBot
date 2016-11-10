package com.amadornes.modcast.bot

import java.io.File

import com.typesafe.config.ConfigFactory

/**
  * Created by rewbycraft on 11/2/16.
  */
object Configuration {
	val config = ConfigFactory.parseFile(new File("modbot.conf"))
	
	lazy val showName = config.getString("general.showName")
}
