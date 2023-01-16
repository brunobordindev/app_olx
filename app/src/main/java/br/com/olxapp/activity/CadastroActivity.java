package br.com.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.olxapp.R;
import br.com.olxapp.databinding.ActivityCadastroBinding;
import br.com.olxapp.helper.ConfiguracaoFirebase;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cadastro);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        binding.btnAcesso.setOnClickListener(view -> {

            String email = binding.editEmail.getText().toString();
            String senha = binding.editPassword.getText().toString();

            //verifica estado do switch
            if (binding.switchAcesso.isChecked()){ //cadastro

                autenticacao.createUserWithEmailAndPassword(
                        email, senha
                ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            try {

                                startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            Toast.makeText(CadastroActivity.this, "Sucesso ao realizar o cadastro!", Toast.LENGTH_SHORT).show();

                        }else{

                            String excecao = "";
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e){
                                excecao = "Digite uma senha mais forte";
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                excecao = "Digite um e-mail válido";
                            }catch (FirebaseAuthUserCollisionException e){
                                excecao = "E-mail já cadastrado!";
                            }catch (Exception e){
                                excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(CadastroActivity.this, "Erro: " + excecao, Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }else{ // logar

                autenticacao.signInWithEmailAndPassword(
                        email, senha
                ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));

                            Toast.makeText(getApplicationContext(), "Logado com sucesso!", Toast.LENGTH_SHORT).show();

                        }else{

                            String excecao = "";
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthInvalidUserException e){
                                excecao = "Usuário não está cadastrado";
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                excecao = "E-mail e senha não corresponde a um usuário cadastrado";
                            }catch (Exception e){
                                excecao = "Erro ao logar usuário" + e.getMessage();
                            }

                            Toast.makeText(getApplicationContext(), "Erro ao fazer login: "+ excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            if (!email.isEmpty()){
                if (!senha.isEmpty()){

                }else{
                    Toast.makeText(getApplicationContext(), "Preencha a senha!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
            }


        });

    }

}