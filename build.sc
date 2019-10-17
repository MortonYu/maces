import mill._
import mill.scalalib._
import mill.scalalib.publish._

object maces extends ScalaModule with PublishModule {
  def scalaVersion = "2.12.8"

  override def ivyDeps = Agg(
    ivy"com.lihaoyi::upickle:0.7.5",
    ivy"com.lihaoyi::os-lib:0.2.7",
    ivy"com.lihaoyi::upickle:0.8.0",
    ivy"com.lihaoyi::ammonite-ops:1.7.1",
  )

  object tests extends Tests {
    def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.7.1")

    def testFrameworks = Seq("utest.runner.Framework")
  }

  def publishVersion = "0.0.1"

  def pomSettings = PomSettings(
    description = "maces",
    organization = "me.sequencer",
    url = "https://github.com/sequencer/maces",
    licenses = Seq(License.`BSD-3-Clause`),
    versionControl = VersionControl.github("sequencer", "maces"),
    developers = Seq(
      Developer("sequencer", "Jiuyang Liu", "https://github.com/sequencer")
    )
  )
}
