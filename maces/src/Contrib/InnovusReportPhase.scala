package maces.contrib.cadence

import maces._
import maces.annotation._
import maces.phase._
import os._

case class InnovusReportPhase(scratchPadIn: ScratchPad) extends CliStage {

  override def workspace: Path = scratchPad.get("runtime.innovus.workspace").get.asInstanceOf[DirectoryPathAnnotationValue].path

  override def env: Map[String, String] = scratchPad.get("runtime.innovus.env").get.asInstanceOf[EnvAnnotationValue].value

  def stdinTclPath: Path = Path(scratchPad.get("runtime.innovus.stdin_shell").get.asInstanceOf[GeneratedFileAnnotationValue].path, generatedPath)

  def enterPath: Path = Path(scratchPad.get("runtime.innovus.enter_shell").get.asInstanceOf[GeneratedFileAnnotationValue].path, generatedPath)

  def bin: Path = scratchPad.get("runtime.innovus.bin").get.asInstanceOf[BinPathAnnotationValue].path

  class waitInit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = ""

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(100) { str =>
        str.contains("@innovus 1>")
      }
      assert(r._1)

      (scratchPad, Some(new loadDb(scratchPad)))
    }
  }

  class loadDb(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = ""

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(100) { str =>
        str.contains("@innovus 1>")
      }
      assert(r._1)

      (scratchPad, Some(new loadDb(scratchPad)))
    }
  }

  class timming(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = ""

    override def should: (ScratchPad, Option[ProcessNode]) = {
      /** todo*/
      val r = waitUntil(100) { str =>
        str.contains("@innovus 1>")
      }
      assert(r._1)

      (scratchPad, Some(new qor(scratchPad)))
    }
  }

  class qor(var scratchPad: ScratchPad) extends ProcessNode {
    def qorJson: Path = workspace / "qor.json"

    def input: String = s"report_qor -format json -file ${qorJson.toString}"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      /** todo*/
      val r = waitUntil(30) { str =>
        str.contains("@innovus 1>")
      }
      assert(r._1)

      /** read qor json and convert necessary information here */
      val j = ujson.read(read(qorJson))("snapshots").arr.map(m => m("name").str -> m).toMap

      /** we currently not implement the metadata system,
       * in case of phase with broken links,
       * we copy all targets and remove broken link caused by EDA bug.
       * */
      os.walk(pwd, followLinks = false).filter(os.isLink).foreach { l =>
        val t = os.readLink.absolute(l)
        if (os.exists(t)) {
          os.copy.over(t, l)
        } else {
          os.remove.all(l)
        }
      }

      def getAreaFromJson(name: String, key: String): Double = j(name)("metrics").arr.filter(k => k("name").str == key).head("value").str.replace(" um^2", "").toDouble

      def getFinalAreaFromJson(key: String): Double = getAreaFromJson("route_design", key)

      scratchPad = scratchPad add
        Annotation("runtime.innovus.cell_area", AreaAnnotationValue(getFinalAreaFromJson("design.area"))) add
        Annotation("runtime.innovus.area_io_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.area_io"))) add
        Annotation("runtime.innovus.blackbox_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.blackbox"))) add
        Annotation("runtime.innovus.buffer_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.buffer"))) add
        Annotation("runtime.innovus.combinatorial_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.combinatorial"))) add
        Annotation("runtime.innovus.icg_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.icg"))) add
        Annotation("runtime.innovus.inverter_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.inverter"))) add
        Annotation("runtime.innovus.io_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.io"))) add
        Annotation("runtime.innovus.iso_ls_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.iso_ls"))) add
        Annotation("runtime.innovus.latch_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.latch"))) add
        Annotation("runtime.innovus.logical_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.logical"))) add
        Annotation("runtime.innovus.macro_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.macro"))) add
        Annotation("runtime.innovus.physical_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.physical"))) add
        Annotation("runtime.innovus.power_switch_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.power_switch"))) add
        Annotation("runtime.innovus.register_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.register"))) add
        Annotation("runtime.innovus.sequential_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.sequential"))) add
        Annotation("runtime.innovus.std_cell_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.std_cell")))

      (scratchPad, Some(new exit(scratchPad)))
    }
  }

  class exit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "exit\n"

    write(stdinTclPath, stdinLogger.toString)
    write(enterPath, "#!/bin/bash\n" + env.map(m => s"export ${m._1}=${m._2}\n").reduce(_ + _) + command.reduce(_ + " " + _), perms = "r-x------")
  }

  override val node: ProcessNode = new waitInit(scratchPadIn)

  def command: Seq[String] = {
    Seq(bin.toString, "-nowin", "-common_ui")
  }
}