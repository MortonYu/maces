package maces.tests

import maces._
import maces.annotation._
import maces.phase._
import os.FilePath
import ammonite.ops._
import utest._
import utest.framework.Formatter


object UntarVendorFilesSpec extends MacesTestSuite {
  val tests = Tests {
    test("annotated tar should be extracted") {
      rm ! testPath
      val scratchPad = ScratchPad(Set(
        Annotation("vendor.tars", TarsPathAnnotationValue(Seq(resourcesDir / "files.tar.xz"))),
        Annotation("vendor.internal_tars", RelTarsPathAnnotationValue(Seq(RelPath("files.tar.xz/internal_files.tar")))),
        Annotation("system.tar_cache", DirectoryPathAnnotationValue(testPath / "tar_cache"))
      ))
      UntarStage(scratchPad).scratchPadOut
      assert((testPath / "tar_cache" / "files.tar.xz").isDir)
      assert((testPath / "tar_cache" / "files.tar.xz" / "someFile0").isFile)
      assert((testPath / "tar_cache" / "files.tar.xz" / "someFile0").isFile)
      assert((testPath / "tar_cache" / "files.tar.xz" / "internal_files.tar").isDir)
      assert((testPath / "tar_cache" / "files.tar.xz" / "internal_files.tar" / "someInternalFile0").isFile)
      assert((testPath / "tar_cache" / "files.tar.xz" / "internal_files.tar" / "someInternalFile1").isFile)
    }
  }
}
