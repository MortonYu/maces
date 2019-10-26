package maces.tests

import maces._
import maces.annotation._
import maces.phase._
import ammonite.ops._
import os.FilePath
import utest._
import utest.framework.Formatter

abstract class MacesTestSuite extends TestSuite {
  def sanitizePath(path: FilePath): FilePath = Path(path.toString.replaceAll(" ", "_").replaceAll("\\W+", ""))


  def testPath()(implicit testPath: utest.framework.TestPath) = Path("test_run_dir", pwd)

  def resourcesDir = Path("maces/tests/resources", pwd)

  override def utestFormatter: Formatter = new Formatter {
    override def exceptionStackFrameHighlighter(s: StackTraceElement): Boolean = {
      Set("maces").map(s.getClassName.startsWith(_)).reduce(_ && _)
    }
  }

}

object UntarVendorFilesSpec extends MacesTestSuite {
  val tests = Tests {
    test("annotated tar should be extracted") {
      val scratchPad = ScratchPad(Set(
        Annotation("vendor.tars", TarsPathAnnotationValue(Seq(resourcesDir / "files.tar.xz"))),
        Annotation("vendor.internal_tars", RelTarsPathAnnotationValue(Seq(RelPath("files.tar.xz/internal_files.tar")))),
        Annotation("system.tar_cache", DirectoryPathAnnotationValue(testPath / "tar_cache"))
      ))
      UntarStage(scratchPad).scratchPadOut
    }
  }
}
