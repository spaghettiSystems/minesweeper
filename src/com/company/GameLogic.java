package com.company;

import java.io.*;
import java.util.Random;

public class GameLogic {
    int flagged;
    int flaggedBombs;
    int totalFlags;
    boolean firstClick;
    private Board realBoard;
    private char[][] fakeBoard;

    GameLogic(int x, int y, int chance, boolean absolute) {
        realBoard = new Board(x, y, chance, true, false, absolute);
        fakeBoard = new char[x][y];
        fillFakeBoard();
        flagged = realBoard.bombCount;
        flaggedBombs = 0;
        totalFlags = 0;
        firstClick = true;
    }

    public GameLogic() {

    }

    void fillFakeBoard() {
        for (int i = 0; i < realBoard.width; i++) {
            for (int j = 0; j < realBoard.height; j++) {
                fakeBoard[i][j] = '#';
            }
        }
    }

    private void moveBomb(int x, int y) {
        Random random = new Random();
        int newx, newy;

        do {
            newx = random.nextInt(realBoard.width);
            newy = random.nextInt(realBoard.height);
        } while (realBoard.board[newx][newy].isBomb || newx == x && newy == y);

        Board.cell temp;
        temp = realBoard.board[newx][newy];
        realBoard.board[newx][newy] = realBoard.board[x][y];
        realBoard.board[x][y] = temp;
    }

    boolean clickCell(int x, int y) {

        if (firstClick) {
            firstClick = false;
            /*if (realBoard.board[x][y].isBomb) {
                moveBomb(x, y);
            }*/
            for (int updown = -1; updown <= 1; updown++) {
                for (int leftright = -1; leftright <= 1; leftright++) {
                    if (x + updown >= 0 && x + updown < realBoard.width &&
                            y + leftright >= 0 && y + leftright < realBoard.height
                            && realBoard.board[x + updown][y + leftright].isBomb){
                        moveBomb(x + updown, y + leftright);
                    }
                }
            }
            realBoard.createNumbers();
            realBoard.displayBoard();
        }

        if (realBoard.board[x][y].isBomb) {
            clearEmptyHiddenCell(x, y);
            System.out.println("loss");
            return true;
        }
        if (realBoard.board[x][y].isFlagged) {
            return false;
        }

        clearEmptyHiddenCell(x, y);

        if (!realBoard.board[x][y].isEmpty) {
            return false;
        }

        for (int updown = -1; updown <= 1; updown++) {
            for (int leftright = -1; leftright <= 1; leftright++) {
                if (x + updown >= 0 && x + updown < realBoard.width &&
                        y + leftright >= 0 && y + leftright < realBoard.height &&
                        fakeBoard[x + updown][y + leftright] == '#'
                ) {
                    clickCell(x + updown, y + leftright);
                }
            }
        }
        return false;
    }

    void displayHiddenBoard() {
        for (int i = 0; i < realBoard.width; i++) {
            for (int j = 0; j < realBoard.height; j++) {
                System.out.print(fakeBoard[i][j] + "  ");
            }
            System.out.println();
        }
    }

    private void clearEmptyHiddenCell(int x, int y) {
        fakeBoard[x][y] = realBoard.board[x][y].content;
    }

