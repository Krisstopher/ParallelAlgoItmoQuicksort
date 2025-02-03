import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveAction
import kotlin.random.Random

class ParQuicksort<T : Comparable<T>>(threads: Int, private val threshold: Int) : Quicksort<T>() {
  private val pool = ForkJoinPool(threads)
  private val seqQuicksort = SeqQuicksort<T>()
  private val random = Random(1792)

  override fun sort(a: Array<T>): Array<T> = a.copyOf().also {
    pool.invoke(StepTask(it, 0, it.size - 1))
  }

  inner class StepTask(
    private val array: Array<T>,
    private val leftIndex: Int,
    private val rightIndex: Int
  ) : RecursiveAction() {
    override fun compute() {
      if (rightIndex - leftIndex + 1 < threshold) {
        seqQuicksort.sort(array, leftIndex, rightIndex)
      } else if (leftIndex < rightIndex) {
        val pivotIndex = array.partition(leftIndex, rightIndex)
        val leftTask = StepTask(array, leftIndex, pivotIndex - 1)
        val rightTask = StepTask(array, pivotIndex + 1, rightIndex)

        invokeAll(leftTask, rightTask)
      }
    }
  }

  private fun Array<T>.partition(leftIndex: Int, rightIndex: Int): Int {
    val tempArray = this.sliceArray(leftIndex until rightIndex)
    val pivotIndex = random.nextInt(leftIndex, rightIndex)
    val pivot = this[pivotIndex]
    swap(pivotIndex, rightIndex)

    val size = tempArray.size
    val flags = BooleanArray(size)

    tempArray.indices.parallelFor {
      flags[it] = tempArray[it] <= pivot
    }

    val prefixSum = IntArray(size).apply {
      if (size == 0) return@apply
      this[0] = if (flags[0]) 1 else 0
      for (i in 1 until size) {
        this[i] = this[i - 1] + if (flags[i]) 1 else 0
      }
    }

    tempArray.indices.parallelFor {
      val newPos = if (flags[it]) prefixSum[it] - 1 else prefixSum.last() + it - prefixSum[it]
      tempArray[newPos] = this[it]
    }
    (leftIndex until rightIndex).parallelFor {
      this[it] = tempArray[it - leftIndex]
    }

    val pivotPos = leftIndex + prefixSum.last()
    swap(pivotPos, rightIndex)

    return pivotPos
  }

  private fun <E> Iterable<E>.parallelFor(action: (E) -> Unit) {
    toList().parallelStream().forEach { action(it) }
  }
}