package maces.annotation

import maces._

import ammonite.ops._


trait PathsAnnotation extends AnnotationValue {
  val paths: Seq[Path]
}

trait RelPathsAnnotation extends AnnotationValue {
  val paths: Seq[RelPath]
}

trait PathAnnotation extends AnnotationValue {
  val path: Path
}

trait RelPathAnnotation extends AnnotationValue {
  val path: RelPath
}

trait EncryptAnnotation extends AnnotationValue {
  val paraphrase: Option[String]
  val privateKey: Option[Path]
}

case class TarPathAnnotation(path: RelPath) extends RelPathAnnotation

case class SecuredTarPathAnnoatation(path: Path, paraphrase: Option[String], privateKey: Option[Path]) extends PathAnnotation with EncryptAnnotation

