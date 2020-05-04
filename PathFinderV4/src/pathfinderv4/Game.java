package pathfinderv4;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;

/**
 *
 * @author Tylar
 */
public class Game extends Canvas implements Runnable, MouseWheelListener, MouseListener, MouseMotionListener{
    
    public final int HEIGHT = 800;
    public final int WIDTH = HEIGHT*9/6;
    
    //scale controls how big everything is
    //offset used to adjust grid to start below the options panel
    public int scale, offset;
    public GameState gameState;
    
    private Thread thread;
    private boolean running = false;
    private Handler handler;
    
    public enum GameState{
        Walls,
        Player,
        End,
    };
    
    public Game(){
        this.scale = 80;
        this.offset = 40;
        new Window(WIDTH, HEIGHT, "PathFinder V2", this);
        handler = new Handler(this, scale, offset);
        this.addMouseWheelListener(this);
        this.addMouseListener(this);
        addMouseListener(handler);
        addMouseMotionListener(handler);
        //starts game on walls option
        gameState = GameState.Walls;
    }
    
    public void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs==null){
            this.createBufferStrategy(2);
            return;
        }
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        
        g.setColor(Color.gray);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        drawBoard(g);
        drawOptions(g);
        handler.render(g);
                
        g.dispose();
        bs.show();
    }
    
    public void drawOptions(Graphics2D g){
        g.setColor(Color.black);
        Font font = new Font("Arial", 1, 25);
        g.setFont(font);
        //whole top pane box
        g.drawRect(0,0, WIDTH-7, 40);
        
        //fills circle of structure option based on varibale gameState
        g.setColor(Color.red);
        if(null != gameState)switch (gameState) {
            case Walls:
                g.fillOval(200, 15, 15, 15);
                break;
            case Player:
                g.fillOval(85, 15, 15, 15);
                break;
            case End:
                g.fillOval(362, 15, 15, 15);
                break;
            default:
                break;
        }
        //fills circle of solve speed option based on variable solveSpeed in Handler class
        switch(handler.solveSpeed){
            case 10:
                g.fillOval(860, 15, 15, 15);
                break;
            case 100:
                g.fillOval(1040, 15, 15, 15);
                break;
            case 500:
                g.fillOval(1170, 15, 15, 15);
                break;
        }
        
        //structure types
        g.setColor(Color.black);
        g.drawRect(1, 1, (WIDTH-7)/3, 38);
        g.drawString("Player ", 5, 30);
        g.drawOval(85, 15, 15, 15);
        g.drawString("Walls ", 130, 30);
        g.drawOval(200, 15, 15, 15);
        g.drawString("End Point ", 240, 30);
        g.drawOval(362, 15, 15, 15);
        
        //solve button
        g.drawRect(1+(WIDTH-7)/3, 1, (WIDTH-7)/3, 38);
        g.drawLine((WIDTH-7)/2, 1, (WIDTH-7)/2, 38);
        g.drawString("Solve!", 60+(WIDTH-7)/3, 30);
        
        //reset button
        g.drawString("Reset", (WIDTH-7)/2+60, 30);

        //solve speeds
        g.drawRect(1+(WIDTH-7)/3*2, 1, (WIDTH-7)/3, 38);
        g.drawString("Fast", 800, 30);
        g.drawOval(860, 15, 15, 15);
        g.drawString("Moderate", 920, 30);
        g.drawOval(1040, 15, 15, 15);
        g.drawString("Slow", 1105, 30);
        g.drawOval(1170, 15, 15, 15);
    }
    
    public void drawBoard(Graphics2D g){
        //horizontal lines
        g.setColor(Color.black);
        for(int i=0; i < HEIGHT/scale+1; i++){
            g.drawLine(1, i*scale+offset, WIDTH, i*scale+offset);
        }
        //vertical lines
        for(int i=0; i < WIDTH/scale+1; i++){
            g.drawLine(i*scale, 1+offset, i*scale, HEIGHT);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        
        //change gameState variable based on mouse click location
        if(mouseOver(mx, my, 85, 15, 15, 15)){
            gameState = GameState.Player;
        }else if(mouseOver(mx, my, 200, 15, 15, 15)){
            gameState = GameState.Walls;
        }else if(mouseOver(mx, my, 362, 15, 15, 15)){
            gameState = GameState.End;
        }
        //changes solveSpeed variable based on mouse click location
        else if(mouseOver(mx, my, 860, 15, 15, 15)){
            handler.solveSpeed = 10;
        }else if(mouseOver(mx, my, 1040, 15, 15, 15)){
            handler.solveSpeed = 100;
        }else if(mouseOver(mx, my, 1170, 15, 15, 15)){
            handler.solveSpeed = 500;
        }
    }

    //takes in mouse click x and y, then the x, y, width, and height of an object
    //returns true if mouse click is within bounds of the object (if the mouse is over the object)
    private boolean mouseOver(int mx, int my, int x, int y, int width, int height){
        if(mx > x && mx < x + width){
            if(my > y && my < y + height){
                return true;
            }else return false;
        }else return false;
    }
    
    //mouse wheel as zoom in/zoom out
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        scale+=notches;
    }
    
    public static void main(String[] args) {
        new Game();
    }
    
    public synchronized void stop(){
       try{
            thread.join();
            running = false;
        }catch(InterruptedException e){
        } 
    }
    
    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running = true;
    }
    
    //used to update scale variable throughout program
    public void tick(){
        handler.tick();
    }
    
    //game engine
    @Override
        public void run() {
            this.requestFocus();
            long lastTime = System.nanoTime();
            double amountOfTicks = 60.0;
            double ns = 1000000000 / amountOfTicks;
            double delta = 0;
            long timer = System.currentTimeMillis();
            int frames = 0;
            while(running){
                long now = System.nanoTime();
                delta+= (now - lastTime) / ns;
                lastTime = now;
                while(delta >= 1){
                    tick();
                    delta--;
                }
                if(running)
                    render();
                frames++;

                if(System.currentTimeMillis() - timer > 1000){
                    timer += 1000;
                    System.out.println("FPS " + frames);
                    frames = 0;
                }
            }
            stop();
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
    public void mouseDragged(MouseEvent e) {
    }
    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
