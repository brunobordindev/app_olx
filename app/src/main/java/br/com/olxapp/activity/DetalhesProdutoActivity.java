package br.com.olxapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.ImageListener;

import br.com.olxapp.R;
import br.com.olxapp.databinding.ActivityDetalhesProdutoBinding;
import br.com.olxapp.model.Anuncio;

public class DetalhesProdutoActivity extends AppCompatActivity {

    private ActivityDetalhesProdutoBinding binding;
    private Anuncio anuncioSelecionado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detalhes_produto);

        //configurar toolbar - Mexer na manifest - Se quiser so a seta para voltar é so mexer no manifest, se quiser colocar outro tipo de icone deve acrescentar as duas linha de baixo
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24); //seta
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Detalhes do produto");

        //recupera anuncio para exibicao
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");

        if (anuncioSelecionado != null){
            binding.textTituloDetalhe.setText(anuncioSelecionado.getTitulo());
            binding.textPrecoDetalhe.setText(anuncioSelecionado.getValor());
            binding.textEstadoDetalhe.setText(anuncioSelecionado.getEstado());
            binding.textDescricaoDetalhe.setText(anuncioSelecionado.getDescricao());

            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {

                    String urlString = anuncioSelecionado.getFotos().get(position);
                    Picasso.get().load(urlString).into(imageView);
                }
            };

            binding.carouselView.setPageCount(anuncioSelecionado.getFotos().size());
            binding.carouselView.setImageListener(imageListener);
        }

        binding.btnTelefone.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", anuncioSelecionado.getTelefone(), null));
            startActivity(intent);
        });

    }
}