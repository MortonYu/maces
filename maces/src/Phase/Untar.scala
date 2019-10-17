package maces.phase

import ammonite.ops._
import maces.annotation.{FileAnnotation, ScratchPad}

case class UntarVendorFiles(scratchPadIn: ScratchPad) extends Phase {
  def transform(scratchPad: ScratchPad): ScratchPad = {
    /** collect necessary annotations*/
    val workspace = scratchPad.get("user.workspace").get
    val files = scratchPad.get("vendor.files.normalfiles").get.asInstanceOf[FileAnnotation]
    scratchPad
  }
}

