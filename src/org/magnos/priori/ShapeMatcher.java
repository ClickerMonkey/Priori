package org.magnos.priori;


public interface ShapeMatcher
{
	public void onShapeAdd(Shape s);
	public void onShapeExpire(Shape s);
	public void prepare();
	public void findAllMatches(ShapeMatcherListener listener);
	public void findMatches(Shape a, ShapeMatcherListener listener);
}
