package pathfinderv4;

import java.awt.Color;
import java.awt.Graphics2D;

public class Point{
      
    private final int x, y, offset;
    private int scale;
    private Game game;
    private final PointType pointType;
    public boolean visited;
    
    //class holds locations of start/end points, and paints them
    public Point(int x, int y, int scale, int offset, Game game, PointType pointType){
        this.x = x; 
        this.y = y;
        this.scale = scale;
        this.offset = offset; 
        this.game = game;
        this.pointType = pointType;
        visited = false;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y; 
    }
    public void tick(){
        scale = game.scale;
    }
    
    public void render(Graphics2D g){
        if(null != this.pointType)switch (pointType) {
            case Start:
                g.setColor(Color.green);
                break;
            case End:
                g.setColor(Color.red);
                break;
            case Wall:
                g.setColor(Color.black);
                break;

            default:
            break;
        }
        
        g.fillRect(x*scale+1, y*scale + offset + 1 , scale-1, scale-1);
    }


    
}
