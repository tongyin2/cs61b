
/**
 * Created by tongyin on 17/5/28.
 */
public class QuadTree {

    private Tile root;

    public QuadTree(String name, double ullon, double ullat, double lrlon, double lrlat) {
        root = new Tile(name, ullon, ullat, lrlon, lrlat, 0);
    }

    public Tile getRoot() {
        return root;
    }
}
