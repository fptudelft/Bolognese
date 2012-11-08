package com.bolognese
import oscar.cp.modeling.CPSolver
import oscar.cp.core.CPVarInt

object ConstraintModel {
	def fromBolognese(modules:List[Module], categories:List[Category], totalEcts:Int) : CPModel = {
		val vars = modules.map(m=>m.categories.map(c=>CPMVarInt(m.id+":"+c, 0, 1))).flatten
		val varmap = vars.map(v=>(v.name,v)).foldLeft(Map[String,CPMVarInt]())((m,t)=>m+t)

		val mc = modules.map(m=>(m,categories.filter(c=>m.categories contains c.id)))
			.foldLeft(Map[Module,List[Category]]())((m,t)=>m+t)

		val cm = categories.map(c=>(c, modules.filter(m=>m.categories contains c.id)))
			.foldLeft(Map[Category,List[Module]]())((m,t)=>m+t)

		val bookedEcts = for ((c,ms)<-cm) yield {
			(c, CPMIntSum(ms.map(m=>CPMIntMul(CPMIntRef(m.id+":"+c.id), CPMIntCons(m.ects)))))
		}
		
		val minEctsConstraints = bookedEcts.map(t=>CPMIntGtEq(t._2, CPMIntCons(t._1.min)))

		val moduleBookingConstraints = for ((m,cs)<-mc) yield {
			CPMIntLtEq(CPMIntSum(cs.map(c=>CPMIntRef(m.id+":"+c.id))), CPMIntCons(1))
		}

		val totalEctsConstraint = 
			CPMIntGtEq(CPMIntSum(bookedEcts.map(t=>new CPMIntMinimum(t._2, t._1.max))), CPMIntCons(totalEcts))

		val cons : Collection[AbstractCPMConstraint] = minEctsConstraints++moduleBookingConstraints++List(totalEctsConstraint)
		
		return CPModel(vars,cons,CPMMinimize(CPMIntSum(bookedEcts.values)))
	}
}


case class CPModel(vars:Collection[CPMVarInt], constraints:Collection[AbstractCPMConstraint], goal:AbstractCPMGoal)

case class CPMFixedVar(name:String, value:Int)

case class CPMVarInt(name:String, start:Int, end:Int)

abstract class AbstractCPMConstraint {
    val lhs : AbstractCPMIntExp
    val rhs : AbstractCPMIntExp
}
case class CPMIntLt(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMConstraint
case class CPMIntGt(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMConstraint
case class CPMIntLtEq(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMConstraint
case class CPMIntGtEq(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMConstraint

abstract class AbstractCPMIntExp
abstract class AbstractCPMBinaryIntExp extends AbstractCPMIntExp
case class CPMIntMul(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMBinaryIntExp
case class CPMIntMinimum(lhs:AbstractCPMIntExp, rhs:Int) extends AbstractCPMBinaryIntExp

abstract class AbstractCPMUnaryIntExp extends AbstractCPMIntExp
case class CPMIntSum(value:Collection[AbstractCPMIntExp]) extends AbstractCPMUnaryIntExp
case class CPMIntRef(value:String) extends AbstractCPMUnaryIntExp
case class CPMIntCons(value:Int) extends AbstractCPMUnaryIntExp

abstract class AbstractCPMGoal
case class CPMMinimize(function:AbstractCPMIntExp) extends AbstractCPMGoal