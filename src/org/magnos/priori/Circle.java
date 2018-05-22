
package org.magnos.priori;

public class Circle extends Shape
{

	public static final int TYPE = Priori.newType();

	protected float radius;

	public Circle()
	{
	}
	
	public Circle( float radius )
	{
		setRadius( radius );
	}
	
	public Circle( float radius, float x, float y )
	{
		setRadius( radius );
		reset( x, y );
	}
	
	public Circle( float radius, Vector center )
	{
		setRadius( radius );
		reset( center );
	}

	public float getRadius()
	{
		return radius;
	}

	public void setRadius( float radius )
	{
		this.radius = radius;
	}

	@Override
	public int getType()
	{
		return TYPE;
	}

	@Override
	public String toString()
	{
		return "Circle [radius=" + radius + ", id=" + id + ", attachment=" + attachment + ", expired=" + expired + ", inverseMass=" + mass + ", friction=" + friction + ", restitution=" + restitution + ", start=" + start + ", end=" + end + "]";
	}
	
}
