package org.magnos.priori;


public interface IntersectionListener
{
	
	// This intersection was detected. Should it be resolved? We will be moving forward either way.
	// You might return false if you plan on expiring one of the shapes.
	public boolean onIntersection(Intersection inter);
	
	// A resolution has occurred
	public void onResolution(Intersection inter);
	
}
