import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveAction

class ParQuicksort<T : Comparable<T>>(threads: Int, private val threshold: Int) : Quicksort<T>() {
  private val pool = ForkJoinPool(threads)

  override fun sort(a: Array<T>): Array<T> = a.copyOf().also { pool.invoke(StepTask(it, 0, it.size - 1)) }

  inner class StepTask(
    private val array: Array<T>,
    private val leftIndex: Int,
    private val rightIndex: Int
  ) : RecursiveAction() {
    override fun compute() {
      if (rightIndex - leftIndex + 1 < threshold) {
        array.seqStep(leftIndex, rightIndex)
      } else if (leftIndex < rightIndex) {
        val pi = array.partition(leftIndex, rightIndex)
        val leftTask = StepTask(array, leftIndex, pi - 1)
        val rightTask = StepTask(array, pi + 1, rightIndex)

        invokeAll(leftTask, rightTask)
      }
    }
  }

  private fun Array<T>.seqStep(leftIndex: Int, rightIndex: Int) {
    if (leftIndex < rightIndex) {
      val pi = partition(leftIndex, rightIndex)
      seqStep(leftIndex, pi - 1)
      seqStep(pi + 1, rightIndex)
    }
  }

  private fun Array<T>.partition(leftIndex: Int, rightIndex: Int): Int {
    val pivot = this[rightIndex]
    var i = leftIndex - 1

    for (j in leftIndex until rightIndex) {
      if (this[j] <= pivot) {
        i++
        swap(i, j)
      }
    }
    swap(i + 1, rightIndex)

    return i + 1
  }
}