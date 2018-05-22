
package org.magnos.priori;

public abstract class Shape
{

	public static final float DEFAULT_MASS = 1.0f;
	public static final float DEFAULT_INVERSE_MASS = 1.0f;
	public static final float DEFAULT_FRICTION = 0.0f;
	public static final float DEFAULT_RESTITUTION = 1.0f;
	public static final int DEFAULT_GROUP = -1;

	protected int id;
	protected Object attachment;
	protected boolean expired;
	protected long group = DEFAULT_GROUP;
	protected long collidesWith = DEFAULT_GROUP;
	protected float mass = DEFAULT_MASS;
	protected float inverseMass = DEFAULT_INVERSE_MASS;
	protected float friction = DEFAULT_FRICTION;
	protected float restitution = DEFAULT_RESTITUTION;
	protected final Vector start = new Vector();
	protected final Vector end = new Vector();
	protected final Vector velocity = new Vector();
	protected float instantaneousVelocity = 0.0f;

	public abstract int getType();

	public void updateState( float instantaneousVelocity )
	{
		this.instantaneousVelocity = instantaneousVelocity;
		this.end.set( start ).addsi( velocity, instantaneousVelocity );
	}

	public boolean canCollide(Shape other)
	{
		return (collidesWith & other.group) != 0;
	}
	
	protected void onCollision( Shape s )
	{

	}

	public int getIdentifier()
	{
		return id;
	}

	public boolean isExpired()
	{
		return expired;
	}

	public void expire()
	{
		expired = true;
	}

	public void attach( Object attachment )
	{
		this.attachment = attachment;
	}

	@SuppressWarnings("unchecked")
	public <T> T attachment()
	{
		return (T)attachment;
	}

	public Vector getStart()
	{
		return start;
	}

	public void setStart( Vector start )
	{
		this.start.set( start );
	}

	public void setStart( float x, float y )
	{
		this.start.set( x, y );
	}

	public Vector getEnd()
	{
		return end;
	}

	public void setEnd( Vector end )
	{
		this.end.set( end );
	}

	public void setEnd( float x, float y )
	{
		this.end.set( x, y );
	}

	public void reset( Vector position )
	{
		this.start.set( position );
		this.end.set( position );
	}

	public void reset( float x, float y )
	{
		this.start.set( x, y );
		this.end.set( x, y );
	}

	public float getMass()
	{
		return mass;
	}

	public float getInverseMass()
	{
		return inverseMass;
	}

	public void setMass( float mass )
	{
		this.mass = mass;
		this.inverseMass = mass == 0.0f ? 0.0f : 1.0f / mass;
	}

	public float getFriction()
	{
		return friction;
	}

	public void setFriction( float friction )
	{
		this.friction = friction;
	}

	public float getRestitution()
	{
		return restitution;
	}

	public void setRestitution( float restitution )
	{
		this.restitution = restitution;
	}

	public long getGroup()
	{
		return group;
	}

	public void setGroup( long group )
	{
		this.group = group;
	}

	public long getCollidesWith()
	{
		return collidesWith;
	}

	public void setCollidesWith( long collidesWith )
	{
		this.collidesWith = collidesWith;
	}

	public float getInstantaneousVelocity()
	{
		return instantaneousVelocity;
	}

	public void setInstantaneousVelocity( float instantaneousVelocity )
	{
		this.instantaneousVelocity = instantaneousVelocity;
	}

	public Vector getVelocity()
	{
		return velocity;
	}
	
	public void setVelocity(Vector velocity)
	{
		this.velocity.set( velocity );
	}
	
	public void setVelocity(float x, float y)
	{
		this.velocity.set( x, y );
	}

	@Override
	public String toString()
	{
		return "Shape [id=" + id + ", attachment=" + attachment + ", expired=" + expired + ", inverseMass=" + mass + ", friction=" + friction + ", restitution=" + restitution + ", start=" + start + ", end=" + end + "]";
	}

	public static boolean canCollide( Shape a, Shape b )
	{
		return (a.group & b.collidesWith) != 0 || (a.collidesWith & b.group) != 0;
	}

	public static void notifyCollisions( Shape a, Shape b )
	{
		if ((a.collidesWith & b.group) != 0)
		{
			a.onCollision( b );
		}

		if ((b.collidesWith & a.group) != 0)
		{
			b.onCollision( a );
		}
	}

}
