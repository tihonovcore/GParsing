@Experimental
class FirstFollowWrapper(nonterminals: List<Char>) {
    private val map = mutableMapOf<Char, MutableSet<Char>>()
    private var changed = true

    init {
        nonterminals.forEach { map[it] = mutableSetOf() }
    }

    fun changed(): Boolean {
        return changed.also { changed = false }
    }

    operator fun get(ch: Char): Set<Char> {
        require(map.contains(ch))
        return map[ch]!!
    }

    operator fun set(key: Char, value: Set<Char>) {
        this.put(key, value)
    }

    operator fun set(key: Char, value: Char) {
        this.put(key, setOf(value))
    }

    private fun put(ch: Char, value: Set<Char>) {
        require(map.contains(ch))

        val previousSize = map[ch]!!.size
        map[ch]!! += value
        changed = changed || previousSize != map[ch]!!.size
    }

    fun getMap(): Map<Char, Set<Char>> {
        return map
    }
}
