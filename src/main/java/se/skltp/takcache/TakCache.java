package se.skltp.takcache;

import java.util.List;

public interface TakCache {

    TakCacheLog refresh();

    TakCacheLog refresh(List<String> tjanstegranssnittFilter);

    TakCacheLog refresh(VagvalFilter vagvalFilter, BehorighetFilter behorighetFilter);

    BehorigheterCache getBehorigeterCache();

    VagvalCache getVagvalCache();
}
