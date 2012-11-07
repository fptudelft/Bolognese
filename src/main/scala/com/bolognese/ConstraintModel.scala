package com.bolognese

object ConstraintModel {
	def fromBolognese(modules:List[Module], categories:List[Category], totalEcts:Int) : CPModel = {
		val vars = modules.map(m=>m.categories.map(c=>new CPMVarInt(m.id+":"+c, 0 to 1))).flatten
		val varmap = vars.map(v=>(v.name,v)).foldLeft(Map[String,CPMVarInt]())((m,t)=>m+t)

		val mc = modules.map(m=>(m,categories.filter(c=>m.categories contains c.id)))
			.foldLeft(Map[Module,List[Category]]())((m,t)=>m+t)

		val cm = categories.map(c=>(c, modules.filter(m=>m.categories contains c.id)))
			.foldLeft(Map[Category,List[Module]]())((m,t)=>m+t)

		val bookedEcts = for ((c,ms)<-cm) yield {
			(c, CPMIntSum(ms.map(m=>new CPMIntMul(CPMIntRef(m.id+":"+c.id), m.ects))))
		}
		
		val minEctsConstraints = bookedEcts.map(t=>new CPMIntGtEq(t._2, t._1.min))

		val moduleBookingConstraints = for ((m,cs)<-mc) yield {
			new CPMIntLtEq(CPMIntSum(cs.map(c=>CPMIntRef(m.id+":"+c.id))), 1)
		}

		val totalEctsConstraint = 
			new CPMIntGtEq(CPMIntSum(bookedEcts.map(t=>new CPMIntMinimum(t._1.max, t._2))), totalEcts)

		val cons : Collection[AbstractCPMConstraint] = minEctsConstraints++moduleBookingConstraints++List(totalEctsConstraint)
		
		return CPModel(vars,cons,CPMMinimize(CPMIntSum(bookedEcts.values)))
	}
}


case class CPModel(vars:Collection[AbstractCPMVar], constraints:Collection[AbstractCPMConstraint], goal:AbstractCPMGoal)

abstract class AbstractCPMVar() {
    val name : String
}
case class CPMVarInt(name:String, start:Int, end:Int) extends AbstractCPMVar {
    def this(n:String, r:Range) = this(n, r.start,if (r.isInclusive) r.end else r.end-1) 
}
abstract class AbstractCPMConstraint
case class CPMIntLt(left:Either[Int, AbstractCPMIntExp], right:Either[Int,AbstractCPMIntExp]) extends AbstractCPMConstraint {
    def this(li:Int, ri:Int) = this(Left(li), Left(ri))
	def this(li:AbstractCPMIntExp, ri:AbstractCPMIntExp) = this(Right(li), Right(ri))
	def this(li:Int, ri:AbstractCPMIntExp) = this(Left(li), Right(ri))
	def this(li:AbstractCPMIntExp, ri:Int) = this(Right(li), Left(ri))
}
case class CPMIntGt(left:Either[Int,AbstractCPMIntExp], right:Either[Int,AbstractCPMIntExp]) extends AbstractCPMConstraint {
    def this(li:Int, ri:Int) = this(Left(li), Left(ri))
	def this(li:AbstractCPMIntExp, ri:AbstractCPMIntExp) = this(Right(li), Right(ri))
	def this(li:Int, ri:AbstractCPMIntExp) = this(Left(li), Right(ri))
	def this(li:AbstractCPMIntExp, ri:Int) = this(Right(li), Left(ri))
}
case class CPMIntLtEq(left:Either[Int,AbstractCPMIntExp], right:Either[Int,AbstractCPMIntExp]) extends AbstractCPMConstraint {
    def this(li:Int, ri:Int) = this(Left(li), Left(ri))
	def this(li:AbstractCPMIntExp, ri:AbstractCPMIntExp) = this(Right(li), Right(ri))
	def this(li:Int, ri:AbstractCPMIntExp) = this(Left(li), Right(ri))
	def this(li:AbstractCPMIntExp, ri:Int) = this(Right(li), Left(ri))
}
case class CPMIntGtEq(left:Either[Int,AbstractCPMIntExp], right:Either[Int,AbstractCPMIntExp]) extends AbstractCPMConstraint {
    def this(li:Int, ri:Int) = this(Left(li), Left(ri))
	def this(li:AbstractCPMIntExp, ri:AbstractCPMIntExp) = this(Right(li), Right(ri))
	def this(li:Int, ri:AbstractCPMIntExp) = this(Left(li), Right(ri))
	def this(li:AbstractCPMIntExp, ri:Int) = this(Right(li), Left(ri))
}
abstract class AbstractCPMIntExp
case class CPMIntMul(left:Either[Int,AbstractCPMIntExp], right:Either[Int,AbstractCPMIntExp]) extends AbstractCPMIntExp {
	def this(li:Int, ri:Int) = this(Left(li), Left(ri))
	def this(li:AbstractCPMIntExp, ri:AbstractCPMIntExp) = this(Right(li), Right(ri))
	def this(li:Int, ri:AbstractCPMIntExp) = this(Left(li), Right(ri))
	def this(li:AbstractCPMIntExp, ri:Int) = this(Right(li), Left(ri))
} 
case class CPMIntMinimum(left:Either[Int,AbstractCPMIntExp], right:Either[Int,AbstractCPMIntExp]) extends AbstractCPMIntExp {
    def this(li:Int, ri:Int) = this(Left(li), Left(ri))
	def this(li:AbstractCPMIntExp, ri:AbstractCPMIntExp) = this(Right(li), Right(ri))
	def this(li:Int, ri:AbstractCPMIntExp) = this(Left(li), Right(ri))
	def this(li:AbstractCPMIntExp, ri:Int) = this(Right(li), Left(ri))
}
case class CPMIntSum(parts:Collection[AbstractCPMIntExp]) extends AbstractCPMIntExp
case class CPMIntRef(name:String) extends AbstractCPMIntExp
abstract class AbstractCPMGoal
case class CPMMinimize(function:AbstractCPMIntExp) extends AbstractCPMGoal