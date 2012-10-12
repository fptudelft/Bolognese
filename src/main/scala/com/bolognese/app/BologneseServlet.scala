package com.bolognese.app

import org.scalatra._
import scalate.ScalateSupport

import oscar.cp.modeling._
import oscar.search._
import oscar.cp.core._

class BologneseServlet extends ScalatraServlet with ScalateSupport {

  get("/") {
      val cp = CPSolver()
      
      val n = 8 //number of queens
      val Queens = 0 until n
      //variables
      val queens = for(i <- Queens) yield CPVarInt(cp,1 to n)
      
      var nbsol = 0
      cp.solveAll subjectTo {
          cp.add(alldifferent(queens),Strong)
          cp.add(alldifferent(for(i <- Queens) yield queens(i) + i),Strong)
          cp.add(alldifferent(for(i <- Queens) yield queens(i) - i),Strong)
      } exploration {        
        for (q <- Queens.suspendable) {
          cp.branchAll(1 to n)(v => cp.post(queens(q) == v))
        }
        nbsol += 1
      }
  
      //print some statistics
      "Number of solutions, with " + n + " " + "queens: " + nbsol
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
