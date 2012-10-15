package com.bolognese.app

import org.scalatra._
import scalate.ScalateSupport

import oscar.cp.modeling._
import oscar.search._
import oscar.cp.core._

class BologneseServlet extends ScalatraServlet with ScalateSupport {

  class Category(id: Int, name: String, min: Int, max: Int) {
    
  }
  
  class Module(id: Int , name: String, ects: Int, categories: Array[Int]) {
    
  }
  
  get("/") {
      val cp = CPSolver()
    
      val totalEcts = 120
      
      val categories = Array(new Category(0, "Compulsory", 33, 33),
    		  				 new Category(1, "Specialization", 17, 17),
    		  				 new Category(2, "Faculty", 0, 10),
    		  				 new Category(3, "Free", 15, 15))
    		  				 
      val modules = Array(new Module(0, "Methods", 5, Array(0)),
    		  			  new Module(1, "Computer Architecture", 5, Array(0)),
    		  			  new Module(2, "Computer Arithemtics", 5, Array(0)),
    		  			  new Module(3, "Processor Design Project", 5, Array(0)),
    		  			  new Module(4, "Intro Computer Enginerring", 2, Array(0)),
      					  new Module(5, "Parallel Algorithms", 6, Array(0)),
      					  new Module(6, "Compolier Construction", 5, Array(0)))
      					 
      val dcm = Array.fill(modules.length) {CPVarInt(cp, 0 to 1)}
      
//      val n = 8 //number of queens
//      val Queens = 0 until n
//      //variables
//      val queens = for(i <- Queens) yield CPVarInt(cp,1 to n)
//      
//      var nbsol = 0
//      cp.solveAll subjectTo {
//          cp.add(alldifferent(queens),Strong)
//          cp.add(alldifferent(for(i <- Queens) yield queens(i) + i),Strong)
//          cp.add(alldifferent(for(i <- Queens) yield queens(i) - i),Strong)
//      } exploration {        
//        for (q <- Queens.suspendable) {
//          cp.branchAll(1 to n)(v => cp.post(queens(q) == v))
//        }
//        nbsol += 1
//      }
  
      //print some statistics
//      "Number of solutions, with " + n + " " + "queens: " + nbsol
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
