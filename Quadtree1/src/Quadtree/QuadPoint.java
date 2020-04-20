package Quadtree;

public class QuadPoint
{
    public float x;
    public float y;
    public QuadPoint(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
	public String toString()
    {
        return "(" + x + "," + y + ")";
    }
}

