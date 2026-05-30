package com.edu.matchvagasempresas.domain.repository;

import com.edu.matchvagasempresas.data.remote.dto.LookupItem;

import java.util.List;

public interface LookupRepository {

    void preload(OnReady onReady);

    List<LookupItem> getPortes();
    List<LookupItem> getRamos();
    List<LookupItem> getTiposVaga();
    List<LookupItem> getModalidades();
    List<LookupItem> getEscolaridades();
    List<LookupItem> getCidades();
    List<LookupItem> getStatusVaga();

    void clear();

    interface OnReady { void onReady(); }
}
