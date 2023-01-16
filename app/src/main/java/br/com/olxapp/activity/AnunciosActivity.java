package br.com.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.olxapp.R;
import br.com.olxapp.adapter.AdapterAnuncios;
import br.com.olxapp.databinding.ActivityAnunciosBinding;
import br.com.olxapp.helper.ConfiguracaoFirebase;
import br.com.olxapp.helper.RecyclerItemClickListener;
import br.com.olxapp.model.Anuncio;
import dmax.dialog.SpotsDialog;

public class AnunciosActivity extends AppCompatActivity {

    private ActivityAnunciosBinding binding;
    private FirebaseAuth autenticacao;
    private AdapterAnuncios adapterAnuncios;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private DatabaseReference anunciosPublicosRef;
    private AlertDialog dialog;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;
    private Anuncio anuncioS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_anuncios);

        //configuracoes iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase().child("anuncios");

        //configurar RecyclerView
        adapterAnuncios = new AdapterAnuncios(listaAnuncios, this);
        binding.recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAnunciosPublicos.setHasFixedSize(true);
        binding.recyclerAnunciosPublicos.setAdapter(adapterAnuncios);

        recuperarAnunciosPublicos();

        //aplicar evento de click
        binding.recyclerAnunciosPublicos.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                binding.recyclerAnunciosPublicos,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Anuncio anuncioSelecionado = listaAnuncios.get(position);

                        Intent intent = new Intent(getApplicationContext(), DetalhesProdutoActivity.class);
                        intent.putExtra("anuncioSelecionado", anuncioSelecionado);
                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

        //filtrar por estado
        binding.btnRegiao.setOnClickListener(view -> {

            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione o ESTADO desejado.");

            //configurar spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null );

            //confi spinner estado
            Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinner_filtro);
            String[] estados = getResources().getStringArray(R.array.estados); // posso usar o de cima
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    estados
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEstado.setAdapter(adapter);

            dialogEstado.setView(viewSpinner);


            dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    filtroEstado = spinnerEstado.getSelectedItem().toString();
                    recuperarAnunciosPorEstados();
                    filtrandoPorEstado = true;
                }
            });
            dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog dialog = dialogEstado.create();
            dialog.show();

        });

        binding.btnCategoria.setOnClickListener(view -> {

            if (filtrandoPorEstado == true){

                AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
                dialogEstado.setTitle("Selecione o CATEGORIA desejada.");

                //configurar spinner
                View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null );

                //confi spinner categoria
                Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinner_filtro);
                String[] estados = getResources().getStringArray(R.array.categorias); // posso usar o de cima
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        estados
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoria.setAdapter(adapter);

                dialogEstado.setView(viewSpinner);


                dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                        recuperarAnunciosPorCategoria();
                    }
                });
                dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog dialog = dialogEstado.create();
                dialog.show();

            }else {
                Toast.makeText(getApplicationContext(), "Escolha primeiro uma região", Toast.LENGTH_SHORT).show();
            }

        });

    }

    private void recuperarAnunciosPorEstados() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando anúncios por estado")
                .setCancelable(false)
                .build();
        dialog.show();

        //configura nó por estado
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaAnuncios.clear();
                //para cada no de estado percorre a categoria que o estado tem
                for (DataSnapshot categoria: snapshot.getChildren()){

                    //percorrer as categorias para pega o anuncio de cada vez
                    for (DataSnapshot  anuncios: categoria.getChildren()){

                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);

                    }
                }

                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarAnunciosPorCategoria() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando anúncios por categoria")
                .setCancelable(false)
                .build();
        dialog.show();

        //configura nó por categoria
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroCategoria);
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaAnuncios.clear();

                //percorrer as categorias para pega o anuncio de cada vez
                for (DataSnapshot  anuncios: snapshot.getChildren()){

                    Anuncio anuncio = anuncios.getValue(Anuncio.class);
                    listaAnuncios.add(anuncio);

                }

                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void recuperarAnunciosPublicos(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando anúncios")
                .setCancelable(false)
                .build();
        dialog.show();

        listaAnuncios.clear();

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for (DataSnapshot estados: snapshot.getChildren()){

                    //para cada no de estado percorre a categoria que o estado tem
                    for (DataSnapshot categoria: estados.getChildren()){

                        //percorrer as categorias para pega o anuncio de cada vez
                        for (DataSnapshot  anuncios: categoria.getChildren()){

                            Anuncio anuncio = anuncios.getValue(Anuncio.class);
                            listaAnuncios.add(anuncio);

                        }
                    }
                }

                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //montagem do menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (autenticacao.getCurrentUser() == null){// usuario deslogado
            menu.setGroupVisible(R.id.group_deslogado, true);

        }else{// usuario logado
            menu.setGroupVisible(R.id.group_logado, true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_cadastrar:
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
                break;
            case R.id.menu_anuncios:
                startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                break;
            case  R.id.menu_sair:
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}