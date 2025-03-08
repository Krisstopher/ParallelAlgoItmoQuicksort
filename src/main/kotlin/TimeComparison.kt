import java.security.SecureRandom
import kotlin.math.min
import kotlin.system.measureTimeMillis

fun main() {
  println(Runtime.getRuntime().availableProcessors())
  val random = SecureRandom()
  val testRuns = 5
  val threads = 4
  val parallelThreshold = 2000
  println("Configuring $testRuns random arrays\n")
  val arraysForRuns = Array(testRuns) { Array(20_000_000) { random.nextInt() } }

  val seqQuicksort = SeqQuicksort<Int>()
  val parQuicksort = ParQuicksort<Int>(threads, parallelThreshold)
  val parQuicksortWithFilter = ParQuicksortWithFilter(threads, parallelThreshold)
  var seqTimeSum = 0L
  var parTimeSum = 0L
  var parFilterTimeSum = 0L

  println("Running sequential quicksort:")
  for (i in 0 until testRuns) {
    val seqTime = measureTimeMillis { seqQuicksort.sort(arraysForRuns[i]) }
    println("For ${i}th run time is: $seqTime ms")
    seqTimeSum += seqTime
  }

  val avgSeqTime = seqTimeSum / testRuns
  println("Average sequential sort time: $avgSeqTime ms\n")

  println("Running parallel quicksort:")
  for (i in 0 until testRuns) {
    val parTime = measureTimeMillis { parQuicksort.sort(arraysForRuns[i]) }
    println("For ${i}th run time is: $parTime ms")
    parTimeSum += parTime
  }
  val avgParTime = parTimeSum / testRuns
  println("Average parallel sort time: $avgParTime ms\n")

//  println("Running parallel quicksort with filter:")
//  for (i in 0 until testRuns) {
//    val parTime = measureTimeMillis { parQuicksortWithFilter.sort(arraysForRuns[i]) }
//    println("For ${i}th run time is: $parTime ms")
//    parFilterTimeSum += parTime
//  }
//
//  val avgFilterParTime = parFilterTimeSum / testRuns
//  println("Average parallel sort with filter time: $avgFilterParTime ms\n")

//  val minAvgParTime = min(parTimeSum, parFilterTimeSum) / testRuns
  val minAvgParTime = avgParTime

  val timesFaster = String.format("%.2f", avgSeqTime.toDouble() / minAvgParTime.toDouble())
  println("Parallel quicksort on $threads threads is $timesFaster times faster than sequential")
}
