package org.magnos.priori.finder;

import org.magnos.priori.AbstractIntersectionFinder;
import org.magnos.priori.Circle;
import org.magnos.priori.Intersection;
import org.magnos.priori.Plane;
import org.magnos.priori.Shape;
import org.magnos.priori.Vector;


public class IntersectionFinderPlaneCircle extends AbstractIntersectionFinder
{

	private static final Plane shifted = new Plane();
	private static final Plane line = new Plane();
	private static final Vector intersection = new Vector();

	@Override
	public int getSubjectType()
	{
		return Plane.TYPE;
	}

	@Override
	public int getObjectType()
	{
		return Circle.TYPE;
	}
	
	@Override
	protected Intersection findNormalized( Shape subjectShape, Shape objectShape, Vector objectStart, Vector objectEnd )
	{
		Plane subject = (Plane)subjectShape;
		Circle object = (Circle)objectShape;
		
		float a = subject.getA();
		float b = subject.getB();
		float c = subject.getC();
		float radius = object.getRadius();
		
		// No intersection if start is already intersecting with the plane or
		// end is not intersecting with the plane.
		if (subject.distance( objectStart ) < radius || subject.distance( objectEnd ) > radius)
		{
			return null;
		}
      
		float startX = objectStart.x;
		float startY = objectStart.y;
		float endX = objectEnd.x;
		float endY = objectEnd.y;
      
		// line.setFromLine( objectStart, objectEnd );
		float dx = (endX - startX);
		float dy = (endY - startY);
		float travelDistance = (float)Math.sqrt( dx * dx + dy * dy );
		float travelDistanceInv = 1.0f / travelDistance;
		float lineA = -dy * travelDistanceInv;
		float lineB = dx * travelDistanceInv;
		float lineC = -(lineA * startX + lineB * startY);
		line.set( lineA, lineB, lineC );
      
		// To set a shifted plane, simply add or subtract from c, since it's 
		// simply the signed distance between the plane and the origin.
		shifted.set( a, b, c - radius - Plane.THICKNESS );
		shifted.intersection( line, intersection );
      
		float distanceToIntersection = objectStart.distance( intersection );
		float time = distanceToIntersection * travelDistanceInv;
		float radiusWithThickness = radius + Plane.THICKNESS;
		float contactX = intersection.x - (a * radiusWithThickness);
		float contactY = intersection.y - (b * radiusWithThickness);

		return new Intersection( subject, object, time, a, b, contactX, contactY );
	}

}
