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

 
public class CirclePlane extends JPanel implements MouseInputListener
{
 
   private static final long serialVersionUID = 1L;
 
   public static void main( String[] args )
   {
      JFrame window = new JFrame();
      window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      window.setTitle( "Circle Plane" );
      window.setLocationRelativeTo( null );
 
      CirclePlane space = new CirclePlane();
      window.add( space );
      window.setSize( 640, 480 );
      window.setResizable( false );
 
      window.setVisible( true );
 
      space.start();
   }
 
   public CirclePlane()
   {
      setBackground( Color.BLACK );
      addMouseListener( this );
      addMouseMotionListener( this );
      setDoubleBuffered( true );
   }
 
   public static final Font FONT = new Font( "Monospaced", Font.PLAIN, 12 );
 
   private enum DraggingState
   {
      START, END, RADIUS, NONE, PLANE_POINT, PLANE_NORMAL;
   }
 
   private float pointRadius = 8.0f;
   private Vector start;
   private Vector end;
   private Vector radiusPoint;
   private float radius;
   private Vector planePoint;
   private Vector planeNormal;
   private DraggingState dragging;
 
   public void start()
   {
      planePoint = new Vector( 150, 150 );
      planeNormal = new Vector( 120, 120 );
      start = new Vector( 50, 400 );
      end = new Vector( 320, 240 );
      radius = 40.0f;
      radiusPoint = new Vector( start.x, start.y - radius );
      dragging = DraggingState.NONE;
      
      Priori.loadDefaults();
   }
 
   public void paint( Graphics g )
   {
	  if (g == null || planeNormal == null)
	  {
		  return;
	  }
	   
      Graphics2D g2d = (Graphics2D)g;
      
      g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
 
      g2d.setColor( getBackground() );
      g2d.fillRect( 0, 0, getWidth(), getHeight() );
 
      Vector normal = planeNormal.sub( planePoint ).normali();
 
      g2d.setColor( Color.BLUE );
      g2d.draw( new Line2D.Float( planeNormal.x, planeNormal.y, planePoint.x, planePoint.y ) );
      g2d.draw( new Line2D.Float( planePoint.x + normal.y * 300, planePoint.y - normal.x * 300, planePoint.x - normal.y * 300, planePoint.y + normal.x * 300 ) );
 
      g2d.setColor( Color.WHITE );
      g2d.draw( new Line2D.Float( start.x, start.y, end.x, end.y ) );
 
      g2d.setColor( Color.GREEN );
      g2d.draw( new Ellipse2D.Float( start.x - pointRadius, start.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );
 
      g2d.setColor( Color.RED );
      g2d.draw( new Ellipse2D.Float( end.x - pointRadius, end.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );
 
      g2d.setColor( Color.YELLOW );
      g2d.draw( new Ellipse2D.Float( radiusPoint.x - pointRadius, radiusPoint.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );
      g2d.draw( new Ellipse2D.Float( planePoint.x - pointRadius, planePoint.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );
      g2d.draw( new Ellipse2D.Float( planeNormal.x - pointRadius, planeNormal.y - pointRadius, pointRadius * 2, pointRadius * 2 ) );
      g2d.draw( new Ellipse2D.Float( start.x - radius, start.y - radius, radius * 2, radius * 2 ) );
      g2d.draw( new Ellipse2D.Float( end.x - radius, end.y - radius, radius * 2, radius * 2 ) );
 
      // Check for intersection
 
      g2d.setColor( Color.LIGHT_GRAY );
      g2d.setFont( FONT );

      Circle c = new Circle( radius );
      c.setStart( start );
      c.setEnd( end );
      
      Plane p = Plane.fromPoint( planePoint, normal );
      p.setMass( 0.0f );
      
      Intersection inter = Priori.findIntersection( c, p );
      
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
		
		Priori.resolve( inter );
		
		g2d.setColor( Color.CYAN );
		g2d.draw( new Ellipse2D.Float( c.getStart().x - radius, c.getStart().y - radius, radius * 2, radius * 2 ) );
		g2d.draw( new Ellipse2D.Float( c.getEnd().x - radius, c.getEnd().y - radius, radius * 2, radius * 2 ) );
      }
   }
 
   public void mousePressed( MouseEvent e )
   {
      Vector mouse = new Vector( e.getX(), e.getY() );
 
      if (mouse.distance( start ) <= pointRadius)
      {
         dragging = DraggingState.START;
      }
      else if (mouse.distance( end ) <= pointRadius)
      {
         dragging = DraggingState.END;
      }
      else if (mouse.distance( radiusPoint ) <= pointRadius)
      {
         dragging = DraggingState.RADIUS;
      }
      else if (mouse.distance( planeNormal ) <= pointRadius)
      {
         dragging = DraggingState.PLANE_NORMAL;
      }
      else if (mouse.distance( planePoint ) <= pointRadius)
      {
         dragging = DraggingState.PLANE_POINT;
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
         end.set( mouse );
         break;
      case RADIUS:
         radiusPoint.set( mouse );
         radius = radiusPoint.distance( start );
         break;
      case START:
         start.set( mouse );
         radiusPoint.set( mouse );
         radiusPoint.y -= radius;
         break;
      case PLANE_NORMAL:
         planeNormal.set( mouse );
         break;
      case PLANE_POINT:
         Vector diff = planeNormal.sub( planePoint );
         planePoint.set( mouse );
         planeNormal.set( mouse );
         planeNormal.addi( diff );
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