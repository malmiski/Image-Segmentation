import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Takes an image and segments it into K regions
 * and outputs the image and region info
 */
public class Decomposor extends JPanel
{
    //
    // Task 2: Get a set of neighboring regions  (10%)
    //
    // Given a disjoint set and a region (defined by its root id),
    // return a list of adjacent regions (again, represented by their root ids)
    //
    private TreeSet<Integer> getNeightborSets(DisjointSets<Pixel> ds, int root)
    {
        // Get the pixels that the root holds
        Set<Pixel> pixels = ds.get(root);
        TreeSet<Integer> adjacentRegions = new TreeSet<>();
        ArrayList<Pixel> neighbors;
        int neighbor_root;

        // Go through each of the pixels in the set
        for(Pixel pixel : pixels){

            // Get the neighboring pixels of this pixel
            neighbors = getNeightbors(pixel);
            // Go through each neighbor and see if it is a part of another set
            for(Pixel neighbor : neighbors){
                neighbor_root = ds.find(getID(neighbor));
                if(adjacentRegions.contains(neighbor_root)){
                    continue;
                }
                if(neighbor_root != root) {
                    // if it is then add the root of this neighbor's set to the adjacentRegions set
                    adjacentRegions.add(neighbor_root);
                }
            }
        }
      return adjacentRegions;
    }

    //
    // Task 3: Compute region to region similarity (10%)
    //
    // Given two regions R1 and R2, compute the similarity between these two regions
    // You will need to compute the average color, C, of the union of these two regions
    // And compute the sum of the color differences between C and all pixels in R1 and R2
    //

    private Similarity getSimilarity(DisjointSets<Pixel> ds, int root1, int root2)
    {
        // Get the pixels represented by these roots
        Pixel p = data.get(root1);//getPixel(root1);
        Pixel q = data.get(root2);//getPixel(root2);

        // Get the regions associated with each root
        Set<Pixel> region1 = ds.get(root1);
        Set<Pixel> region2 = ds.get(root2);
        // Get the average colors of each region
        Color averageColor1 = averageColors[p.p][p.q];
        Color averageColor2 = averageColors[q.p][q.q];
        // Compute the combined size of the regions
        int combined_size = (region1.size() + region2.size());
        // Compute the average color of these regions if they were unioned
        Color averageColor = new Color(((averageColor1.getRed() * region1.size()) + (averageColor2.getRed()*region2.size()))/combined_size,
                                            ((averageColor1.getGreen() * region1.size()) + (averageColor2.getGreen()*region2.size()))/combined_size,
                                            ((averageColor1.getBlue() * region1.size()) + (averageColor2.getBlue()*region2.size()))/combined_size);

        int difference = 0;
//        Color previousColor = colors[p.p][p.q];
//        int size = 0;
        // Get differences between averageColor and region1
        for (Pixel pixel : region1) {
                difference += getDifference(averageColor, colors[pixel.p][pixel.q]);//getColor(pixel));
        }

        // Do the same for region 2
        for (Pixel pixel : region2) {
                difference += getDifference(averageColor, colors[pixel.p][pixel.q]);//getColor(pixel));
        }
        return new Similarity(difference, p, q);
    }
    private Color colors[][];
    private Color averageColors[][];
    private ArrayList<Pixel> data;
    //
    // Task 4. Imeplement the decomposor (50%)
    //
    // High-level idea

