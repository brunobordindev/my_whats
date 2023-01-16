package br.com.mywhatsapp2.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.config.ConfiguracaoFirebase;
import br.com.mywhatsapp2.databinding.ActivityConfiguracaoBinding;
import br.com.mywhatsapp2.helper.Permissao;
import br.com.mywhatsapp2.helper.UsuarioFirebase;
import br.com.mywhatsapp2.model.Usuario;

public class ConfiguracaoActivity extends AppCompatActivity {

    private ActivityConfiguracaoBinding binding;
    private String permissoesNecessarias[] = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int REQUESTCODE_CAMERA = 100;
    private static final int REQUESTCODE_GALERIA = 200;
    private StorageReference storageReference;
    private String idUsuario;
    private Usuario usuarioLogado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_configuracao );

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Configuracoes Storage - imagem
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        idUsuario = UsuarioFirebase.getIdentificadorUsuario();

        //Validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Configuracões");
        setSupportActionBar(toolbar);

        //botao voltar - tem que mexer no Manifests
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recuperar dados usuario
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        if (url != null){
            Glide.with(ConfiguracaoActivity.this)
                    .load(url)
                    .into(binding.editarFoto);
        }else{
            binding.editarFoto.setImageResource(R.drawable.padrao);
        }

        binding.textEditNome.setText(usuario.getDisplayName());


        //Camera
        binding.btnCamera.setOnClickListener(view -> {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, REQUESTCODE_CAMERA);
            }
        });

        //Galeria
        binding.btnGaleria.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, REQUESTCODE_GALERIA);
            }
        });

        //Atualizar nome
        binding.salvarEditNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String atualizarNome = binding.textEditNome.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario(atualizarNome);
                if (retorno){

                    usuarioLogado.setNome(atualizarNome);
                    usuarioLogado.atualizar();

                    Toast.makeText(getApplicationContext(), "Nome alterado com sucesso", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Erro ao atualizar nome", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {

                switch (requestCode){
                    case REQUESTCODE_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case REQUESTCODE_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                if (imagem != null){
                    binding.editarFoto.setImageBitmap(imagem);

                    //recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 72, baos);
                    byte dadosImagem[] = baos.toByteArray();

                    //Salvar imagem no firebase
                    final StorageReference imagemRef = storageReference.child("imagens")
                            .child("perfil")
                            .child(idUsuario + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(ConfiguracaoActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(ConfiguracaoActivity.this, "Upload da imagem falhou: " + e.getMessage().toString(), Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(ConfiguracaoActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    atualizarfotoUsuario(url);
                                }
                            });
                            Toast.makeText(getApplicationContext(), "Upload realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    private void atualizarfotoUsuario(Uri url) {
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if (retorno){
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualizar();
            Toast.makeText(getApplicationContext(), "Sua foto foi alterada com sucesso", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Erro ao alterar foto", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder( this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}