import mill._
import scalalib._
import publish._
import $ivy.`com.lihaoyi::mill-contrib-bsp:$MILL_VERSION`

object maces extends ScalaModule with PublishModule {
  def scalaVersion = "2.12.10"

  override def ivyDeps = Agg(
    ivy"com.lihaoyi::os-lib:0.4.0",
    ivy"com.lihaoyi::upickle:0.8.0",
    ivy"com.lihaoyi::ammonite-ops:1.8.1",
    ivy"edu.berkeley.cs::chisel3:3.2.1"
  )

  object tests extends Tests {
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.7.2"
    )

    def testFrameworks = Seq("utest.runner.Framework")
  }

  def publishVersion = "0.1.0"

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
