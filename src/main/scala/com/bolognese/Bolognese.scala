package com.bolognese

import java.io.File
import java.io.IOException
import scala.io.Source
import scala.collection.immutable.HashMap
import scala.collection.immutable.List
import scala.util.parsing.json.JSON

/**
 * The comtainer object responsible for parsing Bolognese input files.
 */
object BologneseBarfer {
  
  /**
   * Reads an input file and turns it into a list of lines.
   */
  def readContents(fn:String) : String = {
    ("" /: Source.fromFile(fn).getLines)((l, r) => l ++ "\n" ++ r)
  }
  
  /**
   * Reads an input file and turns it into a list of lines.
   */
  def readFile(fn:String) : List[String] = {
    var results = List[String]()
    Source.fromFile(fn).getLines.foreach( (line) => {
      results = line :: results
    })
    results.reverse
  }
}

object Bolognese {
  def main(args:Array[String]) = {
    val filepath = args.mkString("")
    // var lines = BologneseBarfer.readFile(filepath)
    var fileContents = BologneseBarfer.readContents(filepath)
    val p = JSON.parseFull(fileContents)
    println(p)
    println
    p.foreach( (item) => println(item + "\n") )
  }
}
