package icu.twtool.chat.cache

/**
 * 最近最少使用(Least Recently Used)缓存
 */
class LruCache<K, V>(private val capacity: Int) {

    /**
     * 使查询的复杂度为 O(1)
     */
    private val map = HashMap<K, DoublyLinkedList<K, V>.Node<K, V>>()

    /**
     * 使插入删除的复杂度为 O(1)
     */
    private val list = DoublyLinkedList<K, V>()

    /**
     * 添加 LruCache 的值，保证不超出容量
     *
     * TODO: 使其线程安全
     */
    fun put(key: K, value: V) {
        val node = map[key]
        if (node != null) list.remove(node) // 如果存在则删除之前的值
        else if (map.size == capacity) list.removeLast()?.let { map.remove(it.key) } // 删除最近最少使用的值

        map[key] = list.addFirst(key, value)
    }

    /**
     * 获取缓存值
     */
    fun get(key: K): V? {
        val value = map[key] ?: return null
        list.remove(value)
        list.addFirst(value.key!!, value.value!!)
        return value.value
    }
}

/**
 * 双向链表
 */
private class DoublyLinkedList<K, V> {

    private val head = Node<K, V>()
    private val tail = Node<K, V>()

    init {
        head.next = tail
        tail.prev = head
    }

    fun addFirst(key: K, value: V): Node<K, V> {
        val node = Node(key, value, head, head.next)
        head.next = node
        return node
    }

    fun remove(value: Node<K, V>) {
        value.prev?.let { it.next = value.next }
        value.next?.let { it.prev = value.prev }

        value.prev = null
        value.next = null
    }

    fun removeLast(): Node<K, V>? {
        if (tail.prev == head) return null
        val node = tail.prev
        node?.let { remove(it) }
        return node
    }

    inner class Node<K, V>(
        val key: K? = null,
        var value: V? = null,
        var prev: Node<K, V>? = null,
        var next: Node<K, V>? = null
    )
}