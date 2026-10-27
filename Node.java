
public class Node {
    int key;
    int baseHeight;     
    int height;         
    double count;       
    boolean inTall;     
    Node[] next;        

    Node(int key, int height, int maxLevel) {
        this.key = key;
        this.baseHeight = height;
        this.height = height;
        this.next = new Node[maxLevel];
    }
}
