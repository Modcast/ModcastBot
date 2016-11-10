package com.amadornes.modcast.bot.servers.http

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.amadornes.modcast.bot.database.{DB, StreamKey}
import com.amadornes.modcast.bot.discord.DiscordNotifier
import com.amadornes.modcast.bot.helpers.{ShutdownHelper, StreamKeyHelper}
import com.amadornes.modcast.bot.{Actors, Configuration}
import grizzled.slf4j.Logging

/**
  * Created by rewbycraft on 11 / 9 / 16.
  */

object RTMPControlServer extends Logging {
	implicit val system = Actors.system
	implicit val materializer = Actors.materializer
	implicit val executionContext = system.dispatcher
	
	def start() = {
		val routes = {
			path("on_publish") {
				post {
					formFields(('call, 'addr, 'clientid, 'app, 'flashver, 'swfurl, 'tcurl, 'pageurl, 'name)).as(OnPlayPublishForm) {
						form => {
							if (form.app == Configuration.config.getString("rtmp.ingestapp")) {
								val obj = DB.query[StreamKey].whereEqual("key", form.name).fetchOne()
								if (obj.isDefined) {
									info(s"Stream published for ${obj.get.name}.")
									Actors.discord.notifier ! DiscordNotifier.StreamStartNotification(obj.get.name)
									complete(HttpResponse(entity = HttpEntity("Ok")))
								} else {
									info("Stream publish rejected.")
									complete(HttpResponse(status = StatusCodes.Unauthorized, entity = HttpEntity("Unknown key")))
								}
							} else if (form.app == "" && form.addr.startsWith("127.0.0.1/" + Configuration.config.getString("rtmp.ingestapp"))) {
								//Allow the on-play redirect to function
								complete(HttpResponse(entity = HttpEntity("Ok")))
							} else
								complete(HttpResponse(status = StatusCodes.NotFound, entity = HttpEntity("Unknown ingest")))
						}
					}
				}
			} ~ path("on_play") {
				post {
					formFields(('call, 'addr, 'clientid, 'app, 'flashver, 'swfurl, 'tcurl, 'pageurl, 'name)).as(OnPlayPublishForm) {
						form => {
							if (form.app != Configuration.config.getString("rtmp.app")) {
								if (form.app == Configuration.config.getString("rtmp.ingestapp") && form.addr == "127.0.0.1")
									complete(HttpResponse(entity = HttpEntity("Ok")))
								else
									complete(HttpResponse(status = StatusCodes.NotFound, entity = HttpEntity("Unknown app")))
							} else {
								val key = StreamKeyHelper.getStreamKeyForName(form.name)
								if (key.isDefined) {
									info(s"Stream play accepted for ${form.name}.")
									val url = s"rtmp://127.0.0.1/${Configuration.config.getString("rtmp.ingestapp")}/${key.get}"
									redirect(Uri(url), StatusCodes.TemporaryRedirect)
								}
								else
									complete(HttpResponse(status = StatusCodes.NotFound, entity = HttpEntity("Unknown stream")))
							}
						}
					}
				}
			} ~ path("on_publish_done") {
				post {
					formFields(('call, 'addr, 'clientid, 'app, 'flashver, 'swfurl, 'tcurl, 'pageurl, 'name)).as(OnPlayPublishForm) {
						form => {
							if (form.app == Configuration.config.getString("rtmp.ingestapp")) {
								val obj = DB.query[StreamKey].whereEqual("key", form.name).fetchOne()
								if (obj.isDefined) {
									info(s"Stream unpublished for ${obj.get.name}.")
									Actors.discord.notifier ! DiscordNotifier.StreamStopNotification(obj.get.name)
									
								}
							}
							complete(HttpResponse(entity = HttpEntity("Ok")))
						}
					}
				}
			}
		}
		
		val bindFuture = Http().bindAndHandle(routes, Configuration.config.getString("http.rtmpcontrol.host"), Configuration.config.getInt("http.rtmpcontrol.port"))
		info("RTMP control server online.")
		
		ShutdownHelper.registerHandler(() => {
			info("Unbinding RTMP control server")
			//Unbind
			bindFuture.flatMap(_.unbind())
		})
	}
	
	case class OnPlayPublishForm(call: String, addr: String, clientId: String, app: String, flashVer: String, swfUrl: String, tcUrl: String, pageUrl: String, name: String)
	
}


