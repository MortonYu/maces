package maces.annotation

import maces._

import ammonite.ops._


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

case class DirectoryPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoriesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue