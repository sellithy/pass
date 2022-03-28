import java.util.Properties

class GoodProperties : Properties() {
    operator fun get(key: String) = super.getProperty(key) ?: ""
}

val String.asProperties: GoodProperties
    get() = GoodProperties().apply {
        load(Main::class.java.getResource(this@asProperties)!!.openStream())
    }
