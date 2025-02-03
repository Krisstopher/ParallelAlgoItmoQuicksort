import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask

class ParallelUtils(private val pool: ForkJoinPool, private val threshold: Int) {
  fun <T, R> Array<T>.parallelMap(function: (T) -> R): List<R> {
    if (isEmpty()) return emptyList()

    val output = arrayOfNulls<R>(size)
    val task = ParallelMapTask(this, output, function, 0, size)

    pool.invoke(task)

    return output.toList()
  }

  fun <T, R> Array<T>.parallelScan(function: (T, T) -> R): Array<R> {

  }

  fun <T> Array<T>.parallelFilter(function: (T) -> Boolean): Array<T> {

  }

  private inner class ParallelMapTask<T, R>(
    private val inArray: Array<T>,
    private val outArray: Array<R>,
    private val function: (T) -> R,
    private val leftIndex: Int,
    private val rightIndex: Int
  ) : RecursiveTask<Unit>() {
    override fun compute() {
      if (rightIndex - leftIndex <= threshold) {
        for (i in leftIndex until rightIndex) {
          outArray[i] = function(inArray[i])
        }
      } else {
        val mid = (leftIndex + rightIndex) / 2
        val left = ParallelMapTask(inArray, outArray, function, leftIndex, mid)
        val right = ParallelMapTask(inArray, outArray, function, mid, rightIndex)
        invokeAll(left, right)
      }
    }
  }
}