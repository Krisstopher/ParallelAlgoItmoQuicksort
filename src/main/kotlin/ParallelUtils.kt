import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask

@Suppress("UNCHECKED_CAST")
class ParallelUtils(val pool: ForkJoinPool, private val threshold: Int) {
  inline fun <T, reified R> parallelMap(array: Array<T>, noinline function: (T) -> R): Array<R> {
    if (array.isEmpty()) return emptyArray<R>()

    val output = java.lang.reflect.Array.newInstance(R::class.java, array.size) as Array<R>
    val task = ParallelMapTask(array, output, function, 0, array.size - 1)

    pool.invoke(task)

    return output
  }

//  fun <T, R> Array<T>.parallelScan(function: (T, T) -> R): Array<R> {
//
//  }
//
//  fun <T> Array<T>.parallelFilter(function: (T) -> Boolean): Array<T> {
//
//  }

  inner class ParallelMapTask<T, R>(
    private val inArray: Array<T>,
    private val outArray: Array<R>,
    private val function: (T) -> R,
    private val leftIndex: Int,
    private val rightIndex: Int
  ) : RecursiveTask<Unit>() {
    override fun compute() {
      if (rightIndex - leftIndex < threshold) {
        for (i in leftIndex..rightIndex) {
          outArray[i] = function(inArray[i])
        }
      } else {
        val mid = (leftIndex + rightIndex) / 2
        val left = ParallelMapTask(inArray, outArray, function, leftIndex, mid)
        val right = ParallelMapTask(inArray, outArray, function, mid + 1, rightIndex)
        invokeAll(left, right)
      }
    }
  }
}