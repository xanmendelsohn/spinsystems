import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

class Ising extends Canvas implements Runnable {

    int size = 100;                             // number of lattice sites in a row (change if desired)
    int squareWidth = 5;                        // pixels across one lattice site (change if desired)
    int canvasSize = size * squareWidth;        // total pixels across canvas
    int[][] s = new int[size][size];            // the 2D array of dipoles (each equal to 1 or -1)
    boolean running = false;                    // true when simulation is running
    Button startButton = new Button("  Start  ");
    Scrollbar tScroller;                        // scrollbar to adjust temperature
    Label tLabel = new Label("Temperature = 2.27  ");    // text label next to scrollbar
    DecimalFormat twoPlaces = new DecimalFormat("0.00");    // to format temperature readout
    Image offScreenImage;                       // for double-buffering
    Graphics offScreenGraphics;
    Color upColor = new Color(100,0,255);       // purple
    Color downColor = new Color(255,255,255);   // white
    
    // Constructor method handles all the initializations:
    Ising() {
        Frame isingFrame = new Frame("Ising Model");       // initialize the GUI...
        isingFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);                            // close button exits program
            }
        });
        Panel canvasPanel = new Panel();
        isingFrame.add(canvasPanel);
        canvasPanel.add(this);
        setSize(canvasSize,canvasSize);
        Panel controlPanel = new Panel();
        isingFrame.add(controlPanel,BorderLayout.SOUTH);
        controlPanel.add(tLabel);
        tScroller = new Scrollbar(Scrollbar.HORIZONTAL,227,1,1,1001) {
            public Dimension getPreferredSize() {
                return new Dimension(100,15);            // make it bigger than default
            }
        };
        tScroller.setBlockIncrement(1);        // enables fine adjustments in Mac OS X 10.7+
        tScroller.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                tLabel.setText("Temperature = " + twoPlaces.format(tScroller.getValue()/100.0));
            }
        });
        controlPanel.add(tScroller);
        controlPanel.add(new Label("     "));            // leave some space
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                running = !running;
                if (running) startButton.setLabel("Pause"); else startButton.setLabel("Resume");
            }
        });
        controlPanel.add(startButton);
        isingFrame.pack();
        offScreenImage = createImage(canvasSize,canvasSize);
        offScreenGraphics = offScreenImage.getGraphics();

        for (int i=0; i < size; i++) {                    // initialize the lattice...
            for (int j=0; j < size; j++) {
                if (Math.random() < 0.5) s[i][j] = 1; else s[i][j] = -1;
                colorSquare(i,j);
            }
        }

        isingFrame.setVisible(true);          // we're finally ready to show it!

        Thread t = new Thread(this);          // create a thread to run the simulation
        t.start();                            // and let 'er rip...
    }

    // Run method gets called by new thread to carry out the simulation:
    public void run() {
        while (true) {
            if (running) {
                double temp = tScroller.getValue() / 100.0;
                for (int step=0; step<10000; step++) {       // adjust number of steps as desired
                    int i = (int) (Math.random() * size);    // choose a random row and column
                    int j = (int) (Math.random() * size);
                    double eDiff = deltaU(i,j);                // compute energy change if flipped
                    if ((eDiff <= 0) || (Math.random() < Math.exp(-eDiff/temp))) {    // Metropolis!
                        s[i][j] *= -1;
                        colorSquare(i,j);
                    }
                }
                repaint();        // causes update method to be called soon
            }
            try { Thread.sleep(1); } catch (InterruptedException e) {}  // sleep time in milliseconds
        }
    }

    // Given a lattice site, compute energy change from hypothetical flip; note pbc:
    double deltaU(int i, int j) {
        int leftS, rightS, topS, bottomS;  // values of neighboring spins
        if (i == 0) leftS = s[size-1][j]; else leftS = s[i-1][j];
        if (i == size-1) rightS = s[0][j]; else rightS = s[i+1][j];
        if (j == 0) topS = s[i][size-1]; else topS = s[i][j-1];
        if (j == size-1) bottomS = s[i][0]; else bottomS = s[i][j+1];
        return 2.0 * s[i][j] * (leftS + rightS + topS + bottomS);
    }

    // Color a given square according to the site's orientation:
    void colorSquare(int i, int j) {
        if (s[i][j] == 1) offScreenGraphics.setColor(upColor); 
                     else offScreenGraphics.setColor(downColor);
        offScreenGraphics.fillRect(i*squareWidth,j*squareWidth,squareWidth,squareWidth);
    }

    // Override default update method to skip drawing the background:
    public void update(Graphics g) {
        paint(g);
    }

    // Paint method just blasts the off-screen image to the screen:
    public void paint(Graphics g) {
        g.drawImage(offScreenImage,0,0,canvasSize,canvasSize,this);
    }

    // Main method just calls constructor to get started:
    public static void main(String[] args) {
        new Ising();    
    }  
}
