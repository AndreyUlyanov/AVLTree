import java.lang.StringBuilder
import java.util.*
import kotlin.NoSuchElementException
import kotlin.math.max
import java.util.Stack


class AVLTree<T : Comparable<T>> : NavigableSet<T> {

    private var root: Node<T>? = null

    override var size = 0

    private class Node<T>(val value: T) {

        var left: Node<T>? = null

        var right: Node<T>? = null

        var parent: Node<T>? = null

        var height: Int = 1

        fun isLeaf() = right == null && left == null

        fun updateHeight() {
            height = max((right?.height ?: 0), (left?.height ?: 0)) + 1
        }

        fun getBalanceFactor() = (right?.height ?: 0) - (left?.height ?: 0)

        override fun toString() = "value = $value, height = $height, parent = ${parent?.value}, " +
                "left = ${left?.value}, right = ${right?.value}"
    }

    override fun add(element: T): Boolean {
        val closest = find(element)
        val comparison = if (closest == null) -1 else element.compareTo(closest.value)
        if (comparison == 0) {
            return false
        }
        val newNode = Node(element)
        newNode.parent = closest
        when {
            closest == null -> root = newNode
            comparison < 0 -> {
                assert(closest.left == null)
                closest.left = newNode
            }
            else -> {
                assert(closest.right == null)
                closest.right = newNode
            }
        }
        size++

        var updateNode = newNode.parent
        while (updateNode != null) {
            updateNode.updateHeight()
            updateNode = updateNode.parent
        }
        root?.updateHeight()

        balanceTree(newNode.parent)
        //root?.updateHeight()
        return true
    }

    private fun rotateRight(node: Node<T>) {
        val parent = node.parent
        val swapNode = node.left        // left child
        val child = swapNode?.right     // right child of left child

        swapNode?.right = node
        node.parent = swapNode
        node.left = child
        child?.parent = node

        if (parent == null) {
            this.root = swapNode
            swapNode?.parent = null
            return
        }

        swapNode?.parent = parent
        if (parent.left == node) parent.left = swapNode
        else parent.right = swapNode

        node.updateHeight()
        swapNode?.updateHeight()
    }

    private fun rotateLeft(node: Node<T>) {
        val parent = node.parent
        val swapNode = node.right       //right child
        val child = swapNode?.left      //left child of right child

        swapNode?.left = node
        node.parent = swapNode
        node.right = child
        child?.parent = node

        if (parent == null) {
            this.root = swapNode
            swapNode?.parent = null
            return
        }

        swapNode?.parent = parent
        if (parent.left == node) parent.left = swapNode
        else parent.right = swapNode

        node.updateHeight()
        swapNode?.updateHeight()
    }

    private fun rotateLeftThenRight(node: Node<T>) {
        node.left?.let { rotateLeft(it) }
        rotateRight(node)
    }

    private fun rotateRightThenLeft(node: Node<T>) {
        node.right?.let { rotateRight(it) }
        rotateLeft(node)
    }

    private fun balanceTree(node: Node<T>?) {
        if (node == null) return
        val difference = node.getBalanceFactor()
        val parent = node.parent
        if (difference == -2) {
            if (node.left?.left?.height ?: 0 >= node.left?.right?.height ?: 0)
                rotateRight(node)
            else rotateLeftThenRight(node)
        }
        else if (difference == 2) {
            if (node.right?.right?.height ?: 0 >= node.right?.left?.height ?: 0)
                rotateLeft(node)
            else rotateRightThenLeft(node)
        }

        if (parent != null) balanceTree(parent)

        node.updateHeight()
    }

    fun checkInvariant(): Boolean =
        root?.let { checkInvariant(it) } ?: true

    private fun checkInvariant(node: Node<T>): Boolean {
        val left = node.left
        if (left != null && (left.value >= node.value || !checkInvariant(left))) return false
        val right = node.right
        return right == null || right.value > node.value && checkInvariant(right)
    }

    fun height(): Int = root?.height ?: 0

