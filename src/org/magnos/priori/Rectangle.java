
package org.magnos.priori;

public class Rectangle extends Shape
{

	public static final int TYPE = Priori.newType();

	protected float extentLeft;
	protected float extentTop;
	protected float extentRight;
	protected float extentBottom;

	public Rectangle()
	{
	}

	public Rectangle( float x, float y, float extentLeft, float extentTop, float extentRight, float extentBottom )
	{
		reset( x, y );
		setExtentLeft( extentLeft );
		setExtentRight( extentRight );
		setExtentTop( extentTop );
		setExtentBottom( extentBottom );
	}
	
	public Rectangle( float left, float top, float right, float bottom )
	{
		float w = (right - left);
		float h = (bottom - top);
		
		reset( (right + left) * 0.5f, (top + bottom) * 0.5f );
		setExtentLeft( -w * 0.5f );
		setExtentRight( w * 0.5f );
		setExtentTop( -h * 0.5f );
		setExtentBottom( h * 0.5f );
	}

	public float getExtentLeft()
	{
		return extentLeft;
	}

	public void setExtentLeft( float extentLeft )
	{
		this.extentLeft = extentLeft;
	}

	public float getExtentTop()
	{
		return extentTop;
	}

	public void setExtentTop( float extentTop )
	{
		this.extentTop = extentTop;
	}

	public float getExtentRight()
	{
		return extentRight;
	}

	public void setExtentRight( float extentRight )
	{
		this.extentRight = extentRight;
	}

	public float getExtentBottom()
	{
		return extentBottom;
	}

	public void setExtentBottom( float extentBottom )
	{
		this.extentBottom = extentBottom;
	}

	@Override
	public int getType()
	{
		return TYPE;
	}

	@Override
	public String toString()
	{
		return "Rectangle [extentLeft=" + extentLeft + ", extentTop=" + extentTop + ", extentRight=" + extentRight + ", extentBottom=" + extentBottom + ", id=" + id + ", attachment=" + attachment + ", expired=" + expired + ", inverseMass=" + mass + ", friction=" + friction + ", restitution=" + restitution + ", start=" + start + ", end=" + end + "]";
	}
	
}
