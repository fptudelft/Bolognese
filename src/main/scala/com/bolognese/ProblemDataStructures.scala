package com.bolognese

/**
 * A Category from a Bolognese problem instance.
 * @param id: the integer id of the Category.
 * Should be unique for all Categories of a Bolognese problem instance.
 * @param name: the name of the Category.
 * @param min: the minimum number of ECTS that should be booked into this Category,
 * in order for the student to be able to pass the study program.
 * @param max: the maximum number of ECTS that can be booked into this Category,
 * and for which the student can get actually get credit.
 * This assumes overbooking is allowed, but it won't count towards the total ECTS.
 */
case class Category(val id: Int, name: String, min: Int, max: Int) {}

/**
 * A Module from a Bolognese problem instance.
 * @param id: the integer id of the Module.
 * Should be unique for all Modules of a Bolognese problem instance.
 * @param name: the name of the Module.
 * @param ects: the ECTS that a student gets for this course.
 * @param categories: a list of ids of Categories, in which this Module can possibly be booked.
 */
case class Module(val id: Int , name: String, ects: Int, val categories: List[Int]) {}
