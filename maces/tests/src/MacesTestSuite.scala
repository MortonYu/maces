package maces.tests

import os.FilePath
import ammonite.ops._
import utest._
import utest.framework.Formatter

abstract class MacesTestSuite extends TestSuite {
  def sanitizePath(path: String): String = path.toString.replaceAll(" ", "_").replaceAll("\\W+", "")

  def testPath()(implicit testPath: utest.framework.TestPath) = Path(s"test_run_dir/${this.getClass.getSimpleName.dropRight(1)}/" + sanitizePath(testPath.value.reduce(_ + _)), pwd)

  def resourcesDir = Path("maces/tests/resources", pwd)

  override def utestFormatter: Formatter = new Formatter {
    override def exceptionStackFrameHighlighter(s: StackTraceElement): Boolean = {
      Set("maces").map(s.getClassName.startsWith(_)).reduce(_ && _)
    }
  }
}