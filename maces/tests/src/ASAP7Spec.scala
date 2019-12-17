package maces.tests

import maces._
import maces.annotation._
import maces.phase._
import ammonite.ops._
import utest._

object ASAP7Spec extends MacesTestSuite {
  val tests: Tests = Tests {
    test("asap7 should untar") {
      val workspace = testPath / "workspace"

      val pdkStageRunDir = workspace / "PDKStage" / System.currentTimeMillis.toString

      val scratchPad = ScratchPad(Set(
        Annotation("user.input.pdk.download_dir", DirectoryPathAnnotationValue(os.Path("/home/sequencer/Documents/projects/asap7"))),
        Annotation("system.tar_cache", DirectoryPathAnnotationValue(pdkStageRunDir / "cache"))
      ))
      val stage = ASAP7Stage(scratchPad)
      stage.scratchPadOut.annotations.map(_.value).foreach {
        case LefsPathAnnotationValue(ps) => ps.foreach(p => assert(p.isFile))
        case LayerMapPathAnnotationValue(p) => assert(p.isFile)
        case RuleDecksAnnotationValue(rds) => rds.foreach(rd => assert(rd.path.isFile))
        case LibraryAnnotationValue(l) => {
          (Seq[Path]() ++
            l.gdsFile ++
            l.itfFile ++
            l.lefFile ++
            l.libertyFile ++
            l.qrcTechFile ++
            l.spiceFile ++
            l.vsimFile).foreach(p => p.isFile)
        }
        case _ =>
      }
    }
  }
}
