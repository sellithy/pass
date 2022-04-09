fun <T> choices(list: List<T>, k: Int = 1) =
    (0 until k).map { list.random() }

val Int.digitChar get() =
    if (this in 0..9)
        (this + '0'.code).toChar()
    else
        throw IllegalArgumentException("$this is not a single digit")
