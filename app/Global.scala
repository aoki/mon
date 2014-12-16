import play.api.{Logger, Application, GlobalSettings}

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    Logger.info("Start application")
    super.beforeStart(app)
  }

}
