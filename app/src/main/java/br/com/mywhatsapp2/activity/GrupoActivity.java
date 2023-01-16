package br.com.mywhatsapp2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.adapter.AdapterContato;
import br.com.mywhatsapp2.adapter.AdapterGrupoSelecionado;
import br.com.mywhatsapp2.config.ConfiguracaoFirebase;
import br.com.mywhatsapp2.databinding.ActivityGrupoBinding;
import br.com.mywhatsapp2.helper.RecyclerItemClickListener;
import br.com.mywhatsapp2.helper.UsuarioFirebase;
import br.com.mywhatsapp2.model.Usuario;

public class GrupoActivity extends AppCompatActivity {

    private ActivityGrupoBinding binding;
    private RecyclerView rvMembrosSelecionados, rvMembros;
    private AdapterContato adapterContato;
    private AdapterGrupoSelecionado adapterGrupoSelecionado;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerMembros;
    private FirebaseUser usuarioApp;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_grupo);

        iniciando();
        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioApp = UsuarioFirebase.getUsuarioAtual();

        toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar(toolbar);

        //botao voltar - tem que mexer no Manifests
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Adapter Recycler Membros
        adapterContato = new AdapterContato(listaMembros, getApplicationContext());

        //Config Recycler Membros
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvMembros.setLayoutManager(layoutManager);
        rvMembros.setHasFixedSize(true);
        rvMembros.setAdapter(adapterContato);

        rvMembros.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                rvMembros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaMembros.get(position);

                        //remover usuario selecionado da lista
                        listaMembros.remove(usuarioSelecionado);
                        adapterContato.notifyDataSetChanged();

                        //Add usuario na nova lista de selecionados
                        listaMembrosSelecionados.add(usuarioSelecionado);
                        adapterGrupoSelecionado.notifyDataSetChanged();
                        membrosSelecinadosToolbar();

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

        //Config adapter grupoSelecionados
        adapterGrupoSelecionado = new AdapterGrupoSelecionado(listaMembrosSelecionados, getApplicationContext());

        //config recycler GrupoSelecionados
        RecyclerView.LayoutManager layoutManagerGrupo = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        rvMembrosSelecionados.setLayoutManager(layoutManagerGrupo);
        rvMembrosSelecionados.setHasFixedSize(true);
        rvMembrosSelecionados.setAdapter(adapterGrupoSelecionado);

        rvMembrosSelecionados.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                rvMembrosSelecionados,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);
                        listaMembrosSelecionados.remove(usuarioSelecionado);
                        adapterGrupoSelecionado.notifyDataSetChanged();

                        listaMembros.add(usuarioSelecionado);
                        adapterContato.notifyDataSetChanged();
                        membrosSelecinadosToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

        binding.fabEnviar.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), CadastroGrupoActivity.class);
            i.putExtra("listaMembros", (Serializable) listaMembrosSelecionados);
            startActivity(i);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioRef.removeEventListener(valueEventListenerMembros);

    }

    public void recuperarContatos() {

        valueEventListenerMembros = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dados : snapshot.getChildren()) {

                    Usuario usuario = dados.getValue(Usuario.class);

                    //para nao exibir o usuario do app
                    if (!usuarioApp.getEmail().equals(usuario.getEmail())) {
                        listaMembros.add(usuario);
                    }
                }

                adapterContato.notifyDataSetChanged();
                membrosSelecinadosToolbar();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void membrosSelecinadosToolbar(){

        int totalSelecionados = listaMembrosSelecionados.size();
        int totalMembros = listaMembros.size() + totalSelecionados;

        toolbar.setSubtitle(totalSelecionados + " de " + totalMembros + " selecionados");
    }

    private void iniciando() {

        rvMembros = findViewById(R.id.rv_membros);
        rvMembrosSelecionados = findViewById(R.id.rv_membros_selecionados);
    }
}