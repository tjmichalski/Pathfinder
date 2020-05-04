package pathfinderv4;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

public class Handler implements MouseListener, MouseMotionListener{
    
    private int scale, offset;
    private Game game;
    private ArrayList<Point> walls;
    private ArrayList<CheckPoint> checkPoints;
    private Point start;
    private Point end;
    
    //algorithim finished
    private boolean finished = false;
    
    public int solveSpeed = 10;
    
    public Handler(Game game, int scale, int offset){
        this.game = game;
        this.scale = scale; 
        this.offset = offset;
        walls = new ArrayList<>();
        checkPoints = new ArrayList<>();
    }
    
    public void solver(){
        
        //sets first checkPoint = start node, adds it to checkPoints list
        CheckPoint startCheck = new CheckPoint(start.getX(), start.getY(), scale, offset, game, PointType.CheckPoint, null, end, start);
        checkPoints.add(startCheck);
        
        while(!finished){
            //find values for current iteration point, flags visited as true
            startCheck.setValues();
            startCheck.visited = true;
            
            //nested for loops allow the checking of all 8 coordinate pairs surrounding current iteration point (startCheck)
            for(int i=startCheck.getY()-1; i<=startCheck.getY()+1; i++){
                if(i < 0) i++;

                for(int j=startCheck.getX()-1; j<=startCheck.getX()+1; j++){
                    if(j < 0) j++;
                    
                    //creates dummy checkPoint on coordinates j, i from nested loops
                    //sets neighbor to current iteration point
                    CheckPoint tempCheck = new CheckPoint(j, i, scale, offset, game, PointType.CheckPoint, startCheck, end, start);
                    tempCheck.setValues();

                    //true when no structures have been found on dummy checkPoint's location
                    boolean clear = true;
                    
                    //checks for walls on dummy points location
                    for(int p=0; p<walls.size(); p++){
                        if(walls.get(p).getX() == j && walls.get(p).getY() ==  i){
                            clear = false;
                        }
                    }
                    
                    //assures no startPoint on dummy location
                    if(startCheck.getX() == j && startCheck.getY() == i){
                        clear = false;
                    }
                    
                    //true when dummy point is on top of end point - indicates final while loop
                    else if(end.getX() == j && end.getY() == i){
                        finished = true;
                    }
                    
                    //checks for any checkPoints on dummy checkPoints location
                    //if true, checks fVal of both, smaller one stays
                    //if fVals are equal, smalled hVal stays
                    for(int p=0; p<checkPoints.size(); p++){
                        if(checkPoints.get(p).getX() == j && checkPoints.get(p).getY() == i){
                            clear = false;
                            if(tempCheck.getfVal() < checkPoints.get(p).getfVal()){
                                checkPoints.set(p, tempCheck);
                            }else if(tempCheck.getfVal() == checkPoints.get(p).getfVal()){
                                if(tempCheck.gethVal() < checkPoints.get(p).gethVal()){
                                    checkPoints.set(p, tempCheck);
                                }
                            }
                        }
                    }
                    //if no obstructions found, add dummy point to official list
                    if(clear){
                        checkPoints.add(tempCheck);
                    }
                }
            }
            
            //arbitrarily high number required for alg.
            int minFval = 100000;
            
            //sets next starting point based of lowest fVal, then lowest hVal
            for(int i=0; i<checkPoints.size(); i++){
                if(checkPoints.get(i).getfVal() < minFval && checkPoints.get(i).visited != true){
                    minFval = checkPoints.get(i).getfVal();
                    startCheck = checkPoints.get(i);
                }else if(checkPoints.get(i).getfVal() == minFval && checkPoints.get(i).visited != true){
                    if(checkPoints.get(i).gethVal() < startCheck.gethVal()){
                        startCheck = checkPoints.get(i);
                    }
                }
            }
            //timer to slow program down for visual
            try{
                TimeUnit.MILLISECONDS.sleep(solveSpeed);
            }catch(InterruptedException e){
                System.err.format("IOException: %s%n", e);
            }
        
        }
        
        //executes after finished variable is true
        //begins painting winning route blue
        startCheck.winner = true;
        
    }
    
