package maces.phase

import maces._
import maces.annotation._

import os._

case class PDKNameAnnotationValue(value: String) extends AnnotationValue

object LibraryNotFoundException extends Exception

case class ASPA7Patch(scratchPadIn: ScratchPad) extends Phase with HasTarCache {
  def script = Path(getClass.getResource("asap7.py").getPath)

  override def transform: ScratchPad = ???
}

case class DigestASPA7LibraryPhase(scratchPadIn: ScratchPad) extends Phase with HasTarCache {

  implicit class Crossable[X](xs: Traversable[X]) {
    def cross[Y](ys: Traversable[Y]): Traversable[(X, Y)] = for {x <- xs; y <- ys} yield (x, y)
  }

  val s = scratchPadIn

  lazy val pdkName = s.get("user.input.pdk.name")
  lazy val cacheDir = s.get("user.input.pdk.cache_dir")
  lazy val downloadDir = s.get("user.input.pdk.download_dir")

  private def asap7File(relPath: String): Path = Path(relPath, cacheDir.getOrElse(downloadDir.get).asInstanceOf[DirectoryPathAnnotationValue].path)

  private def libAnnotation(libType: String, nominalType: String, vt: String): Annotation = {
    val name = s"${libType}_${vt}_${nominalType}"
    val voltage: Double = nominalType match {
      case "ss" => 0.63
      case "tt" => 0.70
      case "ff" => 0.77
    }
    val temperature: Double = nominalType match {
      case "ss" => 100
      case "tt" => 25
      case "ff" => 0
    }

    def vtShortName: String = vt.replace("VT", "").toUpperCase

    Annotation(s"system.library.asap7.libraries.$name",
      LibraryAnnotationValue(Library(
        name, voltage, temperature, nominalType,
        libertyFile = Some(asap7File(s"ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB/asap7PDK_24.tar.bz2/asap7libs_24/lib/asap7sc7p5t_24_${libType.toUpperCase}_${vt.toUpperCase}_${vt.toUpperCase()}.lib")),
        voltageThreshold = Some(vt),
        qrcTechFile = Some(asap7File("ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB/asap7PDK_24.tar.bz2/asap7libs_24/qrc/qrcTechFile_typ03_scaled4xV06")),
        lefFile = Some(asap7File(s"ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB/asap7PDK_24.tar.bz2/asap7libs_24/lef/scaled/asap7sc7p5t_24_${vtShortName}_4x_170912.lef")),
        spiceFile = Some(asap7File(s"ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB/asap7PDK_24.tar.bz2/asap7libs_24/cdl/lvs/asap7_75t_${vtShortName}.cdl")),
        gdsFile = Some(asap7File(s"ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB/asap7PDK_24.tar.bz2/asap7libs_24/gds/asap7sc7p5t_24_${vtShortName}.gds"))
      ))
    )
  }

  val libType: Set[String] = Set("ao", "invbuf", "oa", "seq", "simple")
  val nominalTypeSet: Set[String] = Set("ss", "tt", "ff")
  val vtSet: Set[String] = Set("rvt", "lvt", "slvt", "sram")

  lazy val asap7Annotations = Set(
    Annotation("system.library.asap7.tech_lef", LefsPathAnnotationValue(Seq(asap7File("untar_cache/ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB/asap7PDK_24.tar.bz2/asap7libs_24/techlef_misc/asap7_tech_4x_170803.lef")))),
    Annotation("system.library.asap7.physical_only_cell", CellsNameAnnotationValue(Seq(
      "TAPCELL_ASAP7_75t_R", "TAPCELL_ASAP7_75t_L", "TAPCELL_ASAP7_75t_SL", "TAPCELL_ASAP7_75t_SRAM",
      "TAPCELL_WITH_FILLER_ASAP7_75t_R", "TAPCELL_WITH_FILLER_ASAP7_75t_L", "TAPCELL_WITH_FILLER_ASAP7_75t_SL", "TAPCELL_WITH_FILLER_ASAP7_75t_SRAM",
      "FILLER_ASAP7_75t_R", "FILLER_ASAP7_75t_L", "FILLER_ASAP7_75t_SL", "FILLER_ASAP7_75t_SRAM",
      "FILLERxp5_ASAP7_75t_R", "FILLERxp5_ASAP7_75t_L", "FILLERxp5_ASAP7_75t_SL", "FILLERxp5_ASAP7_75t_SRAM",
      "DECAPx1_ASAP7_75t_R", "DECAPx1_ASAP7_75t_L", "DECAPx1_ASAP7_75t_SL", "DECAPx1_ASAP7_75t_SRAM",
      "DECAPx2_ASAP7_75t_R", "DECAPx2_ASAP7_75t_L", "DECAPx2_ASAP7_75t_SL", "DECAPx2_ASAP7_75t_SRAM",
      "DECAPx4_ASAP7_75t_R", "DECAPx4_ASAP7_75t_L", "DECAPx4_ASAP7_75t_SL", "DECAPx4_ASAP7_75t_SRAM",
      "DECAPx6_ASAP7_75t_R", "DECAPx6_ASAP7_75t_L", "DECAPx6_ASAP7_75t_SL", "DECAPx6_ASAP7_75t_SRAM",
      "DECAPx10_ASAP7_75t_R", "DECAPx10_ASAP7_75t_L", "DECAPx10_ASAP7_75t_SL", "DECAPx10_ASAP7_75t_SRAM"))),
    Annotation("system.library.asap7.tap_cells", CellsNameAnnotationValue(Seq("TAPCELL_ASAP7_75t_R", "TAPCELL_ASAP7_75t_SL", "TAPCELL_ASAP7_75t_L", "TAPCELL_ASAP7_75t_SRAM"))),
    Annotation("system.library.asap7.filler_cells", CellsNameAnnotationValue(Seq(
      "FILLER_ASAP7_75t_R", "FILLER_ASAP7_75t_L", "FILLER_ASAP7_75t_SL", "FILLER_ASAP7_75t_SRAM",
      "FILLERxp5_ASAP7_75t_R", "FILLERxp5_ASAP7_75t_L", "FILLERxp5_ASAP7_75t_SL", "FILLERxp5_ASAP7_75t_SRAM",
      "DECAPx1_ASAP7_75t_R", "DECAPx1_ASAP7_75t_L", "DECAPx1_ASAP7_75t_SL", "DECAPx1_ASAP7_75t_SRAM",
      "DECAPx2_ASAP7_75t_R", "DECAPx2_ASAP7_75t_L", "DECAPx2_ASAP7_75t_SL", "DECAPx2_ASAP7_75t_SRAM",
      "DECAPx4_ASAP7_75t_R", "DECAPx4_ASAP7_75t_L", "DECAPx4_ASAP7_75t_SL", "DECAPx4_ASAP7_75t_SRAM",
      "DECAPx6_ASAP7_75t_R", "DECAPx6_ASAP7_75t_L", "DECAPx6_ASAP7_75t_SL", "DECAPx6_ASAP7_75t_SRAM",
      "DECAPx10_ASAP7_75t_R", "DECAPx10_ASAP7_75t_L", "DECAPx10_ASAP7_75t_SL", "DECAPx10_ASAP7_75t_SRAM"
    ))),
    Annotation("system.library.asap7.tie_high_cells", CellsNameAnnotationValue(Seq("TIEHIx1_ASAP7_75t_R", "TIEHIx1_ASAP7_75t_L", "TIEHIx1_ASAP7_75t_SL", "TIEHIx1_ASAP7_75t_SRAM"))),
    Annotation("system.library.asap7.tie_low_cells", CellsNameAnnotationValue(Seq("TIEHIx1_ASAP7_75t_R", "TIEHIx1_ASAP7_75t_L", "TIEHIx1_ASAP7_75t_SL", "TIEHIx1_ASAP7_75t_SRAM"))),
    Annotation("system.library.asap7.layer_maps", LayerMapPathAnnotationValue(asap7File("ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB_v1p5/asap7PDK_r1p5.tar.bz2/asap7PDK_r1p5/cdslib/asap7_TechLib/asap7_fromAPR.layermap"))),
    Annotation("system.library.asap7.grid_unit", GridUnitAnnotationValue(0.001)),
    Annotation("system.library.asap7.time_unit", TimeUnitAnnotationValue("ps")),
    Annotation("system.library.asap7.lvs_rule_decks", RuleDecksAnnotationValue(Seq(RuleDeck(
      "all_lvs",
      asap7File("ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB_v1p5/asap7PDK_r1p5.tar.bz2/asap7PDK_r1p5/calibre/ruledirs/lvs/lvsRules_calibre_asap7.rul"),
      "calibre"
    )))),
    Annotation("system.library.asap7.drc_rule_decks", RuleDecksAnnotationValue(Seq(RuleDeck(
      "all_drc",
      asap7File("ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB_v1p5/asap7PDK_r1p5.tar.bz2/asap7PDK_r1p5/calibre/ruledirs/drc/drcRules_calibre_asap7.rul"),
      "calibre"
    ))))


  ) ++ (libType cross nominalTypeSet cross vtSet).asInstanceOf[Set[(String, String, String)]].map(l => libAnnotation(l._1, l._2, l._3))

  override def transform: ScratchPad = {
    pdkName match {
      case None => s
      case Some(PDKNameAnnotationValue("asap7")) =>
        cacheDir match {
          case None => downloadDir match {
            case None => throw LibraryNotFoundException
            case Some(DirectoryPathAnnotationValue(d)) =>
              s add
                Annotation("runtime.untar.tars", TarsPathAnnotationValue(Seq(d))) add
                Annotation("runtime.untar.internal_tars", RelTarsPathAnnotationValue(Seq(
                  RelPath("ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB_v1p5/asap7libs_24.tar.bz2"),
                  RelPath("ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB_v1p5/asap7PDK_r1p5.tar.bz2")
                ))) add
                Annotation("system.library.asap7.base_path", DirectoryPathAnnotationValue(tarCache)) add
                asap7Annotations
          }
          case Some(DirectoryPathAnnotationValue(d)) =>
            s delete
              "user.input.pdk.tars" add
              Annotation("system.library.base_path", DirectoryPathAnnotationValue(d))
        }
    }

  }
}

case class ASAP7Stage(scratchPadIn: ScratchPad) extends Stage {
  def phases: Seq[ScratchPad => Phase] = Seq(
    DigestASPA7LibraryPhase,
    UntarStage,
    ???
  )
}
