package com.bolognese
import oscar.cp.core.CPVarInt
import oscar.cp.modeling.CPSolver

case class Test[A](val i:A) {
  
	def map[B] (f: A=> B) : Test[B] = {
	  return new Test(f(i));
	}
	
	def flatMap[B](f:A=>Test[B]) : Test[B] = {
	  return f(i)
	}
	
}

object Main {
  
  
	def main(args: Array[String]) {
		val l = for (
			x <- Test(5)
		) yield {
		  println(x)
		  x+"burp"
		}
		println(l)
	}
}


