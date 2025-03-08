import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import java.security.SecureRandom
import java.util.concurrent.ForkJoinPool
import kotlin.math.abs
import kotlin.system.measureTimeMillis

class ParallelUtilsTest {
  private val random = SecureRandom()
  private val utils = ParallelUtils()

  @Test
  fun parallelMapInplace() {
    val testRuns = 100

    for (i in 1..testRuns) {
      val size = abs(random.nextLong()) % 10000 + 1000
      val array = random.ints(size).toArray().toTypedArray()
      val function: (Int) -> Int = { it / 2 }

      val expected = array.copyOf().map(function).toTypedArray()
      utils.parallelMapInplace(array, function)
      assertThat(array).containsExactly(*expected)
    }
  }

  @Test
  fun parallelMapInplace_empty() {
    val function: (Int) -> Int = { it / 2 }
    val resultArray = utils.parallelMapInplace(emptyArray(), function)

    assertThat(resultArray).isEmpty()
  }

  @Test
  fun parallelMapInplace_mustBeFasterThanSequential() {
    val availableProcessors = Runtime.getRuntime().availableProcessors()
    val pool = ForkJoinPool(availableProcessors)
    val threshold = 1000

    val testRuns = 200
    val function: (Int) -> Int = { it / 2 }
    val parArray = Array(6000000) { 100000 }
    var seqArray = parArray.copyOf()

    var seqTimeSum = 0L
    var parTimeSum = 0L

    for (i in 1..testRuns) {
      seqTimeSum += measureTimeMillis {
        seqArray = seqArray.map(function).toTypedArray()
      }
      parTimeSum += measureTimeMillis {
        utils.parallelMapInplace(parArray, function, threshold, pool)
      }
    }

    assertThat(parTimeSum * availableProcessors / 2).isLessThan(seqTimeSum)
  }

  @Test
  fun parallelScanInplace() {
    val testRuns = 100

    for (i in 1..testRuns) {
      val size = abs(random.nextLong()) % 10000 + 1000
      val array = random.ints(size, -1000, 1000).toArray().toTypedArray()
//            val array = arrayOf(2, 23, 12, 4, -30, 10, 21, 13, 1, 1, 0)
      val expected = array.copyOf().prefixSum()

      utils.parallelScanInplace(array, Int::plus)
      assertThat(array).containsExactly(*expected)
    }
  }

  @Test
  fun parallelScanInplace_empty() {
    val resultArray = utils.parallelScanInplace(emptyArray(), Int::plus)

    assertThat(resultArray).isEmpty()
  }

//  @Test
//  fun parallelScanInplace_mustBeFasterThanSequential() {
//    val availableProcessors = Runtime.getRuntime().availableProcessors()
//    val pool = ForkJoinPool(8)
//    val threshold = 1000
//
//    val testRuns = 10
//
//    var seqTimeSum = 0L
//    var parTimeSum = 0L
//
//    for (i in 1..testRuns) {
//      val parArray = random.ints(7000000, -10000, 10000).toArray().toTypedArray()
//      var seqArray = parArray.copyOf()
//      seqTimeSum += measureTimeMillis {
//        seqArray = seqArray.prefixSum()
//      }
//      parTimeSum += measureTimeMillis {
//        utils.parallelScanInplace(parArray, Int::times, threshold, pool)
//      }
//    }
//
//    println("parallel time: $parTimeSum")
//    println("sequential time: $seqTimeSum")
//    assertThat(parTimeSum * availableProcessors / 2).isLessThan(seqTimeSum)
//  }

  @Test
  fun parallelFilter() {
    val testRuns = 100

    for (i in 1..testRuns) {
      val size = abs(random.nextLong()) % 10000 + 1000
      val array = random.ints(size, -1000, 1000).toArray().toTypedArray()
      val average = array.average()
      val filterFunc: (Int) -> Int = { if (it > average) 1 else 0 }
      val filterFuncBool: (Int) -> Boolean = { it > average }
      val expected = array.filter { filterFuncBool(it) }.toTypedArray()

      val actual = utils.parallelFilter(array, filterFunc)
      assertThat(actual).containsExactly(*expected)
    }
  }

  @Test
  fun parallelFilter_empty() {
    val filterFunc: (Int) -> Int = { if (it > 1) 1 else 0 }
    val resultArray = utils.parallelFilter(emptyArray(), filterFunc)

    assertThat(resultArray).isEmpty()
  }

  private fun Array<Int>.prefixSum(): Array<Int> = runningFold(0, Int::plus).dropLast(1).toTypedArray()
}