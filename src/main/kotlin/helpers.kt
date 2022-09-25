import kotlin.random.Random

val randomAliasPattern = "Random([0-9]+)".toRegex()

fun <T> choices(list: List<T>, k: Int = 1) =
    (0 until k).map { list.random() }

val Int.digitChar get() =
    if (this in 0..9)
        (this + '0'.code).toChar()
    else
        throw IllegalArgumentException("$this is not a single digit")

fun generateRandomPassword() = mutableListOf<Char>().run {
    val strangeChars = listOf('$', '#', '!', '@', '%', '^')
    val numbers = (0..9).map { it.digitChar }
    val asciiLetters = ('a'..'z').toList() + ('A'..'Z').toList()

    plusAssign(choices(strangeChars, Random.nextInt(from = 3, until = 6)))
    plusAssign(choices(numbers, Random.nextInt(from = 2, until = 5)))
    plusAssign(choices(asciiLetters, 16 - size))
    shuffle()
    toCharArray().concatToString()
}