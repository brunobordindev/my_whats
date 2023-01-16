package br.com.mywhatsapp2.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.model.Conversa;
import br.com.mywhatsapp2.model.Grupo;
import br.com.mywhatsapp2.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterConversa extends RecyclerView.Adapter<AdapterConversa.MyViewHolder> {


    private List<Conversa> listaConversas;
    private Context context;
    public AdapterConversa(List<Conversa> listaConversa, FragmentActivity activity){
        this.listaConversas = listaConversa;
        this.context = activity;
    }

    @NonNull
    @Override
    public AdapterConversa.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemListaConversa = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contato, parent, false);
        return new MyViewHolder(itemListaConversa);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Conversa conversa = listaConversas.get(position);
        holder.ultimaMensagem.setText(conversa.getUltimaMensagem());

        if (conversa.getIsGroup().equals("true")){

            Grupo grupo = conversa.getGrupo();
            holder.nome.setText(grupo.getNome());

            if (grupo.getFoto() != null){
                Uri uri = Uri.parse(grupo.getFoto());
                Glide.with(context).load(uri).into(holder.foto);
            }else{
                holder.foto.setImageResource(R.drawable.padrao);
            }

        }else{
            Usuario usuario = conversa.getUsuarioExibicao();
            holder.nome.setText(usuario.getNome());

            if (usuario.getFoto() != null){
                Uri uri = Uri.parse(usuario.getFoto());
                Glide.with(context).load(uri).into(holder.foto);
            }else{
                holder.foto.setImageResource(R.drawable.padrao);
            }
        }



    }

    @Override
    public int getItemCount() {
        return listaConversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome, ultimaMensagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.imageAdapter);
            nome = itemView.findViewById(R.id.textNomeAdaper);
            ultimaMensagem = itemView.findViewById(R.id.textEmailAdaper);
        }
    }
}