    override fun remove(element: T): Boolean {
        val item = find(element)

        return if (item == null || element.compareTo(item.value) != 0) false
        else {
            size--
            val parent = item.parent

            when {
                item.left == null && item.right == null -> parent.swapElement(item, null)
                item.left == null -> parent.swapElement(item, item.right)
                item.right == null -> parent.swapElement(item, item.left)
                else -> {
                    var swapNode = item.left!!

                    while (swapNode.right != null) {
                        swapNode = swapNode.right!!
                    }

                    swapNode.parent.swapElement(swapNode, swapNode.left)

                    val newNode = Node(swapNode.value)

                    newNode.left = if (item.left?.value == swapNode.value) item.left?.left else item.left
                    newNode.right = item.right

                    parent.swapElement(item, newNode)
                }
            }
            balanceTree(parent ?: root)
            true
        }
    }

    private fun Node<T>?.swapElement(node: Node<T>, newNode: Node<T>?) {
        newNode?.parent = this

        when {
            this == null -> root = newNode
            this.left?.value?.compareTo(node.value) == 0 -> this.left = newNode
            else -> this.right = newNode
        }
    }

    override operator fun contains(element: T): Boolean {
        val closest = find(element)
        return closest != null && element.compareTo(closest.value) == 0
    }

    private fun find(value: T): Node<T>? =
        root?.let { find(it, value) }

    private fun find(start: Node<T>, value: T): Node<T> {
        val comparison = value.compareTo(start.value)
        return when {
            comparison == 0 -> start
            comparison < 0 -> start.left?.let { find(it, value) } ?: start
            else -> start.right?.let { find(it, value) } ?: start
        }
    }

    inner class BinaryTreeIterator internal constructor() : MutableIterator<T> {

        private var current: Node<T>? = null
        private var stack: Stack<Node<T>> = Stack()

        init {
            var node = root
            while (node != null) {
                stack.push(node)
                node = node.left
            }
        }

        override fun hasNext(): Boolean = stack.isNotEmpty()

        override fun next(): T {
            if (!hasNext()) throw NoSuchElementException()

            var node = stack.pop()
            current = node
            if (node.right != null) {
                node = node.right

                while (node != null) {
                    stack.push(node)
                    node = node.left
                }
            }
            return current!!.value
        }

        override fun remove() {
            remove(current?.value ?: return)
        }
    }

    override fun iterator(): MutableIterator<T> = BinaryTreeIterator()

    override fun comparator(): Comparator<in T>? = null

    override fun first(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.left != null) {
            current = current.left!!
        }
        return current.value
    }

    override fun last(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.right != null) {
            current = current.right!!
        }
        return current.value
    }

    override fun lower(element: T): T? {
        var node = root ?: return null
        var ans = node.value
         do {
             node = if (node.value > element) node.left ?: break else node.right ?: break

             if (root?.value == ans && node.value < element) ans = node.value
             if (node.value > ans && node.value < element) ans = node.value
         } while (!node.isLeaf())
        return if (ans < element) ans else null
    }

    override fun higher(element: T): T? {
        var node = root ?: return null
        var ans = node.value
        do {
            node = if (node.value > element) node.left ?: break else node.right ?: break

            if (root?.value == ans && node.value > element) ans = node.value
            if (node.value < ans && node.value > element) ans = node.value
        } while (!node.isLeaf())

        return if (ans > element) ans else null
    }

    override fun isEmpty(): Boolean = root == null

    fun info(): String {
        val queue = ArrayDeque<Node<T>?>()
        val ans = StringBuilder()
        queue.addFirst(root)
        while (queue.isNotEmpty()) {
            val element = queue.pollFirst()
            ans.append("${element?.toString()}\n")
            if (element?.left != null) queue.addLast(element.left)
            if (element?.right != null) queue.addLast(element.right)
        }
        //ans.append("${root?.value} ${root?.left?.value} ${root?.right?.value}")
        return ans.toString()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tailSet(fromElement: T, inclusive: Boolean): NavigableSet<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tailSet(fromElement: T): SortedSet<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun headSet(toElement: T, inclusive: Boolean): NavigableSet<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun headSet(toElement: T): SortedSet<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subSet(fromElement: T, fromInclusive: Boolean, toElement: T, toInclusive: Boolean): NavigableSet<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subSet(fromElement: T, toElement: T): SortedSet<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun descendingSet(): NavigableSet<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun descendingIterator(): MutableIterator<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun floor(e: T): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ceiling(e: T): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pollFirst(): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pollLast(): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
