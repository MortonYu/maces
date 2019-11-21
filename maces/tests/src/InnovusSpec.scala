package maces.tests

import ammonite.ops._
import maces._
import maces.annotation._
import maces.contrib.cadence.InnovusStage
import utest._

object InnovusSpec extends MacesTestSuite {
  val tests: Tests = Tests {
    test("innovus should synthesis") {
      val workspace = testPath / "workspace"

      /** prepare GenusStage result,
       * copy from resources, and sed the RESOURCESDIR from which
       * */
      lazy val genusStageDir: Path = {
        val d = workspace / "GenusStage" / System.currentTimeMillis.toString
        os.makeDir.all(d)
        os.walk(resourcesDir / "genus_out").foreach(f => {
          os.copy.into(f, d)
        })
        os.walk(d).foreach { f =>
          os.proc("sed", "-i", s"s:RESOURCESDIR:${resourcesDir.toString}:g", f).call()
        }
        d
      }

      val netlists: Seq[Path] = Seq(genusStageDir / "GCD_syn.v")

      val mmmc: Path = genusStageDir / "GCD_mmmc.tcl"

      /** the runtime commercial EDA tool installation should be /opt/eda_tools/vendor/tool_name/tool,
       * license should be /opt/eda_tools/vendor/vendor.lic
       * maybe we need to provide a better way to execute this NDA-related test
       * */
      lazy val cadenceHome: Path = {
        val default = Path("/opt/eda_tools/cadence")
        if (default.isDir) default else {
          println("no default test machine, please input the cadence installation path(for example: /opt/eda_tools/cadence):")
          Path(scala.io.StdIn.readLine())
        }
      }

      val innovusBin: Path = {
        val default = cadenceHome / "INNOVUS" / "INNOVUS171" / "bin" / "innovus"
        if (default.isFile) default else {
          println("no default test machine, please input the innovus installation path(for example: /opt/eda_tools/cadence/INNOVUS/INNOVUS171):")
          Path(scala.io.StdIn.readLine()) / "bin" / "innovus"
        }
      }

      val licenseFile: Path = {
        val default = cadenceHome / "cadence.lic"
        if (default.isFile) default else {
          println("no default test machine, please input the innovus license path:")
          Path(scala.io.StdIn.readLine())
        }
      }

      val scratchPad = ScratchPad(Set(
        Annotation("runtime.innovus.stdin_shell", GeneratedFileAnnotationValue("par.tcl")),
        Annotation("runtime.innovus.enter_shell", GeneratedFileAnnotationValue("enter.sh")),
        Annotation("runtime.innovus.workspace", DirectoryPathAnnotationValue(workspace)),
        Annotation("runtime.innovus.env", EnvAnnotationValue(Map(
          "CADENCE_HOME" -> cadenceHome.toString,
          "CDS_LIC_FILE" -> licenseFile.toString,
          "OA_UNSUPPORTED_PLAT" -> "linux_rhel50_gcc48x",
          "COLUMNS" -> "1024",
          "LINES" -> "1024"
        ))),
        Annotation("runtime.innovus.core_limit", CoreLimitAnnotationValue(8)),

        /** ASAP7 is a 28 wrapper from 28 to 7 */
        Annotation("runtime.innovus.process_node", ProcessNodeAnnotationValue(28)),
        Annotation("runtime.innovus.bin", BinPathAnnotationValue(innovusBin)),
        Annotation("runtime.innovus.netlists", HdlsPathAnnotationValue(netlists)),
        Annotation("runtime.innovus.top_name", InstanceNameAnnotationValue("GCD")),
        Annotation("runtime.innovus.power_name", WireNameAnnotationValue("VDD")),
        Annotation("runtime.innovus.ground_name", WireNameAnnotationValue("GND")),
        Annotation("runtime.innovus.time_unit", TimeUnitAnnotationValue("ps")),
        Annotation("runtime.innovus.tech_lef_files", LefsPathAnnotationValue(Seq(resourcesDir / "asap7" / "asap7_tech_4x_170803.lef"))),
        Annotation("runtime.innovus.hierarchical_mode", HierarchicalModeAnnotationValue("flat")),
        Annotation("runtime.innovus.ilms", IlmsAnnotationValue(Nil)),
        Annotation("runtime.innovus.floorplan", PlacementConstraintsAnnotationValue(Seq(
          PlacementConstraint(
            path = "GCD",
            placementType = "toplevel",
            x = 0,
            y = 0,
            width = 35,
            height = 35
          )
        ))),
        Annotation("runtime.innovus.power_spec_mode", PowerSpecModeAnnotationValue("empty")),
        Annotation("runtime.innovus.designEffort", DesignEffortAnnotationValue("standard")),
        Annotation("runtime.innovus.dont_use", CellsNameAnnotationValue(Nil)),
        Annotation("runtime.innovus.auto_clock_gate", AutoClockGatingAnnotationValue(false)),
        Annotation("runtime.innovus.clock_gate_cell_prefix", CellNameAnnotationValue("CLKGATE_")),
        Annotation("runtime.innovus.site", SiteNameAnnotationValue("coreSite")),
        Annotation("runtime.innovus.mmmc_tcl", TclPathAnnotationValue(mmmc)),
        Annotation("runtime.innovus.libraries", LibrariesAnnotationValue(asap7Libraries))
      ))
      val stage = InnovusStage(scratchPad)
      val scratchPadOut = stage.scratchPadOut
    }
  }
}