    // - Iteratively merging two adjacent regions with most similar colors until the number of regions is K.
    //
    public void segment(int K) //K is the number of desired segments
    {
      if(K<2)
      {
          throw new IllegalArgumentException(new String("! Error: K should be greater than 1, current K="+K));
      }


      int width = this.image.getWidth();
      int height = this.image.getHeight();
      //Todo: Your code here (remove this line)
        // If K is too big return
      if(K > width*height){
          return;
      }

        data = new ArrayList<>(height*width);
        // Initialize the data for the disjoint sets
        for(int h = 0; h<height; h++) {
            for(int w = 0; w<width; w++){
                data.add(new Pixel(w, h));
            }
        }
        ds = new DisjointSets<>(data);

        // Initialize the priority queue for all the different similarities
        PriorityQueue<Similarity> priorityQueue = new PriorityQueue<Similarity>();
        // Go through each pixel first by height then by width and get
        // the neighboring sets for that pixel and then add the similarities for that
        // pixel and neighbor
        Similarity similarity;

        for(int i = 0; i<height; i++) {
            for(int j = 0; j<width; j++) {
                for (Pixel neighbor : getNeightbors(data.get(width*i + j))) {
                    priorityQueue.add(getSimilarity(ds, width*i + j, getID(neighbor)));
                }
            }
        }
        // Get the neighboring pixels
        TreeSet<Integer> neighbors;
        // Start at the top right pixel
        int currentRegion = 0;
        // Until ds gets to K regions we won't stop
        Similarity nearest = null;
        int root_1 = -1;
        int root_2 = -1;
        int p_id = -1;
        int q_id = -1;
        // Loop until we have K sets in the disjoint set
        while(ds.getNumSets() != K){
            nearest = priorityQueue.remove();
            // Get the ids of the pixels of the similarity
            p_id = getID(nearest.pixels.p);
            q_id = getID(nearest.pixels.q);
            // Get the roots of these pixels, if they are in a another set
            root_1 = ds.find(p_id);
            root_2 = ds.find(q_id);
            // If the pixels are in the same set ignore this pair
            if(root_1 == root_2){
                continue;
            }
            // Other wise if one of the pixels is not a root of its set
            if(root_1 != p_id || root_2 != q_id){
                // Get the similarity between the two roots
                //Similarity similarity = getSimilarity(ds, root_1, root_2);
                // If the distance between the two pixels was more than 0
                if(nearest.distance > 0){
                    similarity = getSimilarity(ds, root_1, root_2);
                    // If the new similarity is still the same as the old one
                    // then that means we are going to add it back to priority queue
                    // and pop it off so we might as well skip that and union it here
                    // this is an optimization
                    if(similarity.distance == nearest.distance){
                        currentRegion = ds.union(root_1, root_2);
                        //Get the neighbors of this new set
                        neighbors = getNeightborSets(ds, currentRegion);
                        // update the average colors array
                        averageColors[data.get(currentRegion).p][data.get(currentRegion).q] = computeAverageColor(ds.get(currentRegion));
                        // Go through the neighbors and add the similarities between them and the new region
                        for(int neighbor : neighbors) {
                            final int curr = currentRegion;
                            Runnable run = new Runnable() {
                                @Override
                                public void run() {
                                    priorityQueue.add(getSimilarity(ds, curr, neighbor));
                                }
                            };
                            run.run();
                        }

                    }else {
                        //otherwise just add it to the queue
                        priorityQueue.add(similarity);
                    }
//                    continue;
                }else{
                    // Otherwise we want to union these two roots
                    ds.union(root_1, root_2);
                    // and continue
                    //break;
                }
                // Otherwise if p and q are roots of their own set
            }else{
                // if the pixels are roots of their own set union them
                // otherwise just continue
                    similarity = getSimilarity(ds, root_1, root_2);
                    if (similarity.distance == nearest.distance) {
                        currentRegion = ds.union(root_1, root_2);

                        // if the distance
                        if(nearest.distance == 0){
                            continue;
                        }

                        // If the distance is not 0 then get its neighbors
                        //Get the neighbors of this new set
                        neighbors = getNeightborSets(ds, currentRegion);
                        // update the average color array
                        averageColors[data.get(currentRegion).p][data.get(currentRegion).q] = computeAverageColor(ds.get(currentRegion));
                        // Go through the neighbors and add the similarities between them and the new region
                        for(int neighbor : neighbors) {
//                            Thread thread = new Thread();
                            final int curr = currentRegion;
                            Runnable run = new Runnable() {
                                @Override
                                public void run() {
                                    priorityQueue.add(getSimilarity(ds, curr, neighbor));
                                }
                            };
                            run.run();
//                            priorityQueue.add(getSimilarity(ds, currentRegion, neighbor));
                        }
                    }
            }
        }
        System.err.println("\nWe are done unioning the image regions");
    }

