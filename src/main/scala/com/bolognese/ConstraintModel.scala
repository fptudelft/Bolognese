package com.bolognese
import oscar.cp.modeling.CPSolver
import oscar.cp.core.CPVarInt

/**
 * A utility class to create Constraint Models.
 * Currently it only contains a function to create a
 * Constraint Model for the Bolognese problem.
 */
object ConstraintModel {
  
  /**
   * Creates a CPModel for an instance of the Bolognese problem.
   * @param modules: the Modules of the problem instance, that should be booked
   * @param categories: the Categories of the problem instance,
   *                    in which Modules can be booked
   * @param totalEcts: the total amount of ECTS required to complete the study program
   */
  def fromBolognese(modules:List[Module],
                    categories:List[Category],
                    totalEcts:Int) : CPModel = {
    // create CPMVarInts with name="<module.id>:<category.id>", range:[0,1]
    val vars = modules.map(m=>m.categories.map(c=>CPMVarInt(m.id+":"+c, 0, 1))).flatten
    // create a Map of these variables, indexed by their name
    val varmap = vars.map(v=>(v.name,v)).foldLeft(Map[String,CPMVarInt]())((m,t)=>m+t)
    // create a Map of Modules to Categories in which they can be booked
    val mc = modules.map(m=>(m,categories.filter(c=>m.categories contains c.id)))
    .foldLeft(Map[Module,List[Category]]())((m,t)=>m+t)
    // create a Map of Categories to Modules which can be booked in them
    val cm = categories.map(c=>(c, modules.filter(m=>m.categories contains c.id)))
    .foldLeft(Map[Category,List[Module]]())((m,t)=>m+t)
    // create a Map of Categories to the sum of the ECTS of the booked
    // Modules for this Category
    val bookedEcts = for ((c,ms)<-cm) yield {
      (c, CPMIntSum(ms.map(m=>CPMIntMul(CPMIntRef(m.id+":"+c.id), CPMIntCons(m.ects)))))
    }
    // create a constraint for each Category, that it should have enough ECTS booked
    val minEctsConstraints = bookedEcts.map(t=>CPMIntGtEq(t._2, CPMIntCons(t._1.min)))
    // create constraint for each Module, that it can only be booked
    // in one Category at a time
    val moduleBookingConstraints = for ((m,cs)<-mc) yield {
      CPMIntLtEq(CPMIntSum(cs.map(c=>CPMIntRef(m.id+":"+c.id))), CPMIntCons(1))
    }
    // create a constraint that the total amount of creditable booked
    // ECTS is at least totalEcts
    val totalEctsConstraint = 
      CPMIntGtEq(CPMIntSum(bookedEcts.map(t=>new CPMIntMinimum(t._2, t._1.max))),
                 CPMIntCons(totalEcts))
    // collect all the constraints
    val cons : Collection[AbstractCPMConstraint] =
      minEctsConstraints ++ moduleBookingConstraints ++ List(totalEctsConstraint)
    // create a model with the variables, constraints and the minimization goal
    return CPModel(vars,cons,CPMMinimize(CPMIntSum(bookedEcts.values)))
  }
}

//		.-----------------------.
//		|	AST constructors	|
//		'-----------------------'


/**
*  Generic Constraint Model for our OscaR interface 
*/
case class CPModel(vars:Collection[CPMVarInt], constraints:Collection[AbstractCPMConstraint], goal:AbstractCPMGoal)

/**
* Fixed integer variable which contains the result of running OscaR on a Constraint Model.
* @param name: the name of the constraint model integer variable that got fixed
* @param value: the integer value that was determined for the corresponding constraint model integer variable  
*/
case class CPMFixedVar(name:String, value:Int)

/**
* Constraint Model integer variable.
* It represents a range from start (inclusive) to end (inclusive), and will eventually be fixed to a
* CPMFixedVar, when OscaR is done. 
* The name should be unique for all constraint model variables in the constraint model.
* @param name: String name for this variable, should be unique for all variables in the constraint model.
* @param start: the Integer where the range of possible values for this integer variable should start (inclusive)
* @param end: the Integer where the range of possible values for this integer variable should end (inclusive) 
*/
case class CPMVarInt(name:String, start:Int, end:Int)

/**
* A constraint for the constraint model.
*/
abstract class AbstractCPMConstraint

/**
* An implementation of the AbstractCPMConstraint.
* It represents the constraint lhs < rhs
*/
case class CPMIntLt(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMConstraint

/**
* An implementation of the AbstractCPMConstraint.
* It represents the constraint lhs > rhs
*/
case class CPMIntGt(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMConstraint

/**
* An implementation of the AbstractCPMConstraint.
* It represents the constraint lhs <= rhs
*/
case class CPMIntLtEq(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMConstraint

/**
* An implementation of the AbstractCPMConstraint.
* It represents the constraint lhs >= rhs
*/
case class CPMIntGtEq(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMConstraint

/**
* An expression that can be used in a constraint.
* It should represent a CPMVarInt typed expression
*/
abstract class AbstractCPMIntExp

/**
* Multiplication of CPMVarInt typed expressions.
* It represents lhs * rhs
*/
case class CPMIntMul(lhs:AbstractCPMIntExp, rhs:AbstractCPMIntExp) extends AbstractCPMIntExp

/**
* Minimum of CPMVarInt typed expression and an integer.
* It represents Math.min(lhs,rhs)
*/
case class CPMIntMinimum(lhs:AbstractCPMIntExp, rhs:Int) extends AbstractCPMIntExp

/**
* Sum of CPMVarInt typed expressions.
* It represents sum(value)
*/
case class CPMIntSum(value:Collection[AbstractCPMIntExp]) extends AbstractCPMIntExp

/**
* A reference to a CPMVarInt from the variables in the constraint model.
* @param value: the name of a CPMVarInt from the constraint model.
*/
case class CPMIntRef(value:String) extends AbstractCPMIntExp

/**
* An integer constant.
* @param value: the integer it represents.
*/
case class CPMIntCons(value:Int) extends AbstractCPMIntExp

/**
 * An optimization goal for the constraint model.
 * It tells what kind of solution you are looking for. 
 */
abstract class AbstractCPMGoal

/**
 * The minimization goal.
 * It means you want to find a solution to the constraint model, 
 * which minimizes a certain CPMVarInt typed function
 */
case class CPMMinimize(function:AbstractCPMIntExp) extends AbstractCPMGoal
