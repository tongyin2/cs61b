
/**
 * Created by tongyin on 17/5/30.
 */
public class Tile implements Comparable<Tile>{
    public final String tileName;
    public final double tile_ullon;
    public final double tile_ullat;
    public final double tile_lrlon;
    public final double tile_lrlat;
    public Tile[] child;
    public int depth;

    public Tile(String name, double ullon, double ullat, double lrlon, double lrlat, int de) {
        tileName = name;
        tile_ullon = ullon;
        tile_ullat = ullat;
        tile_lrlon = lrlon;
        tile_lrlat = lrlat;
        child = new Tile[4];

        for (int i = 0; i < child.length; i++) {
            child[i] = null;
        }

        depth = de;
    }

    public double lonDPP() {
        return (tile_lrlon - tile_ullon) / MapServer.TILE_SIZE;
    }

    public boolean intersectsQuery(double query_ullon, double query_ullat,
                                   double query_lrlon, double query_lrlat) {

        if (tile_ullon < query_lrlon && tile_lrlon > query_ullon &&
                tile_ullat > query_lrlat && tile_lrlat < query_ullat) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public int compareTo(Tile o) {
        if (this.depth != o.depth) {
            throw new IllegalArgumentException("cannot compare tiles with different depths");
        }

        if (this.tile_ullat > o.tile_ullat) {
            return -1;
        }else if (this.tile_ullat < o.tile_ullat) {
            return 1;
        }else {
            if (this.tile_ullon < o.tile_ullon) {
                return -1;
            }else if (this.tile_ullon > o.tile_ullon) {
                return 1;
            }else {
                return 0;
            }
        }
    }
}
