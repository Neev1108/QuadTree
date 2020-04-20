package Quadtree;

public class QuadRectangle
{
    public int id;
    public QuadPoint top_left;
    public QuadPoint bottom_right;
    public QuadPoint bottom_left;
    public QuadPoint top_right;
 
    
    public QuadRectangle(int id, QuadPoint top_left, QuadPoint bottom_right)
    {
        this.id = id;
        this.top_left = top_left;
        this.bottom_right = bottom_right;
        top_right = new QuadPoint(bottom_right.x, top_left.y);
        bottom_left = new QuadPoint(top_left.x, bottom_right.y);
    }
    public String toString()
    {
        return top_left.toString() + ", " + bottom_right.toString();
    }
    
    public String print() {
    	return "(" + bottom_left.x + ", " + bottom_left.y + "), (" + top_right.x + ", "  + top_right.y + ")";
    }
    
    public String printAll() {
    	return bottom_left.toString() + " " + bottom_right.toString() + " " + top_right.toString() + " " + top_left.toString();
    }
    
    
}
