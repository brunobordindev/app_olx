package br.com.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.olxapp.R;
import br.com.olxapp.adapter.AdapterAnuncios;
import br.com.olxapp.databinding.ActivityMeusAnunciosBinding;
import br.com.olxapp.helper.ConfiguracaoFirebase;
import br.com.olxapp.helper.RecyclerItemClickListener;
import br.com.olxapp.model.Anuncio;
import dmax.dialog.SpotsDialog;

public class MeusAnunciosActivity extends AppCompatActivity {

    private ActivityMeusAnunciosBinding binding;
    private RecyclerView  recyclerAnuncios;
    private List<Anuncio> anuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private DatabaseReference anuncioUsuarioRef;
    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_meus_anuncios);

        getSupportActionBar().setTitle("Meus anúncios");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configuracoes iniciais
        anuncioUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios")
                .child(ConfiguracaoFirebase.getIdUsuario());

        //inicializar componentes
        inicializarComponentes();

        //configurar RecyclerView
        adapterAnuncios = new AdapterAnuncios(anuncios, this);
        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnuncios.setHasFixedSize(true);
        recyclerAnuncios.setAdapter(adapterAnuncios);
        
        //recupera anuncios para o usuario
        recuperarAnuncios();

        //Adiciona evento de clique no recyclerView
        recyclerAnuncios.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                recyclerAnuncios,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                        Anuncio anuncioSelecionado = anuncios.get(position);
                        anuncioSelecionado.remover();

                        //avisando ue os dados foram alterados
                        adapterAnuncios.notifyDataSetChanged();
                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

        binding.fab.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), CadastrarAnuncioActivity.class);
            startActivity(i);
        });

    }

    private void recuperarAnuncios() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando anúncios")
                .setCancelable(false)
                .build();
        dialog.show();

        anuncioUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                anuncios.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    anuncios.add(ds.getValue(Anuncio.class));
                }

                Collections.reverse(anuncios);
                adapterAnuncios.notifyDataSetChanged();

                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void inicializarComponentes() {
        recyclerAnuncios = findViewById(R.id.recycler_anuncios);
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
        return false;
    }

}