package com.mr21yse.application;


import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;


public class subActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final numchangerActivity numchanger;
        final EditText editText;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);





        //↓はリターンボタン
        Button returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //↓はnumchangerActivityとテキストのやり取りをする　　https://akira-watson.com/android/global-val.html
        numchanger = (numchangerActivity) this.getApplication();
        editText = (EditText)findViewById(R.id.tgtText);
        Button buttonMain = (Button) findViewById(R.id.button_main);
        buttonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String message = editText.getText().toString();

                if(TextUtils.isEmpty(message)){
                    message="1000";
                }

                if(message.equals("0")){
                    message="1000";
                }

                int changemessage =Integer.parseInt(message);
                message= String.valueOf(changemessage); //IntString変換を繰り返して入力文字先頭部分に0が存在してた場合0を消す(正規化)



                numchanger.setZerocount(message);



                Intent intent = new Intent(getApplication(), MainActivity.class);
                finish();
                startActivity(intent);
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // ... 処理 ...


                String message = editText.getText().toString();

                if(TextUtils.isEmpty(message)){
                    message="1000";
                }

                if(message.equals("0")){
                    message="1000";
                }

                int changemessage =Integer.parseInt(message);
                message= String.valueOf(changemessage); //IntString変換を繰り返して入力文字先頭部分に0が存在してた場合0を消す(正規化)



                numchanger.setZerocount(message);



                Intent intent = new Intent(getApplication(), MainActivity.class);
                finish();
                startActivity(intent);


                return false;
            }
        });

        editText.setSelection(editText.getText().length());//カーソル位置を最後尾に

    }


}