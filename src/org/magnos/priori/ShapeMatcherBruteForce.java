
package org.magnos.priori;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShapeMatcherBruteForce implements ShapeMatcher
{

	private Map<Integer, Shape> shapeMap = new HashMap<Integer, Shape>();

	@Override
	public void onShapeAdd( Shape s )
	{
		shapeMap.put( s.id, s );
	}

	@Override
	public void onShapeExpire( Shape s )
	{
		shapeMap.remove( s.id );
	}

	@Override
	public void prepare()
	{

	}

	@Override
	public void findAllMatches( ShapeMatcherListener listener )
	{
		List<Shape> shapeList = new ArrayList<Shape>( shapeMap.values() );
		int shapeMax = shapeList.size() - 1;

		for (int i = 0; i < shapeMax; i++)
		{
			for (int j = i + 1; j <= shapeMax; j++)
			{
				Shape a = shapeList.get( i );
				Shape b = shapeList.get( j );

				if (Shape.canCollide( a, b ))
				{
					listener.onFoundMatch( a, b );
				}
			}
		}
	}

	@Override
	public void findMatches( Shape a, ShapeMatcherListener listener )
	{
		for (Shape b : shapeMap.values())
		{
			if (a != b && Shape.canCollide( a, b ))
			{
				listener.onFoundMatch( a, b );
			}
		}
	}

}
