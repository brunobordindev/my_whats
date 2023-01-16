package br.com.mywhatsapp2.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterContato extends RecyclerView.Adapter<AdapterContato.MyViewHolder> {

    private List<Usuario> listaUsuarioContatos = new ArrayList<>();
    private Context context;
    public AdapterContato(List<Usuario> listaContatos, Context context) {
        this.listaUsuarioContatos = listaContatos;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contato, parent,false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Usuario usuario = listaUsuarioContatos.get(position);
        boolean cabecalho = usuario.getEmail().isEmpty();


        holder.nome.setText(usuario.getNome());
        holder.email.setText(usuario.getEmail());

        if (usuario.getFoto() != null){

            Uri uri = Uri.parse(usuario.getFoto());
            Glide.with(context).load(uri).into(holder.foto);
        }else{
            if (cabecalho){
                holder.foto.setImageResource(R.drawable.icone_grupo);
                holder.email.setVisibility(View.GONE);
            }else{
                holder.foto.setImageResource(R.drawable.padrao);
            }
        }

    }

    @Override
    public int getItemCount() {
        return listaUsuarioContatos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome, email;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imageAdapter);
            nome = itemView.findViewById(R.id.textNomeAdaper);
            email = itemView.findViewById(R.id.textEmailAdaper);
        }
    }
}

