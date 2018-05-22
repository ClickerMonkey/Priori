package org.magnos.priori;



public interface IntersectionFinder
{
	public int getSubjectType();
	public int getObjectType();
	public Intersection find(Shape subject, Shape object);
}
