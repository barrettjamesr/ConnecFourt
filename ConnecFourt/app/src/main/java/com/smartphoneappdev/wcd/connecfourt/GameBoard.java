package com.smartphoneappdev.wcd.connecfourt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class GameBoard extends Activity implements View.OnClickListener {
    private boolean gameWonStatus;
    private boolean gameDrawnStatus;
    private GridLayout gridLayout;
    private ImageButton btnNewGame;
    private ImageButton btnUndo;
    private ImageButton btnSettings;
    private ImageButton btnStats;

    private int playerTurn;
    private int[] playersPieces = new int[5];
    private List<Integer> moveList;
    private int[][] gameBoard = new int[Constants.num_rows][Constants.num_cols];
    private TextView txtTurn;
    private ImageView imgTurn;
    private int selectedCol;
    private int currentRow;
    protected Handler timeHandler = new Handler();
    private boolean placed = true;
    private int token1 = -1;
    private int token2 = -1;

    private int gameNumber;
    private DataSource dataSource;
    private Date startTime;
    private int playerStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);
        gameWonStatus = false;

        //Link views and set default values from bundle
        btnNewGame = (ImageButton)findViewById(R.id.new_game);
        btnUndo = (ImageButton)findViewById(R.id.undo);
        btnStats = (ImageButton)findViewById(R.id.stats);
        btnSettings = (ImageButton)findViewById(R.id.settings);

        btnNewGame.setOnClickListener(this);
        btnUndo.setOnClickListener(this);
        btnStats.setOnClickListener(this);
        btnSettings.setOnClickListener(this);

        Intent extras = getIntent();
        Bundle bundle = extras.getExtras();
        //get colours from bundle
        if (extras != null) {
            token1 = bundle.getInt("player1", 0);
            token2 = bundle.getInt("player2", 0);
            playerTurn = bundle.getInt("playerStart", 1);
            if (playerTurn == 0) {
                int player = (Math.random() < 0.5 ? 1 : 2);
                playerTurn = player;
            }
        // all other cases
        } else {
            token1 = 0;
            token2 = 1;
            playerTurn = 1;
        }

        playersPieces[1] = Constants.intTokens[token1];
        playersPieces[2] = Constants.intTokens[token2];
        playersPieces[3] = Constants.intWinTokens[token1];
        playersPieces[4] = Constants.intWinTokens[token2];

        gridLayout = (GridLayout)findViewById(R.id.grid_layout);
        txtTurn = (TextView)findViewById(R.id.lblTurn);
        imgTurn = (ImageView)findViewById(R.id.imgTurn);
        moveList = new ArrayList<Integer>();

        //db vars
        dataSource = new DataSource(GameBoard.this);
        gameNumber = 1+ Math.max(0, dataSource.LastEntry(DatabaseContract.Statistics.TABLE_NAME, DatabaseContract.Statistics.COLUMN_NAME_GAME_NUMBER));
        startTime = new java.util.Date();
        playerStart = playerTurn;

        //set up display
        populateGameBoard(gridLayout);
        txtTurn.setText(getString(R.string.player) + " " + playerTurn + getString(R.string.owns) + " " + getString(R.string.turn));
        imgTurn.setImageResource(playersPieces[playerTurn]);

        // Listener so you can click and drag between columns and release on the one you want
        gridLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(placed && !gameWonStatus && !gameDrawnStatus) {
                    float lowerBoundX = 0;
                    float upperBoundX = 0;
                    float lowerBoundY = 0;
                    float upperBoundY = 0;
                    float x = event.getX();
                    float y = event.getY();
                    for (int col = 1; col <= Constants.num_cols; col++) {
                        final int column = col;
                        ImageView firstRow = (ImageView) findViewById(column);
                        float minValue = firstRow.getX();
                        if (col == 1) {
                            lowerBoundX = minValue;
                        }
                        float maxValue = firstRow.getX() + firstRow.getWidth();
                        if (col == Constants.num_cols) {
                            upperBoundX = maxValue;
                        }
                        lowerBoundY = firstRow.getY();
                        upperBoundY = lowerBoundY + firstRow.getHeight() * (Constants.num_rows + 1);

                        if (x > minValue && x < maxValue) {
                            firstRow.setImageResource(playersPieces[playerTurn]);
                            selectedCol = col;
                        } else {
                            firstRow.setImageResource(R.drawable.blank_space);
                        }
                    }

                    if (((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) && x > lowerBoundX && x < upperBoundX && y > lowerBoundY && y < upperBoundY) {
                        currentRow = 1;
                        timeHandler.postDelayed(placePiece, Constants.drop_speed);
                    }
                }
                return true;

            }

        });

    }

    private void populateGameBoard(GridLayout gridLayout) {
        gridLayout.setColumnCount(Constants.num_cols);
        gridLayout.setRowCount(Constants.num_rows+1);

        //Additional blank row for game play
        for (int row = 0; row<= Constants.num_rows; row++) {
            for(int col = 1; col<=Constants.num_cols; col++) {

                final ImageView imageView = new ImageView(this);

                if (row==0){
                    imageView.setImageResource(R.drawable.blank_space);
                } else {
                    imageView.setImageResource(R.drawable.empty_t);
                    gameBoard[row-1][col-1] = 0;
                }

                final int imageID = row * 10 + col;
                imageView.setPadding(Constants.grid_padding_cols,Constants.grid_padding_cols,Constants.grid_padding_cols,Constants.grid_padding_cols);

                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams(gridLayout.getLayoutParams());
                imageView.setLayoutParams(gridParams);
                imageView.setId(imageID);
                GridLayout.Spec rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 1);
                GridLayout.Spec colSpan = GridLayout.spec(GridLayout.UNDEFINED, 1);
                GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(rowSpan, colSpan);
                gridLayout.addView(imageView, gridParam);

            }
        }
    }

    //"Drop" the pieces, move one row at a time so the users experience the original game play
    private Runnable placePiece = new Runnable() {
        public void run() {
            if (currentRow == 0) {
                //Do Nothing
            } else if (gameBoard[0][selectedCol-1] != 0 ){
                Toast.makeText(getApplicationContext(), getString(R.string.full), Toast.LENGTH_LONG).show();
            } else {
                placed = false;

                ImageView prevSlot = (ImageView)findViewById((currentRow-1)*10+selectedCol);
                if (currentRow-1 == 0) {
                    prevSlot.setImageResource(R.drawable.blank_space);
                } else{
                    prevSlot.setImageResource(R.drawable.empty_t);
                }
                ImageView currSlot = (ImageView)findViewById((currentRow)*10+selectedCol);
                currSlot.setImageResource(playersPieces[playerTurn]);
                placed = moveOver();
                currentRow++;
                if (currentRow <= Constants.num_rows && !placed){
                    timeHandler.postDelayed(this, Constants.drop_speed);
                }
            }


        }
    };

    //When the piece has landed, check if the game is over, otherwise change turns
    private boolean moveOver(){
        final boolean over;

        over = currentRow == Constants.num_rows || (gameBoard[currentRow][selectedCol-1] != 0);
        if (over){
            gameBoard[currentRow-1][selectedCol-1] = playerTurn;
            moveList.add(selectedCol-1);
            checkWin();
            checkDraw();
            if (gameWonStatus) {
                txtTurn.setText(getString(R.string.player) + " " + playerTurn + getString(R.string.wins));
                imgTurn.setImageResource(playersPieces[playerTurn + 2]);
                dataSource.open();
                dataSource.saveGame(gameNumber, startTime, playerTurn, playerStart);
                dataSource.close();
            } else if (gameDrawnStatus) {
                txtTurn.setText(getString(R.string.draw));
                imgTurn.setImageResource(R.drawable.empty_t);
                dataSource.open();
                dataSource.saveGame(gameNumber, startTime, 0, playerStart);
                dataSource.close();
            } else {
                if (playerTurn == 1) {
                    playerTurn = 2;
                } else {
                    playerTurn = 1;
                }
                imgTurn.setImageResource(playersPieces[playerTurn]);
                txtTurn.setText(getString(R.string.player) + " " + playerTurn + getString(R.string.owns) + " " + getString(R.string.turn));
            }
        }
        return over;
    }

    private void checkWin(){
        for (int r = Math.max(currentRow-Constants.num_connected, 0); r<Constants.num_rows; r++  ){
            for (int c = Math.max(selectedCol-Constants.num_connected, 0); c<Constants.num_cols; c++  ){
                //Check Horizontal
                if (c<=Constants.num_cols-Constants.num_connected) {
                    if (gameBoard[r][c] > 0 && gameBoard[r][c] == gameBoard[r][c + 1] && gameBoard[r][c] == gameBoard[r][c + 2] && gameBoard[r][c] == gameBoard[r][c + 3]) {
                        gameWonStatus = true;
                    for (int i = 1; i <=Constants.num_connected ;i++) {
                        ImageView wonToken = (ImageView) findViewById((r+1) * 10 + (c + i));
                        wonToken.setImageResource(playersPieces[playerTurn+2]);
                    }
                    }
                }
                //Check Vertical
                if (r<=Constants.num_rows-Constants.num_connected) {
                    if (gameBoard[r][c] > 0 && gameBoard[r][c] == gameBoard[r + 1][c] && gameBoard[r][c] == gameBoard[r + 2][c] && gameBoard[r][c] == gameBoard[r + 3][c]) {
                        gameWonStatus = true;
                        for (int i = 1; i <=Constants.num_connected ;i++) {
                            ImageView wonToken = (ImageView) findViewById((r+i) * 10 + (c+1));
                            wonToken.setImageResource(playersPieces[playerTurn+2]);
                        }
                    }
                }
                //Check Down Diagonal
                if (r<=Constants.num_rows-Constants.num_connected && c<=Constants.num_cols-Constants.num_connected) {
                    if (gameBoard[r][c] > 0 && gameBoard[r][c] == gameBoard[r + 1][c + 1] && gameBoard[r][c] == gameBoard[r + 2][c + 2] && gameBoard[r][c] == gameBoard[r + 3][c + 3]) {
                        gameWonStatus = true;
                        for (int i = 1; i <=Constants.num_connected ;i++) {
                            ImageView wonToken = (ImageView) findViewById((r+i) * 10 + (c+i));
                            wonToken.setImageResource(playersPieces[playerTurn+2]);
                        }
                    }
                }
                //Check Up Diagonal
                if (r  > Constants.num_rows-Constants.num_connected && c<=Constants.num_cols-Constants.num_connected) {
                    if (gameBoard[r][c] > 0 && gameBoard[r][c] == gameBoard[r - 1][c + 1] && gameBoard[r][c] == gameBoard[r - 2][c + 2] && gameBoard[r][c] == gameBoard[r - 3][c + 3]) {
                        gameWonStatus = true;
                        for (int i = 1; i <=Constants.num_connected ;i++) {
                            ImageView wonToken = (ImageView) findViewById((r-i+2) * 10 + (c+i));
                            wonToken.setImageResource(playersPieces[playerTurn+2]);
                        }
                    }
                }
            }
        }
    }

    public void checkDraw(){
        gameDrawnStatus = true;
        for (int c = 0; c<Constants.num_cols; c++  ){
            if (gameBoard[0][c] == 0){
                gameDrawnStatus = false;
            }
        }
    }

    //standard practise for games to override the back button with undo
    @Override
    public void onBackPressed() {
        // go back to menu if the game is over or hasn't started
        if (gameWonStatus || moveList.isEmpty()){
            finish();
        } else {
            //undo move
            int lastCol = moveList.get(moveList.size()-1);
            for (int r = 0; r< Constants.num_rows; r++) {
                if (gameBoard[r][lastCol] != 0){
                    gameBoard[r][lastCol] = 0;
                    ImageView undoSlot = (ImageView)findViewById((r+1)*10+(lastCol+1));
                    undoSlot.setImageResource(R.drawable.empty_t);
                    moveList.remove(moveList.size()-1);
                    r = Constants.num_rows;
                }
            }
        }
        return;
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.new_game:
                newGame();
                break;
            case R.id.undo:
                onBackPressed();
                break;
            case R.id.settings:
                changeSettings();
                break;
            case R.id.stats:
                showStats();
                break;
        }
    }

    //Directly launch new game if the current one is done, otherwise check user's wish
    public void newGame(){
        if(!gameWonStatus && !gameDrawnStatus) {
            AlertDialog.Builder newGameDialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);

            newGameDialogBuilder.setTitle(getString(R.string.new_game));
            newGameDialogBuilder.setMessage(getString(R.string.restart));
            newGameDialogBuilder.setIcon(R.mipmap.ic_launcher);

            newGameDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selection) {
                    Intent intent = getIntent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("player1",token1);
                    bundle.putInt("player2",token2);
                    intent.putExtras(bundle);
                    finish();
                    startActivity(intent);
                    dialog.dismiss();
                }
            });

            newGameDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selection) {
                    dialog.dismiss();
                }
            });

            AlertDialog newGameDialog = newGameDialogBuilder.create();
            newGameDialog.show();
        } else {
            Intent intent = getIntent();
            Bundle bundle = new Bundle();
            bundle.putInt("player1",token1);
            bundle.putInt("player2",token2);
            intent.putExtras(bundle);
            finish();
            startActivity(intent);
        }

    }

    //Change token colour dialog
    public void changeSettings(){

        TextView lblPlayer1;
        TextView lblPlayer2;
        final Spinner spnPlayer1;
        final Spinner spnPlayer2;

        AlertDialog.Builder settingsDialogBuilder = new AlertDialog.Builder(GameBoard.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
        View settings = GameBoard.this.getLayoutInflater().inflate(R.layout.settings, null);
        settingsDialogBuilder.setView(settings);

        settingsDialogBuilder.setTitle(getString(R.string.settings));
        settingsDialogBuilder.setIcon(R.mipmap.ic_launcher);

        lblPlayer1= (TextView) settings.findViewById(R.id.player1Settings);
        lblPlayer1.setText(getString(R.string.player) + " 1" + getString(R.string.owns) + " " + getString(R.string.color));
        lblPlayer2= (TextView) settings.findViewById(R.id.player2Settings);
        lblPlayer2.setText(getString(R.string.player) + " 2" + getString(R.string.owns) + " " + getString(R.string.color));

        spnPlayer1= (Spinner) settings.findViewById(R.id.spinner1Settings);
        spnPlayer1.setPrompt(getString(R.string.player) + " 1" + getString(R.string.owns) + " " + getString(R.string.color));
        spnPlayer2= (Spinner) settings.findViewById(R.id.spinner2Settings);
        spnPlayer2.setPrompt(getString(R.string.player) + " 2" + getString(R.string.owns) + " " + getString(R.string.color));

        ImageArrayAdapter adapter1 = new ImageArrayAdapter(GameBoard.this, Constants.intTokens, Constants.strTokens);
        spnPlayer1.setAdapter(adapter1);
        spnPlayer1.setSelection(token1);
        ImageArrayAdapter adapter2 = new ImageArrayAdapter(GameBoard.this, Constants.intTokens, Constants.strTokens);
        spnPlayer2.setAdapter(adapter2);
        spnPlayer2.setSelection(token2);

        settingsDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                if (!checkColours(spnPlayer1.getSelectedItemPosition(), spnPlayer2.getSelectedItemPosition())) {
                    if (Constants.intTokens[spnPlayer1.getSelectedItemPosition()] != playersPieces[1] ||
                            Constants.intTokens[spnPlayer2.getSelectedItemPosition()] != playersPieces[2]){
                        token1 = spnPlayer1.getSelectedItemPosition();
                        token2 = spnPlayer2.getSelectedItemPosition();
                        updateGridColours();
                    }
                }
                dialog.dismiss();
            }
        });
        settingsDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                dialog.dismiss();
            }
        });

        AlertDialog settingsDialog = settingsDialogBuilder.create();
        settingsDialog.show();

    }

    public boolean checkColours(int spnPlayer1, int spnPlayer2){
        if (spnPlayer1 == spnPlayer2) {
            AlertDialog.Builder coloursDialogBuilder = new AlertDialog.Builder(GameBoard.this, android.R.style.Theme_DeviceDefault_Light_Dialog);

            coloursDialogBuilder.setTitle(getString(R.string.sameColours));
            coloursDialogBuilder.setMessage(getString(R.string.moreFun));
            coloursDialogBuilder.setIcon(R.mipmap.ic_launcher);

            coloursDialogBuilder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selection) {
                    dialog.dismiss();
                    changeSettings();
                }
            });

            AlertDialog newGameDialog = coloursDialogBuilder.create();
            newGameDialog.show();
            return true;
        } else {
            return false;
        }
    }

    //If they are in teh middle of a game, update all current pieces
    public void updateGridColours(){
        playersPieces[1] = Constants.intTokens[token1];
        playersPieces[2] = Constants.intTokens[token2];
        playersPieces[3] = Constants.intWinTokens[token1];
        playersPieces[4] = Constants.intWinTokens[token2];
        //Change board tokens if game not yet won
        if (!gameWonStatus && !gameDrawnStatus) {
            imgTurn.setImageResource(playersPieces[playerTurn]);
            for (int r = 0; r < Constants.num_rows; r++) {
                for (int c = 1; c < Constants.num_cols; c++) {
                    ImageView changeToken = (ImageView) findViewById((r + 1) * 10 + (c + 1));
                    if (gameBoard[r][c] == 1) {
                        changeToken.setImageResource(playersPieces[1]);
                    }
                    if (gameBoard[r][c] == 2) {
                        changeToken.setImageResource(playersPieces[2]);
                    }
                }
            }
        }
    }

    //Statistics dialog box
    public void showStats(){
        dataSource.open();
        List<Integer> winners = dataSource.getWinners();
        dataSource.close();
        //2D Array= row: Drawn, Player1, Player2; column: Total Games Won, Current Streak, Max Streak
        int[][] winSummary = new int[][]{ {0, 0, 0}, {0, 0, 0}, {0, 0, 0}};

        if(!winners.isEmpty()) {
            for (int i = 0; i < winners.size(); i++) {
                if (winners.get(i) == 0) {
                    //Total wins go up by one
                    winSummary[0][0]= winSummary[0][0]+1;
                    //This streak increases, the others are now zero (no streak)
                    winSummary[0][1]= winSummary[0][1]+1;
                    winSummary[1][1]= 0;
                    winSummary[2][1]= 0;
                    // Max Streak check
                    if (winSummary[0][1] > winSummary[0][2]){
                        winSummary[0][2] = winSummary[0][1];
                    }

                }
                if (winners.get(i) == 1) {
                    //Total wins go up by one
                    winSummary[1][0]= winSummary[1][0]+1;
                    //This streak increases, the others are now zero (no streak)
                    winSummary[1][1]= winSummary[1][1]+1;
                    winSummary[0][1]= 0;
                    winSummary[2][1]= 0;
                    // Max Streak check
                    if (winSummary[1][1] > winSummary[1][2]){
                        winSummary[1][2] = winSummary[1][1];
                    }

                }
                if (winners.get(i) == 2) {
                    //Total wins go up by one
                    winSummary[2][0]= winSummary[2][0]+1;
                    //This streak increases, the others are now zero (no streak)
                    winSummary[2][1]= winSummary[2][1]+1;
                    winSummary[0][1]= 0;
                    winSummary[1][1]= 0;
                    // Max Streak check
                    if (winSummary[2][1] > winSummary[2][2]){
                        winSummary[2][2] = winSummary[2][1];
                    }

                }


            }
        }

        TextView lblGames;
        TextView lblDrawnWins;
        TextView lblDrawnStreak;
        TextView lblPlayer1Wins;
        TextView lblPlayer1Streak;
        TextView lblPlayer2Wins;
        TextView lblPlayer2Streak;
        ProgressBar progressWins;

        AlertDialog.Builder settingsDialogBuilder = new AlertDialog.Builder(GameBoard.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
        View stats = GameBoard.this.getLayoutInflater().inflate(R.layout.stats, null);
        settingsDialogBuilder.setView(stats);

        settingsDialogBuilder.setTitle(getString(R.string.stats));
        settingsDialogBuilder.setIcon(R.mipmap.ic_launcher);

        lblDrawnWins= (TextView) stats.findViewById(R.id.drawn_total);
        lblDrawnWins.setText(Integer.toString(winSummary[0][0]));
        lblDrawnStreak= (TextView) stats.findViewById(R.id.drawn_streak);
        lblDrawnStreak.setText(Integer.toString(winSummary[0][2]));
        lblPlayer1Wins= (TextView) stats.findViewById(R.id.player1_total);
        lblPlayer1Wins.setText(Integer.toString(winSummary[1][0]));
        lblPlayer1Streak= (TextView) stats.findViewById(R.id.player1_streak);
        lblPlayer1Streak.setText(Integer.toString(winSummary[1][2]));
        lblPlayer2Wins= (TextView) stats.findViewById(R.id.player2_total);
        lblPlayer2Wins.setText(Integer.toString(winSummary[2][0]));
        lblPlayer2Streak= (TextView) stats.findViewById(R.id.player2_streak);
        lblPlayer2Streak.setText(Integer.toString(winSummary[2][2]));
        progressWins = (ProgressBar) stats.findViewById(R.id.progressWins);
        if(winSummary[0][0]+winSummary[1][0]+winSummary[2][0] > 0) {
            progressWins.setProgress(Math.round(100 * winSummary[1][0] / (winSummary[0][0] + winSummary[1][0] + winSummary[2][0])));
            progressWins.setSecondaryProgress(Math.round(100 * (winSummary[0][0] + winSummary[1][0]) / (winSummary[0][0] + winSummary[1][0] + winSummary[2][0])));
        } else {
            progressWins.setProgress(50);
            progressWins.setSecondaryProgress(50);
        }
        lblGames= (TextView) stats.findViewById(R.id.games);
        lblGames.setText(Integer.toString(winSummary[0][0] + winSummary[1][0] + winSummary[2][0]));

        settingsDialogBuilder.setNegativeButton (getString(R.string.reset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                dataSource.resetStats(DatabaseContract.Statistics.TABLE_NAME);
                dialog.dismiss();
            }
        });

        settingsDialogBuilder.setPositiveButton (getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                dialog.dismiss();
            }
        });

        AlertDialog settingsDialog = settingsDialogBuilder.create();
        settingsDialog.show();
    }

}
