package pathfinderv4;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import static java.lang.Math.abs;

public class CheckPoint {
    private int hVal, fVal, gVal;
    private CheckPoint neighbor;
    private Point start;
    private Point end;
    private int x, y, scale, offset;
    private Game game;
    private PointType pointType;
    
    //visited variable used to avoid revisiting one point more than once
    public boolean visited;
    public boolean winner = false;
    
    
    public CheckPoint(int x, int y, int scale, int offset, Game game, PointType pointType, CheckPoint neighbor, Point end, Point start) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.offset = offset;
        this.game = game;
        this.pointType = pointType;
        this.neighbor = neighbor;
        this.end = end;
        this.start = start;
    }
    
    
    //sets algorithim values for solver() (fVal, hVal, gVal)
    public void setValues(){
        //if no neighbor, that means its the starting point, so gVal is 0
        //else adds 14 to gVal for every diagonal move it needs to make to get to end, and 10 for every horizontal/vertical move
        if(neighbor == null){
            gVal = 0;
        }else{
            if(abs(neighbor.getX()-x) == 1 && abs(neighbor.getY()-y) == 1){
                gVal = neighbor.getGval() + 14;
            }else{
                gVal = neighbor.getGval() + 10;
            }
        }
        
        //tempDiags find offsets between x values, then y values
        int tempXdiag = abs(end.getX() - x);
        int tempYdiag = abs(end.getY() - y);
        
        //whichever offset is smaller, add 14 for each, then add 10 for remainder of big-small
        if(tempXdiag > tempYdiag){
           hVal = tempYdiag*14 + 10*(tempXdiag - tempYdiag); 
        }else if(tempYdiag > tempXdiag){
            hVal = tempXdiag*14 + 10*(tempYdiag-tempXdiag);
        }else if(tempYdiag == tempXdiag){
            hVal = 14*tempXdiag;
        }
        
        //simple enough
        fVal = hVal + gVal;
    }
     public void render(Graphics2D g){
        //winner is true when this checkPoint is on top of end point
        //sets blue if winner, then if it has a neighbor, calls this function in it's neighbor instance
         if(winner){
            g.setColor(Color.blue);
            if(neighbor != null) neighbor.winner = true;
        }else{
            g.setColor(Color.yellow);
        }
         //paints checkPoint
        g.fillRect(x*scale+1, y*scale + offset + 1 , scale-1, scale-1);
        
        //paints algorithim values if scale > 50
        if(scale >=50){
            g.setColor(Color.black);
            Font font = new Font("Arial", 1, scale/5);
            Font font2 = new Font("Arial", 1, scale/3);   
            g.setFont(font);
            g.drawString(Integer.toString(gVal), x*scale+scale/16, y*scale+offset+scale/5);
            g.drawString(Integer.toString(hVal), x*scale+scale/5*3, y*scale+offset+scale/5);
            g.setFont(font2);
            g.drawString(Integer.toString(fVal), x*scale+scale/3, y*scale+offset+scale/4*3);
        }
    }
    
     //called from handler, updates scale
     public void tick(){
        scale = game.scale;
    }
     
    public int getGval(){
        return gVal;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
     public int gethVal() {
        return hVal;
    }

    public void sethVal(int hVal) {
        this.hVal = hVal;
    }

    public int getfVal() {
        return fVal;
    }

    public void setfVal(int fVal) {
        this.fVal = fVal;
    }

    public int getgVal() {
        return gVal;
    }

    public void setgVal(int gVal) {
        this.gVal = gVal;
    }

    
}
