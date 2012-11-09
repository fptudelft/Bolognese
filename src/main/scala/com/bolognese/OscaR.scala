package com.bolognese
import oscar.cp.modeling.CPSolver
import oscar.cp.core.CPVarInt
import oscar.cp.modeling.sum

/*
 * The OscaR monad is a monad which does some OscaR computations,
 * and then returns the result.
 * It can be viewed as a box with something, and when you take the
 * something out of the box, side-effects happen.
 * 
 * The only OscaR a monad that initially makes sense is the OscaR
 * Collection[CPMFixedVar] monad, which can be constructed with a CPModel.
 * It runs some OscaR computations based on the CPModel and returns
 * the collection of fixed variable values, as solved by OscaR.
 * To encapsulate the side-effects of OscaR, these computations will
 * not happen until you extract the value using the apply function.
 * Use map and flatMap to map the CPFixedVars to a suitable solution
 * and then only at the very end run apply to get the value back out.
 * 
 * Based on the work of James Iry:
 * http://james-iry.blogspot.nl/2007/11/monads-are-elephants-part-4.html
 */


/**
 *  The monad with its functions 
 *  	- map (often referred to as fmap), 
 *  	- flatten (often referred to as join), 
 *	- flatMap (Scala name for bind)
 */
sealed abstract class OscaR[+A] extends Function0[A] {
  def map[B](f:A=>B):OscaR[B] = {
    OscaR.unit(f(this()))
  }
  
  def flatten[B](a:OscaR[OscaR[B]]):OscaR[B] = {
    OscaR.unit(a.apply().apply())
  }
  
  def flatMap[B](f:A=>OscaR[B]):OscaR[B] = {
    flatten(map(f))
  }
}

/**
 *  An instance creator of OscaR monads, 
 *  this has the unit (often referred to as return) function for the OscaR monad.
 *  
 *  It also has the more factory function create, 
 *  which creates a meaningful OscaR[Collection[CPMFixedVar]] monad, from a CPModel
 */
object OscaR {
  def unit[A](e: => A):OscaR[A] = new OscaRImpl(e) 
  
  /**
   *  Creates an OscaR[Collection[CPMFixedVar]] from a CPModel. 
   *  See the notes at the top of this file.
   */
  def create(model:CPModel):OscaR[Collection[CPMFixedVar]] = {
    new OscaRImpl({
      val cp = new CPSolver
      val varmap =  model.vars.map(oscarize(_,cp)).toMap
      var fixedMap = Map[String, Int]()
      oscarize(model.goal, cp, varmap) subjectTo 
      model.constraints.foreach(oscarize(_,cp,varmap)) exploration {
	cp.binaryFirstFail(varmap.values.toArray)
	fixedMap = varmap.map(x=>(x._1, x._2.getValue))
      }
      fixedMap.map(t=>CPMFixedVar(t._1,t._2))
    })
  }
  
  /**
   *  Implementing class of the OscaR monad, using lazy expressions 
   */
  private class OscaRImpl[+A](e: =>A) extends OscaR[A] {
    def apply() = {e}
  }
  
  /*
   *  Conversion of the Constraint Model to stuff OscaR understands.
   *  If you extend the Constraint AST, make sure to add the appropriate entries
   *  here as well!
   *  
   *  This recursive pattern matching approach was inspired by fold algebras,
   *  as introduced by Ralf Laemmel at the Delft University of Technology.
   *  See: http://101companies.org/index.php/101implementation:tabaluga
   */
  
  // TODO: consider moving all this to some place that is better suited
  private def oscarize(g:AbstractCPMGoal,
                       cp:CPSolver,
                       vars:Map[String,CPVarInt]) : CPSolver = g match {
    case CPMMinimize(f) => cp.minimize(oscarize(f,cp,vars))
  }
  
  private def oscarize(v:CPMVarInt,
                       cp:CPSolver) : (String, CPVarInt) = v match {
    case CPMVarInt(n,s,e) => (n, CPVarInt(cp,s,e))
  }
  
  private def oscarize(c:AbstractCPMConstraint,
                       cp:CPSolver,
                       vars:Map[String,CPVarInt]) : Unit = c match {
    case CPMIntLt(l,r) => cp.add(oscarize(l,cp,vars) < oscarize(r,cp,vars))
    case CPMIntLtEq(l,r) => cp.add(oscarize(l,cp,vars) <= oscarize(r,cp,vars))
    case CPMIntGt(l,r) => cp.add(oscarize(l,cp,vars) > oscarize(r,cp,vars))
    case CPMIntGtEq(l,r) => cp.add(oscarize(l,cp,vars) >= oscarize(r,cp,vars))
  }

  private def oscarize(e:AbstractCPMIntExp,
                       cp:CPSolver,
                       vars:Map[String,CPVarInt]) : CPVarInt = e match {
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
