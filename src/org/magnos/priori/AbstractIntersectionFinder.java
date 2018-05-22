package org.magnos.priori;


public abstract class AbstractIntersectionFinder implements IntersectionFinder
{
	
	private static final Vector cacheStart = new Vector();
	private static final Vector cacheEnd = new Vector();
	private static final Vector cachePoint = new Vector();
	
	
	protected abstract Intersection findNormalized( Shape subjectShape, Shape objectShape, Vector objectStart, Vector objectEnd );
	
	
	@Override
	public Intersection find( Shape a, Shape b )
	{
		boolean ordered = a.getType() == getSubjectType();
		Shape subject = ordered ? a : b;
		Shape object = ordered ? b : a;
		
		getObjectPath( subject, object, cacheStart, cacheEnd );
		
		Intersection result = findNormalized( subject, object, cacheStart, cacheEnd );
		
		if (result != null && result.isFiniteTime())
		{
			denormalize( result );
		}
		
		return result;
	}
	
	protected void getObjectPath( Shape subject, Shape object, Vector start, Vector end )
	{
		// start = object.start
		// end = object.end + (subject.start - subject.end)
		
		start.set( object.getStart() );
		end.set( subject.getStart() );
		end.subi( subject.getEnd() );
		end.addi( object.getEnd() );
	}
	
	public void denormalize( Intersection inter )
	{
		// inter.contact += inter.getSubjectIntersectionPoint()
		
		inter.getSubjectIntersectionPosition( cachePoint );
		inter.contact.addi( cachePoint );
	}

}
