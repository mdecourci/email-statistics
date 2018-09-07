package com.service.email

import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import java.io.File
import java.nio.file.Files

import scala.io.Source


class EmailBatchTest extends FunSuite with Matchers with BeforeAndAfter {
    
    val emailBatch = EmailBatch
    
    val source = Source.fromURL(getClass.getResource("/"))
    
    def enronDirectory = new File(getClass.getResource("/").toURI)
    def zipFile = new File(enronDirectory, "edrm-enron-v2_arora-h_xml.zip")
    
    test("Verify that there are emails with to recipients from enron.com in the batch of email documents") {
        val documents = emailBatch.findDocuments(zipFile.toPath)
        documents.toList.map(p => p.to).toList.mkString contains "enron.com"
    }
    
}
