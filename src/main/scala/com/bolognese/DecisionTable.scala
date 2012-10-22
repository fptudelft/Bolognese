package com.bolognese
import oscar.cp.core.CPVarInt
import oscar.cp.modeling.CPSolver
import oscar.cp.modeling.sum

class DecisionTable(cp: CPSolver, categories : List[Category], modules : List[Module]) {

	val decisionTable = categories.map { c => 
	modules.map { 
		m => if (m.categories.contains(c.id)) {
			CPVarInt(cp, 0, 1)
		} else {
			CPVarInt(cp, 0)
		}}}

	def get(): List[CPVarInt] = {
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
		categories.map { c=> (c, modules.filter{ m=>decisionTable(c.id)(m.id).isBoundTo(1) })}
	}

}

// Data format for our problem
case class Category(val id: Int, name: String, min: Int, max: Int) {}

case class Module(val id: Int , name: String, ects: Int, val categories: List[Int]) {}
