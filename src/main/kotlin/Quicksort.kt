abstract class Quicksort<T : Comparable<T>> {
  abstract fun sort(a: Array<T>): Array<T>

  protected fun <T: Any> Array<T>.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
  }
}