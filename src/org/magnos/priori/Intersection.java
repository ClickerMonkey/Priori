
package org.magnos.priori;

/**
 * A potential intersection was detected between the subject and object. An 
 * intersection is relative to the subject - ie. from their perspective. In
 * physics they would be considered the frame of reference.
 * 
 * @author phil
 *
 */
public class Intersection implements Comparable<Intersection>
{
	
	public static float TIME_EPSILON = 0.0001f;

	public Shape subject;
	public Shape object;
	public float time;
	public final Vector normal = new Vector();
	public final Vector contact = new Vector();

	public Intersection( Shape subject, Shape object, float time, float normalX, float normalY, float contactX, float contactY )
	{
		this.subject = subject;
		this.object = object;
		this.time = time;
		this.normal.set( normalX, normalY );
		this.contact.set( contactX, contactY );
	}
	
	public Vector getFutureDirection( Vector dir, Vector out )
	{
		return dir.reflect( normal, out );
	}
	
	public Vector getFutureDirection( Vector dir )
	{
		return dir.reflecti( normal );
	}
	
	public Vector getObjectIntersectionPosition( Vector out )
	{
		return getIntersectionPosition( object, out );
	}
	
	public Vector getSubjectIntersectionPosition( Vector out )
	{
		return getIntersectionPosition( subject, out );
	}
	
	public Vector getIntersectionPosition( Shape shape, Vector out )
	{
		Vector start = shape.getStart();
		Vector end = shape.getEnd();
		
		return out.set( end ).subi( start ).muli( time - TIME_EPSILON ).addi( start );
	}
	
	public Vector getFutureObjectPosition( Vector out, float restitution, float friction )
	{
		return getFuturePosition( object, out, restitution, friction );
	}
	
	public Vector getFutureSubjectPosition( Vector out, float restitution, float friction )
	{
		return getFuturePosition( subject, out, restitution, friction );
	}
	
	public Vector getFuturePosition( Shape shape, Vector out, float restitution, float friction )
	{
		Vector start = shape.getStart();
		Vector end = shape.getEnd();
		
		float remainingTime = 1.0f - time + TIME_EPSILON;
		
		out.set( end ).subi( start ).reflecti( normal ).muli( remainingTime ).muli( restitution );
		
		float reflectedX = out.x;
		float reflectedY = out.y;
		
		getIntersectionPosition( shape, out );
		
		out.x += reflectedX;
		out.y += reflectedY;
		
		return out;
	}
	
	public Vector getFutureObjectPosition( Vector out )
	{
		return getFuturePosition( object, out, Shape.DEFAULT_RESTITUTION, Shape.DEFAULT_FRICTION );
	}
	
	public Vector getFutureSubjectPosition( Vector out )
	{
		return getFuturePosition( subject, out, Shape.DEFAULT_RESTITUTION, Shape.DEFAULT_FRICTION );
	}
	
	public Vector getFuturePosition( Shape shape, Vector out )
	{
		return getFuturePosition( shape, out, Shape.DEFAULT_RESTITUTION, Shape.DEFAULT_FRICTION );
	}

	public boolean isValidTime()
	{
		return (time >= 0.0f && time < 1.0f && isFiniteTime());
	}
	
	public boolean isFiniteTime()
	{
		return !Float.isInfinite( time ) && !Float.isNaN( time );
	}

	public boolean hasShape( Shape s )
	{
		return (subject == s || object == s);
	}

	public Shape getOther( Shape s )
	{
		return (subject == s ? object : subject);
	}

	public Shape getType( int type )
	{
		return (subject.getType() == type ? subject : (object.getType() == type ? object : null));
	}

	@Override
	public int compareTo( Intersection o )
	{
		return (int)Math.signum( time - o.time );
	}

	@Override
	public String toString()
	{
		return "Intersection [time=" + time + ", normal=" + normal + ", contact=" + contact + ", subject=" + subject + ", object=" + object + "]";
	}

}