    //Task 5: Output results (10%)
    //Recolor all pixels with the average color and save output image
    public void outputResults(int K)
    {
        //collect all sets
        int region_counter=1;
        ArrayList<Pair<Integer>> sorted_regions = new ArrayList<Pair<Integer>>();

        int width = this.image.getWidth();
        int height = this.image.getHeight();
        // If the k is too big for the image that means we just
        // print out each pixel fromm bottom right to top left
        // for some reason, i don't know why
        if(K > width*height){
                for (int j = height-1; j >-1; j--) {
                    for(int i = width-1; i>-1;i--) {
                        System.err.println(String.format("region %d size= %d color=%s", region_counter, 1, colors[i][j].toString()));
                        region_counter++;
                }
            }
            return;
        }
        for(int h=0; h<height; h++){
          for(int w=0; w<width; w++){
              // no need to call getID, we ca
              int id=width*h + w;
              int setid=ds.find(id);
              if(id!=setid) continue;
              sorted_regions.add(new Pair<Integer>(ds.get(setid).size(),setid));
          }//end for w
        }//end for h

        //sort the regions
        Collections.sort(sorted_regions, new Comparator<Pair<Integer>>(){
          @Override
          public int compare(Pair<Integer> a, Pair<Integer> b) {
              if(a.p!=b.p) return b.p-a.p;
              else return b.q-a.q;
          }
        });

        //recolor and output region info
		int k = 0;
      //Todo: Your code here (remove this line)
        for(Pair<Integer> region : sorted_regions){
            int id = region.q;
            Set<Pixel> pixels = ds.get(id);
            k++;
            int averageColor = averageColors[data.get(id).p][data.get(id).q].getRGB();
            for(Pixel pixel : pixels){
                image.setRGB(pixel.p, pixel.q, averageColor);
            }
            System.err.println(String.format("region %d size= %d color=%s", k, pixels.size(), new Color(averageColor).toString()));
        }
      //Hint: Use image.setRGB(x,y,c.getRGB()) to change the color of a pixel (x,y) to the given color "c"

      //save output image
      String out_filename = img_filename+"_seg_"+K+".png";
      try
      {
        File ouptut = new File(out_filename);
        ImageIO.write(image, "png", ouptut);
        System.err.println("- Saved result to "+out_filename);
      }
      catch (Exception e) {
        System.err.println("! Error: Failed to save image to "+out_filename);
      }
    }

    //-----------------------------------------------------------------------
    //
    //
    // Todo: Read and provide comments, but do not change the following code
    //
    //
    //-----------------------------------------------------------------------

    //
    //Data
    //
    public BufferedImage image;       //this is the 2D array of RGB pixels
    private String img_filename;      //input image filename without .jpg or .png
    private DisjointSets<Pixel> ds;   //the disjoint set

    //
    // constructor, read image from file
    //
    public Decomposor(String imgfile)
    {
      File imageFile = new File(imgfile);
      try
      {
        this.image = ImageIO.read(imageFile);
      }
      catch(IOException e)
      {
        System.err.println("! Error: Failed to read "+imgfile+", error msg: "+e);
        return;
      }
      this.img_filename=imgfile.substring(0, imgfile.lastIndexOf('.')); //remember the filename
      colors = new Color[image.getWidth()][image.getHeight()];
      averageColors = new Color[image.getWidth()][image.getHeight()];
      for(int i = 0; i<image.getHeight(); i++){
          for(int j = 0; j<image.getWidth(); j++){
              colors[j][i] = getColor(getPixel(image.getWidth()*i + j));
              averageColors[j][i] = colors[j][i];
          }
      }
    }


