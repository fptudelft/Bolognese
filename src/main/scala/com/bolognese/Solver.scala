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
	
	def solve(model:CPModel) : Collection[CPMFixedVar] = {
	    val cp = new CPSolver;
	    val varmap =  model.vars.map(oscarize(_,cp)).toMap
	    var fixedMap = Map[String, Int]()
	    oscarize(model.goal, cp, varmap) subjectTo 
	    	model.constraints.foreach(oscarize(_,cp,varmap)) exploration {
	    		cp.binaryFirstFail(varmap.values.toArray)
	    		fixedMap = varmap.map(x=>(x._1, x._2.getValue))
	    	}
	    return fixedMap.map(t=>CPMFixedVar(t._1,t._2))
	}
	
	def oscarize(g:AbstractCPMGoal, cp:CPSolver, vars:Map[String,CPVarInt]) : CPSolver = g match {
	    case CPMMinimize(f) => cp.minimize(oscarize(f,cp,vars))
	}
	
	def oscarize(v:CPMVarInt, cp:CPSolver) : (String, CPVarInt) = v match {
	    case CPMVarInt(n,s,e) => (n, CPVarInt(cp,s,e))
	}
	
	def oscarize(c:AbstractCPMConstraint, cp:CPSolver, vars:Map[String,CPVarInt]) : Unit = c match {
	    case CPMIntLt(l,r) => cp.add(oscarize(l,cp,vars) < oscarize(r,cp,vars))
	    case CPMIntLtEq(l,r) => cp.add(oscarize(l,cp,vars) <= oscarize(r,cp,vars))
	    case CPMIntGt(l,r) => cp.add(oscarize(l,cp,vars) > oscarize(r,cp,vars))
	    case CPMIntGtEq(l,r) => cp.add(oscarize(l,cp,vars) >= oscarize(r,cp,vars))
	}
	
	def oscarize(e:AbstractCPMIntExp, cp:CPSolver, vars:Map[String,CPVarInt]) : CPVarInt = e match {
	    case CPMIntMul(l,r) => oscarize(l,cp,vars) * oscarize(r,cp,vars)
	    case CPMIntMinimum(l,r) => {
	        val x = oscarize(l,cp,vars)
	        CPVarInt(cp, x.min, Math.min(x.max,r))
	    }
	    case CPMIntSum(es) => sum(es.map(oscarize(_,cp,vars)))
	    case CPMIntRef(n) => vars(n)
	    case CPMIntCons(i) => CPVarInt(cp,i)
	}
}
