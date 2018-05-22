package org.magnos.priori.finder;

import org.magnos.priori.AbstractIntersectionFinder;
import org.magnos.priori.Intersection;
import org.magnos.priori.Rectangle;
import org.magnos.priori.Shape;
import org.magnos.priori.Vector;


public class IntersectionFinderRectangleRectangle extends AbstractIntersectionFinder
{

	@Override
	public int getSubjectType()
	{
		return Rectangle.TYPE;
	}

	@Override
	public int getObjectType()
	{
		return Rectangle.TYPE;
	}

	@Override
	protected Intersection findNormalized( Shape subjectShape, Shape objectShape, Vector objectStart, Vector objectEnd )
	{
		Rectangle subject = (Rectangle)subjectShape;
		Rectangle object = (Rectangle)objectShape;
		
		Vector subjectCenter = subject.getStart();
		float L = subject.getExtentLeft() + subjectCenter.x;
		float R = subject.getExtentRight() + subjectCenter.x;
		float T = subject.getExtentTop() + subjectCenter.y;
		float B = subject.getExtentBottom() + subjectCenter.y;
		
		float startX = objectStart.x;
		float startY = objectStart.y;
		float endX = objectEnd.x;
		float endY = objectEnd.y;
		
		float extentLeft = object.getExtentLeft();
		float extentRight = object.getExtentRight();
		float extentTop = object.getExtentTop();
		float extentBottom = object.getExtentBottom();
		
		 // If the bounding box around the start and end points (+extents on all
      // sides) does not intersect with the rectangle, definitely not an
      // intersection
      if ((Math.max( startX, endX ) + extentRight < L) ||
          (Math.min( startX, endX ) + extentLeft > R) ||
          (Math.max( startY, endY ) + extentBottom < T) ||
          (Math.min( startY, endY ) + extentTop > B))
      {
         return null;
      }
 
      // If the rectangle around start intersects with the bounds, error
      if (!((startX + extentRight < L)  || 
      	   (startX + extentLeft > R) ||
            (startY + extentBottom < T) || 
            (startY + extentTop > B)))
      {
         return null;
      }
		
      float dx = endX - startX;
      float dy = endY - startY;
      float invdx = (dx == 0.0f ? 0.0f : 1.0f / dx);
      float invdy = (dy == 0.0f ? 0.0f : 1.0f / dy);
      

      /** Left Side **/
      // Does the rectangle go from the left side to the right side of the
      // rectangle's left?
      if (startX + extentLeft < L && endX + extentRight > L)
      {
         float ltime = ((L + extentLeft) - startX) * invdx;
 
         if (ltime >= 0.0f && ltime <= 1.0f)
         {
            float ly = dy * ltime + startY;
            float lyT = ly + extentTop;
            float lyB = ly + extentBottom;
 
            // Does the collisions point lie on the left side?
            if (lyT < B && lyB > T)
            {
               return new Intersection( subject, object, ltime, -1, 0, L, clamp( ly, T, B ) );
            }
            // Is the collision directly on the top-left corner?
            else if (lyB == T)
            {
               return new Intersection( subject, object, ltime, -1, -1, L, lyB );
            }
            // Is the collision directly on the bottom-left corner?
            else if (lyT == B)
            {
               return new Intersection( subject, object, ltime, -1, -1, L, lyT );
            }
         }
      }
 
      /** Right Side **/
      // Does the rectangle go from the right side to the left side of the
      // rectangle's right?
      if (startX + extentRight > R && endX + extentLeft < R)
      {
         float rtime = (startX - (R + extentRight)) * -invdx;
 
         if (rtime >= 0.0f && rtime <= 1.0f)
         {
            float ry = dy * rtime + startY;
            float ryT = ry + extentTop;
            float ryB = ry + extentBottom;
 
            // Does the collisions point lie on the right side?
            if (ryB > T && ryT < B)
            {
               return new Intersection( subject, object, rtime, 1, 0, R, clamp( ry, T, B ) );
            }
            // Is the collision directly on the top-right corner?
            else if (ryB == T)
            {
               return new Intersection( subject, object, rtime, 1, -1, L, ryB );
            }
            // Is the collision directly on the bottom-right corner?
            else if (ryT == B)
            {
               return new Intersection( subject, object, rtime, 1, 1, L, ryT );
            }
         }
      }
 
      /** Top Side **/
      // Does the rectangle go from the top side to the bottom side of the
      // rectangle's top?
      if (startY + extentTop < T && endY + extentBottom > T)
      {
         float ttime = ((T + extentTop) - startY) * invdy;
 
         if (ttime >= 0.0f && ttime <= 1.0f)
         {
            float tx = dx * ttime + startX;
            float txL = tx + extentLeft;
            float txR = tx + extentRight;
 
            // Does the collisions point lie on the top side?
            if (txR > L && txL < R)
            {
               return new Intersection( subject, object, ttime, 0, -1, clamp( tx, L, R ), T );
            }
            // Is the collision directly on the top-left corner?
            else if (txR == L)
            {
               return new Intersection( subject, object, ttime, -1, -1, txR, T );
            }
            // Is the collision directly on the top-right corner?
            else if (txL == R)
            {
               return new Intersection(subject, object, ttime, 1, -1, txL, T );
            }
         }
      }
 
      /** Bottom Side **/
      // Does the rectangle go from the bottom side to the top side of the
      // rectangle's bottom?
      if (startY + extentBottom > B && endY + extentTop < B)
      {
         float btime = (startY - (B + extentBottom)) * -invdy;
 
         if (btime >= 0.0f && btime <= 1.0f)
         {
            float bx = dx * btime + startX;
            float bxL = bx + extentLeft;
            float bxR = bx + extentRight;
 
            // Does the collisions point lie on the bottom side?
            if (bxR > L && bxL < R)
            {
               return new Intersection( subject, object, btime, 0, 1, clamp( bx, L, R ), B );
            }
            // Is the collision directly on the top-left corner?
            else if (bxR == L)
            {
               return new Intersection( subject, object, btime, -1, 1, bxR, B );
            }
            // Is the collision directly on the top-right corner?
            else if (bxL == R)
            {
               return new Intersection( subject, object, btime, 1, 1, bxL, B );
            }
         }
      }
 
      // No intersection at all!
      return null;
	}
	
   private float clamp( float x, float min, float max )
   {
      return (x < min ? min : (x > max ? max : x));
   }

}
