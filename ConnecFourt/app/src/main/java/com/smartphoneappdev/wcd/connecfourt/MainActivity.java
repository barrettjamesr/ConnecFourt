package com.smartphoneappdev.wcd.connecfourt;

/**
 * Created by JRB on 25/10/2016.
 * Initial activity, to set game params and star
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
    private Button newGame;
    private TextView lblPlayer1;
    private TextView lblPlayer2;
    private Spinner spnPlayer1;
    private Spinner spnPlayer2;
    private Spinner spnPlayerStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Connect views & set values and listeners
        newGame = (Button) findViewById(R.id.new_game);
        newGame.setOnClickListener(this);
        lblPlayer1= (TextView)findViewById(R.id.player1);
        lblPlayer1.setText(getString(R.string.player) + " 1" + getString(R.string.owns) + " " + getString(R.string.color));
        lblPlayer2= (TextView)findViewById(R.id.player2);
        lblPlayer2.setText(getString(R.string.player) + " 2" + getString(R.string.owns) + " " + getString(R.string.color));

        spnPlayer1= (Spinner) findViewById(R.id.spinner1);
        spnPlayer1.setPrompt(getString(R.string.player) + " 1" + getString(R.string.owns) + " " + getString(R.string.color));
        spnPlayer2= (Spinner) findViewById(R.id.spinner2);
        spnPlayer2.setPrompt(getString(R.string.player) + " 2" + getString(R.string.owns) + " " + getString(R.string.color));
        spnPlayerStart= (Spinner) findViewById(R.id.spinnerStart);

        ImageArrayAdapter adapter = new ImageArrayAdapter(getApplicationContext(), Constants.intTokens, Constants.strTokens);
        spnPlayer1.setAdapter(adapter);
        spnPlayer1.setSelection(0);
        spnPlayer2.setAdapter(adapter);
        spnPlayer2.setSelection(1);
        adapter = new ImageArrayAdapter(getApplicationContext(), null, Constants.strPlayers);
        spnPlayerStart.setAdapter(adapter);
        spnPlayerStart.setSelection(0);

    }

    @Override
    public void onClick(View v) {
        //Pass values to start game if the colours are different
        if (v.getId() == R.id.new_game && !checkColours()) {
            Intent i = new Intent(MainActivity.this, GameBoard.class);
            Bundle bundle = new Bundle();
            bundle.putInt("player1",spnPlayer1.getSelectedItemPosition());
            bundle.putInt("player2",spnPlayer2.getSelectedItemPosition());
            bundle.putInt("playerStart",spnPlayerStart.getSelectedItemPosition());
            i.putExtras(bundle);
            this.startActivity(i);
        }
    }

    public boolean checkColours(){
        //Alert Dialog if the colours match - get the players to change
        if (spnPlayer1.getSelectedItemPosition()==spnPlayer2.getSelectedItemPosition()) {
            AlertDialog.Builder coloursDialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);

            coloursDialogBuilder.setTitle(getString(R.string.sameColours));
            coloursDialogBuilder.setMessage(getString(R.string.moreFun));
            coloursDialogBuilder.setIcon(R.mipmap.ic_launcher);

            coloursDialogBuilder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selection) {
                    dialog.dismiss();
                }
            });

            AlertDialog newGameDialog = coloursDialogBuilder.create();
            newGameDialog.show();
            return true;
        } else {
            return false;
        }
    }

}
