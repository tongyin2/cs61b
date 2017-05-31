import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.
    private QuadTree imgQuadTree;
    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgRoot) {
        // YOUR CODE HERE
        imgQuadTree = new QuadTree("root", MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT,
                                    MapServer.ROOT_LRLON, MapServer.ROOT_LRLAT);
        buildTree(imgQuadTree.getRoot(), 7);
    }

    private void buildTree(Tile parentTile, int levelLeft) {

        if (levelLeft <= 0) {
            return;
        }

        double x1, y1, x2, y2;

        for (int i = 1; i < 5; i++) {
            if (i == 1) {
                x1 = parentTile.tile_ullon;
                y1 = parentTile.tile_ullat;
                x2 = (parentTile.tile_ullon + parentTile.tile_lrlon) / 2.0;
                y2 = (parentTile.tile_ullat + parentTile.tile_lrlat) / 2.0;
            }else if (i == 2) {
                x1 = (parentTile.tile_ullon + parentTile.tile_lrlon) / 2.0;
                y1 = parentTile.tile_ullat;
                x2 = parentTile.tile_lrlon;
                y2 = (parentTile.tile_ullat + parentTile.tile_lrlat) / 2.0;
            }else if (i == 3) {
                x1 = parentTile.tile_ullon;
                y1 = (parentTile.tile_ullat / 2.0 + parentTile.tile_lrlat / 2.0);
                x2 = (parentTile.tile_ullon + parentTile.tile_lrlon) / 2.0;
                y2 = parentTile.tile_lrlat;
            }else {
                x1 = (parentTile.tile_ullon + parentTile.tile_lrlon) / 2.0;
                y1 = (parentTile.tile_ullat + parentTile.tile_lrlat) / 2.0;
                x2 = parentTile.tile_lrlon;
                y2 = parentTile.tile_lrlat;
            }
            String nameTag;
            if (parentTile.tileName.equals("root")) {
                nameTag = String.valueOf(i);
            }else {
                nameTag = parentTile.tileName + String.valueOf(i);
            }
            Tile newChild = new Tile(nameTag, x1, y1, x2, y2, parentTile.depth + 1);
            parentTile.child[i - 1] = newChild;
            buildTree(newChild, levelLeft - 1);
        }
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #//REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        //System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        /*System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
                + "your browser.");*/

        /*
        results.put("raster_ul_lon", -122.2998046875);
        results.put("raster_ul_lat", 37.87484726881516);
        results.put("raster_lr_lon", -122.2119140625);
        results.put("raster_lr_lat", 37.82280243352756);
        results.put("depth", 2);
        results.put("query_success", true);

        String[][] grid= new String[][]{{"img/13.png", "img/14.png", "img/23.png", "img/24.png"},
        {"img/31.png", "img/32.png", "img/41.png", "img/42.png"},
        {"img/33.png", "img/34.png", "img/43.png", "img/44.png"}};

        results.put("render_grid", grid);
        */

        // declare instances to be mapped into results
        double raster_ullon, raster_ullat, raster_lrlon, raster_lrlat;
        int depth;
        boolean query_success;
        String[][] grid;
        ArrayList<Tile> li = new ArrayList<>();

        // get query info
        double query_ullon = params.get("ullon");
        double query_ullat = params.get("ullat");
        double query_lrlon = params.get("lrlon");
        double query_lrlat = params.get("lrlat");
        double query_w = params.get("w");

        if (query_ullon > query_lrlon || query_ullat < query_lrlat ||
                !imgQuadTree.getRoot().intersectsQuery(query_ullon, query_ullat, query_lrlon, query_lrlat)) {
            // if query window doesn't make sense or out of root's boundry, set it to false
            raster_ullon = MapServer.ROOT_ULLON;
            raster_ullat = MapServer.ROOT_ULLAT;
            raster_lrlon = MapServer.ROOT_LRLON;
            raster_lrlat = MapServer.ROOT_LRLAT;
            depth = 0;
            query_success = false;
            grid = new String[1][1];
        }else {
            checkTile(imgQuadTree.getRoot(), li, query_ullon, query_ullat, query_lrlon, query_lrlat, query_w);
            li.sort(null);

            //store values into results
            raster_ullon = li.get(0).tile_ullon;
            raster_ullat = li.get(0).tile_ullat;
            raster_lrlon = li.get(li.size() - 1).tile_lrlon;
            raster_lrlat = li.get(li.size() - 1).tile_lrlat;
            depth = li.get(0).depth;
            query_success = true;

            // put tile name into render_grid
            int row = (int) ((li.get(0).tile_ullat - li.get(li.size() - 1).tile_lrlat) /
                    (li.get(0).tile_ullat - li.get(0).tile_lrlat) + 0.1);
            int col = li.size() / row;
            grid = new String[row][col];
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    grid[i][j] = "img/" + li.get(i * col + j).tileName + ".png";
                }
            }
        }


        results.put("raster_ul_lon", raster_ullon);
        results.put("raster_ul_lat", raster_ullat);
        results.put("raster_lr_lon", raster_lrlon);
        results.put("raster_lr_lat", raster_lrlat);
        results.put("depth", depth);
        results.put("query_success", query_success);
        results.put("render_grid", grid);

        return results;
    }

    private void checkTile(Tile tile, ArrayList<Tile> li, double query_ullon, double query_ullat,
                           double query_lrlon, double query_lrlat, double query_w) {

        if (!tile.intersectsQuery(query_ullon, query_ullat, query_lrlon, query_lrlat)) {
            return;
        }
        double query_lonDPP = (query_lrlon - query_ullon) / query_w;
        if (tile.lonDPP() <= query_lonDPP || tile.depth == 7) {
            li.add(tile);
        }else {
            for (int i = 0; i < 4; i++) {
                checkTile(tile.child[i], li, query_ullon, query_ullat, query_lrlon, query_lrlat, query_w);
            }
        }
    }
    /*
    public static void main(String[] args) {
        Rasterer rs = new Rasterer("img/");
        Map<String, Double> para = new HashMap<>();
        para.put("ullon", -122.23995662778569);
        para.put("ullat", 37.877266154010954);
        para.put("lrlon", -122.22275132672245);
        para.put("lrlat", 37.85829260830337);
        para.put("w", 613.0);
        para.put("h", 676.0);
        rs.getMapRaster(para);
    }*/

}