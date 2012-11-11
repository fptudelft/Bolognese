package com.bolognese.app

import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet

import com.bolognese.Category
import com.bolognese.ConstraintModel
import com.bolognese.Module
import com.bolognese.OscaR
import com.bolognese.CPMFixedVar
import com.bolognese.CPModel

import net.liftweb.json.Serialization.{read => sread}
import net.liftweb.json.Serialization.{write => swrite}
import net.liftweb.json.NoTypeHints
import net.liftweb.json.Serialization
import net.liftweb.json.parse

import oscar.cp.modeling.CPSolver

/**
 * Case class for JSON serialisation
 */
case class JsonData (
  val categories : List[Map[String, List[String]]],
  val modules : List[Map[String, List[String]]],
  val totalEcts : Int
)

/**
 * Case class for JSON deserialisation
 */
case class Result (
  val modulesPerCategory : Map[String, List[String]]
)

/**
 * A simple closure for generating unids
 */
object IdGenerator {
  var currentId : Int = 0;

  def nextId() : Int = {
    this.currentId = this.currentId + 1;
    this.currentId;
  }
}

/**
 * Object container for helper functions
 */
object Helper {
  def categoriesFrom(jd : JsonData) : List[Category] = {
    var catsList : List[Category] = List()
    for (cat <- jd.categories) {
      val catId : Int = IdGenerator.nextId
      val name : String = cat.get(Const.CAT_NAME).get(0)
      val minEcts : Int = cat.get(Const.CAT_MIN_ECTS).get(0).toInt
      val maxEcts : Int = cat.get(Const.CAT_MAX_ECTS).get(0).toInt

      catsList = catsList ++ List(new Category(catId, name, minEcts, maxEcts))
    }
    catsList
  }

  def modulesFrom(jd : JsonData,
                  catToIdMap : Map[String, Int]) : List[Module] = {
    var modsList : List[Module] = List()
    for (mod <- jd.modules) {
      val modId : Int = IdGenerator.nextId
      val name : String = mod.get(Const.MOD_NAME).get(0)
      val ects : Int = mod.get(Const.MOD_ECTS).get(0).toInt
      val bookableCats : List[String] = mod.get(Const.MOD_BOOKABLE_CATS).get
      val bookableCatIds : List[Int] = bookableCats.map(c => catToIdMap.get(c).get)

      modsList = modsList ++ List(new Module(modId, name, ects, bookableCatIds))
    }
    modsList
  }

  def catToIdMapFrom(cats : List[Category]) : Map[String, Int] = {
    var ctim : Map[String, Int] = Map()
    for (cat : Category <- cats) {
      ctim = ctim ++ Map(cat.name -> cat.id)
    }
    ctim
  }

  def idToCatMapFrom(cats : List[Category]) : Map[Int, String] = {
    var itcm : Map[Int, String] = Map()
    for (cat : Category <- cats) {
      itcm = itcm ++ Map(cat.id -> cat.name)
    }
    itcm
  }

  def filteredModules(vs : Collection[CPMFixedVar],
                      c : Category,
                      modules : List[Module]) : Iterable[Module] = {

    def bookedVarsInCategory() : Collection[CPMFixedVar] = {
      // Closes over the vs and c vars from the container function
      vs.filter(v => v.name.endsWith(":"+c.id) && v.value == 1)
    }

    def moduleCorrespondingTo(v : CPMFixedVar) : Module = {
      // Each module has unique module id, therefore 
      // there can only be one module in the list
      modules.filter((m : Module) => v.name.startsWith(m.id+":")).head
    }

    return for (v <- bookedVarsInCategory())
           yield moduleCorrespondingTo(v)
  }

  def resultFrom(mpc : Map[String, Iterable[String]]) : Result = {
    var data = Map[String, List[String]]()
    mpc foreach ( (pair) => data += (pair._1 -> pair._2.toList) )
    Result(data)
  }

  def logInterestingData(categories : List[Category],
                         modules : List[Module],
                         totalEcts : Int,
                         result : Result) : Unit = {
    println()
    println("------------------------------------------------------------")
    println("Total number of ECTS: ")
    println(totalEcts)
    println("------------------------------------------------------------")
    println("Categories:")
    categories.foreach (println)
    println("------------------------------------------------------------")
    println("Modules:")
    modules.foreach (println)
    println("------------------------------------------------------------")
    println("result to be sent back to the user: ")
    println(result)
    println("------------------------------------------------------------")
    println()
  }
}

// Object containing constants
object Const {
  // Category key constants
  val CAT_NAME : String = "Name"
  val CAT_MIN_ECTS : String = "Minimum Points"
  val CAT_MAX_ECTS : String = "Maximum Points"

  // Module key constants
  val MOD_NAME : String = "Name"
  val MOD_ECTS : String = "Points"
  val MOD_BOOKABLE_CATS : String = "Bookable Categories"
}
  
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
	(c,
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
  
  post("/solve") {
    contentType = "application/json"
    response.setHeader("Access-Control-Allow-Origin", "*")
    implicit val formats = Serialization.formats(NoTypeHints)
    val jsonData = sread[JsonData](request.body)

    val totalEcts : Int = jsonData.totalEcts
    val categories : List[Category] = Helper.categoriesFrom(jsonData)
    val catToIdMap : Map[String, Int] = Helper.catToIdMapFrom(categories)
    val idToCatMap : Map[Int, String] = Helper.idToCatMapFrom(categories)
    val modules : List[Module] = Helper.modulesFrom(jsonData, catToIdMap)

    val model : CPModel =
      ConstraintModel.fromBolognese(modules, categories, totalEcts)
    val oscarResult : OscaR[Collection[CPMFixedVar]] = OscaR.create(model)

    val x : OscaR[List[(Category, Iterable[Module])]] =
      for (vs <- oscarResult) yield {
        for (c <- categories) yield (c, Helper.filteredModules(vs, c, modules))
      }
    val y = x.map(x => x.toMap)
    val z = y.map( m => m.map(tuple => {
      val category = tuple._1
      val bookedModules = tuple._2
      (category.name, bookedModules.map(m => m.name))
    }))

    // This is the end result the user is interested in
    val modulesPerCategory : Map[String, Iterable[String]] = z.apply
    val result : Result  = Helper.resultFrom(modulesPerCategory)

    // Let's log some stuff serverside for easy access 
    Helper.logInterestingData(categories, modules, totalEcts, result)

    // Send the results back to the user
    swrite[Result](result)
  }
  
}
