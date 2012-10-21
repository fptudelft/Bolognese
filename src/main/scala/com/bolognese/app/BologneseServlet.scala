package com.bolognese.app

import org.scalatra._
import scalate.ScalateSupport

import oscar.cp.modeling._
import oscar.search._
import oscar.cp.core._

class BologneseServlet extends ScalatraServlet with ScalateSupport {

  case class Category(val id: Int, name: String, min: Int, max: Int) {}
  
  case class Module(val id: Int , name: String, ects: Int, val categories: List[Int]) {}
  
  class DecisionTable(cp: CPSolver, categories : List[Category], modules : List[Module]) {
   
   val decisionTable = categories.map { c => 
        		modules.map { 
        		  m => if (m.categories.contains(c.id)) {
    	    		CPVarInt(cp, 0, 1)
        		  } else {
    	    		CPVarInt(cp, 0)
    	    	}}}
            
   	def get() : List[CPVarInt] = {
   	  decisionTable.flatten
   	}
   
     def get(module : Module) : List[CPVarInt] = {
    	  decisionTable.map { c => c(module.id) }
     }
      
     def get(category : Category) : List[CPVarInt] = {
    	  decisionTable(category.id)
     }
     
     def getBooked(category: Category) = {
      sum((this.get(category), modules).zipped map { (a, b) => (a * b.ects) })
     }
     
     override def toString() = {
        decisionTable.transpose.map {a => a.mkString(" ")}.mkString("\n")
     }
     
     def getBookings() = {
       categories.map {c=>(c, modules.filter{m=>decisionTable(c.id)(m.id).isBoundTo(1)})}
     }
  }
  
  def bookingToString(i : List[(Category, List[Module])]) : String = {
    i.map(bookingToString).mkString("\n")
  }
  def bookingToString(i:(Category, List[Module])) : String = {
    i._1.name + " : {\n" + i._2.map(m=>"\t" + m.name + "\n").mkString("") + "}"
  }
  
  get("/") {
      val cp = CPSolver()
      
      val totalEcts = 17
      
//      val categories = List(new Category(0, "Compulsory", 33, 33),
//    		  				new Category(1, "Specialization", 17, 17),
//    		  				new Category(2, "Faculty", 0, 10),
//    		  				new Category(3, "Free", 15, 15))
//    		  				 
//      val modules = List(new Module(0, "Methods", 5, List(0)),
//    		  			 new Module(1, "Computer Architecture", 5, List(0)),
//    		  			 new Module(2, "Computer Arithemtics", 5, List(0)),
//    		  			 new Module(3, "Processor Design Project", 5, List(0, 2)),
//    		  			 new Module(4, "Intro Computer Enginerring", 2, List(0)),
//      					 new Module(5, "Parallel Algorithms", 6, List(0)),
//      					 new Module(6, "Compiler Construction", 5, List(0)))
//      
 
    val categories = List(new Category(0, "Compulsory", 10, 33),
    		  				new Category(1, "Specialization", 7, 7))
    		  		
    val modules = List(new Module(0, "Methods", 5, List(0)),
    		  			 new Module(1, "Computer Architecture", 5, List(0)),
    		  			 new Module(2, "Computer Arithemtics", 5, List(0)),
    		  			 new Module(3, "Processor Design Project", 5, List(1)),
    		  			 new Module(4, "Intro Computer Enginerring", 2, List(1)),
      					 new Module(5, "Parallel Algorithms", 6, List(0)))
      					 
      val decisionTable = new DecisionTable(cp, categories, modules)
      
      var str = ""
      var solution : List[List[(Category, List[Module])]]= List()

        cp.minimize(sum(categories.map {c => decisionTable.getBooked(c)})) subjectTo {
    	  modules.foreach { m => cp.add(sum(decisionTable.get(m)) <= 1)}
    	  categories.foreach { c => cp.add(decisionTable.getBooked(c) >= c.min)}
    	  
    	  def boundedBooked(category: Category) = {
    		  val sumOfBooked = decisionTable.getBooked(category)
    		  if (sumOfBooked.max > category.max)
    		    sumOfBooked.updateMax(category.max)
    		  sumOfBooked
    	  }  	  
    	 cp.add(sum(categories.map { c => boundedBooked(c) }) >= totalEcts)	
    	 
      } exploration {
        cp.binaryFirstFail(decisionTable.get.toIndexedSeq)
      	str += "\n" + decisionTable + "\n"
      	solution = decisionTable.getBookings::solution
      }
       cp.printStats()
//      if (str != "") {
//        str 
//      } else {
//       "No Result"  
//        }
       bookingToString(solution.last)
    }

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
