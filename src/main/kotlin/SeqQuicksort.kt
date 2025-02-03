import kotlin.random.Random
import kotlin.random.nextInt

class SeqQuicksort<T: Comparable<T>> : Quicksort<T>() {
  private val random = Random(1792)

  override fun sort(a: Array<T>): Array<T> = a.copyOf().also { it.step(0, it.size - 1) }

  fun sort(a: Array<T>, leftIndex: Int, rightIndex: Int): Array<T> = a.copyOf().also { it.step(leftIndex, rightIndex) }

  private fun Array<T>.step(leftIndex: Int, rightIndex: Int) {
    if (leftIndex < rightIndex) {
      val pi = partition(leftIndex, rightIndex)
      step(leftIndex, pi - 1)
      step(pi + 1, rightIndex)
    }
  }

  private fun Array<T>.partition(leftIndex: Int, rightIndex: Int): Int {
    val pivotIndex = random.nextInt(leftIndex..rightIndex)
    val pivot = this[pivotIndex]
    var i = leftIndex - 1

    swap(pivotIndex, rightIndex)

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