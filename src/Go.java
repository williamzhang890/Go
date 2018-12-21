
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;

public class Go extends JPanel implements ActionListener, MouseListener {

    /*
    * DIAMETER: diameter of a piece
    * RADIUS: radius of a piece
    * BOARD_COLOR: the color of board
    * SELECTED_COLOR: the color of the border of the selected piece
     */
    public static final int DIAMETER;
    public static final int RADIUS = 50;
    private static final Color BOARD_COLOR = new Color(220, 181, 121); // color found on https://www.computerhope.com
    private static final Color SELECTED_COLOR = new Color(0, 0, 255); // color found on https://www.rapidtables.com

    static {
        DIAMETER = RADIUS * 2;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Go");
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel(new GridBagLayout()); // for future improvement
        GridBagConstraints c = new GridBagConstraints();
        p.add(new Go(9, 9));
        f.add(p);
        f.pack();
        f.setLocationRelativeTo(null); // null puts window in the center of the screen
                                       // see java.awt.Window javadocs
        f.setVisible(true);
    }

    /* game variables
    * turn: black = 1, white = 2
    * board: matrix of pieces represented by a short where empty = 0, black = 1, white = 2
    * pieceRow: current row of the piece that the mouse is hovering over
    * pieceCol: current col of the piece that the mouse is hovering over
    * legal: if legal[i][j] is true, it is legal to place a piece there, but if it is false
    *        it is legal only if hovered[i][j] is true
    * hovered: if hovered[i][j] is true, the mouse visited that coordinate previously
    */
    private short turn;
    private short[][] board;
    int pieceRow, pieceCol;
    private boolean[][] legal;
    private boolean[][] hovered;

    /* dimensions 
    * rows: number of pieces along the y axis
    * cols: number of pieces along the x axis 
    * gridWidth: the width of the grid
    * gridHeight: the height of the grid
    * boardWidth: the width of the board
    * boardHeight: the height of the board
    * width: the total width of the panel
    * height: the total height of the panel
     */
    private final int rows, cols;
    private final int gridWidth, gridHeight;
    private final int boardWidth, boardHeight;
    private final int width, height;

    public Go(int cols, int rows) {
        board = new short[rows][cols];
        legal = new boolean[rows][cols];
        hovered = new boolean[rows][cols];
        turn = 1;

        this.cols = cols;
        this.rows = rows;

        gridWidth = DIAMETER * (cols - 1);
        gridHeight = DIAMETER * (rows - 1);
        boardWidth = gridWidth + DIAMETER;
        boardHeight = gridWidth + DIAMETER;
        width = boardWidth;
        height = boardHeight;

        this.addMouseListener(this);
        this.setPreferredSize(new Dimension(width, height));

        javax.swing.Timer t = new javax.swing.Timer(30, this);
        t.start();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        drawBoard(g);
        drawPieces(g);
    }

    public void drawBoard(Graphics g) {
        g.setColor(BOARD_COLOR);
        g.fillRect(0, 0, boardWidth, boardHeight);
        g.setColor(Color.BLACK);
        g.drawRect(RADIUS, RADIUS, gridWidth, gridHeight);

        for (int i = 0; i < cols - 1; i++) {
            g.drawLine(RADIUS + DIAMETER * i, RADIUS, RADIUS + DIAMETER * i, RADIUS + gridHeight);
        }
        for (int i = 0; i < rows - 1; i++) {
            g.drawLine(RADIUS, RADIUS + DIAMETER * i, RADIUS + gridWidth, RADIUS + DIAMETER * i);
        }
    }

