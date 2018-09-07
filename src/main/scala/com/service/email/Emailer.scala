package com.service.email


import java.io.File
import java.nio.file.{FileSystems, Paths}
import java.util.concurrent.Executors

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent._
import scala.util.{Failure, Success};

object Emailer {
 
    def main(args: Array[String]): Unit = {
        implicit val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

        println("Start processing... thread " + Thread.currentThread().getName)
        
        if (args.length != 2) throw new IllegalArgumentException
        val parallelism = args(0).toInt
        val dataDir = args(1)
        val startTime = System.currentTimeMillis
        val dataFiles = new File(dataDir, "edrm-enron-v2").listFiles().toList.map(_.toString).map(Paths.get(_))
        val zipFilesMatcher = FileSystems.getDefault().getPathMatcher(s"glob:**/*_xml.zip")
        val batchFiles = dataFiles.filter(zipFilesMatcher.matches).par
        val emailStatistics = batchFiles.flatMap(path => EmailBatch.findDocuments(path)).toSet.toList
    
        batchFiles.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(parallelism))
    
        val averageWordCount = emailStatistics.map(_.wordCount).sum / emailStatistics.size
        val top100 = emailStatistics.flatMap(stats => stats.to.map((_, 1.0)) ++ stats.cc.map((_, 0.5)))
                .groupBy(_._1).toList.map { case (email, weights) => (weights.map(_._2).sum, email) }
                .sortBy(-_._1)
                .take(100)
    
        println("Top 100 recipients (ascending ranking where the weight of regular recipient is 1.0 and cc = 0.5):")
        top100.reverse.foreach(println)

        println("Num. of batcheFiles: " + batchFiles.size)
        println("Processing time: " + ((System.currentTimeMillis - startTime) / 600).toDouble / 100 + " minutes")
        println("Total unique messages: " + emailStatistics.size)
        println("Average num.of words per message: " + averageWordCount)
        
        println("Completed processing... thread " + Thread.currentThread().getName)
    }
}
