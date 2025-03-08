import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.security.SecureRandom
import kotlin.math.abs

class QuicksortTest {
  private val random = SecureRandom()

  @Test
  fun testSeq() {
    val testRuns = 1000

    for (i in 1..testRuns) {
      val size = abs(random.nextLong()) % 1000
      val array = random.doubles(size).toArray().toTypedArray()
      val quicksort = SeqQuicksort<Double>()

      val resultArray = quicksort.sort(array)
      assertThat(resultArray).isSorted
    }
  }

  @Test
  fun testPar() {
    val testRuns = 1000

    for (i in 1..testRuns) {
      val size = abs(random.nextLong()) % 10000 + 1000
      val array = random.doubles(size).toArray().toTypedArray()
      val quicksort = ParQuicksort<Double>(5, 10)

      val resultArray = quicksort.sort(array)
      assertThat(resultArray).isSorted
    }
  }

  @Test
  fun testParWithFilter() {
    val testRuns = 1000

    for (i in 1..testRuns) {
      val size = abs(random.nextLong()) % 10000 + 1000
      val array = random.ints(size).toArray().toTypedArray()
      val quicksort = ParQuicksortWithFilter(5, 4)

      val resultArray = quicksort.sort(array)
      assertThat(resultArray).isSorted
    }
  }
}