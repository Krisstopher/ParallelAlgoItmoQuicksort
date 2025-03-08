import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.min

class ParallelUtils {
  private companion object {
    const val DEFAULT_THRESHOLD = 1000
  }

  fun parallelMapInplace(
    array: Array<Int>,
    function: (Int) -> Int,
    threshold: Int = DEFAULT_THRESHOLD,
    pool: ForkJoinPool = ForkJoinPool(),
  ): Array<Int> {
    val task = ParallelMapTask(array, function, 0, array.size - 1, threshold)
    pool.invoke(task)

    return array
  }

  fun parallelScanInplace(
    array: Array<Int>,
    function: (Int, Int) -> Int,
    threshold: Int = DEFAULT_THRESHOLD,
    pool: ForkJoinPool = ForkJoinPool(),
  ): Array<Int> {
    val n = array.size
    if (n == 0) return emptyArray()

    val logn = ceil(log2(n.toDouble())).toInt()
    val upSweepFuncCreator: (Int) -> ((Int) -> Unit) = { offset ->
      {
        val left = it + offset - 1
        val right = min(it + offset * 2 - 1, n - 1)
        array[right] = function(array[left], array[right])
      }
    }
    for (d in 0 until logn) {
      val offset = 1 shl d
      val func = upSweepFuncCreator.invoke(offset)
      pool.invoke(ParallelForTask(func, 0, n - offset, offset * 2, threshold))
    }

    array[n - 1] = 0

    val downSweepFuncCreator: (Int) -> ((Int) -> Unit) = { offset ->
      {
        val left = it + offset - 1
        val right = min(it + offset * 2 - 1, n - 1)
        val tmp = array[left]
        array[left] = array[right]
        array[right] = function(array[right], tmp)
      }
    }
    for (d in (logn - 1) downTo 0) {
      val offset = 1 shl d
      val func = downSweepFuncCreator.invoke(offset)
      pool.invoke(ParallelForTask(func, 0, n - offset, offset * 2, threshold))
    }

    return array
  }

  fun parallelFilter(
    array: Array<Int>,
    function: (Int) -> Int, // 0 = false, 1 = true
    threshold: Int = DEFAULT_THRESHOLD,
    pool: ForkJoinPool = ForkJoinPool(),
  ): Array<Int> {
    val n = array.size.also { if (it == 0) return emptyArray() }
    val resultSize = AtomicInteger(0)
    val filterWithSizeCntFunc: (Int) -> Int = {
      function(it).also { res -> if (res == 1) resultSize.incrementAndGet() }
    }
    val flags = parallelMapInplace(array.copyOf(), filterWithSizeCntFunc, threshold, pool) // :(
    val lastFlag = flags.last()
    parallelScanInplace(flags, Int::plus, threshold, pool)
    val result = Array(resultSize.get()) { 0 }

    val func: (Int) -> Unit = {
      val condition = if (it == n - 1) lastFlag == 1 else flags[it + 1] > flags[it]
      if (condition) result[flags[it]] = array[it]
    }
    pool.invoke(ParallelForTask(func, 0, n, 1, threshold))

    return result
  }

  private inner class ParallelMapTask(
    private val array: Array<Int>,
    private val function: (Int) -> Int,
    private val left: Int,
    private val right: Int,
    private val threshold: Int,
  ) : RecursiveTask<Unit>() {
    override fun compute() {
      if (right - left < threshold) {
        for (i in left..right) {
          array[i] = function(array[i])
        }
      } else {
        val mid = (left + right) / 2
        val left = ParallelMapTask(array, function, left, mid, threshold)
        val right = ParallelMapTask(array, function, mid + 1, right, threshold)
        invokeAll(left, right)
      }
    }
  }

  private inner class ParallelForTask(
    private val function: (Int) -> Unit,
    private val left: Int,
    private val right: Int,
    private val step: Int,
    private val threshold: Int,
  ) : RecursiveTask<Unit>() {
    override fun compute() {
      if (left >= right) return

      val count = (right - left + step - 1) / step

      if (count < threshold) {
        for (i in left until right step step) function(i)
      } else {
        val mid = left + (count / 2) * step
        val left = ParallelForTask(function, left, mid, step, threshold)
        val right = ParallelForTask(function, mid, right, step, threshold)
        invokeAll(left, right)
      }
    }
  }
}