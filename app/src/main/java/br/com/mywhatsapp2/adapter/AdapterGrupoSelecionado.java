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

public class AdapterGrupoSelecionado extends RecyclerView.Adapter<AdapterGrupoSelecionado.MyViewHolder> {

    private List<Usuario> usuarioSelecionados = new ArrayList<>();
    private Context context;

    public AdapterGrupoSelecionado(List<Usuario> usuarioSelecionados, Context context) {
        this.usuarioSelecionados = usuarioSelecionados;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemListaGrupo = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grupo_selecionado, parent, false);
        return new MyViewHolder(itemListaGrupo);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Usuario usuario = usuarioSelecionados.get(position);

        holder.nome.setText(usuario.getNome());

        if (usuario.getFoto() != null){

            Uri uri = Uri.parse(usuario.getFoto());
            Glide.with(context).load(uri).into(holder.foto);
        }else{
            holder.foto.setImageResource(R.drawable.padrao);
        }
    }

    @Override
    public int getItemCount() {
        return usuarioSelecionados.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imageAdapterGrupo);
            nome = itemView.findViewById(R.id.nomeGrupo);
        }
    }
}

