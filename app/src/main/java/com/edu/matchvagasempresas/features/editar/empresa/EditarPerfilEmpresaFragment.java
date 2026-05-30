package com.edu.matchvagasempresas.features.editar.empresa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
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
import com.edu.matchvagasempresas.data.local.SessionManager;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaRequest;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.LookupItem;
import com.edu.matchvagasempresas.data.repository.LookupRepositoryImpl;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditarPerfilEmpresaFragment extends Fragment {

    private EditarPerfilEmpresaViewModel viewModel;
    private AutoCompleteTextView actvPorte, actvRamo;
    private ImageView ivLogo;
    private MaterialButton btnAlterarFoto;
    private EmpresaResponse empresaAtual;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_editar_perfil_empresa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EditarPerfilEmpresaViewModel.class);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        actvPorte = view.findViewById(R.id.actv_porte);
        actvRamo  = view.findViewById(R.id.actv_ramo);
        ivLogo    = view.findViewById(R.id.iv_logo_empresa);
        btnAlterarFoto = view.findViewById(R.id.btn_alterar_foto);

        btnAlterarFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        view.findViewById(R.id.btn_salvar).setOnClickListener(v -> salvarPerfil(view));

        setupTelefoneMask(view.findViewById(R.id.et_telefone));

        viewModel.getEmpresaCarregada().observe(getViewLifecycleOwner(), e -> {
            empresaAtual = e;
            preencherFormulario(view, e);
            carregarLookups(view);
        });

        viewModel.getSalvarResult().observe(getViewLifecycleOwner(), resource -> {
            view.findViewById(R.id.btn_salvar).setEnabled(true);
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) {
                        new SessionManager(requireContext()).saveEmpresa(
                                resource.getData().id, resource.getData().nomeFantasia, resource.getData().status);
                    }
                    Toast.makeText(requireContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view.findViewById(R.id.btn_salvar)).navigateUp();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                default: break;
            }
        });

        viewModel.getUploadResult().observe(getViewLifecycleOwner(), resource -> {
            btnAlterarFoto.setEnabled(true);
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) carregarLogo(resource.getData().logoUrl);
                    Toast.makeText(requireContext(), "Logo atualizada!", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                default: break;
            }
        });

        viewModel.carregarPerfil();
    }

    private void carregarLookups(View view) {
        LookupRepositoryImpl.get(requireContext()).preload(() -> {
            if (!isAdded()) return;
            LookupRepositoryImpl repo = LookupRepositoryImpl.get(requireContext());
            bindDropdown(actvPorte, repo.getPortes());
            bindDropdown(actvRamo, repo.getRamos());
            if (empresaAtual != null && empresaAtual.porte != null)
                actvPorte.setText(empresaAtual.porte, false);
            if (empresaAtual != null && empresaAtual.ramoAtuacao != null)
                actvRamo.setText(empresaAtual.ramoAtuacao, false);
        });
    }

    private void bindDropdown(AutoCompleteTextView actv, List<LookupItem> items) {
        List<String> labels = new ArrayList<>();
        for (LookupItem item : items) labels.add(item.getLabel());
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));
    }

    private void preencherFormulario(View view, EmpresaResponse e) {
        setEditText(view, R.id.et_cnpj,          e.cnpj);
        setEditText(view, R.id.et_razao_social,  e.razaoSocial);
        setEditText(view, R.id.et_nome_fantasia, e.nomeFantasia);
        setEditText(view, R.id.et_descricao,     e.descricao);
        setEditText(view, R.id.et_site,          e.site);
        if (e.telefone != null && e.telefone.numero != null)
            setEditText(view, R.id.et_telefone, aplicarMascaraTelefone(e.telefone.numero));
        if (e.porte != null)       actvPorte.setText(e.porte, false);
        if (e.ramoAtuacao != null) actvRamo.setText(e.ramoAtuacao, false);
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
            int p = (int)(20 * getResources().getDisplayMetrics().density);
            ivLogo.setPadding(p, p, p, p);
            ivLogo.setImageResource(R.drawable.ic_business);
        }
    }

    private void onImagePicked(@Nullable Uri uri) {
        if (uri == null || !isAdded()) return;
        btnAlterarFoto.setEnabled(false);
        new Thread(() -> {
            try {
                File tmp = redimensionarImagem(uri);
                RequestBody rb = RequestBody.create(MediaType.parse("image/jpeg"), tmp);
                MultipartBody.Part part = MultipartBody.Part.createFormData("arquivo", tmp.getName(), rb);
                requireActivity().runOnUiThread(() -> viewModel.uploadLogo(part));
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    btnAlterarFoto.setEnabled(true);
                    Toast.makeText(requireContext(), "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
                });
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

    private void salvarPerfil(View view) {
        if (empresaAtual == null) {
            Toast.makeText(requireContext(), "Dados não carregados", Toast.LENGTH_SHORT).show();
            return;
        }

        String cnpj        = getText(view, R.id.et_cnpj);
        String razaoSocial  = getText(view, R.id.et_razao_social);
        String nomeFantasia = getText(view, R.id.et_nome_fantasia);
        String descricao    = getText(view, R.id.et_descricao);
        String site         = getText(view, R.id.et_site);
        String telefoneNum  = getText(view, R.id.et_telefone);

        LookupRepositoryImpl repo = LookupRepositoryImpl.get(requireContext());
        Long porteId = getSelectedId(repo.getPortes(), actvPorte.getText().toString());
        Long ramoId  = getSelectedId(repo.getRamos(),  actvRamo.getText().toString());

        if (porteId == null && empresaAtual.porte != null)
            porteId = getSelectedId(repo.getPortes(), empresaAtual.porte);
        if (ramoId == null && empresaAtual.ramoAtuacao != null)
            ramoId = getSelectedId(repo.getRamos(), empresaAtual.ramoAtuacao);

        if (porteId == null || ramoId == null) {
            Toast.makeText(requireContext(), "Selecione porte e ramo de atuação", Toast.LENGTH_SHORT).show();
            return;
        }

        Long empresaId = new SessionManager(requireContext()).getEmpresaId();
        if (empresaId == null) empresaId = empresaAtual.id;

        EmpresaRequest req = new EmpresaRequest(
                cnpj.isEmpty()        ? empresaAtual.cnpj        : cnpj,
                razaoSocial.isEmpty() ? empresaAtual.razaoSocial : razaoSocial,
                nomeFantasia.isEmpty()? empresaAtual.nomeFantasia : nomeFantasia,
                descricao.isEmpty()   ? empresaAtual.descricao   : descricao,
                porteId, ramoId,
                site.isEmpty()        ? empresaAtual.site         : site
        );

        if (!telefoneNum.isEmpty()) {
            Long tipoId = (empresaAtual.telefone != null && empresaAtual.telefone.tipoTelefoneId != null)
                    ? empresaAtual.telefone.tipoTelefoneId : 1L;
            boolean wpp = empresaAtual.telefone != null && empresaAtual.telefone.wpp;
            req.telefone = new EmpresaRequest.Telefone(telefoneNum, tipoId, wpp);
        }

        view.findViewById(R.id.btn_salvar).setEnabled(false);
        viewModel.salvarPerfil(empresaId, req);
    }

    private void setEditText(View root, int id, String value) {
        TextInputEditText et = root.findViewById(id);
        if (et != null && value != null) et.setText(value);
    }

    private String getText(View root, int id) {
        TextInputEditText et = root.findViewById(id);
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private Long getSelectedId(List<LookupItem> list, String label) {
        for (LookupItem item : list) {
            if (item.getLabel().equals(label)) return item.id;
        }
        return null;
    }

    private void setupTelefoneMask(TextInputEditText et) {
        if (et == null) return;
        et.addTextChangedListener(new TextWatcher() {
            private boolean updating = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (updating) return;
                updating = true;
                s.replace(0, s.length(), aplicarMascaraTelefone(s.toString()));
                updating = false;
            }
        });
    }

    private String aplicarMascaraTelefone(String input) {
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.length() > 11) digits = digits.substring(0, 11);
        String mask = digits.length() <= 10 ? "(##) ####-####" : "(##) #####-####";
        StringBuilder out = new StringBuilder();
        int d = 0;
        for (int m = 0; m < mask.length() && d < digits.length(); m++) {
            if (mask.charAt(m) == '#') out.append(digits.charAt(d++));
            else                       out.append(mask.charAt(m));
        }
        return out.toString();
    }
}
