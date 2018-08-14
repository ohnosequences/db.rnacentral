package ohnosequences.db.rnacentral

import java.io.File

final case class RNACentralData(
    val idMapping: File,
    val speciesSpecificFasta: File
)

final case class RNASequence(
    val rnaID: RNAID,
    val sequence: String
)

final case class Entry(
    val rnaSequence: RNASequence,
    val sequenceAnnotations: Set[SequenceAnnotation],
    val entryAnnotations: Set[EntryAnnotation]
)

final case class EntryAnnotation(
    val rnaID: RNAID,
    val ncbiTaxonomyID: TaxonID,
    val databaseEntry: DatabaseEntry,
    val rnaType: RNAType,
    val geneName: Option[String]
)

final case class DatabaseEntry(
    val database: Database,
    val id: String
)

final case class SequenceAnnotation(
    val rnaID: RNAID,
    val ncbiTaxonomyID: TaxonID,
    val description: String
)

sealed class RNAType(val name: String)

case object RNAType {

  case object rRNA          extends RNAType("rRNA")
  case object tRNA          extends RNAType("tRNA")
  case object tmRNA         extends RNAType("tmRNA")
  case object precursorRNA  extends RNAType("precursor_RNA")
  case object telomeraseRNA extends RNAType("telomerase_RNA")
  case object ncRNA         extends RNAType("ncRNA")
  case object mRNA          extends RNAType("mRNA")
  case object antisenseRNA  extends RNAType("antisense_RNA")
  case object autocataliticallySplicedIntron
      extends RNAType("autocatalytically_spliced_intron")
  case object ribozyme           extends RNAType("ribozyme")
  case object hammerheadRibozyme extends RNAType("hammerhead_ribozyme")
  case object lncRNA             extends RNAType("lncRNA")
  case object RNasePRNA          extends RNAType("RNase_P_RNA")
  case object RNaseMRPRNA        extends RNAType("RNase_MRP_RNA")
  case object guideRNA           extends RNAType("guide_RNA")
  case object rasiRNA            extends RNAType("rasiRNA")
  case object sRNA               extends RNAType("sRNA")
  case object scRNA              extends RNAType("scRNA")
  case object siRNA              extends RNAType("siRNA")
  case object miRNA              extends RNAType("miRNA")
  case object piRNA              extends RNAType("piRNA")
  case object snoRNA             extends RNAType("snoRNA")
  case object snRNA              extends RNAType("snRNA")
  case object SRPRNA             extends RNAType("SRP_RNA")
  case object vaultRNA           extends RNAType("vault_RNA")
  case object YRNA               extends RNAType("Y_RNA")
  case object miscRNA            extends RNAType("misc_RNA")
  case object other              extends RNAType("other")

  val from: String => Option[RNAType] =
    _ match {
      case rRNA.name  => Some(rRNA)
      case tRNA.name  => Some(tRNA)
      case ncRNA.name => Some(ncRNA)
      case mRNA.name  => Some(mRNA)
      case autocataliticallySplicedIntron.name =>
        Some(autocataliticallySplicedIntron)
      case ribozyme.name                   => Some(ribozyme)
      case hammerheadRibozyme.name         => Some(hammerheadRibozyme)
      case lncRNA.name                     => Some(lncRNA)
      case RNasePRNA.name                  => Some(RNasePRNA)
      case RNaseMRPRNA.name                => Some(RNaseMRPRNA)
      case guideRNA.name                   => Some(guideRNA)
      case rasiRNA.name                    => Some(rasiRNA)
      case scRNA.name                      => Some(scRNA)
      case sRNA.name                       => Some(sRNA)
      case siRNA.name                      => Some(siRNA)
      case miRNA.name                      => Some(miRNA)
      case piRNA.name                      => Some(piRNA)
      case snoRNA.name                     => Some(snoRNA)
      case snRNA.name                      => Some(snRNA)
      case SRPRNA.name                     => Some(SRPRNA)
      case YRNA.name                       => Some(YRNA)
      case miscRNA.name                    => Some(miscRNA)
      case tmRNA.name                      => Some(tmRNA)
      case precursorRNA.name               => Some(precursorRNA)
      case telomeraseRNA.name              => Some(telomeraseRNA)
      case vaultRNA.name | "vaultRNA"      => Some(vaultRNA)
      case antisenseRNA.name | "antisense" => Some(antisenseRNA)
      case other.name                      => Some(other)
      case _                               => None
    }
}

sealed class Database(val name: String)

case object Database {

  case object dictyBase  extends Database("DICTYBASE")
  case object ENA        extends Database("ENA")
  case object Ensembl    extends Database("ENSEMBL")
  case object FlyBase    extends Database("FLYBASE")
  case object GENCODE    extends Database("GENCODE")
  case object GreenGenes extends Database("GREENGENES")
  case object GtRNAdb    extends Database("GTRNADB")
  case object HGNC       extends Database("HGNC")
  case object LNCipedia  extends Database("LNCIPEDIA")
  case object lncRNAdb   extends Database("LNCRNADB")
  case object miRBase    extends Database("MIRBASE")
  case object Modomics   extends Database("MODOMICS")
  case object NONCODE    extends Database("NONCODE")
  case object PDBe       extends Database("PDB")
  case object PomBase    extends Database("POMBASE")
  case object RDP        extends Database("RDP")
  case object RefSeq     extends Database("REFSEQ")
  case object Rfam       extends Database("RFAM")
  case object SGD        extends Database("SGD")
  case object SILVA      extends Database("SILVA")
  case object snOPY      extends Database("SNOPY")
  case object SRPDB      extends Database("SRPDB")
  case object TAIR       extends Database("TAIR")
  case object tmRNA_Web  extends Database("TMRNA_WEB")
  case object WormBase   extends Database("WORMBASE")

  val from: String => Option[Database] =
    _ match {
      case dictyBase.name  => Some(dictyBase)
      case ENA.name        => Some(ENA)
      case Ensembl.name    => Some(Ensembl)
      case FlyBase.name    => Some(FlyBase)
      case GENCODE.name    => Some(GENCODE)
      case GreenGenes.name => Some(GreenGenes)
      case GtRNAdb.name    => Some(GtRNAdb)
      case HGNC.name       => Some(HGNC)
      case LNCipedia.name  => Some(LNCipedia)
      case lncRNAdb.name   => Some(lncRNAdb)
      case miRBase.name    => Some(miRBase)
      case Modomics.name   => Some(Modomics)
      case NONCODE.name    => Some(NONCODE)
      case PDBe.name       => Some(PDBe)
      case PomBase.name    => Some(PomBase)
      case RDP.name        => Some(RDP)
      case RefSeq.name     => Some(RefSeq)
      case Rfam.name       => Some(Rfam)
      case SGD.name        => Some(SGD)
      case SILVA.name      => Some(SILVA)
      case snOPY.name      => Some(snOPY)
      case SRPDB.name      => Some(SRPDB)
      case TAIR.name       => Some(TAIR)
      case tmRNA_Web.name  => Some(tmRNA_Web)
      case WormBase.name   => Some(WormBase)
      case _               => None
    }
}
