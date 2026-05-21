package com.edu.matchvagasempresas.feature.perfil;

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
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.DataCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilEmpresaFragment extends Fragment {

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

        ivLogo      = view.findViewById(R.id.iv_logo_empresa);
        progressLogo = view.findViewById(R.id.progress_logo);
        cardLogo    = view.findViewById(R.id.card_logo_empresa);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btn_editar_perfil).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_editarPerfil));

        cardLogo.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        carregarPerfil(view);
    }

    private void carregarPerfil(View view) {
        DataCache.get().loadEmpresa(requireContext(),
                cached -> { if (cached != null && isAdded()) preencherDados(view, cached); },
                fresh  -> { if (isAdded()) preencherDados(view, fresh); }
        );
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
            Glide.with(this)
                    .load(logoUrl)
                    .placeholder(R.drawable.ic_business)
                    .error(R.drawable.ic_business)
                    .centerCrop()
                    .into(ivLogo);
        } else {
            ivLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            int p = dpToPx(20);
            ivLogo.setPadding(p, p, p, p);
            ivLogo.setImageResource(R.drawable.ic_business);
        }
    }

    private void onImagePicked(@Nullable Uri uri) {
        if (uri == null || !isAdded()) return;
        setLogoCarregando(true);
        new Thread(() -> {
            try {
                File tmp = uriParaArquivo(uri);
                String mime = requireContext().getContentResolver().getType(uri);
                if (mime == null) mime = "image/jpeg";
                RequestBody rb = RequestBody.create(MediaType.parse(mime), tmp);
                MultipartBody.Part part = MultipartBody.Part.createFormData("file", tmp.getName(), rb);
                String finalMime = mime;
                requireActivity().runOnUiThread(() ->
                        ApiClient.getService(requireContext()).uploadLogo(part)
                                .enqueue(new Callback<EmpresaResponse>() {
                                    @Override
                                    public void onResponse(@NonNull Call<EmpresaResponse> call,
                                                           @NonNull Response<EmpresaResponse> r) {
                                        if (!isAdded()) return;
                                        setLogoCarregando(false);
                                        if (r.isSuccessful()) {
                                            DataCache.get().invalidateEmpresa(requireContext());
                                            if (r.body() != null) {
                                                DataCache.get().saveEmpresa(requireContext(), r.body());
                                                carregarLogo(r.body().logoUrl);
                                            } else {
                                                carregarLogoLocal(uri);
                                            }
                                            Toast.makeText(requireContext(),
                                                    "Logo atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(requireContext(),
                                                    "Erro ao enviar logo (código " + r.code() + ")",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<EmpresaResponse> call,
                                                          @NonNull Throwable t) {
                                        if (!isAdded()) return;
                                        setLogoCarregando(false);
                                        Toast.makeText(requireContext(),
                                                "Falha de conexão", Toast.LENGTH_SHORT).show();
                                    }
                                }));
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    setLogoCarregando(false);
                    Toast.makeText(requireContext(),
                            "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void carregarLogoLocal(Uri uri) {
        if (!isAdded() || ivLogo == null) return;
        ivLogo.setPadding(0, 0, 0, 0);
        ivLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(uri).centerCrop().into(ivLogo);
    }

    private File uriParaArquivo(Uri uri) throws IOException {
        String mime = requireContext().getContentResolver().getType(uri);
        String ext  = mime != null && mime.contains("png") ? ".png" : ".jpg";
        File tmp = File.createTempFile("logo_", ext, requireContext().getCacheDir());
        try (InputStream in  = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(tmp)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }
        return tmp;
    }

    private void setLogoCarregando(boolean carregando) {
        if (progressLogo != null)
            progressLogo.setVisibility(carregando ? View.VISIBLE : View.GONE);
        if (ivLogo != null)
            ivLogo.setAlpha(carregando ? 0.4f : 1.0f);
        if (cardLogo != null)
            cardLogo.setEnabled(!carregando);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String formatarTelefone(String numero) {
        String digits = numero.replaceAll("[^0-9]", "");
        if (digits.length() == 11)
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 7),
                    digits.substring(7));
        if (digits.length() == 10)
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 6),
                    digits.substring(6));
        return numero;
    }

    private void setText(View root, int id, String value) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(value != null ? value : "");
    }
}
