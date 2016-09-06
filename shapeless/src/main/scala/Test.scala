package prometheus.shapeless

import shapeless._
import shapeless.ops.hlist._

case class LabelledValuedCounter(name: String, labels: List[(String, String)]) {
  def inc(d: Double): Unit = (); //println(s"Incrementing by $d")
  def inc(): Unit = (); //println(s"Incrementing")
}

case class LabelledCounter[A <: HList, ALen <: Nat](name: String, labels: A) extends ProductArgs {
  def labelValuesProduct[B <: HList, BLen <: Nat, Zipped <: HList](labelValues: B)(implicit
                                                                                   labelValuesLen: Length.Aux[B, BLen],
                                                                                   zipper: Zip.Aux[A :: B :: HNil, Zipped],
                                                                                   toTraversable: ToTraversable.Aux[Zipped, List, (String, String)],
                                                                                   labelAndValueLengthsSame: ALen =:= BLen) =
    LabelledValuedCounter(name, labels.zip(labelValues).toList)

  def inc(d: Double)(implicit ev: ALen =:= Nat._0): Unit = (); //println(s"Incrementing by $d")
  def inc()(implicit ev: ALen =:= Nat._0): Unit = (); //println(s"Incrementing")
}

case class Counter(name: String) extends ProductArgs {
  def applyProduct[A <: HList, ALen <: Nat](labels: A)(implicit
                                                       lubString: LUBConstraint[A, String],
                                                       labelLen: Length.Aux[A, ALen],
                                                       toTraversableAux: shapeless.ops.hlist.ToTraversable.Aux[A,List,String]) =
    LabelledCounter[A, ALen](name, labels)
}

// Works
// Counter("num_requests")().inc
// Counter("num_requests")("path").labelValues("/home").inc

// Doesn't work
// Counter("num_requests")("path").inc
// Counter("num_requests")().labelValues("wut?").inc
// Counter("num_requests")(3).inc // not a string
