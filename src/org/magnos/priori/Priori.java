
package org.magnos.priori;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

import org.magnos.priori.finder.IntersectionFinderCircleCircle;
import org.magnos.priori.finder.IntersectionFinderPlaneCircle;
import org.magnos.priori.finder.IntersectionFinderPlaneRectangle;
import org.magnos.priori.finder.IntersectionFinderRectangleCircle;
import org.magnos.priori.finder.IntersectionFinderRectangleRectangle;


public class Priori
{

	private static int typeIdentifier = 0;
	private static IntersectionFinder[][] finders = {};

	public static int newType()
	{
		return typeIdentifier++;
	}

	private static void resizeFinders( int size )
	{
		finders = Arrays.copyOf( finders, size );

		for (int i = 0; i < size; i++)
		{
			if (finders[i] == null)
			{
				finders[i] = new IntersectionFinder[size];
			}
			else
			{
				finders[i] = Arrays.copyOf( finders[i], size );
			}
		}
	}

	public static void addIntersectionFinder( IntersectionFinder finder )
	{
		int stype = finder.getSubjectType();
		int otype = finder.getObjectType();

		if (finders.length < typeIdentifier)
		{
			resizeFinders( typeIdentifier );
		}

		finders[stype][otype] = finder;
		finders[otype][stype] = finder;
	}

	public static IntersectionFinder getFinder( Shape a, Shape b )
	{
		return finders[a.getType()][b.getType()];
	}

	public static Intersection findIntersection( Shape a, Shape b )
	{
		IntersectionFinder finder = getFinder( a, b );

		return (finder == null ? null : finder.find( a, b ));
	}

	public static void loadDefaults()
	{
		addIntersectionFinder( new IntersectionFinderCircleCircle() );
		addIntersectionFinder( new IntersectionFinderPlaneCircle() );
		addIntersectionFinder( new IntersectionFinderPlaneRectangle() );
		addIntersectionFinder( new IntersectionFinderRectangleCircle() );
		addIntersectionFinder( new IntersectionFinderRectangleRectangle() );
	}

	private final ShapeMatcher matcher;
	private final IndexPool identifierPool;
	private Shape[] shapes;
	private int shapeCount;
	private int tryMax = 1000;

	public Priori( ShapeMatcher matcher, int initialCapacity )
	{
		this.matcher = matcher;
		this.identifierPool = new IndexPool( initialCapacity );
		this.shapes = new Shape[initialCapacity];
		this.shapeCount = 0;
	}

	private void ensureCapacity( int count )
	{
		if (shapeCount + count > shapes.length)
		{
			int minimumCapacity = shapeCount + count;
			int desiredCapacity = shapeCount + (shapeCount >> 1);

			shapes = Arrays.copyOf( shapes, Math.max( minimumCapacity, desiredCapacity ) );
		}
	}

	private void internalAdd( Shape s )
	{
		shapes[shapeCount++] = s;

		s.id = identifierPool.pop();

		matcher.onShapeAdd( s );
	}

	public void add( Shape... shapeArray )
	{
		ensureCapacity( shapeArray.length );

		for (Shape shape : shapeArray)
		{
			internalAdd( shape );
		}
	}

	public void add( Collection<? extends Shape> shapeCollection )
	{
		ensureCapacity( shapeCollection.size() );

		for (Shape shape : shapeCollection)
		{
			internalAdd( shape );
		}
	}

	public void add( Shape shape )
	{
		ensureCapacity( 1 );
		internalAdd( shape );
	}

	public void prune()
	{
		int alive = 0;

		for (int i = 0; i < shapeCount; i++)
		{
			Shape s = shapes[i];

			if (s.expired)
			{
				matcher.onShapeExpire( s );

				identifierPool.push( s.id );
			}
			else
			{
				shapes[alive++] = s;
			}
		}

		while (alive < shapeCount)
		{
			shapes[--shapeCount] = null;
		}
	}

