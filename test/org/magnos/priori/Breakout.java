
package org.magnos.priori;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.gameprogblog.engine.Game;
import com.gameprogblog.engine.GameLoop;
import com.gameprogblog.engine.GameLoopVariable;
import com.gameprogblog.engine.GameScreen;
import com.gameprogblog.engine.GameState;
import com.gameprogblog.engine.Scene;
import com.gameprogblog.engine.input.GameInput;


public class Breakout implements Game, IntersectionListener
{

	public static final int WIDTH = 640;
	public static final int HEIGHT = 480;

	public static final long GROUP_BALL 	= 1 << 0;
	public static final long GROUP_BRICK 	= 1 << 1;
	public static final long GROUP_WALL 	= 1 << 2;
	public static final long GROUP_PADDLE 	= 1 << 3;
	
	public static void main( String[] args )
	{
		Game game = new Breakout();
		GameLoop loop = new GameLoopVariable( 0.1f );
		GameScreen screen = new GameScreen( WIDTH, HEIGHT, Color.black, true, loop, game );
		screen.setBackground( Color.black );
		GameScreen.showWindow( screen, "Awwwwww.... Breakout!" );
	}

	private Priori prior;
	private Plane top;
	private Plane left;
	private Plane right;
	private Plane bottom;
	private Rectangle paddle;
	private Ball ball;
	private boolean playing;
	private List<Rectangle> blocks;

	private class Ball
	{

		Vector direction;
		float velocity;
		Circle shape;
	}

	@Override
	public void start( Scene scene )
	{
		Priori.loadDefaults();

		top = Plane.fromLine( 0, 0, WIDTH, 0 );
		top.setMass( 0.0f );
		top.setGroup( GROUP_WALL );
		top.setCollidesWith( GROUP_BALL );

		right = Plane.fromLine( WIDTH, 0, WIDTH, HEIGHT );
		right.setMass( 0.0f );
		right.setGroup( GROUP_WALL );
		right.setCollidesWith( GROUP_BALL );

		bottom = Plane.fromLine( WIDTH, HEIGHT, 0, HEIGHT );
		bottom.setMass( 0.0f );
		bottom.setGroup( GROUP_WALL );
		bottom.setCollidesWith( GROUP_BALL );

		left = Plane.fromLine( 0, HEIGHT, 0, 0 );
		left.setMass( 0.0f );
		left.setGroup( GROUP_WALL );
		left.setCollidesWith( GROUP_BALL );

		paddle = new Rectangle( WIDTH * 0.5f, HEIGHT - 40f, -60f, -10f, 60f, 10f );
		paddle.setMass( 0.0f );
		paddle.setGroup( GROUP_PADDLE );
		paddle.setCollidesWith( GROUP_BALL );

		ball = new Ball();
		ball.velocity = 1000.0f;
		ball.shape = new Circle( 20, WIDTH * 0.5f, HEIGHT * 0.75f );
		ball.direction = new Vector( 1, -1 ).normali();
		ball.shape.setGroup( GROUP_BALL );
		ball.shape.setCollidesWith( GROUP_BRICK | GROUP_PADDLE | GROUP_WALL );

		blocks = new ArrayList<Rectangle>();
		for (int y = 0; y < 5; y++)
		{
			for (int x = 0; x < 10; x++)
			{
				Rectangle b = new Rectangle( x * 50 + 80, y * 30 + 80, -20, -10, 20, 10 );
				b.setMass( 0.0f );
				b.setGroup( GROUP_BRICK );
				b.setCollidesWith( GROUP_BALL );
				blocks.add( b );
			}
		}

		prior = new Priori( new ShapeMatcherBruteForce(), 64 );
		prior.add( top, right, bottom, left, ball.shape, paddle );
		prior.add( blocks );

		playing = true;
	}

	@Override
	public void input( GameInput input )
	{
		if (input.keyDown[KeyEvent.VK_ESCAPE])
		{
			playing = false;
		}

		paddle.getEnd().x = input.mouseX;
	}

	@Override
	public void update( GameState state, Scene scene )
	{
		Vector e = ball.shape.getEnd();
		Vector s = ball.shape.getStart();
		e.set( s ).addsi( ball.direction, ball.velocity * state.seconds );

		prior.prune();
		prior.handleIntersections( this );
		prior.prune();
	}

	@Override
	public void draw( GameState state, Graphics2D gr, Scene scene )
	{
		for (Rectangle b : blocks)
		{
			if (!b.isExpired())
			{
				drawRectangle( gr, b, Color.red, Color.magenta, 3.0f );
			}
		}

		drawRectangle( gr, paddle, Color.lightGray, Color.darkGray, 3.0f );
		drawCircle( gr, ball.shape, Color.cyan, Color.blue, 3.0f );
	}

	private void drawRectangle( Graphics2D gr, Rectangle rectangle, Color fillColor, Color outlineColor, float outlineWidth )
	{
		Rectangle2D.Float rect = new Rectangle2D.Float();
		rect.width = rectangle.getExtentRight() - rectangle.getExtentLeft();
		rect.height = rectangle.getExtentBottom() - rectangle.getExtentTop();
		rect.x = rectangle.getStart().x + rectangle.getExtentLeft();
		rect.y = rectangle.getStart().y + rectangle.getExtentTop();

		drawShape( gr, rect, fillColor, outlineColor, outlineWidth );
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
		return inter.hasShape( ball.shape );
	}

	@Override
	public void onResolution( Intersection inter )
	{
		inter.getFutureDirection( ball.direction );
		
		Shape other = inter.getOther( ball.shape );
		
		if (other.getType() == Rectangle.TYPE && other != paddle)
		{
			other.expire();
		}
	}

}