    //
    // 3 private classes below
    //

    /**
     * Class that holds a pair of objects of type T
     * @param <T> type of the pair
     */
    private class Pair<T>
    {
      public Pair(T p_, T q_){this.p=p_;this.q=q_;}
      T p, q;
    }


    //a pixel is a 2D coordinate (w,h) in an image

    /**
     * class that represents a pixel
     */
    private class Pixel extends Pair<Integer>{public Pixel(int w, int h){ super(w,h); } } //aliasing Pixel

    //this class represents the similarity between the colors of two adjacent pixels or regions

    /**
     * class that represents the euclidean distance between two pixels according
     * to their rgb color vectors
     */
    private class Similarity implements Comparable<Similarity>
    {
      public Similarity(int d, Pixel p, Pixel q)
      {
        this.distance=d;
        this.pixels=new Pair<Pixel>(p,q);
      }

      public int compareTo( Similarity other )
      {
          int diff=this.distance - other.distance;
          if(diff!=0) return diff;
          diff=getID(this.pixels.p) - getID(other.pixels.p);
          if(diff!=0) return diff;
          return getID(this.pixels.q) - getID(other.pixels.q);

      }

      //a pair of ajacent pixels or regions (represented by the "root" pixels)
      public Pair<Pixel> pixels;

      //distance between the color of two pixels or two regions,
      //smaller distance indicates higher similarity
      public int distance;


    }

    //
    // helper functions
    //

    //convert a pixel to an ID
    private int getID(Pixel pixel)
    {
      return this.image.getWidth()*pixel.q+pixel.p;
    }

    //convert ID back to pixel
    private Pixel getPixel(int id)
    {
      int h= id/this.image.getWidth();
      int w= id-this.image.getWidth()*h;

      if(h<0 || h>=this.image.getHeight() || w<0 || w>=this.image.getWidth())
        throw new ArrayIndexOutOfBoundsException();

      return new Pixel(w,h);
    }

    /**
     * Returns the color of a pixel
     * @param p the pixel to get the color of
     * @return the color of p
     */
	private Color getColor(Pixel p) {
		return new Color(image.getRGB(p.p, p.q));
	}

    //compute the average color of a collection of pixels
    private Color computeAverageColor(AbstractCollection<Pixel> pixels)
    {
      int r=0, g=0, b=0;
      for(Pixel p : pixels)
      {
        Color c = colors[p.p][p.q];//new Color(image.getRGB(p.p, p.q));
        r+=c.getRed();
        g+=c.getGreen();
        b+=c.getBlue();
      }
      return new Color(r/pixels.size(),g/pixels.size(),b/pixels.size());
    }

    /**
     * Returns euclidean distance between two colors
     * @param c1 the first color
     * @param c2 the second color
     * @return the euclidean distance between the two colors
     */
    private int getDifference(Color c1, Color c2)
    {
      int r = (int)(c1.getRed()-c2.getRed());
      int g = (int)(c1.getGreen()-c2.getGreen());
      int b = (int)(c1.getBlue()-c2.getBlue());

      return r*r+g*g+b*b;
    }

    //8-neighbors of a given pixel
    private ArrayList<Pixel> getNeightbors(Pixel pixel)
    {
      ArrayList<Pixel> neighbors = new ArrayList<Pixel>();

      for(int i=-1;i<=1;i++)
      {
        int n_w=pixel.p+i;
        if(n_w<0 || n_w==this.image.getWidth()) continue;
        for(int j=-1;j<=1;j++)
        {
          int n_h=pixel.q+j;
          if(n_h<0 || n_h==this.image.getHeight()) continue;
          if(i==0 && j==0) continue;
          neighbors.add( data.get(this.image.getWidth()*n_h + n_w));//new Pixel(n_w, n_h) );
        }//end for j
      }//end for i

      return neighbors;
    }

    //
    // JPanel function
    //
    public void paint(Graphics g)
    {
      g.drawImage(this.image, 0, 0,this);
    }

}