	public boolean handleIntersectionsMine( IntersectionListener listener )
	{
		matcher.prepare();

		ShapeMatcherIntersectionMinimum result = new ShapeMatcherIntersectionMinimum();

		result.intersections.clear();
		matcher.findAllMatches( result );

		int attempt = 0;

		while (!result.intersections.isEmpty() && attempt++ < tryMax)
		{
			while (!result.intersections.isEmpty())
			{
				Intersection inter = result.intersections.poll();

				if (listener.onIntersection( inter ))
				{
					resolve( inter );

					listener.onResolution( inter );

					Shape.notifyCollisions( inter.subject, inter.object );

					break;
				}
			}

			// TODO ideally we only remove potential intersections involving inter.subject & inter.object
			// and we recalculate only potential intersections involving them
			
			result.intersections.clear();
			matcher.findAllMatches( result );
		}

		for (int i = 0; i < shapeCount; i++)
		{
			Shape s = shapes[i];

			if (!s.expired)
			{
				s.start.set( s.end );
			}
		}

		return true;
	}

	public void handleIntersections( IntersectionListener listener )
	{
		matcher.prepare();

		final Set<IndexPair> pairSet = new HashSet<IndexPair>();

		matcher.findAllMatches( new ShapeMatcherPairSetPopulator( pairSet ) );

		PriorityQueue<Intersection> intersectionQueue = new PriorityQueue<Intersection>();

		populateQueue( pairSet, intersectionQueue );

		int handled = 0;
		
		// TODO duplicate intersections
		// think about stacking
		
		while (!intersectionQueue.isEmpty() && handled < tryMax)
		{
			Intersection inter = intersectionQueue.poll();

			pruneIndexPairSet( pairSet, inter.subject.id );
			pruneIndexPairSet( pairSet, inter.object.id );
			
			boolean resolvable = listener.onIntersection( inter );
			
			if (resolvable)
			{
				resolve( inter );

				listener.onResolution( inter );
			}
			
			tick( inter.time, resolvable ? inter.subject : null, resolvable ? inter.object : null );
			tickIntersections( inter.time, intersectionQueue );

			final Set<IndexPair> newPairSet = new HashSet<IndexPair>();

			if (!inter.subject.isExpired())
			{
				matcher.findMatches( inter.subject, new ShapeMatcherPairSetPopulator( newPairSet, inter.object ) );
			}

			if (!inter.object.isExpired())
			{
				matcher.findMatches( inter.object, new ShapeMatcherPairSetPopulator( newPairSet, inter.subject ) );
			}

			populateQueue( newPairSet, intersectionQueue );

			pairSet.addAll( newPairSet );
			handled++;
		}

		for (int i = 0; i < shapeCount; i++)
		{
			Shape s = shapes[i];

			if (!s.expired)
			{
				s.start.set( s.end );
			}
		}
	}

	protected void populatePairSet( final Set<IndexPair> pairSet )
	{
		matcher.findAllMatches( new ShapeMatcherListener()
		{
			public void onFoundMatch( Shape a, Shape b )
			{
				if (!a.isExpired() && !b.isExpired())
				{
					// TODO no newing
					pairSet.add( new IndexPair( a.id, b.id ) );
				}
			}
		} );
	}

	protected Shape findShape( int id )
	{
		for (int i = 0; i < shapeCount; i++)
		{
			if (shapes[i].id == id)
			{
				return shapes[i];
			}
		}

		return null;
	}

	protected void populateQueue( Set<IndexPair> pairSet, PriorityQueue<Intersection> intersectionQueue )
	{
		for (IndexPair pair : pairSet)
		{
			// TODO finding shapes should be instant
			Shape a = findShape( pair.min );
			Shape b = findShape( pair.max );
			
			// TODO no newing
			Intersection intersection = findIntersection( a, b );

			if (intersection != null && intersection.isValidTime())
			{
				intersectionQueue.offer( intersection );
			}
		}
	}

	protected void pruneIndexPairSet( Set<IndexPair> pairSet, int index )
	{
		Iterator<IndexPair> iterator = pairSet.iterator();

		while (iterator.hasNext())
		{
			IndexPair pair = iterator.next();

			if (pair.hasIndex( index ))
			{
				iterator.remove();
			}
		}
	}

	protected void tick( float time, Shape ignoreSubject, Shape ignoreObject )
	{
		for (int i = 0; i < shapeCount; i++)
		{
			Shape s = shapes[i];
			
			if (s == ignoreSubject || s == ignoreObject)
			{
				continue;
			}
			
			Vector start = s.getStart();
			Vector end = s.getEnd();
			
			start.x += (end.x - start.x) * time;
			start.y += (end.y - start.y) * time;
		}
	}

	protected void tickIntersections( float time, PriorityQueue<Intersection> intersectionQueue )
	{
		float remainingTimeInv = 1.0f / (1.0f - time);

		for (Intersection inter : intersectionQueue)
		{
			inter.time = (inter.time - time) * remainingTimeInv;
		}
	}
	