    public void drawPieces(Graphics g) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                switch (board[i][j]) {
                    case 0:
                        continue;
                    case 1:
                        g.setColor(Color.BLACK);
                        break;
                    case 2:
                        g.setColor(Color.WHITE);
                        break;
                }
                g.fillOval(j * DIAMETER, i * DIAMETER, DIAMETER, DIAMETER);
            }
        }

        if (pieceRow != -1) {
            if (turn == 1) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillOval(pieceCol * DIAMETER, pieceRow * DIAMETER, DIAMETER, DIAMETER);
            g.setColor(SELECTED_COLOR);
            g.drawOval(pieceCol * DIAMETER, pieceRow * DIAMETER, DIAMETER, DIAMETER);
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Point p = this.getMousePosition();
        if (p != null) {
            int row = (p.y) / DIAMETER;
            int col = (p.x) / DIAMETER;
            if (p.x >= 0 && p.x < boardWidth && board[row][col] == 0 && isLegal(row, col)) {
                pieceRow = row;
                pieceCol = col;
            } else {
                pieceRow = -1;
                pieceCol = -1;
            }
        } else {
            pieceRow = -1;
            pieceCol = -1;
        }
        repaint();
    }

    public void endTurn() {
        turn = (short) (3 - turn);
        legal = new boolean[rows][cols];
        hovered = new boolean[rows][cols];
    }

    public boolean isLegal(int row, int col) {
    	if (!withinBounds(row , col) || board[row][col] != 0) {
    		return false;
    	}
    	if (hovered[row][col]) {
    		return legal[row][col];
    	}
    	hovered[row][col] = true;
    	
    	
    	boolean output;
    	board[row][col] = turn;
    	if (isSurrounded(row, col, turn, new boolean[rows][cols])) {
    		short enemy = (short) (3 - turn);
    		if (withinBounds(row - 1, col) && board[row - 1][col] == enemy
    				&& isSurrounded(row - 1, col, enemy, new boolean[rows][cols])) {
    			output = true;
    		} else if (withinBounds(row + 1, col) && board[row + 1][col] == enemy 
    				&& isSurrounded(row + 1, col, enemy, new boolean[rows][cols])) {
    			output = true;
    		} else if (withinBounds(row, col - 1) && board[row][col - 1] == enemy
    				&& isSurrounded(row, col - 1, enemy, new boolean[rows][cols])) {
    			output = true;
    		} else if (withinBounds(row, col + 1) && board[row][col + 1] == enemy
    				&& isSurrounded(row, col + 1, (short) (3-turn), new boolean[rows][cols])) {
    			output = true;
    		} else {
    			output = false;
    		}
    		
    	} else { 
    		output = true;
    	}
    	board[row][col] = 0;
    	legal[row][col] = output;
    	return output;
    }

    public boolean isSurrounded(int row, int col, short piece, boolean[][] visited) {
        if (!withinBounds(row, col) || visited[row][col]) {
            return true;
        }
        
        if (board[row][col] == 3 - piece) {
        	return true;
        } else if (board[row][col] == 0) {
        	return false;
        } else {    
        	visited[row][col] = true;  
        	boolean s = isSurrounded(row - 1, col, piece, visited) 
        				 && isSurrounded(row + 1, col, piece, visited)
        				 && isSurrounded(row, col - 1, piece, visited)
        				 && isSurrounded(row, col + 1, piece, visited);
        	return s;
        }
    }
    
    public boolean withinBounds(int row, int col) {
    	return row >= 0 && row < rows && col >= 0 && col < cols;
    }
    
    private void or(boolean[][] a, boolean[][] b) {
    	for (int r = 0; r < rows; r++) {
    		for (int c = 0; c < cols; c++) {
    			if (b[r][c]) {
    				a[r][c] = true;
    			}
    		}
    	}
    }
    
    private boolean[][] copyOf(boolean[][] a) {
    	boolean[][] copy = new boolean[rows][cols];
    	for (int r = 0; r < rows; r++) {
    		for (int c = 0; c < cols; c++) {
    			copy[r][c] = a[r][c];
    		}
    	}
    	return copy;
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    	if (isLegal(pieceRow, pieceCol)) {
    		board[pieceRow][pieceCol] = turn;
    		
    		boolean[][] dead = new boolean[rows][cols];
    		boolean[][] visited = new boolean[rows][cols];
    		short enemy = (short) (3 - turn);
    		if (isSurrounded(pieceRow - 1, pieceCol, enemy, visited)) {
    			or(dead, visited);
    		}
    		visited = copyOf(dead);
    		if (isSurrounded(pieceRow + 1, pieceCol, enemy, visited)) {
    			or(dead, visited);
    		}
    		visited = copyOf(dead);
    		if (isSurrounded(pieceRow, pieceCol - 1, enemy, visited)) {
    			or(dead, visited);
    		}
    		visited = copyOf(dead);
    		if (isSurrounded(pieceRow, pieceCol + 1, enemy, visited)) {
    			or(dead, visited);
    		}
    		
    		for (int r = 0; r < rows; r++) {
    			for (int c = 0; c < cols; c++) {
    				if (dead[r][c]) {
    					board[r][c] = 0;
    					
    				}
    			}
    		}
    		
    		endTurn();
    	}
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }
    

    @Override
    public void mousePressed(MouseEvent me) {
    }
    

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

}