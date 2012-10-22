package com.bolognese

import java.io.File
import java.io.IOException
import scala.io.Source
import scala.collection.immutable.HashMap
import scala.collection.immutable.List

/**
 * The comtainer object responsible for parsing Bolognese input files.
 */
object BologneseBarfer {
  
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
  
  /**
   * Parses the Bolognese input format.
   * fileLines: The lines to parse
   * data: The parse results
   */
  def parse(fileLines:List[String]) : Map[String, _] = {
    def startsSection(sectionName:String, sectionToken:String, line:String) =
      sectionName.isEmpty && !line.isEmpty && line.startsWith(sectionToken)
    
    val categoryToken = "#"
    def isCategory(sectionName:String, line:String) = {
      !startsSection(sectionName, categoryToken, line) &&
      line.startsWith(categoryToken)
    }
    def parseCategory(sectionName:String, line:String) =
      line.stripPrefix(categoryToken)
    
    val bookableToken = "+"
    def isBookableModule(sectionName:String, line:String) = {
      !startsSection(sectionName, bookableToken, line) &&
      line.startsWith(bookableToken)
    }
    def parseBookableModule(sectionName:String, line:String) = {
      var splitLine = line.stripPrefix(bookableToken).split('[')
      val courseName = splitLine(0).stripSuffix(" ")
      splitLine = splitLine(1).split(']')
      val ectsWorkload = splitLine(0).toInt
      val bookableCategories =
        List.fromArray(splitLine(1).stripPrefix(" ").split(", "))
      Map() + ("courseName" -> courseName) +
              ("ects" -> ectsWorkload) +
              ("bookableCategories" -> bookableCategories)
    }
    
    val bookedToken = "-"
    def isBookedModule(sectionName:String, line:String) = {
      !startsSection(sectionName, bookedToken, line) && 
      line.startsWith(bookedToken)
    }
    def parseBookedModule(sectionName:String, line:String) = {
      var splitLine = line.stripPrefix(bookedToken).split(':')
      val courseName = splitLine(0).stripSuffix(" ")
      val category = splitLine(1).stripPrefix(" ")
      Map() + ("courseName" -> courseName) +
              ("category" -> category)
    }
    
    def isEndOfSection(line:String) = line.isEmpty
    
    def processSectionLine[T](pf:(String, String) => T, sectionName:String,
                              line:String,              data:Map[String, _],
                              rest:List[String],        coercionType : T) = {
      val parseResults = pf(sectionName, line)
      val categories : List[T] =
        data.get(sectionName).get.asInstanceOf[List[T]]
      val newCategories =  parseResults :: categories
      slave(rest, sectionName, data + (sectionName -> newCategories))
    }

    def slave(fileLines:List[String],
                     sectionName:String,
                     data:Map[String, _]) : Map[String, _] = {
      fileLines match {
        case Nil => data // EOF
        case line :: rest => {
          if (isEndOfSection(line)) {
            // Empty out the group name
            slave(rest, "", data)
          }
          else if (startsSection(sectionName, categoryToken, line)) {
            val sectionName = line.stripPrefix(categoryToken)
            slave(rest, sectionName, data ++ Map(sectionName -> List[String]()))
          }
          else if (isCategory(sectionName, line))
            processSectionLine(parseCategory, sectionName, line,
                               data, rest, classOf[String])
          else if (startsSection(sectionName, bookableToken, line)) {
            val sectionName = line.stripPrefix(bookableToken)
            slave(rest, sectionName, data ++ Map(sectionName -> List[String]()))
          }
          else if (isBookableModule(sectionName, line))
            processSectionLine(parseBookableModule, sectionName, line, 
                               data, rest, classOf[Map[String, _]])
          else if (startsSection(sectionName, bookedToken, line)) {
            val sectionName = line.stripPrefix(bookedToken)
            slave(rest, sectionName, data ++ Map(sectionName -> List[String]()))
          }
          else if (isBookedModule(sectionName, line))
            processSectionLine(parseBookedModule, sectionName, line, 
                               data, rest, classOf[Map[String, _]])
          else {
            throw new IOException("Malformed entry: " + line)
          }
        }
      }
    }

    slave(fileLines, "", Map[String, Map[String,_]]())
  }
}

object Bolognese {
  def main(args:Array[String]) = {
    val filepath = args.mkString("")
    var lines = BologneseBarfer.readFile(filepath)
    val p = BologneseBarfer.parse(lines)
    println
    p.foreach( (item) => println(item + "\n") )
  }
}
