package Quadtree;

public class QuadRecord
{
    public String label;
    public QuadPoint point;
 
    public QuadRecord(String label, QuadPoint point)
    {
        this.label = label;
        this.point = point;
    }
    public QuadRecord(String label, float x, float y)
    {
        this.label = label;
        this.point = new QuadPoint(x, y);
    }
    public String toString()
    {
        return label + ","+ point.toString();
    }
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public QuadPoint getPoint() {
		return point;
	}
	public void setPoint(QuadPoint point) {
		this.point = point;
	}
}
