
package org.magnos.priori;

public class IndexPair
{

	public int min;
	public int max;

	public IndexPair( int a, int b )
	{
		set( a, b );
	}
	
	public void set(int a, int b)
	{
		min = Math.min( a, b );
		max = Math.max( a, b );
	}

	public boolean hasIndex( int index )
	{
		return (index == min || index == max);
	}

	@Override
	public int hashCode()
	{
		return (min << 16) ^ max;
	}

	@Override
	public boolean equals( Object o )
	{
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		IndexPair other = (IndexPair)o;

		return (min == other.min && max == other.max);
	}

	@Override
	public String toString()
	{
		return "[" + min + "," + max + "]";
	}

}
