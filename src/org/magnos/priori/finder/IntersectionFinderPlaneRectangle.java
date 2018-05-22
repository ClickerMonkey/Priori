
package org.magnos.priori.finder;

import org.magnos.priori.AbstractIntersectionFinder;
import org.magnos.priori.Intersection;
import org.magnos.priori.Plane;
import org.magnos.priori.Rectangle;
import org.magnos.priori.Shape;
import org.magnos.priori.Vector;


public class IntersectionFinderPlaneRectangle extends AbstractIntersectionFinder
{
	
	private static final Vector out = new Vector();
	private static final Vector first = new Vector();

	@Override
	public int getSubjectType()
	{
		return Plane.TYPE;
	}

	@Override
	public int getObjectType()
	{
		return Rectangle.TYPE;
	}

	@Override
	protected Intersection findNormalized( Shape subjectShape, Shape objectShape, Vector objectStart, Vector objectEnd )
	{
		Plane subject = (Plane)subjectShape;
		Rectangle object = (Rectangle)objectShape;

		float extentLeft = object.getExtentLeft();
		float extentRight = object.getExtentRight();
		float extentTop = object.getExtentTop();
		float extentBottom = object.getExtentBottom();

		float startX = objectStart.x;
		float startY = objectStart.y;
		float endX = objectEnd.x;
		float endY = objectEnd.y;
		
		float sL = startX + extentLeft;
		float sR = startX + extentRight;
		float sT = startY + extentTop;
		float sB = startY + extentBottom;

		if (subject.distance( sL, sT ) < 0 ||
			 subject.distance( sL, sB ) < 0 ||
			 subject.distance( sR, sT ) < 0 ||
 			 subject.distance( sR, sB ) < 0)
		{
			return null;
		}

      float eL = endX + extentLeft;
      float eR = endX + extentRight;
      float eT = endY + extentTop;
      float eB = endY + extentBottom;
 
      if (!(subject.distance( eL, eT ) < 0 || 
      	   subject.distance( eL, eB ) < 0 ||
      	   subject.distance( eR, eT ) < 0 || 
      	   subject.distance( eR, eB ) < 0))
      {
         return null;
      }
      
      float dx = endX - startX;
      float dy = endY - startY;
      float sq = dx * dx + dy * dy;
      float invsq = 1.0f / sq;
      float dist = 1.0f / (float)Math.sqrt( sq );
      float a = -dy * dist;
      float b = dx * dist;
		
      Vector out = IntersectionFinderPlaneRectangle.out;
      Vector first = IntersectionFinderPlaneRectangle.first;
      float firstTime = 1.0f;

      subject.intersection( a, b, sL, sT, out );
      float LTtime = ((out.x - sL) * dx + (out.y - sT) * dy) * invsq;
      if (LTtime < firstTime)
      {
         first.set( out );
         firstTime = LTtime;
      }
 
      subject.intersection( a, b, sL, sB, out );
      float LBtime = ((out.x - sL) * dx + (out.y - sB) * dy) * invsq;
      if (LBtime < firstTime)
      {
         first.set( out );
         firstTime = LBtime;
      }
 
      subject.intersection( a, b, sR, sT, out );
      float RTtime = ((out.x - sR) * dx + (out.y - sT) * dy) * invsq;
      if (RTtime < firstTime)
      {
         first.set( out );
         firstTime = RTtime;
      }
 
      subject.intersection( a, b, sR, sB, out );
      float RBtime = ((out.x - sR) * dx + (out.y - sB) * dy) * invsq;
      if (RBtime < firstTime)
      {
         first.set( out );
         firstTime = RBtime;
      }
 
      if (firstTime == 1.0f)
      {
         return null;
      }
      
      return new Intersection( subject, object, firstTime, subject.getNormalX(), subject.getNormalY(), first.x, first.y );
	}

}
