import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AVLTreeTest{

    @Test
    fun bigTest() {
        val tree = AVLTree<Int>()
        tree.add(10)
        tree.add(7)
        tree.add(12)
        tree.add(5)
        tree.add(4)
        assertTrue(tree.size == 5)
        assertEquals(3, tree.height())
        assertEquals("value = 10, height = 3, parent = null, left = 5, right = 12\n" +
                "value = 5, height = 2, parent = 10, left = 4, right = 7\n" +
                "value = 12, height = 1, parent = 10, left = null, right = null\n" +
                "value = 4, height = 1, parent = 5, left = null, right = null\n" +
                "value = 7, height = 1, parent = 5, left = null, right = null\n", tree.info())
        tree.add(8)
        assertEquals("value = 7, height = 3, parent = null, left = 5, right = 10\n" +
                "value = 5, height = 2, parent = 7, left = 4, right = null\n" +
                "value = 10, height = 2, parent = 7, left = 8, right = 12\n" +
                "value = 4, height = 1, parent = 5, left = null, right = null\n" +
                "value = 8, height = 1, parent = 10, left = null, right = null\n" +
                "value = 12, height = 1, parent = 10, left = null, right = null\n", tree.info())
        tree.remove(7)
        assertEquals("value = 5, height = 3, parent = null, left = 4, right = 10\n" +
                "value = 4, height = 1, parent = 7, left = null, right = null\n" +
                "value = 10, height = 2, parent = 7, left = 8, right = 12\n" +
                "value = 8, height = 1, parent = 10, left = null, right = null\n" +
                "value = 12, height = 1, parent = 10, left = null, right = null\n", tree.info())
        assertEquals(12, tree.lower(13))
        assertEquals(5, tree.higher(4))
        assertEquals(null, tree.lower(2))
        assertEquals(8, tree.higher(7))
    }
}