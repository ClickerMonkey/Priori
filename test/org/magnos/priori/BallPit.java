
package org.magnos.priori;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import com.gameprogblog.engine.Game;
import com.gameprogblog.engine.GameLoop;
import com.gameprogblog.engine.GameLoopVariable;
import com.gameprogblog.engine.GameScreen;
import com.gameprogblog.engine.GameState;
import com.gameprogblog.engine.Scene;
import com.gameprogblog.engine.input.GameInput;


public class BallPit implements Game, IntersectionListener
{

	public static final int WIDTH = 640;
	public static final int HEIGHT = 480;
	
	public static final long GROUP_WALL = 1;
	public static final long GROUP_BALL = 2;

	public static void main( String[] args )
	{
		Game game = new BallPit();
		GameLoop loop = new GameLoopVariable( 0.1f );
		GameScreen screen = new GameScreen( WIDTH, HEIGHT, Color.black, true, loop, game );
		GameScreen.showWindow( screen, "Ball Pit!" );
	}

	private Priori prior;
	private Plane top;
	private Plane left;
	private Plane right;
	private Plane bottom;
	private List<Circle> balls;
	private boolean playing;
	private boolean simulating;
	private List<IntersectionInstance> intersections;

	class IntersectionInstance
	{

		Vector s = new Vector();
		Vector e = new Vector();
		float time;
		float radius;

		IntersectionInstance( Vector s, Vector e, float time, float radius )
		{
			this.s.set( s );
			this.e.set( e );
			this.time = time;
			this.radius = radius;
		}
	}

	@Override
	public void start( Scene scene )
	{
		Priori.loadDefaults();

		top = Plane.fromLine( 0, 0, WIDTH, 0 );
		top.setMass( 0.0f );
		top.setGroup(GROUP_WALL);
		top.setCollidesWith(GROUP_BALL);

		right = Plane.fromLine( WIDTH, 0, WIDTH, HEIGHT );
		right.setMass( 0.0f );
		right.setGroup(GROUP_WALL);
		right.setCollidesWith(GROUP_BALL);
		
		bottom = Plane.fromLine( WIDTH, HEIGHT, 0, HEIGHT );
		bottom.setMass( 0.0f );
		bottom.setGroup(GROUP_WALL);
		bottom.setCollidesWith(GROUP_BALL);
		
		left = Plane.fromLine( 0, HEIGHT, 0, 0 );
		left.setMass( 0.0f );
		left.setGroup(GROUP_WALL);
		left.setCollidesWith(GROUP_BALL);
		
		balls = new ArrayList<Circle>();
		for (int i = 0; i < 16; i++)
		{
			Circle b = new Circle();
			b.setRadius( rnd( 2, 20 ) );
			b.reset( rnd( b.getRadius(), WIDTH - b.getRadius() ), rnd( b.getRadius(), HEIGHT - b.getRadius() ) );
//			b.setMass( rnd( 0.1f, 5.0f ) );
//			b.setFriction( rnd( 0.0f, 0.2f ) );
//			b.setRestitution( rnd( 0.5f, 1.5f ) );
			b.setVelocity( rnd( -1000.0f, 1000f ), rnd( -1000, 1000 ) );
			b.setGroup(GROUP_BALL);
			b.setCollidesWith(GROUP_BALL | GROUP_WALL);
			balls.add( b );
		}

		prior = new Priori( new ShapeMatcherBruteForce(), 64 );
		prior.add( top, right, bottom, left );
		prior.add( balls );

		intersections = new ArrayList<IntersectionInstance>();

		playing = true;
	}

	@Override
	public void input( GameInput input )
	{
		if (input.keyDown[KeyEvent.VK_ESCAPE])
		{
			playing = false;
		}

		simulating = input.keyDown[KeyEvent.VK_P];
	}

	@Override
	public void update( GameState state, Scene scene )
	{
		if (simulating)
		{
			for (Circle b : balls)
			{
				b.updateState( state.seconds );
			}
			  
			intersections.clear();
			
			prior.getMatcher().findAllMatches( new ShapeMatcherListener()
			{

				public void onFoundMatch( Shape a, Shape b )
				{
					Intersection i = Priori.findIntersection( a, b );

					if (i != null && i.isValidTime())
					{
						if (i.subject instanceof Circle)
						{
							Circle c = (Circle)i.subject;
							intersections.add( new IntersectionInstance( c.start, c.end, i.time, c.radius ) );
						}

						if (i.object instanceof Circle)
						{
							Circle c = (Circle)i.object;
							intersections.add( new IntersectionInstance( c.start, c.end, i.time, c.radius ) );
						}
					}
				}
			} );
			
			prior.handleIntersections( this );
		}
	}

	@Override
	public void draw( GameState state, final Graphics2D gr, Scene scene )
	{
		for (Circle b : balls)
		{
			drawCircle( gr, b, Color.cyan, Color.blue, 3.0f );
		}
		
		gr.setColor( Color.red );
		gr.setStroke( new BasicStroke( 1.0f ) );

		for (IntersectionInstance i : intersections)
		{
			drawIntersection( gr, i );
		}

		gr.setColor( Color.white );
		gr.drawString( "#" + intersections.size(), 10, 26 );
		
		gr.drawString( "=" + draws++, 10, 46 );
	}
	
	public int draws = 0;

	private void drawIntersection( Graphics2D gr, IntersectionInstance i )
	{
		Vector m = new Vector().interpolatei( i.s, i.e, i.time );

		gr.draw( new Line2D.Float( i.s.x, i.s.y, m.x, m.y ) );
		gr.draw( new Ellipse2D.Float( m.x - i.radius, m.y - i.radius, i.radius * 2, i.radius * 2 ) );
	}

	private void drawCircle( Graphics2D gr, Circle circle, Color fillColor, Color outlineColor, float outlineWidth )
	{
		float radius = circle.getRadius();
		float diameter = radius * 2.0f;

		Ellipse2D.Float ellipse = new Ellipse2D.Float();
		ellipse.height = diameter;
		ellipse.width = diameter;
		ellipse.x = circle.getStart().x - radius;
		ellipse.y = circle.getStart().y - radius;

		drawShape( gr, ellipse, fillColor, outlineColor, outlineWidth );
	}

	private void drawShape( Graphics2D gr, java.awt.Shape shape, Color fillColor, Color outlineColor, float outlineWidth )
	{
		if (fillColor != null)
		{
			gr.setColor( fillColor );
			gr.fill( shape );
		}

		if (outlineColor != null && outlineWidth != 0.0f)
		{
			gr.setStroke( new BasicStroke( outlineWidth ) );
			gr.setColor( outlineColor );
			gr.draw( shape );
		}
	}

	@Override
	public void destroy()
	{

	}

	@Override
	public boolean isPlaying()
	{
		return playing;
	}

	@Override
	public boolean onIntersection( Intersection inter )
	{
		return true;
	}

	@Override
	public void onResolution( Intersection inter )
	{
		if (inter.subject instanceof Circle)
		{
			inter.subject.velocity.reflecti( inter.normal );
		}
		
		if (inter.object instanceof Circle)
		{
			inter.object.velocity.reflecti( inter.normal );
		}
	}

	private float rnd( float min, float max )
	{
		return (float)((max - min) * Math.random() + min);
	}

}
