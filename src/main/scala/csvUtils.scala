package era7bio.db

import better.files._

case object csvUtils {

  // TODO use tsv
  import com.github.tototoshi.csv._

  val separator = "\t"

  def csvReader(file: File): CSVReader = CSVReader.open(file.toJava)(new TSVFormat {})

  def lines(file: File): Iterator[Seq[String]] = csvReader(file) iterator

  // TODO much unsafe, add errors
  def rows(file: File)(headers: Seq[String]): Iterator[Map[String,String]] =
    lines(file) map { line => (headers zip line) toMap }
}

import ohnosequences.cosas._, klists._

case class HArray[T <: AnyKList](val rep: Array[Any]) extends AnyVal
