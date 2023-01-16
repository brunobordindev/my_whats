package br.com.mywhatsapp2.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.helper.UsuarioFirebase;
import br.com.mywhatsapp2.model.Mensagem;

public class AdapterMensagem extends RecyclerView.Adapter<AdapterMensagem.MyViewHolder> {

    private List<Mensagem> listaMensagem;
    private Context context;
    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;



    public AdapterMensagem(List<Mensagem> listaMensagem, Context c) {
        this.listaMensagem = listaMensagem;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;
        if(viewType == TIPO_REMETENTE){

            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_mensagem_remetente, parent, false);

        }else if (viewType == TIPO_DESTINATARIO){

            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_mensagem_destinatario, parent, false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Mensagem mensagem = listaMensagem.get(position);
        String msg = mensagem.getMensagem();
        String img= mensagem.getImagem();


        if (img != null){

            Uri url = Uri.parse(img);
            Glide.with(context)
                    .load(url)
                    .into(holder.imagem);

            holder.imagem.setVisibility(View.VISIBLE);
            holder.mensagem.setText(msg);

        }else{
            holder.mensagem.setText(msg);
            holder.imagem.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return listaMensagem.size();
    }

    //retornar o tipo de visualizacao
    @Override
    public int getItemViewType(int position) {

        Mensagem mensagem = listaMensagem.get(position);
        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();

        if(idUsuario.equals(mensagem.getIdUsuario())){

            return TIPO_REMETENTE;
        }
        return TIPO_DESTINATARIO;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView mensagem;
        private ImageView imagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mensagem = itemView.findViewById(R.id.text_mensagem_texto);
            imagem = itemView.findViewById(R.id.image_mensagem_foto);

        }
    }
}

