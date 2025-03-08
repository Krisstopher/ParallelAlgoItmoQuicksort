import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveAction
import kotlin.random.Random
import kotlin.random.nextInt

class ParQuicksortWithFilter(threads: Int, private val threshold: Int) : Quicksort<Int>() {
  private val utils = ParallelUtils()
  private val pool = ForkJoinPool(threads)
  private val seqQuicksort = SeqQuicksort<Int>()
  private val random = Random(1792)

  override fun sort(a: Array<Int>): Array<Int> = a.copyOf().also {
    pool.invoke(StepTask(it, 0, it.size - 1))
  }

  inner class StepTask(
    private val array: Array<Int>,
    private val leftIndex: Int,
    private val rightIndex: Int
  ) : RecursiveAction() {
    override fun compute() {
      if (rightIndex - leftIndex < threshold && leftIndex < rightIndex) {
        val sorted = seqQuicksort.sort(array, leftIndex, rightIndex)
        for (index in leftIndex..rightIndex) {
          array[index] = sorted[index]
        }
      } else if (leftIndex < rightIndex) {
        val pivot = array[random.nextInt(leftIndex until rightIndex)]
        val leftFunc: (Int) -> Int = { if (it < pivot) 1 else 0 }
        val eqFunc: (Int) -> Int = { if (it == pivot) 1 else 0 }
        val rightFunc: (Int) -> Int = { if (it > pivot) 1 else 0 }
        val arraySlice = array.sliceArray(leftIndex..rightIndex)
        val left = utils.parallelFilter(arraySlice, leftFunc, threshold, pool)
        val eq = utils.parallelFilter(arraySlice, eqFunc, threshold, pool)
        val right = utils.parallelFilter(arraySlice, rightFunc, threshold, pool)

        (left + eq + right).forEachIndexed { index, element ->
          array[index + leftIndex] = element
        }

        val leftTask = StepTask(array, leftIndex, leftIndex + left.size - 1)
        val rightTask = StepTask(array, rightIndex - right.size + 1, rightIndex)

        invokeAll(leftTask, rightTask)
      }
    }
  }
}