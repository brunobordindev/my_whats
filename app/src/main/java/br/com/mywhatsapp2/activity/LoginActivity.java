package br.com.mywhatsapp2.activity;

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

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.config.ConfiguracaoFirebase;
import br.com.mywhatsapp2.databinding.ActivityLoginBinding;
import br.com.mywhatsapp2.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login );

        binding.linearCadastro.setOnClickListener(view -> {
            startActivity(new Intent( getApplicationContext(), CadastroActivity.class));
        });

        binding.btnLogin.setOnClickListener(view -> {

            String email = binding.editEmailLogin.getText().toString();
            String senha = binding.editSenhaLogin.getText().toString();

            if (!email.isEmpty()){
                if (!senha.isEmpty()){

                    usuario = new Usuario(email, senha);
                    validarLogin();

                }else{
                    Toast.makeText(getApplicationContext(), "Preencha a senha", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Preencha o e-mail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarLogin() {

        auth = ConfiguracaoFirebase.getFirebaseAuth();
        auth.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    verificarUsuarioLogado();
                    Toast.makeText(getApplicationContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

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

                    Toast.makeText(getApplicationContext(), excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    private void abrirTelaPrincipal() {
        startActivity(new Intent( getApplicationContext(), PrincipalActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarUsuarioLogado();
    }

    private void verificarUsuarioLogado() {

        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAuth();
//        auth.signOut();
        if (auth.getCurrentUser() != null){
            abrirTelaPrincipal();
        }
    }
}