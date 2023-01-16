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
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.adapter.AdapterMensagem;
import br.com.mywhatsapp2.config.ConfiguracaoFirebase;
import br.com.mywhatsapp2.databinding.ActivityChatBinding;
import br.com.mywhatsapp2.helper.Base64Custom;
import br.com.mywhatsapp2.helper.UsuarioFirebase;
import br.com.mywhatsapp2.model.Conversa;
import br.com.mywhatsapp2.model.Grupo;
import br.com.mywhatsapp2.model.Mensagem;
import br.com.mywhatsapp2.model.Usuario;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private Usuario usuarioDestinatario;
    private FloatingActionButton fabEnviarMensagem;
    private EditText campoMensagem;
    private ImageView fotoChat;

    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    private RecyclerView recyclerViewMensagem;
    private AdapterMensagem adapter;
    private List<Mensagem> listaMensagem = new ArrayList<>();

    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    private static final int REQUESTCODE_GALERIA = 1;

    private Grupo grupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        toolbar.setTitle("Chat");
        setSupportActionBar(toolbar);

        //instanciando content
        fabEnviarMensagem = findViewById(R.id.fab_enviar_mensagem);
        campoMensagem = findViewById(R.id.edit_mensagem_chat);
        recyclerViewMensagem = findViewById(R.id.recycler_chat);
        fotoChat = findViewById(R.id.foto_chat);

        //recuperar dados usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        //botao voltar - tem que mexer no Manifests
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperar dados do usuario destinatario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            if (bundle.containsKey("chatGrupo")){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                binding.textNomeToolbar.setText(grupo.getNome());

                //foto toolbar
                String foto = grupo.getFoto();
                if (foto != null){
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(binding.imagemToolbar);
                }else{
                    binding.imagemToolbar.setImageResource(R.drawable.padrao);
                }

            }else{
                //nome toolbar
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                binding.textNomeToolbar.setText(usuarioDestinatario.getNome());

                //foto toolbar
                String foto = usuarioDestinatario.getFoto();
                if (foto != null){
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(binding.imagemToolbar);
                }else{
                    binding.imagemToolbar.setImageResource(R.drawable.padrao);
                }

                //recuperar dados usuario destinatario
                idUsuarioDestinatario = Base64Custom.codificadorBase64(usuarioDestinatario.getEmail());
            }
        }

        fabEnviarMensagem.setOnClickListener( view -> {

            String textoMensagem = campoMensagem.getText().toString();

            if (!textoMensagem.isEmpty()){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //salvar pro remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //salvar pro destinatario
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //salvar conversa para aba conversa
                salvarConversa(mensagem);


            }else{
                Toast.makeText(getApplicationContext(), "Digite uma mensagem!", Toast.LENGTH_SHORT).show();
            }

        });

        //Conf Adapter
        adapter = new AdapterMensagem(listaMensagem, getApplicationContext());

        //Config recycler
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMensagem.setLayoutManager(layoutManager);
        recyclerViewMensagem.setHasFixedSize(true);
        recyclerViewMensagem.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);


        fotoChat.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, REQUESTCODE_GALERIA);
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

                    //recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 72, baos);
                    byte dadosImagem[] = baos.toByteArray();

                    //Criar nome da imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //Configurar referencia do firebase
                    final StorageReference imagemRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(ChatActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                            Log.d("Erro", "Erro ao fazer upload");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    Mensagem mensagem = new Mensagem();
                                    mensagem.setIdUsuario(idUsuarioRemetente);
                                    mensagem.setMensagem("imagem.jpeg");
                                    mensagem.setImagem(url.toString());

                                    //salvar msg para o remetente e destnatario
                                    salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                                    salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                                    Toast.makeText(getApplicationContext(), "Sucesso ao enviar imagem!", Toast.LENGTH_SHORT).show();
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

    public void salvarConversa(Mensagem msg){

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idUsuarioRemetente);
        conversaRemetente.setIdDestinatario(idUsuarioDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());
        conversaRemetente.setUsuarioExibicao(usuarioDestinatario);

        conversaRemetente.salvar();
    }

    public void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){

        DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = databaseReference.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //limpa o texto
        campoMensagem.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagensFirebase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagensFirebase(){

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            //quando um item é add
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                listaMensagem.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            //quando um item é alterado
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            //quando um item é removido
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            //quando um item é movido
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            //quando tem algum erro
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

}