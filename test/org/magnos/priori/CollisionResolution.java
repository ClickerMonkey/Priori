package org.magnos.priori;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.List;

import com.gameprogblog.engine.Game;
import com.gameprogblog.engine.GameLoop;
import com.gameprogblog.engine.GameLoopVariable;
import com.gameprogblog.engine.GameScreen;
import com.gameprogblog.engine.GameState;
import com.gameprogblog.engine.Scene;
import com.gameprogblog.engine.input.GameInput;


public class CollisionResolution implements Game
{

   public static void main( String[] args )
   {
      Game game = new CollisionResolution();
      GameLoop loop = new GameLoopVariable( 0.1f );
      GameScreen screen = new GameScreen( 640, 480, Color.black, true, loop, game );
      GameScreen.showWindow( screen, "CollisionResolution" );
   }

   public static final Font FONT = new Font( "Monospaced", Font.PLAIN, 12 );

   private float draggableRadius = 8.0f;
   private List<Vector> draggables;
   private int draggingIndex = -1;
   private Vector pos0, pos1, velp0, velp1, normalp;
   private boolean playing = false;
   private float mass0, mass1, restitution0, restitution1, friction0, friction1;

   @Override
   public void start( Scene scene )
   {
      pos0 = new Vector( 300, 300 );
      pos1 = new Vector( 400, 400 );
      velp0 = new Vector( 400, 300 );
      velp1 = new Vector( 300, 400 );
      normalp = new Vector( 320, 320 );
      draggables = Arrays.asList( pos0, pos1, velp0, velp1, normalp );

      mass0 = 1.0f;
      mass1 = 1.0f;
      restitution0 = 1.0f;
      restitution1 = 1.0f;
      friction0 = 0.0f;
      friction1 = 0.0f;

      playing = true;
   }

   @Override
   public void input( GameInput input )
   {
      if (input.keyDown[KeyEvent.VK_ESCAPE])
      {
         playing = false;
      }

      if (input.keyUp[KeyEvent.VK_Q]) mass0 -= 0.01f;
      if (input.keyUp[KeyEvent.VK_W]) mass0 += 0.01f;
      if (input.keyUp[KeyEvent.VK_A]) mass1 -= 0.01f;
      if (input.keyUp[KeyEvent.VK_S]) mass1 += 0.01f;
      if (input.keyUp[KeyEvent.VK_E]) restitution0 -= 0.01f;
      if (input.keyUp[KeyEvent.VK_R]) restitution0 += 0.01f;
      if (input.keyUp[KeyEvent.VK_D]) restitution1 -= 0.01f;
      if (input.keyUp[KeyEvent.VK_F]) restitution1 += 0.01f;
      if (input.keyUp[KeyEvent.VK_T]) friction0 -= 0.01f;
      if (input.keyUp[KeyEvent.VK_Y]) friction0 += 0.01f;
      if (input.keyUp[KeyEvent.VK_G]) friction1 -= 0.01f;
      if (input.keyUp[KeyEvent.VK_H]) friction1 += 0.01f;

      Vector mouse = new Vector( input.mouseX, input.mouseY );

      if (draggingIndex != -1 && !input.mouseDragging)
      {
         draggingIndex = -1;
      }

      if (draggingIndex == -1 && input.mouseDragging)
      {
         for (int i = 0; i < draggables.size(); i++)
         {
            if (mouse.distance( draggables.get( i ) ) < draggableRadius)
            {
               draggingIndex = i;
            }
         }
      }

      if (draggingIndex != -1)
      {
         draggables.get( draggingIndex ).set( mouse );
      }
   }

   @Override
   public void update( GameState state, Scene scene )
   {
   }

