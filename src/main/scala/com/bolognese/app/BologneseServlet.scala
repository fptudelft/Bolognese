package com.bolognese.app

import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet

import com.bolognese.Category
import com.bolognese.ConstraintModel
import com.bolognese.Module
import com.bolognese.OscaR

import net.liftweb.json.Serialization.read
import net.liftweb.json.Serialization.{write => swrite}
import net.liftweb.json.NoTypeHints
import net.liftweb.json.Serialization
import net.liftweb.json.parse
import oscar.cp.modeling.CPSolver

/**
 * The main API to the server.
 * This ScalatraServlet defines all API calls that can be done.
 */
class BologneseServlet extends ScalatraServlet with ScalateSupport {
  
    get("/") {
        val totalEcts = 17
        val categories = List(new Category(0, "Compulsory", 10, 33),
        new Category(1, "Specialization", 7, 7))
        val modules = List(new Module(0, "Methods", 5, List(0)),
		    new Module(1, "Computer Architecture", 5, List(0)),
		    new Module(2, "Computer Arithemtics", 5, List(0)),
		    new Module(3, "Processor Design Project", 5, List(1)),
		    new Module(4, "Intro Computer Enginerring", 2, List(1)),
		    new Module(5, "Parallel Algorithms", 6, List(0)))
	    val model = ConstraintModel.fromBolognese(modules, categories, totalEcts)
	    val res = OscaR.create(model)
	    val x = for (
	        vs <- res
	    ) yield {
	        for (c<-categories) yield (
	        	(
	        		c,
	        		(for (v<-vs.filter(v=>v.name.endsWith(":"+c.id) && v.value == 1)) yield
	            		modules.filter(m=>v.name.startsWith(m.id+":"))).flatten
	        	)
	        )
	    } 
        val y = x.map(x=>x.toMap)
	    val z = y.map(m=>m.map(t=>(t._1.name, t._2.map(m=>m.name))))
	    z.apply
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

  // TODO, seems like a test of Joey
  post("/solve") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")
    implicit val formats = Serialization.formats(NoTypeHints)
    // swrite(request.body)
    swrite(parse(""" { "hi" : [1,2,3] } """))
  }

  //TODO delete all of this if it remains unused
  get("/solve") {
    var catId : Int = 0
    def fetchCategories() : List[Category] = {
      var retCats = List[Category]()

      // URI parameter key "cat" is used for categories
      for (category <- multiParams("cat")) {
        val catInfo : Array[String] = category.split(",")
        val name = catInfo(0)
        val minECTS = catInfo(1).toInt
        val maxECTS = catInfo(2).toInt
        retCats = retCats ++ List(new Category(catId, name, minECTS, maxECTS))
        catId = catId+1;
      }
      
      retCats
    }
    
    var modId : Int = 0
    def fetchModules(availableCategories:List[Category]) : List[Module] = {
      var retMods = List[Module]()
      
      // URI parameter key "mod" is used for modules
      for (module <- multiParams("mod")) {
        val modInfo : Array[String] = module.split(",")
        val name = modInfo(0)
        val ects = modInfo(1).toInt
        val categories = List(availableCategories.indexWhere(
          (cat) => cat.name == modInfo(2) 
        ))

        retMods = retMods ++ List(new Module(modId, name, ects, categories))
        modId = modId+1;
      }
      
      retMods
    }

    contentType = "application/json"
    implicit val formats = Serialization.formats(NoTypeHints)
    try {
//      val oldState = read[State](request.body) // Results in a net.liftweb.json.MappingException: No usable value for totalEcts Did not find value which can be converted into int
      val cp = CPSolver()
      val totalEcts = 17
 
// http://127.0.0.1:8080/solve?cat=compulsory,10,33&cat=specialization,7,7&mod=methods,5,compulsory&mod=computer%20architecture,5,compulsory&mod=Computer%20Arithemtics,5,compulsory&mod=Processor%20Design%20Project,5,specialization&mod=Intro%20Computer%20Engineering,2,specialization&mod=Parallel%20Algorithms,6,compulsory

// Torn apart the link looks like this:

// http://127.0.0.1:8080/solve
// ?cat=compulsory,10,33
// &cat=specialization,7,7
// &mod=methods,5,compulsory
// &mod=computer%20architecture,5,compulsory
// &mod=Computer%20Arithemtics,5,compulsory
// &mod=Processor%20Design%20Project,5,specialization
// &mod=Intro%20Computer%20Engineering,2,specialization
// &mod=Parallel%20Algorithms,6,compulsory

// this yields JSON result:
    // {"cp":{},
   //   "categories":[{"id":0,"name":"compulsory","min":10,"max":33},
   //                 {"id":1,"name":"specialization","min":7,"max":7}],
   //   "modules":[{"id":0,"name":"methods","ects":5,"categories":[0]},
   //              {"id":1,"name":"computer architecture","ects":5,"categories":[0]},
   //              {"id":2,"name":"Computer Arithemtics","ects":5,"categories":[0]},
   //              {"id":3,"name":"Processor Design Project","ects":5,"categories":[1]},
   //              {"id":4,"name":"Intro Computer Engineering","ects":2,"categories":[1]},
   //              {"id":5,"name":"Parallel Algorithms","ects":6,"categories":[0]}],
   //   "decisionTable":{"categories":[{"id":0,"name":"compulsory","min":10,"max":33},
   //                                  {"id":1,"name":"specialization","min":7,"max":7}]},
   //   "totalEcts":17,
   //   "bookings":[
   //     {"_1":{"id":0,"name":"compulsory","min":10,"max":33},
   //      "_2":[
   //            {"id":1,"name":"computer architecture","ects":5,"categories":[0]},
   //            {"id":2,"name":"Computer Arithemtics","ects":5,"categories":[0]}
   //           ]},
   //     {"_1":{"id":1,"name":"specialization","min":7,"max":7},
   //      "_2":[{"id":3,"name":"Processor Design Project","ects":5,"categories":[1]},
   //            {"id":4,"name":"Intro Computer Engineering","ects":2,"categories":[1]}]
   //     }
   //           ]
   // }
    
      val categories = fetchCategories()
      val modules = fetchModules(categories)
      
    } catch {
      case me : net.liftweb.json.MappingException => {
        // No world state yet, so create it
        
        val cp = CPSolver()
        val totalEcts = 17 
        
        val categories = fetchCategories()
        val modules = fetchModules(categories)
       
        swrite(parse("""{ "hi" : [1,2,3] }"""))
      }
    }
  }
}
