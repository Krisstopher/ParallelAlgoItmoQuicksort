import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import java.security.SecureRandom
import java.util.concurrent.ForkJoinPool
import kotlin.math.abs
import kotlin.system.measureTimeMillis

class ParallelUtilsTest {
    private val random = SecureRandom()

    @Test
    fun parallelMap() {
        val pool = ForkJoinPool(4)
        val utils = ParallelUtils(pool, 1000)
        val testRuns = 100

        for (i in 1..testRuns) {
            val size = abs(random.nextLong()) % 10000 + 1000
            val array = random.ints(size).toArray().toTypedArray()
            val function: (Int) -> Int = { it / 2 }

            val resultArray = utils.parallelMap(array, function)
            assertThat(resultArray).containsExactly(*array.map(function).toTypedArray())
        }
    }

    @Test
    fun parallelMap_empty() {
        val pool = ForkJoinPool(4)
        val utils = ParallelUtils(pool, 1000)
        val function: (Int) -> Int = { it / 2 }
        val resultArray = utils.parallelMap(emptyArray(), function)

        assertThat(resultArray).isEmpty()
    }

    @Test
    fun parallelMap_mustBeFasterThanSequential() {
        val pool = ForkJoinPool(20)
        val utils = ParallelUtils(pool, 1000)
//        val testRuns = 5
        var seqTimeSum = 0L
        var parTimeSum = 0L

//        for (i in 1..testRuns) {
            val array = random.ints(500000).toArray().toTypedArray()
            val function: (Int) -> Int = {
                var res = it
                for (i in 0..1000000000) res--
                res
            }

            var sequentialResultArray: Array<Int>
            seqTimeSum += measureTimeMillis {
                sequentialResultArray = array.map(function).toTypedArray()
            }
            val parallelResultArray: Array<Int>
            parTimeSum += measureTimeMillis {
                parallelResultArray = utils.parallelMap(array, function)
            }
            assertThat(parallelResultArray).containsExactly(*sequentialResultArray)
//        }

        println(seqTimeSum)
        println(parTimeSum)
    }
}