package br.com.mywhatsapp2.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.activity.ChatActivity;
import br.com.mywhatsapp2.adapter.AdapterConversa;
import br.com.mywhatsapp2.config.ConfiguracaoFirebase;
import br.com.mywhatsapp2.helper.RecyclerItemClickListener;
import br.com.mywhatsapp2.helper.UsuarioFirebase;
import br.com.mywhatsapp2.model.Conversa;


public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private AdapterConversa adapter;
    private List<Conversa> listaConversa = new ArrayList<>();
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;

    public ConversasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);
        recyclerViewConversas = view.findViewById(R.id.recyclerViewConversas);

        //adapter
        adapter = new AdapterConversa(listaConversa, getActivity());

        //recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager(layoutManager);
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setAdapter(adapter);

        //configurar evento de clique no recyclerconversa
        recyclerViewConversas.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerViewConversas,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Conversa conversaSelecionada = listaConversa.get(position);

                        if (conversaSelecionada.getIsGroup().equals("true")){

                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

        //configura conversas ref
        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef =  database.child("conversas").child(idUsuario);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
    }

    //Esse método é chamado somente na activity principal
    public void pesquisarConversas(String texto){

        List<Conversa> listasConversasBusca = new ArrayList<>();

        for (Conversa conversa : listaConversa){

            String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
            String ultimoMsg= conversa.getUltimaMensagem().toLowerCase();

            if (nome.contains(texto) || ultimoMsg.contains(texto) ){
                listasConversasBusca.add(conversa);

            }
        }
        adapter = new AdapterConversa(listasConversasBusca, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recarregarConversas(){
        adapter = new AdapterConversa(listaConversa, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recuperarConversas(){

        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                //recuperar conversas
                Conversa conversa = snapshot.getValue(Conversa.class);
                listaConversa.add(conversa);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}