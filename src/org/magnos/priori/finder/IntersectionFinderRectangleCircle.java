
package org.magnos.priori.finder;

import org.magnos.priori.AbstractIntersectionFinder;
import org.magnos.priori.Circle;
import org.magnos.priori.Intersection;
import org.magnos.priori.Rectangle;
import org.magnos.priori.Shape;
import org.magnos.priori.Vector;


public class IntersectionFinderRectangleCircle extends AbstractIntersectionFinder
{

	@Override
	public int getSubjectType()
	{
		return Rectangle.TYPE;
	}

	@Override
	public int getObjectType()
	{
		return Circle.TYPE;
	}

	@Override
	protected Intersection findNormalized( Shape subjectShape, Shape objectShape, Vector objectStart, Vector objectEnd )
	{
		Rectangle subject = (Rectangle)subjectShape;
		Circle object = (Circle)objectShape;

		Vector subjectCenter = subject.getStart();
		float radius = object.getRadius();

		float L = subject.getExtentLeft() + subjectCenter.x;
		float R = subject.getExtentRight() + subjectCenter.x;
		float T = subject.getExtentTop() + subjectCenter.y;
		float B = subject.getExtentBottom() + subjectCenter.y;
		
		float startX = objectStart.x;
		float startY = objectStart.y;
		float endX = objectEnd.x;
		float endY = objectEnd.y;

		// If the bounding box around the start and end points (+radius on all
		// sides) does not intersect with the rectangle, definitely not an
		// intersection
		if ((Math.max( startX, endX ) + radius < L) ||
		    (Math.min( startX, endX ) - radius > R) ||
			 (Math.max( startY, endY ) + radius < T) ||
			 (Math.min( startY, endY ) - radius > B))
		{
			return null;
		}

		final float dx = endX - startX;
		final float dy = endY - startY;
		final float invdx = (dx == 0.0f ? 0.0f : 1.0f / dx);
		final float invdy = (dy == 0.0f ? 0.0f : 1.0f / dy);

	  /*
	   * HANDLE SIDE INTERSECTIONS
	   *
	   * Calculate intersection times with each side's plane, translated by
	   * radius along normal.
	   *
	   * If the intersection point lies between the bounds of the adjacent
	   * sides AND the line start->end is going in the correct direction...
	   */
	
	  /** Left Side **/
	  float ltime = ((L - radius) - startX) * invdx;
	  if (ltime >= 0.0f && ltime <= 1.0f)
	  {
	     float ly = dy * ltime + startY;
	     if (ly >= T && ly <= B && dx > 0)
	     {
	        return new Intersection( subject, object, ltime, -1, 0, L, ly );
	     }
	  }
 
      /** Right Side **/
      float rtime = (startX - (R + radius)) * -invdx;
      if (rtime >= 0.0f && rtime <= 1.0f)
      {
         float ry = dy * rtime + startY;
         if (ry >= T && ry <= B && dx < 0)
         {
            return new Intersection( subject, object, rtime, 1, 0, R, ry );
         }
      }
      
      /** Top Side **/
      float ttime = ((T - radius) - startY) * invdy;
      if (ttime >= 0.0f && ttime <= 1.0f)
      {
         float tx = dx * ttime + startX;
         if (tx >= L && tx <= R && dy > 0)
         {
            return new Intersection( subject, object, ttime, 0, -1, tx, T );
         }
      }
 
      /** Bottom Side **/
      float btime = (startY - (B + radius)) * -invdy;
      if (btime >= 0.0f && btime <= 1.0f)
      {
         float bx = dx * btime + startX;
         if (bx >= L && bx <= R && dy < 0)
         {
            return new Intersection( subject, object, btime, 0, 1, bx, B );
         }
      }
		

      /*
       * CALCULATE INTERSECTION CORNER
       *
       * With the tangent that is perpendicular to the line {start->end}, get
       * the corner which tangent's intersection with the line is closest to
       * start AND the distance between the line and the corner is <= radius.
       */
 
      float lineLength = (float)Math.sqrt( dx * dx + dy * dy );
      float lineLengthInv = 1.0f / lineLength;
 
      // Calculate the plane on start->end. This is used to calculate the
      // closeness of the tangent to the starting point and for calculating the
      // distance from the line to a corner.
      float a = -dy * lineLengthInv;
      float b = dx * lineLengthInv;
      float c = -(a * startX + b * startY);
 
      float min = Float.MAX_VALUE;
      float cornerX = 0;
      float cornerY = 0;
 
      // @formatter:off
      /*
       * LT,LB,RT,RB
       *        0.0 when the tangent between the corner intersects on
       *        the start, 1.0 when it intersects the end, and 0.5 when it
       *        intersects the middle of the line. These are used to calculate
       *        how close the corner is to the line.
       *
       * Ldx,Rdx,Tdy,Bdy
       *        cached values used to calculate LT,LB,RT,RB.
       *
       * La,Ra,Tb,Bb
       *        cached values used to calculate the distance between the line
       *        and the corner.
       */
      // @formatter:on
 
      float Ldx = (L - startX) * dx;
      float Rdx = (R - startX) * dx;
      float Tdy = (T - startY) * dy;
      float Bdy = (B - startY) * dy;
 
      float La = L * a;
      float Ra = R * a;
      float Tb = T * b;
      float Bb = B * b;
 
      // If the top-left corner is closest to start AND the line is <= radius
      // away from the top-left, it's the new intersecting corner.
      float LT = Ldx + Tdy;
      if (LT < min && Math.abs( La + Tb + c ) <= radius)
      {
         min = LT;
         cornerX = L;
         cornerY = T;
      }
 
      // If the bottom-left corner is closest to start AND the line is <= radius
      // away from the bottom-left, it's the new intersecting corner.
      float LB = Ldx + Bdy;
      if (LB < min && Math.abs( La + Bb + c ) <= radius)
      {
         min = LB;
         cornerX = L;
         cornerY = B;
      }
 
      // If the top-right corner is closest to start AND the line is <= radius
      // away from the top-right, it's the new intersecting corner.
      float RT = Rdx + Tdy;
      if (RT < min && Math.abs( Ra + Tb + c ) <= radius)
      {
         min = RT;
         cornerX = R;
         cornerY = T;
      }
 
      // If the bottom-right corner is closest to start AND the line is <= radius
      // away from the bottom-right, it's the new intersecting corner.
      float RB = Rdx + Bdy;
      if (RB < min && Math.abs( Ra + Bb + c ) <= radius)
      {
         min = RB;
         cornerX = R;
         cornerY = B;
      }
      

      // @formatter:off
      /* Solve the triangle between the start, corner, and intersection point.
       *                    
       *           +-----------T-----------+
       *           |                       |
       *          L|                       |R
       *           |                       |
       *           C-----------B-----------+
       *          / \      
       *         /   \r     _.-E
       *        /     \ _.-'
       *       /    _.-I
       *      / _.-'
       *     S-'
       *
       * S = start of circle's path
       * E = end of circle's path
       * LTRB = sides of the rectangle
       * I = {ix, iY} = point at which the circle intersects with the rectangle
       * C = corner of intersection (and collision point)
       * C=>I (r) = {nx, ny} = radius and intersection normal
       * S=>C = cornerDistance
       * S=>I = intersectionDistance
       * S=>E = lineLength
       * <S = innerAngle
       * <I = angle1
       * <C = angle2
       */
      // @formatter:on
 
      double inverseRadius = 1.0 / radius;
      double cornerdx = cornerX - startX;
      double cornerdy = cornerY - startY;
      double cornerDistance = Math.sqrt( cornerdx * cornerdx + cornerdy * cornerdy );
      double innerAngle = Math.acos( (cornerdx * dx + cornerdy * dy) / (lineLength * cornerDistance) );
 
      // If the circle is too close, no intersection.
      if (cornerDistance < radius)
      {
         return null;
      }
 
      // If inner angle is zero, it's going to hit the corner straight on.
      if (innerAngle == 0.0f)
      {
         float time = (float)((cornerDistance - radius) * lineLengthInv);
 
         // If time is outside the boundaries, return null. This algorithm can
         // return a negative time which indicates a previous intersection, and
         // can also return a time > 1.0f which can predict a corner
         // intersection.
         if (time > 1.0f || time < 0.0f)
         {
            return null;
         }
 
         float nx = (float)(cornerdx / cornerDistance);
         float ny = (float)(cornerdy / cornerDistance);
 
         return new Intersection( subject, object, time, nx, ny, cornerX, cornerY );
      }
 
      double innerAngleSin = Math.sin( innerAngle );
      double angle1Sin = innerAngleSin * cornerDistance * inverseRadius;
 
      // The angle is too large, there cannot be an intersection
      if (Math.abs( angle1Sin ) > 1.0f)
      {
         return null;
      }
 
      double angle1 = Math.PI - Math.asin( angle1Sin );
      double angle2 = Math.PI - innerAngle - angle1;
      double intersectionDistance = radius * Math.sin( angle2 ) / innerAngleSin;
 
      // Solve for time
      float time = (float)(intersectionDistance * lineLengthInv);
 
      // If time is outside the boundaries, return null. This algorithm can
      // return a negative time which indicates a previous intersection, and
      // can also return a time > 1.0f which can predict a corner intersection.
      if (time > 1.0f || time < 0.0f)
      {
         return null;
      }
 
      // Solve the intersection and normal
      float ix = time * dx + startX;
      float iy = time * dy + startY;
      float nx = (float)((ix - cornerX) * inverseRadius);
      float ny = (float)((iy - cornerY) * inverseRadius);
 
      return new Intersection( subject, object, time, nx, ny, cornerX, cornerY );
	}

}
