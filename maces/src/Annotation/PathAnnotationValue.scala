package maces.annotation

import maces._
import os._


trait PathsAnnotationValue extends AnnotationValue {
  val paths: Seq[Path]
}

trait RelPathsAnnotationValue extends AnnotationValue {
  val paths: Seq[RelPath]
}

trait PathAnnotationValue extends AnnotationValue {
  val path: Path
}

trait RelPathAnnotationValue extends AnnotationValue {
  val path: RelPath
}

trait EncryptAnnotationValue extends AnnotationValue {
  val paraphrase: Option[String]
  val privateKey: Option[Path]
}

case class TarsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class RelTarsPathAnnotationValue(paths: Seq[RelPath]) extends RelPathsAnnotationValue

case class BinPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoryPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoriesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

trait HasWorkspace {
  var scratchPad: ScratchPad

  def workspace: Path = scratchPad.get("system.workspace").get.asInstanceOf[DirectoryPathAnnotationValue].path
}