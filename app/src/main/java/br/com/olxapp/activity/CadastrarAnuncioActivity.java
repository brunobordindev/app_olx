package br.com.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.olxapp.R;
import br.com.olxapp.databinding.ActivityCadastrarAnuncioBinding;
import br.com.olxapp.helper.ConfiguracaoFirebase;
import br.com.olxapp.helper.Permissao;
import br.com.olxapp.model.Anuncio;
import dmax.dialog.SpotsDialog;

public class CadastrarAnuncioActivity extends AppCompatActivity implements View.OnClickListener{

    private ActivityCadastrarAnuncioBinding binding;

    private String[] permissoes = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private List<String> listasFotosRecuperadas = new ArrayList<>();
    private List<String> listasUrlFotos= new ArrayList<>();

    private Anuncio anuncio;
    private StorageReference storage;

    private android.app.AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cadastrar_anuncio);

        getSupportActionBar().setTitle("Cadastrar anúncio");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //config iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //validar permissoes
        Permissao.validarPermissoes(permissoes, this, 1 );

        inicializarComponentes();
        carregarDadosSpinner();

        binding.btnSalvarAnuncio.setOnClickListener(view -> {

           anuncio = configuarAnuncio();
            String valor = String.valueOf(binding.editValor.getRawValue());

            if (listasFotosRecuperadas.size() != 0){ //posso forçar ele posta no minimo duas fotos ai eu colocaria >=2 ou == 3 para ter as tres fotos

                if (!anuncio.getEstado().isEmpty()){
                    if (!anuncio.getCategoria().isEmpty()){
                        if (!anuncio.getTitulo().isEmpty()){
                            if (!valor.isEmpty() && !valor.equals("0")){
                                if (!anuncio.getDescricao().isEmpty()){
                                    if (!anuncio.getTelefone().isEmpty() && anuncio.getTelefone().length() >= 10 ){

                                        salvarAnuncio();

                                    }else{
                                        exibirMensagemErro("Preencha o campo telefone!");
                                    }
                                }else{
                                    exibirMensagemErro("Digite a descrição do item!");
                                }
                            }else{
                                exibirMensagemErro("Preencha o campo valor!");
                            }
                        }else{
                            exibirMensagemErro("Preencha o campo título!");
                        }
                    }else{
                        exibirMensagemErro("Preencha o campo categoria!");
                    }
                }else{
                    exibirMensagemErro("Preencha o campo estado!");
                }
            }else{
                exibirMensagemErro("Selecione ao menos uma foto!");
            }

        });

    }

    private Anuncio configuarAnuncio(){

        String estado = binding.spinnerEstado.getSelectedItem().toString();
        String categoria = binding.spinnerCategoria.getSelectedItem().toString();
        String titulo = binding.editTitulo.getText().toString();
        String valor = binding.editValor.getText().toString();
        String descricao = binding.editDescricao.getText().toString();
        String telefone = binding.editTelefone.getText().toString();
        String fone = binding.editTelefone.getUnMasked();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setDescricao(descricao);
        anuncio.setTelefone(telefone);

        return  anuncio;
    }
    public void salvarAnuncio(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando anúncio")
                .setCancelable(false)
                .build();
        dialog.show();

        //salvar  imagem no storage
        for (int i = 0; i <  listasFotosRecuperadas.size() ; i++){
            String urlImagem = listasFotosRecuperadas.get(i);
            int tamanhoLista =  listasFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i);
        }

    }

    private void salvarFotoStorage(String urlString, int totalFotos, int contador){

        /*
        “imagens”
            “anuncios”
                “id_anuncio” (gerar no constructor com push.getKey())
                    imagem0.jpeg
                    imagem1.jpeg
                    imagem2.jpeg
         */

        //Criar nó no storage
        final StorageReference imagemAnuncio  = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem" + contador);

        //Faxer upload do arquivo
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlString));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imagemAnuncio.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                       Uri url =  task.getResult();
                       String urlConvertida = url.toString();
                       listasUrlFotos.add(urlConvertida);

                       if (totalFotos == listasUrlFotos.size()){
                           anuncio.setFotos(listasUrlFotos);
                           anuncio.salvar();

                           dialog.dismiss();
                           finish();
                       }

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload da imagem");
                Log.i("INFO", "Falha ao fazer upload: " + e.getMessage());
            }
        });

    }

    private void exibirMensagemErro(String mensagem){

        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;
            case R.id.imageCadastro2:
                escolherImagem(2);
                break;
            case R.id.imageCadastro3:
                escolherImagem(3);
                break;
        }
    }

    public void escolherImagem(int requestCode){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){

            //recupera imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //configura imagem no imageView
            if(requestCode == 1){
                binding.imageCadastro1.setImageURI(imagemSelecionada);
            }else if (requestCode == 2){
                binding.imageCadastro2.setImageURI(imagemSelecionada);
            }else if (requestCode == 3){
                binding.imageCadastro3.setImageURI(imagemSelecionada);
            }

            listasFotosRecuperadas.add(caminhoImagem);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void carregarDadosSpinner() {

        //Pode fazer assim ou do jeito abaixo
//        String[] estados = new String[]{
//            "SP", "MT", "PR", ...
//        };
        //confi spinner categoria
        String[] estados = getResources().getStringArray(R.array.estados); // posso usar o de cima

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                estados
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEstado.setAdapter(adapter);

        //confi spinner categoria
        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategoria.setAdapter(adapterCategoria);
    }

    private void inicializarComponentes(){

        //Configura localidade para pt -> portugues BR -> Brasil
        Locale locale = new Locale("pt", "BR");
        binding.editValor.setLocale(locale);

        binding.imageCadastro1.setOnClickListener(this);
        binding.imageCadastro2.setOnClickListener(this);
        binding.imageCadastro3.setOnClickListener(this);

    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}