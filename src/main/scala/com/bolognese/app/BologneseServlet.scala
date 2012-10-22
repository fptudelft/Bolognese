package com.bolognese.app

import org.scalatra._
import scalate.ScalateSupport
import oscar.cp.modeling._
import oscar.search._
import oscar.cp.core._
import com.bolognese.DecisionTable
import com.bolognese.Module
import com.bolognese.Category
import com.bolognese.Solver
import com.bolognese.State

import net.liftweb.json._
import Serialization.{read, write => swrite}


class BologneseServlet extends ScalatraServlet with ScalateSupport {
	get("/") {
		contentType = "text/JSON"
		implicit val formats = Serialization.formats(NoTypeHints)

		val cp = CPSolver()

		val totalEcts = 17

				val categories = List(new Category(0, "Compulsory", 10, 33),
						new Category(1, "Specialization", 7, 7))

						val modules = List(new Module(0, "Methods", 5, List(0)),
								new Module(1, "Computer Architecture", 5, List(0)),
								new Module(2, "Computer Arithemtics", 5, List(0)),
								new Module(3, "Processor Design Project", 5, List(1)),
								new Module(4, "Intro Computer Enginerring", 2, List(1)),
								new Module(5, "Parallel Algorithms", 6, List(0)))

		val decisionTable = new DecisionTable(cp, categories, modules)

		
		var newState = Solver.solve(new State(cp, categories, modules, decisionTable, totalEcts))
		swrite(newState)
	}
	
	post("/") {
	  	implicit val formats = Serialization.formats(NoTypeHints)
	  	val state = read[State](request.body)
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
