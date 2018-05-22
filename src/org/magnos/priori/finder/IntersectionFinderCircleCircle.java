
package org.magnos.priori.finder;

import org.magnos.priori.AbstractIntersectionFinder;
import org.magnos.priori.Circle;
import org.magnos.priori.Intersection;
import org.magnos.priori.Shape;
import org.magnos.priori.Vector;


public class IntersectionFinderCircleCircle extends AbstractIntersectionFinder
{

	@Override
	public int getSubjectType()
	{
		return Circle.TYPE;
	}

	@Override
	public int getObjectType()
	{
		return Circle.TYPE;
	}

	@Override
	protected Intersection findNormalized( Shape subjectShape, Shape objectShape, Vector objectStart, Vector objectEnd )
	{
		Circle subject = (Circle)subjectShape;
		Circle object = (Circle)objectShape;

		Vector fixedCenter = subject.getStart();
		float fixedRadius = subject.getRadius();
		float radius = object.getRadius();

		float dx = objectEnd.x - objectStart.x;
		float dy = objectEnd.y - objectStart.y;
		float sq = dx * dx + dy * dy;
		
		// 0 = start, 1 = end, 0.5 = midpoint
		float delta = ((fixedCenter.x - objectStart.x) * dx + (fixedCenter.y - objectStart.y) * dy) / sq;

		// If it's before the start, no intersection.
		if (delta < 0)
		{
			return null;
		}

		// The location of the closest point on the line to the center of the fixed circle.
		float cx = (objectStart.x + delta * dx);
		float cy = (objectStart.y + delta * dy);
 
		// If the distance from the closest point to the line is > radius + fixedRadius,
		// there is no intersection.
		float cdx = cx - fixedCenter.x;
		float cdy = cy - fixedCenter.y;
		float cdsq = cdx * cdx + cdy * cdy;
		float rsum = radius + fixedRadius;
     
		if (cdsq == 0.0f || cdsq > rsum * rsum)
		{
			return null;
		}
 
		double cdist = Math.sqrt( cdsq );
		double angle1 = Math.asin( cdist / rsum );
		double angle2 = Math.PI * 0.5 - angle1;
		double side2 = Math.abs( rsum * Math.sin( angle2 ) );
		double distance = Math.sqrt( sq );
     
		float time = (float)(delta - side2 / distance);
		float rsumInv = 1.0f / rsum;
		float x = time * dx + objectStart.x;
		float y = time * dy + objectStart.y;
		float nx = (x - fixedCenter.x) * rsumInv;
		float ny = (y - fixedCenter.y) * rsumInv;
		float ix = x - nx * radius;
		float iy = y - ny * radius;

		return new Intersection( subject, object, time, nx, ny, ix, iy );
	}

}
