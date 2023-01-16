package br.com.mywhatsapp2.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.adapter.AdapterGrupoSelecionado;
import br.com.mywhatsapp2.config.ConfiguracaoFirebase;
import br.com.mywhatsapp2.databinding.ActivityCadastroGrupoBinding;
import br.com.mywhatsapp2.helper.UsuarioFirebase;
import br.com.mywhatsapp2.model.Grupo;
import br.com.mywhatsapp2.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {


    private ActivityCadastroGrupoBinding binding;
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView participantes;
    private EditText editNomeGrupo;
    private RecyclerView rvCadastroSelecionados;
    private AdapterGrupoSelecionado adapterGrupoSelecionado;
    private CircleImageView imageCadastroGrupo;
    private static final int REQUESTCODE_GALERIA = 200;
    private StorageReference storageReference;
    private Grupo grupo;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cadastro_grupo );

        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Defina o nome");
        setSupportActionBar(toolbar);

        participantes = findViewById(R.id.txt_qtd_membros);
        rvCadastroSelecionados = findViewById(R.id.rv_cadastro_selecionados);
        imageCadastroGrupo = findViewById(R.id.edit_image_grupo);
        editNomeGrupo = findViewById(R.id.edit_nome_grupo);

        //Configuracoes Storage - imagem
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        grupo = new Grupo();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();


        //botao voltar - tem que mexer no Manifests
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recuperar lista de membros da grupoactivity
        if (getIntent().getExtras() != null){

            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("listaMembros");
            listaMembrosSelecionados.addAll(membros);
            participantes.setText("Partipantes: " + listaMembrosSelecionados.size()  );
        }

        //config adapter
        adapterGrupoSelecionado = new AdapterGrupoSelecionado(listaMembrosSelecionados, getApplicationContext());

        //config recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        rvCadastroSelecionados.setLayoutManager(layoutManager);
        rvCadastroSelecionados.setHasFixedSize(true);
        rvCadastroSelecionados.setAdapter(adapterGrupoSelecionado);

        imageCadastroGrupo.setOnClickListener(view ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, REQUESTCODE_GALERIA);
            }
        });

        binding.fabConcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomeGrupo = editNomeGrupo.getText().toString();

                //add lista de membros o usuario que esta logado
                listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());
                grupo.setMembros(listaMembrosSelecionados);
                grupo.setNome(nomeGrupo);
                grupo.salvar();

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
                    case REQUESTCODE_GALERIA:
                        Uri localImagemSelecioada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecioada);
                        break;
                }

                if (imagem != null){
                    imageCadastroGrupo.setImageBitmap(imagem);

                    //recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 72, baos);
                    byte dadosImagem[] = baos.toByteArray();

                    //Salvar imagem no firebase
                    final StorageReference imagemRef = storageReference.child("imagens")
                            .child("grupos")
                            .child( grupo.getId() + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(CadastroGrupoActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    }).addOnSuccessListener(CadastroGrupoActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String url = task.getResult().toString();
                                    grupo.setFoto(url);
                                }
                            });
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}