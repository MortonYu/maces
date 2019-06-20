import mill._
import mill.scalalib._
import mill.scalalib.publish._

object maces extends ScalaModule with PublishModule {
  def scalaVersion = "2.12.8"

  override def ivyDeps = Agg(
    ivy"edu.berkeley.cs::firrtl:1.2-SNAPSHOT",
    ivy"edu.berkeley.cs::chisel3:3.2-SNAPSHOT",
    ivy"org.scalatest::scalatest:3.0.5"
  )

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