    void saveBoard(String fileAddress, String time) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileAddress));
        writer.write(realBoard.width + "");
        writer.newLine();
        writer.write(realBoard.height + "");
        writer.newLine();
        writer.write(realBoard.bombRate + "");
        writer.newLine();
        writer.write(flagged + "");
        writer.newLine();
        writer.write(flaggedBombs + "");
        writer.newLine();
        writer.write(totalFlags + "");
        writer.newLine();
        writer.write(firstClick + "");
        writer.newLine();
        writer.write(time);
        writer.newLine();

        for (int i = 0; i < realBoard.width; i++) {
            for (int j = 0; j < realBoard.height; j++) {
                writer.write(realBoard.board[i][j].content + "_");
            }
            writer.newLine();
        }
        writer.newLine();
        writer.newLine();

        for (int i = 0; i < realBoard.width; i++) {
            for (int j = 0; j < realBoard.height; j++) {
                writer.write(fakeBoard[i][j] + "_");
            }
            writer.newLine();
        }
        writer.close();

    }

    String openBoard(String fileAddress) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileAddress));
        String temp1, temp2, temp3;
        temp1 = reader.readLine();
        temp2 = reader.readLine();
        temp3 = reader.readLine();
        realBoard = new Board(Integer.parseInt(temp1), Integer.parseInt(temp2), Integer.parseInt(temp3), false, false, false);
        fakeBoard = new char[Integer.parseInt(temp1)][Integer.parseInt(temp2)];

        temp1 = reader.readLine();
        temp2 = reader.readLine();
        temp3 = reader.readLine();
        flagged = Integer.parseInt(temp1);
        flaggedBombs = Integer.parseInt(temp2);
        totalFlags = Integer.parseInt(temp3);

        temp1 = reader.readLine();
        firstClick = false;
        if (temp1.equals("true")) {
            firstClick = true;
        }

        temp2 = reader.readLine();

        for (int i = 0; i < realBoard.width; i++) {
            temp1 = reader.readLine();
            String[] split = temp1.split("_");
            for (int j = 0; j < split.length; j++) {
                realBoard.board[i][j] = new Board.cell(split[j].charAt(0));
            }
        }

        reader.readLine();
        reader.readLine();

        for (int i = 0; i < realBoard.width; i++) {
            temp1 = reader.readLine();
            String[] split = temp1.split("_");
            for (int j = 0; j < split.length; j++) {
                fakeBoard[i][j] = (split[j].charAt(0));
            }
        }

        realBoard.updateBombCount();
        updateFlags();

        return temp2;
    }

    private void updateFlags() {
        for (int i = 0; i < realBoard.width; i++) {
            for (int j = 0; j < realBoard.height; j++) {
                if (fakeBoard[i][j] == '!') {
                    realBoard.board[i][j].isFlagged = true;
                }
            }
        }
    }

    public Board getRealBoard() {
        return realBoard;
    }

    public char[][] getFakeBoard() {
        return fakeBoard;
    }

    boolean flagCell(int x, int y) {
        if(fakeBoard[x][y] != '#' && fakeBoard[x][y] != '!'){
            return false;
        }
        if (realBoard.board[x][y].isFlagged) {
            totalFlags--;
            flagged++;
            if (realBoard.board[x][y].isBomb) {
                flaggedBombs--;
            }
            realBoard.board[x][y].isFlagged = false;
            fakeBoard[x][y] = '#';
        } else {
            if (flagged < 1) {
                return false;
            }
            totalFlags++;
            flagged--;
            if (realBoard.board[x][y].isBomb) {
                flaggedBombs++;
            }
            realBoard.board[x][y].isFlagged = true;
            fakeBoard[x][y] = '!';
        }
        if (flaggedBombs == realBoard.bombCount && totalFlags == realBoard.bombCount) {
            System.out.println("win");
            return true;
        }
        return false;
    }


    public static class Board {
        protected int width;
        protected int height;
        protected int bombRate;
        protected int bombCount = 0;
        Board.cell[][] board;

        Board(int x, int y, int chance) {
            this.width = x;
            this.height = y;
            this.bombRate = chance;
            this.board = new Board.cell[width][height];
            fillBoard(true, false);
        }

        Board(int x, int y, int chance, boolean fill, boolean numbered, boolean absolute) {
            this.width = x;
            this.height = y;
            this.bombRate = chance;
            this.board = new Board.cell[width][height];
            if (fill) {
                fillBoard(numbered, absolute);
            }
        }

        void displayBoard() {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    System.out.print(board[i][j].getContent() + "  ");
                }
                System.out.println();
            }
        }

        void updateBombCount() {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (board[i][j].isBomb) {
                        bombCount++;
                    }
                }
            }
        }

        void fillBoard(boolean numbered, boolean literal) {
            bombCount = 0;
            createBombs(literal);
            if (numbered) {
                createNumbers();
            }
        }

        void createBombs(boolean literal) {
            int number;
            Random random = new Random();
            if(literal){
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        board[i][j] = new Board.cell();
                    }
                }

                for (int i = 0; i < bombRate; i++) {
                    int x = random.nextInt(width);
                    int y = random.nextInt(height);
                    if (!board[x][y].isBomb){
                        board[x][y] = new Board.cell(true);
                        bombCount++;
                    }
                    else {
                        i--;
                    }
                }
            }
            else{
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        number = random.nextInt(100);
                        if (number < bombRate) {
                            board[i][j] = new Board.cell(true);
                            bombCount++;
                        } else {
                            board[i][j] = new Board.cell();
                        }
                    }
                }
            }
        }


        void createNumbers() {
            int number = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (!board[i][j].isBomb) {
                        for (int updown = -1; updown <= 1; updown++) {
                            for (int leftright = -1; leftright <= 1; leftright++) {
                                if (i + updown >= 0 && i + updown < width && j + leftright >= 0 && j + leftright < height) {
                                    if (board[i + updown][j + leftright].isBomb) {
                                        number++;
                                    }
                                }
                            }
                        }
                        board[i][j].setContent(number);
                        number = 0;
                    }
                }
            }
        }


        protected static final class cell {
            boolean isEmpty;
            boolean isBomb;
            boolean isFlagged;
            private char content;

            cell() {
                this.isEmpty = true;
                this.isBomb = false;
                this.isFlagged = false;
                this.content = ' ';
            }

            cell(boolean bomb) {
                if (bomb) {
                    this.isBomb = true;
                    this.isEmpty = false;
                    this.content = '*';
                    this.isFlagged = false;
                    return;
                }
                this.isEmpty = true;
                this.isBomb = false;
                this.content = ' ';
                this.isFlagged = false;
            }

            cell(int number) {
                this.isBomb = false;
                this.isEmpty = false;
                this.content = (char) (number + '0');
                this.isFlagged = false;
            }

            cell(char toBeTested) {
                if (toBeTested == ' ') {
                    this.isEmpty = true;
                    this.isBomb = false;
                    this.content = ' ';
                    this.isFlagged = false;
                } else if (toBeTested == '*') {
                    this.isBomb = true;
                    this.isEmpty = false;
                    this.content = '*';
                    this.isFlagged = false;
                } else if (toBeTested == '!') {
                    this.isBomb = false;
                    this.isEmpty = false;
                    this.content = '!';
                    this.isFlagged = true;
                } else if (toBeTested >= '1' && toBeTested <= '9') {
                    this.isBomb = false;
                    this.isEmpty = false;
                    this.content = toBeTested;
                    this.isFlagged = false;
                } else {
                    this.isEmpty = true;
                    this.isBomb = false;
                    this.content = ' ';
                    this.isFlagged = false;
                }
            }

            public char getContent() {
                return content;
            }

            public void setContent(int number) {
                if (number > 0) {
                    this.content = (char) (number + '0');
                    this.isEmpty = false;
                } else {
                    this.content = ' ';
                    this.isEmpty = true;
                }
            }
        }

    }

}
