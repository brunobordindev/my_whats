package br.com.mywhatsapp2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import br.com.mywhatsapp2.R;
import br.com.mywhatsapp2.config.ConfiguracaoFirebase;
import br.com.mywhatsapp2.databinding.ActivityPrincipalBinding;
import br.com.mywhatsapp2.fragment.ContatosFragment;
import br.com.mywhatsapp2.fragment.ConversasFragment;

public class PrincipalActivity extends AppCompatActivity {

    private ActivityPrincipalBinding binding;
    private FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAuth();
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_principal );

        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Zap Black");
        setSupportActionBar(toolbar);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Conversas", ConversasFragment.class)
                .add("Contatos", ContatosFragment.class)
                .create()
        );
        binding.viewPager.setAdapter(adapter);
        binding.viewPagerTab.setViewPager(binding.viewPager);

        //config search view
        searchView = findViewById(R.id.material_search_principal);

        //Listener para o search view quando fechar a caixa de pesquisa
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                fragment.recarregarConversas();
            }
        });

        //Listener para a caixa de tesxto
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                if (newText != null && !newText.isEmpty()){
                    fragment.pesquisarConversas(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);

        //configurar botao de pesquisa
        MenuItem item = menu.findItem(R.id.menu_pesquisa);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_pesquisa:
                break;
            case R.id.menu_configuracoes:
                startActivity(new Intent(getApplicationContext(), ConfiguracaoActivity.class));
                break;
            case R.id.menu_sair:
                auth.signOut();
                voltandoProInicio();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void voltandoProInicio(){
        startActivity(new Intent( getApplicationContext(), MainActivity.class));
    }
}