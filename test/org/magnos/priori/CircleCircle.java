package org.magnos.priori;



import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.magnos.priori.Circle;
import org.magnos.priori.Intersection;
import org.magnos.priori.Priori;
import org.magnos.priori.ShapeMatcherBruteForce;
import org.magnos.priori.Vector;


public class CircleCircle extends JPanel implements MouseInputListener
{

	private static final long serialVersionUID = 1L;

	public static void main( String[] args )
	{
		JFrame window = new JFrame();
		window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		window.setTitle( "Priori: Circle -> Circle" );
		window.setLocationRelativeTo( null );

		CircleCircle space = new CircleCircle();
		window.add( space );
		window.setSize( 640, 480 );
		window.setResizable( false );

		window.setVisible( true );

		space.start();
	}

	public CircleCircle()
	{
		setBackground( Color.BLACK );
		addMouseListener( this );
		addMouseMotionListener( this );
		setDoubleBuffered( true );
	}

	public static final Font FONT = new Font( "Monospaced", Font.PLAIN, 12 );

	private enum DraggingState
	{
		START, END, RADIUS, NONE, OTHER_CENTER, OTHER_RADIUS;
	}

	private float pointRadius = 8.0f;

	private Circle subject;
	private Vector subjectRadiusPoint;

	private Circle object;
	private Vector objectRadiusPoint;

	private DraggingState dragging;

	private Priori prior;

	public void start()
	{
		Priori.loadDefaults();

		object = new Circle( 40.0f );
		object.setStart( 50, 400 );
		object.setEnd( 320, 240 );
		objectRadiusPoint = new Vector( object.getStart().x, object.getStart().y - object.getRadius() );

		subject = new Circle( 50.0f );
		subject.reset( 320, 240 );
		subjectRadiusPoint = new Vector( subject.getStart().x, subject.getStart().y - subject.getRadius() );

		prior = new Priori( new ShapeMatcherBruteForce(), 16 );
		prior.add( object );
		prior.add( subject );

		dragging = DraggingState.NONE;
		
		repaint();
	}

	public void paint( Graphics g )
	{
		Graphics2D g2d = (Graphics2D)g;

		if (prior == null)
		{
			return;
		}

		Vector start = object.getStart();
		Vector end = object.getEnd();
		float radius = object.getRadius();
		float otherRadius = subject.getRadius();
		Vector otherCenter = subject.getStart();

		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g2d.setColor( getBackground() );
		g2d.fillRect( 0, 0, getWidth(), getHeight() );

		g2d.setColor( Color.WHITE );
		g2d.draw( new Line2D.Float( start.x, start.y, end.x, end.y ) );

		g2d.setColor( Color.BLUE );
		g2d.draw( new Ellipse2D.Float( otherCenter.x - otherRadius, otherCenter.y - otherRadius, otherRadius * 2, otherRadius * 2 ) );

		g2d.setColor( Color.GREEN );
		g2d.draw( new Ellipse2D.Float( start.x - pointRadius, start.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );

		g2d.setColor( Color.RED );
		g2d.draw( new Ellipse2D.Float( end.x - pointRadius, end.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );

		g2d.setColor( Color.YELLOW );
		g2d.draw( new Ellipse2D.Float( objectRadiusPoint.x - pointRadius, objectRadiusPoint.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );
		g2d.draw( new Ellipse2D.Float( subjectRadiusPoint.x - pointRadius, subjectRadiusPoint.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );
		g2d.draw( new Ellipse2D.Float( otherCenter.x - pointRadius, otherCenter.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );
		g2d.draw( new Ellipse2D.Float( start.x - radius, start.y - radius, radius * 2, radius * 2 ) );
		g2d.draw( new Ellipse2D.Float( end.x - radius, end.y - radius, radius * 2, radius * 2 ) );

		g2d.setColor( Color.LIGHT_GRAY );
		g2d.setFont( FONT );

		Intersection inter = Priori.findIntersection( subject, object );
		
		if (inter != null)
		{
			Vector intersection = inter.getObjectIntersectionPosition(new Vector());
			Vector future = inter.getFutureObjectPosition(new Vector());

			g2d.setColor( Color.LIGHT_GRAY );
			g2d.drawString( "time: " + inter.time, 10, 20 );

			g2d.setColor( Color.GRAY );
			g2d.draw( new Ellipse2D.Float( intersection.x - radius, intersection.y - radius, radius * 2, radius * 2 ) );
			g2d.draw( new Line2D.Float( intersection.x, intersection.y, intersection.x + inter.normal.x * 20, intersection.y + inter.normal.y * 20 ) );

			g2d.setColor( Color.RED );
			g2d.draw( new Ellipse2D.Float( inter.contact.x - 2, inter.contact.y - 2, 4, 4 ) );

			g2d.setColor( Color.DARK_GRAY );
			g2d.draw( new Ellipse2D.Float( future.x - radius, future.y - radius, radius * 2, radius * 2 ) );
			g2d.draw( new Line2D.Float( intersection.x, intersection.y, future.x, future.y ) );
		}
	}

	public void mousePressed( MouseEvent e )
	{
		Vector mouse = new Vector( e.getX(), e.getY() );

		if (mouse.distance( object.getStart() ) <= pointRadius)
		{
			dragging = DraggingState.START;
		}
		else if (mouse.distance( object.getEnd() ) <= pointRadius)
		{
			dragging = DraggingState.END;
		}
		else if (mouse.distance( objectRadiusPoint ) <= pointRadius)
		{
			dragging = DraggingState.RADIUS;
		}
		else if (mouse.distance( subjectRadiusPoint ) <= pointRadius)
		{
			dragging = DraggingState.OTHER_RADIUS;
		}
		else if (mouse.distance( subject.getStart() ) <= pointRadius)
		{
			dragging = DraggingState.OTHER_CENTER;
		}
		else
		{
			dragging = DraggingState.NONE;
		}
	}

	public void mouseReleased( MouseEvent e )
	{
		dragging = DraggingState.NONE;
	}

	public void mouseDragged( MouseEvent e )
	{
		Vector mouse = new Vector( e.getX(), e.getY() );

		switch (dragging)
		{
		case END:
			object.setEnd( mouse );
			break;
		case RADIUS:
			objectRadiusPoint.set( mouse );
			object.setRadius( objectRadiusPoint.distance( object.getStart() ) );
			break;
		case OTHER_RADIUS:
			subjectRadiusPoint.set( mouse );
			subject.setRadius( subjectRadiusPoint.distance( subject.getStart() ) );
			break;
		case START:
			object.setStart( mouse );
			objectRadiusPoint.set( mouse );
			objectRadiusPoint.y -= object.getRadius();
			break;
		case OTHER_CENTER:
			subject.setStart( mouse );
			subjectRadiusPoint.set( mouse );
			subjectRadiusPoint.y -= subject.getRadius();
			break;
		case NONE:
			break;
		}

		repaint();
	}

	// Unused Mouse Listener Methods
	public void mouseMoved( MouseEvent e )
	{
	}

	public void mouseClicked( MouseEvent e )
	{
	}

	public void mouseEntered( MouseEvent e )
	{
	}

	public void mouseExited( MouseEvent e )
	{
	}

}
