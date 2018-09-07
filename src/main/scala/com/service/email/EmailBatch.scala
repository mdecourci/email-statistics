package com.service.email

import java.io._

import scala.xml.XML
import java.io.{FileOutputStream, InputStream}
import java.nio.file.{FileSystems, Files, Path}
import java.util.zip.{ZipEntry, ZipInputStream}
import java.io.File

import scala.collection.immutable._
import scala.io.Source
import scala.util.Try

case class EmailStatistics(wordCount: Long, to: Set[String], cc: Set[String])

object EmailBatch {
    
    
    def main(args: Array[String]): Unit = {
        val tempDir = FileSystems.getDefault.getPath("/Users/michaeldecourci/Downloads/enron/my-test")
        unzipFile("/Users/michaeldecourci/Downloads/enron/edrm-enron-v2_arora-h_xml.zip", tempDir)
    
        val file = new File(tempDir.toUri)
        file.list().find(_.endsWith("xml")).foreach(println(_))
    }

    val emailAddressPattern = "(([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9\\-\\.]+))".r;
    
    def tempDir = Files.createTempDirectory(this.getClass.getSimpleName)
    
    var xmlFile : String = ""
    
    def addresses(recipients: String): List[String] = {
        emailAddressPattern.findAllIn(recipients).matchData.map {
            m => m.group(1).toLowerCase()
        }.toList
    }
    
    def using[T <: {def close()}, U](resource: T)(block: T => U): U = {
        try {
            block(resource)
        } finally {
            if (resource != null) {
                resource.close()
            }
        }
    }
    
    private var data: List[EmailStatistics] = null
    
    def findDocuments(zipFile: Path): List[EmailStatistics] = {
    
        unzipFile(zipFile.toString, tempDir)
    
        val parentDirectory = new File(xmlFile).getParentFile
    
        if (xmlFile.length > 0) {
            val xml = XML.loadFile(new File(xmlFile))
            val documents = xml \ "Batch" \ "Documents" \ "Document"
            val documentSections = documents filter (_ \@ "MimeType" == "message/rfc822")
    
            val files = for {
                documentSection <- documentSections
            } yield {
        
                val toRecipients = for (tag <- documentSection \\ "Tag"; if (tag \@ "TagName" == "#To")) yield addresses(tag \@ "TagValue")
                val ccRecipients = for (tag <- documentSection \\ "Tag"; if (tag \@ "TagName" == "#CC")) yield addresses(tag \@ "TagValue")
        
                val subjectWordCount = for {
                    tag <- documentSection \\ "Tag"
                    if (tag \@ "TagName" == "#Subject")
                } yield {
                    (tag \@ "TagValue").split("\\s+").length
                }
        
                val bodyWordCount = for {
                    file <- documentSection \\ "File"
                    externalFile <- file \ "ExternalFile"
                    if file \@ "FileType" == "Text"
                } yield {
                    val textContentPath = parentDirectory.toString + File.separator +
                            externalFile \@ "FilePath" + "/" + (externalFile \@ "FileName")
            
                    val textContent = Source.fromFile(textContentPath, "UTF-8").mkString
                            .split("\n\r", 2)(1)
            
                    textContent.split("\\s+").length
                }
                val totalWordCount = (subjectWordCount ++ bodyWordCount).sum
                EmailStatistics(totalWordCount, toRecipients.flatten.toSet, ccRecipients.flatten.toSet)
            }
    
            data = files.filter(_.to.size > 0).toList
        }
        data
    }
    
        def processEntries(entry: ZipEntry, zipFileStream: ZipInputStream, destination: Path): Unit = {
            if (!entry.getName.startsWith("native") && !entry.isDirectory) {
                val outPath = destination.resolve(entry.getName)
                val outPathParent = outPath.getParent
                if (!outPathParent.toFile.exists()) {
                    outPathParent.toFile.mkdirs()
                }
                if (outPath.getFileName.toString.endsWith("xml")) {
                    xmlFile = outPath.toString
                }
    
                using(new FileOutputStream(outPath.toFile)) { out => {
                    val buffer = new Array[Byte](4096)
                    Stream.continually(zipFileStream.read(buffer)).takeWhile(_ != -1).foreach(out.write(buffer, 0, _))
                }
                
                }
            }
        }
    
    def unzipFile(zipFile: String, destination: Path): Unit = {
        val zis = new ZipInputStream(new FileInputStream(zipFile))
    
        Stream.continually(zis.getNextEntry).takeWhile(_ != null).foreach { file =>
            if (!file.getName.startsWith("native") && !file.isDirectory) {
                val outPath = destination.resolve(file.getName)
                val outPathParent = outPath.getParent
                if (!outPathParent.toFile.exists()) {
                    outPathParent.toFile.mkdirs()
                }
                
                if (outPath.getFileName.toString.endsWith("xml")) {
                    xmlFile = outPath.toString
                }
    
                val outFile = outPath.toFile
                val out = new FileOutputStream(outFile)
                val buffer = new Array[Byte](4096)
                Stream.continually(zis.read(buffer)).takeWhile(_ != -1).foreach(out.write(buffer, 0, _))
            }
        }
    }
    
//        def unzipFile(zipFile: String, destination: Path, doStuff: (ZipEntry, ZipInputStream, Path) => Unit) {
//        using(new ZipInputStream(new FileInputStream(zipFile))) { zipInputStream =>
//            val entries = Stream.continually(Try(zipInputStream.getNextEntry()).getOrElse(null))
//                    .takeWhile(_ != null) // while not EOF and not corrupted
//                    .foreach(doStuff(_, zipInputStream, destination))
//        }
//    }
    
    
}