	// https://en.wikipedia.org/wiki/Coefficient_of_restitution#Speeds_after_impact
	public static void resolveWithRestitution( Intersection inter )
	{
		Shape a = inter.subject;
		Shape b = inter.object;
		
		float amass = a.getMass();
		float bmass = b.getMass();
		float massSum = amass + bmass;
		
		if (massSum == 0.0f)
		{
			return;
		}
		
		float restitution = a.getRestitution() * b.getRestitution();
		float massSumInv = 1.0f / massSum;
		
		Vector as = a.getStart();
		Vector ae = a.getEnd();
		Vector bs = b.getStart();
		Vector be = b.getEnd();

		// Velocity
		Vector avel = ae.sub( as ); // new
		Vector bvel = be.sub( bs ); // new
		Vector diff = bvel.sub( avel ); // new
		
		// Velocity * Mass
		Vector aenergy = avel.mul( amass ); // new
		Vector benergy = bvel.mul( bmass ); // new
		
		// Total Velocity * Masses
		Vector totalEnergy = aenergy.add( benergy ); // new
		
		// Final Velocity
		avel.set( totalEnergy ).addsi( diff, +restitution * bmass ).muli( massSumInv );
		bvel.set( totalEnergy ).addsi( diff, -restitution * amass ).muli( massSumInv );
				
		// Adjust Paths
		if (amass != 0f)
		{
			Vector ai = inter.getSubjectIntersectionPosition( new Vector() ); // new
			as.set( ai );
			ae.set( as ).addi( avel );
		}
		
		if (bmass != 0f)
		{
			Vector bi = inter.getObjectIntersectionPosition( new Vector() ); // new
			bs.set( bi );
			be.set( bs ).addi( bvel );
		}
	}
	
	public static void resolvePhysics( Intersection inter )
	{
		Shape a = inter.subject;
		Shape b = inter.object;

		float amass = a.getMass();
		float bmass = b.getMass();

		if (amass == 0.0f && bmass == 0.0f)
		{
			return;
		}

		Vector as = a.getStart();
		Vector ae = a.getEnd();
		Vector bs = b.getStart();
		Vector be = b.getEnd();

		Vector avel = ae.sub( as );
		Vector bvel = be.sub( bs );

		float restitution = (a.getRestitution() * b.getRestitution()) * 0.5f + 0.5f;
		float friction = Math.max( a.getFriction(), b.getFriction() );

		if (amass == 0.0f && bmass != 0.0f)
		{
			Vector bsf = inter.getIntersectionPosition(b, new Vector());
			Vector bef = inter.getFuturePosition(b, new Vector(), restitution, friction);
			
			bs.set( bsf );
			be.set( bef );
			
			return;
		}

		if (bmass == 0.0f && amass != 0.0f)
		{
			Vector asf = inter.getIntersectionPosition(a, new Vector());
			Vector aef = inter.getFuturePosition(a, new Vector(), restitution, friction);
			
			as.set( asf );
			ae.set( aef );

			return;
		}

		float massSumInv = 1.0f / (amass + bmass);

		/*
		 * [Source](http://elancev.name/oliver/2D%20polygon.htm)
		 * 
		 * V = avel - bvel 
		 * Vn = N * (V . N) 
		 * Vt = V - Vn 
		 * Vout = Vt * -friction + Vn * -restitution 
		 * avel += Vout * amass * massSumInv 
		 * bvel -= Vout * bmass * massSumInv
		 */

		Vector V = avel.sub( bvel );
		Vector Vn = inter.normal.mul( V.dot( inter.normal ) );
		Vector Vt = V.sub( Vn );
		Vector Vout = Vn.mul( restitution ).addsi( Vt, friction );

		avel.subi( Vout ).muli( bmass * massSumInv );
		bvel.addi( Vout ).muli( amass * massSumInv );
		
		Vector ai = inter.getIntersectionPosition( a, new Vector() );
		Vector bi = inter.getIntersectionPosition( b, new Vector() );
		
		ae.set( as.set( ai ) ).addsi( avel, 1.0f - inter.time + Intersection.TIME_EPSILON );
		be.set( bs.set( bi ) ).addsi( bvel, 1.0f - inter.time + Intersection.TIME_EPSILON );
	}

