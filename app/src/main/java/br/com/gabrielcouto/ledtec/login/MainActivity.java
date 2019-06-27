package br.com.gabrielcouto.ledtec.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import br.com.gabrielcouto.ledtec.R;
import br.com.gabrielcouto.ledtec.homepage.HomePage;
import br.com.gabrielcouto.ledtec.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin,btnSair;
    private EditText insertSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initReferences();
        welcome();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                welcome();
            }
        });
    }


    private void initReferences(){
        btnLogin = findViewById(R.id.btnLogin);
        btnSair = findViewById(R.id.btnSairLogin);
        insertSenha = findViewById(R.id.inputSenhaLogin);
    }

    private void welcome(){
        //if(Utils.verifyPassword(this,insertSenha.getText().toString())){
            Intent i = new Intent(this, HomePage.class);
            startActivity(i);
        //}
        //else
            //Toast.makeText(getApplicationContext(),"Tente novamente",Toast.LENGTH_SHORT).show();
    }
}