   @Override
   public void draw( GameState state, Graphics2D gr, Scene scene )
   {
      gr.setColor( Color.blue );

      for (Vector d : draggables)
      {
         gr.draw( new Ellipse2D.Float( d.x - draggableRadius, d.y - draggableRadius, draggableRadius * 2, draggableRadius * 2 ) );
      }

      gr.setColor( Color.cyan );
      drawVector( gr, pos0.x, pos0.y, velp0.x, velp0.y );
      drawVector( gr, pos1.x, pos1.y, velp1.x, velp1.y );
      drawVector( gr, (pos0.x + pos1.x) * 0.5f, (pos0.y + pos1.y) * 0.5f, normalp.x, normalp.y );

      Vector contact = pos0.add( pos1 ).muli( 0.5f );
      Vector vel0 = velp0.sub( pos0 );
      Vector vel1 = velp1.sub( pos1 );
      Vector normal = normalp.sub( contact ).normali();

      float friction = Math.max( friction0, friction1 );
      float restitution = (restitution0 * restitution1) * 0.5f + 0.5f;
      float radius = pos0.distance( pos1 ) * 0.5f;
      float masssum = 2.0f / (mass0 + mass1);

      gr.setColor( Color.gray );
      gr.draw( new Ellipse2D.Float( contact.x - 3, contact.y - 3, 3 * 2, 3 * 2 ) );
      gr.draw( new Ellipse2D.Float( pos0.x - radius, pos0.y - radius, radius * 2, radius * 2 ) );
      gr.draw( new Ellipse2D.Float( pos1.x - radius, pos1.y - radius, radius * 2, radius * 2 ) );

      int textY = 8;
      gr.setColor( Color.white );
      gr.setFont( FONT );
      gr.drawString( "vel0: " + vel0, 8, textY += 16 );
      gr.drawString( "vel1: " + vel1, 8, textY += 16 );
      gr.drawString( "normal: " + normal, 8, textY += 16 );
      gr.drawString( "mass0[q/w]: " + mass0, 8, textY += 16 );
      gr.drawString( "mass1[a/s]: " + mass1, 8, textY += 16 );
      gr.drawString( "restitution0[e/r]: " + restitution0, 8, textY += 16 );
      gr.drawString( "restitution1[d/f]: " + restitution1, 8, textY += 16 );
      gr.drawString( "friction0[t/y]: " + friction0, 8, textY += 16 );
      gr.drawString( "friction1[g/h]: " + friction1, 8, textY += 16 );
      gr.drawString( "friction: " + friction, 8, textY += 16 );
      gr.drawString( "restitution: " + restitution, 8, textY += 16 );

      Vector V = vel0.sub( vel1 );
      Vector Vn = normal.mul( V.dot( normal ) );
      Vector Vt = V.sub( Vn );
      Vector Vprime = Vn.mul( restitution ).addsi( Vt, friction );
      
      vel0.addsi( Vprime, -1 );
      vel0.muli( mass1 * masssum );
      
      vel1.addsi( Vprime, +1 );
      vel1.muli( mass0 * masssum );
      
      gr.setColor( Color.orange );
      drawVector( gr, contact.x, contact.y, contact.x + Vn.x, contact.y + Vn.y );
      gr.setColor( Color.pink );
      drawVector( gr, contact.x, contact.y, contact.x + Vt.x, contact.y + Vt.y );
      gr.setColor( Color.red );
      drawVector( gr, pos0.x, pos0.y, pos0.x + vel0.x, pos0.y + vel0.y );
      drawVector( gr, pos1.x, pos1.y, pos1.x + vel1.x, pos1.y + vel1.y );
   }

   private void drawVector( Graphics2D gr, float x0, float y0, float x1, float y1 )
   {
      Vector v = new Vector( x1 - x0, y1 - y0 );
      Vector v0 = v.rotate( 0.3f ).normali().muli( draggableRadius * 2 );
      Vector v1 = v.rotate( -0.3f ).normali().muli( draggableRadius * 2 );

      gr.draw( new Line2D.Float( x0, y0, x1, y1 ) );
      gr.draw( new Line2D.Float( x1, y1, x1 - v0.x, y1 - v0.y ) );
      gr.draw( new Line2D.Float( x1, y1, x1 - v1.x, y1 - v1.y ) );

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

}