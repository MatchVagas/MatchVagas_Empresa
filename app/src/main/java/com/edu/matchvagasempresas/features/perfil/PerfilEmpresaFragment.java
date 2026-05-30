package com.edu.matchvagasempresas.features.perfil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PerfilEmpresaFragment extends Fragment {

    private PerfilEmpresaViewModel viewModel;
    private ImageView ivLogo;
    private ProgressBar progressLogo;
    private View cardLogo;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_perfil_empresa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PerfilEmpresaViewModel.class);

        ivLogo      = view.findViewById(R.id.iv_logo_empresa);
        progressLogo = view.findViewById(R.id.progress_logo);
        cardLogo    = view.findViewById(R.id.card_logo_empresa);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btn_editar_perfil).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_editarPerfil));

        cardLogo.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        viewModel.getEmpresaCached().observe(getViewLifecycleOwner(), e -> {
            if (e != null) preencherDados(view, e);
        });
        viewModel.getEmpresaFresh().observe(getViewLifecycleOwner(), e -> {
            if (e != null) preencherDados(view, e);
        });
        viewModel.getUploadResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    setLogoCarregando(true);
                    break;
                case SUCCESS:
                    setLogoCarregando(false);
                    if (resource.getData() != null) carregarLogo(resource.getData().logoUrl);
                    Toast.makeText(requireContext(), "Logo atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    setLogoCarregando(false);
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.carregarPerfil();
    }

    private void preencherDados(View view, EmpresaResponse e) {
        setText(view, R.id.tv_nome_fantasia, e.nomeFantasia);
        setText(view, R.id.tv_ramo_atuacao,  e.ramoAtuacao);
        setText(view, R.id.tv_cnpj,          e.cnpj);
        setText(view, R.id.tv_razao_social,  e.razaoSocial);
        setText(view, R.id.tv_porte,         e.porte);
        setText(view, R.id.tv_descricao,     e.descricao);
        setText(view, R.id.tv_site,          e.site);
        String telefone = (e.telefone != null && e.telefone.numero != null)
                ? formatarTelefone(e.telefone.numero) : "";
        setText(view, R.id.tv_telefone, telefone);
        carregarLogo(e.logoUrl);
    }

    private void carregarLogo(@Nullable String logoUrl) {
        if (!isAdded() || ivLogo == null) return;
        if (logoUrl != null && !logoUrl.isEmpty()) {
            ivLogo.setPadding(0, 0, 0, 0);
            ivLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(logoUrl)
                    .placeholder(R.drawable.ic_business).error(R.drawable.ic_business)
                    .centerCrop().into(ivLogo);
        } else {
            ivLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            int p = dpToPx(20);
            ivLogo.setPadding(p, p, p, p);
            ivLogo.setImageResource(R.drawable.ic_business);
        }
    }

    private void onImagePicked(@Nullable Uri uri) {
        if (uri == null || !isAdded()) return;
        new Thread(() -> {
            try {
                File tmp = redimensionarImagem(uri);
                RequestBody rb = RequestBody.create(MediaType.parse("image/jpeg"), tmp);
                MultipartBody.Part part = MultipartBody.Part.createFormData("arquivo", tmp.getName(), rb);
                requireActivity().runOnUiThread(() -> viewModel.uploadLogo(part));
            } catch (IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Erro ao processar imagem", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private File redimensionarImagem(Uri uri) throws IOException {
        final int MAX_SIZE = 512;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, opts);
        }
        int scale = 1;
        while (opts.outWidth / scale > MAX_SIZE || opts.outHeight / scale > MAX_SIZE) scale *= 2;
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap bitmap;
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bitmap == null) throw new IOException("Falha ao decodificar imagem");
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        if (w > MAX_SIZE || h > MAX_SIZE) {
            float ratio = Math.min((float) MAX_SIZE / w, (float) MAX_SIZE / h);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int)(w * ratio), (int)(h * ratio), true);
            bitmap.recycle();
            bitmap = scaled;
        }
        File tmp = File.createTempFile("logo_", ".jpg", requireContext().getCacheDir());
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        } finally {
            bitmap.recycle();
        }
        return tmp;
    }

    private void setLogoCarregando(boolean carregando) {
        if (progressLogo != null)
            progressLogo.setVisibility(carregando ? View.VISIBLE : View.GONE);
        if (ivLogo != null) ivLogo.setAlpha(carregando ? 0.4f : 1.0f);
        if (cardLogo != null) cardLogo.setEnabled(!carregando);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String formatarTelefone(String numero) {
        String digits = numero.replaceAll("[^0-9]", "");
        if (digits.length() == 11)
            return String.format("(%s) %s-%s", digits.substring(0,2), digits.substring(2,7), digits.substring(7));
        if (digits.length() == 10)
            return String.format("(%s) %s-%s", digits.substring(0,2), digits.substring(2,6), digits.substring(6));
        return numero;
    }

    private void setText(View root, int id, String value) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(value != null ? value : "");
    }
}