	@SuppressWarnings({ "null", "unused" })
	public static void resolve( Intersection inter )
	{
		// resolveWithRestitution( inter );
		resolvePhysics( inter );
		
		if (inter != null)
		{
			return;
		}
		
		// Not yet ready
		
		Shape a = inter.subject;
		Shape b = inter.object;

		float amass = a.getMass();
		float bmass = b.getMass();

		if (amass == 0.0f && bmass == 0.0f)
		{
			return;
		}

		Vector as = a.getStart();
		Vector ae = a.getEnd();
		Vector bs = b.getStart();
		Vector be = b.getEnd();
		Vector avel = a.getVelocity();
		Vector bvel = b.getVelocity();
		Vector normal = inter.normal;
		float atime = inter.time * a.getInstantaneousVelocity();
		float btime = inter.time * b.getInstantaneousVelocity();

		float restitution = (a.getRestitution() * b.getRestitution()) * 0.5f + 0.5f;
		float friction = 1.0f - Math.max( a.getFriction(), b.getFriction() );

		Vector V = avel.sub( bvel );
		Vector Vn = normal.mul( V.dot( normal ) );
		Vector Vt = V.sub( Vn );
		Vector Vout = Vt.mul( friction ).addsi( Vn, restitution );

		float aenergy = 0.0f;
		float benergy = 0.0f;

		if (amass == 0.0f)
		{
			benergy = 1.0f;
		}
		else if (bmass == 0.0f)
		{
			aenergy = 1.0f;
		}
		else if (amass == bmass)
		{
			aenergy = benergy = 1.0f;
		}
		else
		{
			float massSumInv = 2.0f / (amass + bmass);
			aenergy = bmass * massSumInv;
			benergy = amass * massSumInv;
		}

		as.addsi( avel, atime );
		bs.addsi( bvel, btime );

		Vector uN = inter.normal;
		Vector uT = uN.left();

		float invmass = 1.0f / (amass + bmass);
		float v1n = uN.dot( avel );
		float v1t = uT.dot( avel );
		float v2n = uN.dot( bvel );
		float v2t = uT.dot( bvel );
		float v1 = avel.length();
		float v2 = bvel.length();
		float v1nPost = ((amass - bmass) * invmass * v1) + ((2 * bmass) * invmass * v2);
		float v2nPost = ((bmass - amass) * invmass * v2) + ((2 * amass) * invmass * v1);

		Vector postV1N = uN.mul( v1nPost );
		Vector postV1T = uT.mul( v1t );
		Vector postV2N = uN.mul( v2nPost );
		Vector postV2T = uT.mul( v2t );

		avel.set( postV1N ).addi( postV1T );
		bvel.set( postV2N ).addi( postV2T );
		
//		avel.reflecti( inter.normal );
//		avel.subi( Vout );
//		avel.muli( aenergy );

//		bvel.addi( Vout );
//		bvel.refracti( inter.normal ).negi();
//		bvel.muli( benergy );

		ae.set( as ).addsi( avel, a.getInstantaneousVelocity() * (1.0f - inter.time) );
		be.set( bs ).addsi( bvel, b.getInstantaneousVelocity() * (1.0f - inter.time) );
	}

	public ShapeMatcher getMatcher()
	{
		return matcher;
	}

	private class ShapeMatcherIntersectionMinimum implements ShapeMatcherListener
	{

		private PriorityQueue<Intersection> intersections = new PriorityQueue<Intersection>();

		@Override
		public void onFoundMatch( Shape a, Shape b )
		{
			Intersection inter = findIntersection( a, b );

			if (inter != null && inter.isValidTime())
			{
				intersections.offer( inter );
			}
		}
	}

	private class ShapeMatcherPairSetPopulator implements ShapeMatcherListener
	{

		private final Set<IndexPair> pairSet;
		private final Shape ignore;

		
		public ShapeMatcherPairSetPopulator( Set<IndexPair> pairSet )
		{
			this( pairSet, null );
		}
		
		public ShapeMatcherPairSetPopulator( Set<IndexPair> pairSet, Shape ignore )
		{
			this.pairSet = pairSet;
			this.ignore = ignore;
		}

		@Override
		public void onFoundMatch( Shape a, Shape b )
		{
			if (!a.isExpired() && !b.isExpired() && a != ignore && b != ignore)
			{
				pairSet.add( new IndexPair( a.id, b.id ) );
			}
		}

	}

}
