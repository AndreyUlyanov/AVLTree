import java.lang.StringBuilder
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.AbstractSet
import kotlin.math.max


open class AVLTree<T : Comparable<T>> : AbstractSet<T>(), NavigableSet<T> {

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

        node.left?.parent = newNode
        node.right?.parent = newNode

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

    open inner class BinaryTreeIterator internal constructor() : MutableIterator<T> {

        private val iter: Queue<Node<T>> = LinkedList()
        private var current: Node<T>? = null

        init {
            fun generateIterator(state: Node<T>) {
                if (state.left != null) generateIterator(state.left!!)
                iter.offer(state)
                if (state.right != null) generateIterator(state.right!!)
            }

            if (root != null) {
                generateIterator(root!!)
                current = iter.peek()
            }
        }

        override fun hasNext(): Boolean = iter.isNotEmpty()

        override fun next(): T {
            current = iter.poll()
            if (current == null) throw NoSuchElementException()
            return current!!.value
        }

        override fun remove() {
            remove(current?.value)
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
             node = if (node.value >= element) node.left ?: break else node.right ?: break

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
        if (root == null) return "Empty tree"
        val queue = ArrayDeque<Node<T>?>()
        val ans = StringBuilder()
        queue.addFirst(root)
        while (queue.isNotEmpty()) {
            val element = queue.pollFirst()
            ans.append("${element?.toString()}\n")
            if (element?.left != null) queue.addLast(element.left)
            if (element?.right != null) queue.addLast(element.right)
        }
        return ans.toString()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var ans = true
        for (element in elements) {
            if (ans) ans = add(element) else add(element)
        }
        return ans
    }

    override fun clear() {
        root = null
        size = 0
    }

    override fun tailSet(fromElement: T, inclusive: Boolean): NavigableSet<T> =
        SubTree(fromElement, inclusive, null, false, this)


    override fun tailSet(fromElement: T): SortedSet<T> =
        SubTree(fromElement, true, null, false, this)

    override fun removeAll(elements: Collection<T>): Boolean {
        var ans = true
        for (element in elements) {
            if (ans) ans = remove(element) else remove(element)
        }
        return ans
    }

    override fun headSet(toElement: T, inclusive: Boolean): NavigableSet<T> =
        SubTree(null, false, toElement, inclusive, this)

    override fun headSet(toElement: T): SortedSet<T> =
        SubTree(null, false, toElement, false, this)

    override fun subSet(fromElement: T, fromInclusive: Boolean, toElement: T, toInclusive: Boolean): NavigableSet<T> =
        SubTree(fromElement, fromInclusive, toElement, toInclusive, this)

    override fun subSet(fromElement: T, toElement: T): SortedSet<T> =
        SubTree(fromElement, true, toElement, false, this)

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
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

    override fun floor(element: T): T? {
        var node = root ?: return null
        var ans = node.value
        do {
            node = if (node.value >= element) node.left ?: break else node.right ?: break

            if (node.value == element) return element

            if (root?.value == ans && node.value < element) ans = node.value
            if (node.value > ans && node.value < element) ans = node.value
        } while (!node.isLeaf())
        return if (ans < element) ans else null
    }

    override fun ceiling(element: T): T? {
        var node = root ?: return null
        var ans = node.value
        do {
            node = if (node.value > element) node.left ?: break else node.right ?: break

            if (node.value == element) return element

            if (root?.value == ans && node.value > element) ans = node.value
            if (node.value < ans && node.value > element) ans = node.value
        } while (!node.isLeaf())

        return if (ans > element) ans else null
    }

    override fun pollFirst(): T? {
        if (this.size == 0) return null
        val element = this.first()
        remove(element)
        return element
    }

    override fun pollLast(): T? {
        if (this.size == 0) return null
        val element = this.last()
        remove(element)
        return element
    }

    private inner class SubTree(lowBound: T?, lowInclusive: Boolean,
                                upBound: T?, upInclusive: Boolean, parentTree: AVLTree<T>): AVLTree<T>() {
        private val lowerBound = lowBound
        private val lowerInclusive = lowInclusive
        private val upperBound = upBound
        private val upperInclusive = upInclusive
        private var subTreeSize = 0
        private val parent = parentTree

        private fun T.valid() =
            (lowerBound == null || (this > lowerBound || (lowerInclusive && this == lowerBound)))
                    && (upperBound == null || (this < upperBound || (upperInclusive && this == upperBound)))

        override operator fun contains(element: T): Boolean =
            element.valid() && parent.contains(element)

        override fun add(element: T): Boolean {
            require(element.valid())
            return parent.add(element)
        }

        override fun remove(element: T): Boolean {
            require(element.valid())
            return parent.remove(element)
        }

        override var size: Int
            get() = this.count()
            set(value) {
                subTreeSize = value
            }

        private inner class SubTreeIterator internal constructor() : BinaryTreeIterator() {
            private val iter: Queue<Node<T>> = LinkedList()
            private var current: Node<T>? = null

            private fun T.valid() =
                ((lowerBound == null || this >= lowerBound) && (upperBound == null || this < upperBound))

            init {
                fun generateIterator(state: Node<T>) {
                    if (state.left != null && state.left!!.value.valid())
                        generateIterator(state.left!!)

                    if (state.value.valid()) iter.offer(state)

                    if (state.right != null && state.right!!.value.valid())
                        generateIterator(state.right!!)
                }

                if (root != null) {
                    generateIterator(root!!)
                    current = iter.peek()
                }
            }

            override fun hasNext() = iter.isNotEmpty()

            override fun next(): T {
                current = iter.poll()
                if (current == null) throw NoSuchElementException()
                return current!!.value
            }

            override fun remove() {
                parent.remove(current?.value)
            }
        }

        override fun iterator(): MutableIterator<T> = SubTreeIterator()

        override fun isEmpty(): Boolean = size == 0

        override fun addAll(elements: Collection<T>): Boolean {
            var ans = true
            for (element in elements) {
                if (!element.valid()) {
                    ans = false
                    continue
                }
                if (ans) ans = add(element) else add(element)
            }
            return ans
        }

        override fun tailSet(fromElement: T, inclusive: Boolean): NavigableSet<T> {
            return when {
                lowerBound == null || lowerBound < fromElement ->
                    if (upperBound == null) parent.tailSet(fromElement, inclusive)
                    else parent.subSet(fromElement, inclusive, upperBound, upperInclusive)
                lowerBound == fromElement ->
                    if (upperBound == null) parent.tailSet(fromElement, lowerInclusive && inclusive)
                    else parent.subSet(fromElement, inclusive && lowerInclusive, upperBound, upperInclusive)
                else ->
                    if (upperBound == null) parent.tailSet(lowerBound, lowerInclusive)
                    else parent.subSet(lowerBound, lowerInclusive, upperBound, upperInclusive)
            }
        }

        override fun tailSet(fromElement: T): SortedSet<T> =
            this.tailSet(fromElement, true)

        override fun removeAll(elements: Collection<T>): Boolean {
            var ans = true
            for (element in elements) {
                if (!element.valid()) {
                    ans = false
                    continue
                }
                if (ans) ans = remove(element) else remove(element)
            }
            return ans
        }

        override fun headSet(toElement: T, inclusive: Boolean): NavigableSet<T> {
            return when {
                upperBound == null || upperBound > toElement ->
                    if (lowerBound == null) parent.headSet(toElement, inclusive)
                    else parent.subSet(lowerBound, lowerInclusive, toElement, inclusive)
                upperBound == toElement ->
                    if (lowerBound == null) parent.headSet(toElement, inclusive && upperInclusive)
                    else parent.subSet(lowerBound, lowerInclusive, toElement, inclusive && upperInclusive)
                else ->
                    if (lowerBound == null) parent.headSet(upperBound, upperInclusive)
                    else parent.subSet(lowerBound, lowerInclusive, upperBound, upperInclusive)
            }
        }

        override fun headSet(toElement: T): SortedSet<T> = headSet(toElement, false)

        override fun subSet(fromElement: T, fromInclusive: Boolean, toElement: T, toInclusive: Boolean): NavigableSet<T> {
            val start = when {
                lowerBound == null || lowerBound <= fromElement -> fromElement
                else -> lowerBound
            }
            val startInclusive = when {
                lowerBound == null || lowerBound < fromElement -> fromInclusive
                lowerBound == fromElement -> fromInclusive && lowerInclusive
                else -> lowerInclusive
            }
            val end = when {
                upperBound == null || upperBound >= toElement -> toElement
                else -> upperBound
            }
            val endInclusive = when {
                upperBound == null || upperBound > fromElement -> fromInclusive
                upperBound == fromInclusive -> upperInclusive && fromInclusive
                else -> upperInclusive
            }
            return parent.subSet(start, startInclusive, end, endInclusive)
        }

        override fun subSet(fromElement: T, toElement: T): SortedSet<T> =
            subSet(fromElement, true, toElement, false)

        override fun containsAll(elements: Collection<T>): Boolean {
            return elements.all { contains(it) }
        }

        override fun first(): T {
            var current: Node<T>? = root ?: throw NoSuchElementException()
            var ans: Node<T>? = null
            while (current != null) {
                if (current.value.valid()) {
                    ans = current
                    current = current.left
                }
                else current = current.right
            }
            if (ans != null) return ans.value
            else throw NoSuchElementException()
        }

        override fun last(): T {
            var current: Node<T>? = root ?: throw NoSuchElementException()
            var ans: Node<T>? = null
            while (current != null) {
                if (current.value.valid()) {
                    ans = current
                    current = current.right
                }
                else current = current.left
            }
            if (ans != null) return ans.value
            else throw NoSuchElementException()
        }

        override fun lower(element: T): T? {
            if (lowerBound != null && element <= lowerBound) return null
            if (upperBound != null && element >= upperBound) return this.last()
            var node = root ?: return null
            var ans = node.value
            do {
                node = if (node.value >= element) node.left ?: break else node.right ?: break

                if (root?.value == ans && node.value < element && node.value.valid()) ans = node.value
                if (node.value > ans && node.value < element && node.value.valid()) ans = node.value
            } while (!node.isLeaf())
            return if (ans.valid() &&(ans < element || ans == root!!.value)) ans else null
        }

        override fun higher(element: T): T? {
            if (lowerBound != null && element <= lowerBound) return this.first()
            if (upperBound != null && element >= upperBound) return null
            var node = root ?: return null
            var ans = node.value
            do {
                node = if (node.value > element) node.left ?: break else node.right ?: break

                if (root?.value == ans && node.value > element && node.value.valid()) ans = node.value
                if (node.value < ans && node.value > element && node.value.valid()) ans = node.value
            } while (!node.isLeaf())
            return if (ans.valid() && (ans > element || ans == root!!.value)) ans else null
        }

        override fun floor(element: T): T? {
            if (lowerBound != null && (element < lowerBound || (element == lowerBound && !lowerInclusive))) return null
            if (upperBound != null && (element > upperBound || (element == upperBound && !upperInclusive))) return this.last()
            var node = root ?: return null
            var ans = node.value
            do {
                node = if (node.value >= element) node.left ?: break else node.right ?: break

                if (node.value == element && element.valid()) return element

                if (root?.value == ans && node.value < element && node.value.valid()) ans = node.value
                if (node.value > ans && node.value < element && node.value.valid()) ans = node.value
            } while (!node.isLeaf())
            return if (ans.valid() && (ans < element || ans == root!!.value)) ans else null
        }

        override fun ceiling(element: T): T? {
            if (lowerBound != null && (element < lowerBound || (element == lowerBound && !lowerInclusive))) return this.first()
            if (upperBound != null && (element > upperBound || (element == upperBound && !upperInclusive))) return null
            var node = root ?: return null
            var ans = node.value
            do {
                node = if (node.value > element) node.left ?: break else node.right ?: break

                if (node.value == element && element.valid()) return element

                if (root?.value == ans && node.value > element && node.value.valid()) ans = node.value
                if (node.value < ans && node.value > element && node.value.valid()) ans = node.value
            } while (!node.isLeaf())
            return if (ans.valid() && (ans > element || ans == root!!.value)) ans else null
        }

        override fun pollFirst(): T? {
            if (this.size == 0) return null
            val element = this.first()
            remove(element)
            return element
        }

        override fun pollLast(): T? {
            if (this.size == 0) return null
            val element = this.last()
            remove(element)
            return element
        }

    }
}