    //updates scale variable among various objects
    public void tick(){
        scale = game.scale;
        if(start != null) start.tick();
        if(end != null) end.tick();
        if(walls != null){
            for(int i=0; i < walls.size(); i++){
                walls.get(i).tick();
            }
        }
        if(checkPoints != null){
            for(int i=0; i < checkPoints.size(); i++){
                checkPoints.get(i).tick();
            }
        }
    }
    
    //calls render on the various structures
    public void render(Graphics2D g){
        if(start != null)start.render(g);
        if(end != null)end.render(g);
        if(walls != null){
            for(int i=0; i < walls.size(); i++){
                walls.get(i).render(g);
            }
        }
        if(checkPoints != null){
            for(int i=0; i < checkPoints.size(); i++){
            checkPoints.get(i).render(g);
            }
        }
    }
    
    //used for drawing walls
    @Override
    public void mouseDragged(MouseEvent e) {
        //walls option must be selected
        if(game.gameState == Game.GameState.Walls){
            int mx = e.getX()/scale;
            int my = (e.getY()-offset)/scale;

            //false when a structure has been detected on coordinates you try to draw a wall on
            boolean clear = true;

            //left mouse button draws walls
            if(SwingUtilities.isLeftMouseButton(e) && (e.getY()-offset > 0)){
                //loops through walls array - if it finds a wall with same coords as mousedrag, sets clear to false so that it wont have multiple walls on same coords
                for(int i=0; i < walls.size(); i++){
                   if(walls.get(i).getX() == mx && walls.get(i).getY() == my){
                       clear = false;
                   } 
                }
                //add wall if no obstructions found
                if(clear){
                    walls.add(new Point(mx, my, scale, offset, game, PointType.Wall));
                }
            }
            //right button erases walls
            else if(SwingUtilities.isRightMouseButton(e) && (e.getY()-offset) > 0){
                //loops through array - if it finds a wall object with same coordinates as mousedrag, removes it form list
                for(int i=0; i < walls.size(); i++){
                   if(walls.get(i).getX() == mx && walls.get(i).getY() == my){
                       walls.remove(i);
                   } 
                }
            }
        }
    }
    
    //called when reset button clicked
    private void reset(){
        start = null;
        end = null;
        walls.clear();
        checkPoints.clear();
        finished = false;
    }
    
    //used to draw start/end points
    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX()/scale;
        int my = (e.getY()-offset)/scale;

        //left clicks add components - second condition returns false if clicks are in options area in order to not add components when choosing options
        if(SwingUtilities.isLeftMouseButton(e) && (e.getY()-offset) > 0){
            
            //if gamestate == start then remove old start (only one start on board at a time) and place new start at location
            //if == end, same thing with end point
            if(game.gameState == Game.GameState.Player){
                start = null;
                start = new Point(mx, my, scale, offset, game, PointType.Start);
            }else if(game.gameState == Game.GameState.End){
                end = null;
                end = new Point(mx, my, scale, offset, game, PointType.End);
            }
        }
        //right button erases components 
        else if(SwingUtilities.isRightMouseButton(e)){
            if(game.gameState == Game.GameState.Player && start != null){
                if(mx == start.getX() && my == start.getY()){
                    start = null;
                }
            }else if(game.gameState == Game.GameState.End && end != null){
                if(mx == end.getX() && my == end.getY()){
                    end = null;
                }
            }
            //solve and reset button detections - shouldve gone in game class
        }else if(e.getY() - offset < 0 && e.getX() > 1+(game.WIDTH-7)/3 && e.getX() < (game.WIDTH-7)/2){
            solver();
        }else if(e.getY() - offset < 0 && e.getX() > 1+(game.WIDTH-7)/2 && e.getX() < (game.WIDTH-7)/3*2){
            reset();
        }
        
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
