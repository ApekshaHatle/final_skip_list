// One element (key) of the skip list.
public class Node {
    int key;
    int baseHeight;     // height given at insert time (random coin flips)
    int height;         // current height (can grow when the key is popular)
    double count;       // how popular this key is (used by the counter versions)
    boolean inTall;     // true if this key is currently taller than its base height
    Node[] next;        // next[i] = next node on level i

    Node(int key, int height, int maxLevel) {
        this.key = key;
        this.baseHeight = height;
        this.height = height;
        this.next = new Node[maxLevel];
    }
}
