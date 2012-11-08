package com.bolognese
import oscar.cp.modeling.CPSolver

import net.liftweb.json._
import Serialization.{read, write => swrite}

case class State(  
  val cp:CPSolver, 
  val categories:List[Category], 
  val modules:List[Module], 
  val decisionTable:DecisionTable,
  val totalEcts: Int,
  var bookings : List[(Category, List[Module])] = List()) {
  }

case class ProblemModel(
  val categories:List[Category],
  val modules:List[Module],
  val totalEcts:Int
)

case class JsonData (
  val categories:List[Map[String, String]],
  val modules:List[Map[String, String]]
)
