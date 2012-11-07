package com.bolognese
import oscar.cp.core.CPVarInt
import oscar.cp.modeling.CPSolver
import oscar.cp.modeling.sum

object Solver {

	def solve(state:State) = {

		state.cp.minimize(sum(state.categories.map {c => state.decisionTable.getBooked(c)})) subjectTo
		{
			state.modules.foreach { 
			  m => state.cp.add(sum(state.decisionTable.get(m)) <= 1)
			}
			state.categories.foreach { 
			  c => state.cp.add(state.decisionTable.getBooked(c) >= c.min)
			}
			def boundedBooked(category: Category) = {
				val sumOfBooked = state.decisionTable.getBooked(category)
				if (sumOfBooked.max > category.max) {
					sumOfBooked.updateMax(category.max)
				}
				sumOfBooked
			}  	  
			state.cp.add(sum(state.categories.map { c => boundedBooked(c) }) >= state.totalEcts)	
		} exploration {
			state.cp.binaryFirstFail(state.decisionTable.get.toArray)
			state.bookings = state.decisionTable.getBookings()
		}
		state 
	}
	
	def solve(model:CPModel) = {
	    val cp = new CPSolver;
	    
	}
}
