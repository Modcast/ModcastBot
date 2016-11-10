package com.amadornes.modcast.bot.helpers

import grizzled.slf4j.Logging

import scala.collection.mutable.ArrayBuffer

/**
  * Created by rewbycraft on 11/9/16.
  */
object ShutdownHelper extends Logging {
	private def handlers: ArrayBuffer[() => Unit] = new ArrayBuffer[() => Unit]()
	
	def registerHandler(handler: () => Unit): Unit = handlers prepend handler
	
	def shutdown() = {
		info("Shutting down...")
		handlers.foreach(_())
		info("Handlers ready.")
		info("Goodbye ~")
		System.exit(0)
	}
}
