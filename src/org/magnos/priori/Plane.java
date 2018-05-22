
package org.magnos.priori;

public class Plane extends Shape
{

	public static final int TYPE = Priori.newType();
	public static float THICKNESS = 0.001f;
	
	protected float a;
	protected float b;
	protected float c;

	public Plane()
	{
	}

	public Plane( float a, float b, float c )
	{
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Plane setFromPoint( Vector point, Vector normal )
	{
		return setFromPoint( point.x, point.y, normal.x, normal.y );
	}

	public Plane setFromPoint( float pointX, float pointY, float normalX, float normalY )
	{
		a = normalX;
		b = normalY;
		c = -((normalX * pointX) + (normalY * pointY));

		return this;
	}

	public Plane setFromLine( Vector start, Vector end )
	{
		return setFromLine( start.x, start.y, end.x, end.y );
	}

	public Plane setFromLine( float x0, float y0, float x1, float y1 )
	{
		float dx = (x1 - x0);
		float dy = (y1 - y0);
		float d = 1.0f / (float)Math.sqrt( dx * dx + dy * dy );

		a = -dy * d;
		b = dx * d;
		c = -(a * x0 + b * y0);

		return this;
	}

	public void set( float a, float b, float c )
	{
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public float distance( Vector v )
	{
		return distance( v.x, v.y );
	}

	public float distance( float x, float y )
	{
		return (a * x + b * y + c);
	}

	public boolean intersection( Plane p, Vector out )
	{
		return intersection( p.a, p.b, p.c, out );
	}

	public boolean intersection( float nx, float ny, float x, float y, Vector out )
	{
		return intersection( nx, ny, -(nx * x + ny * y), out );
	}

	public boolean intersection( float pa, float pb, float pc, Vector out )
	{
		float div = (a * pb - b * pa);

		if (div == 0)
		{
			return false;
		}

		div = 1 / div;
		out.x = (-c * pb + b * pc) * div;
		out.y = (-a * pc + c * pa) * div;

		return false;
	}

	public float getA()
	{
		return a;
	}

	public float getB()
	{
		return b;
	}

	public float getC()
	{
		return c;
	}

	public float getNormalX()
	{
		return a;
	}

	public float getNormalY()
	{
		return b;
	}

	public float getSignedDistanceFromOrigin()
	{
		return c;
	}

	@Override
	public int getType()
	{
		return TYPE;
	}

	@Override
	public String toString()
	{
		return "Plane [a=" + a + ", b=" + b + ", c=" + c + ", id=" + id + ", attachment=" + attachment + ", expired=" + expired + ", inverseMass=" + mass + ", friction=" + friction + ", restitution=" + restitution + ", start=" + start + ", end=" + end + "]";
	}

	public static Plane fromLine( float x0, float y0, float x1, float y1 )
	{
		return new Plane().setFromLine( x0, y0, x1, y1 );
	}

	public static Plane fromLine( Vector start, Vector end )
	{
		return new Plane().setFromLine( start, end );
	}

	public static Plane fromPoint( float pointX, float pointY, float normalX, float normalY )
	{
		return new Plane().setFromPoint( pointX, pointY, normalX, normalY );
	}

	public static Plane fromPoint( Vector point, Vector normal )
	{
		return new Plane().setFromPoint( point, normal );
	}

}
