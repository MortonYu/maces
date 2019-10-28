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
