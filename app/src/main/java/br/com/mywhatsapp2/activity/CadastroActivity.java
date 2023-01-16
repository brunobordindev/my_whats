package br.com.mywhatsapp2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.config.ConfiguracaoFirebase;
import br.com.mywhatsapp2.databinding.ActivityCadastroBinding;
import br.com.mywhatsapp2.helper.Base64Custom;
import br.com.mywhatsapp2.helper.UsuarioFirebase;
import br.com.mywhatsapp2.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private FirebaseAuth auth;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cadastro);

        binding.btnCadastrar.setOnClickListener( view -> {

            String nome = binding.editNomeCadastro.getText().toString();
            String email = binding.editEmailCadastro.getText().toString();
            String senha = binding.editSenhaCadastro.getText().toString();
            String confirmandoSenha = binding.editValidandoSenhaCadastro.getText().toString();

            if (!nome.isEmpty()){
                if (!email.isEmpty()){
                    if (!senha.isEmpty()){
                        if (!confirmandoSenha.isEmpty()){

                            if (senha.equals(confirmandoSenha)){

                                usuario = new Usuario(nome, email, senha);
                                cadastrarUsuario();

                            }else{
                                Toast.makeText(getApplicationContext(), "Senhas não compatíveis!", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(getApplicationContext(), "Válide a senha", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Preencha a senha", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Preencha o e-mail", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Preencha o nome", Toast.LENGTH_SHORT).show();
            }


        });

    }

    private void cadastrarUsuario() {

        auth = ConfiguracaoFirebase.getFirebaseAuth();
        auth.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    //Salvando os dados para DatabaseReference
                    String idUsuario = Base64Custom.codificadorBase64(usuario.getEmail());
                    usuario.setIdUsuario(idUsuario);
                    usuario.salvar();
                    finish();
                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                    Toast.makeText(getApplicationContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

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

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}