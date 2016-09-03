package io.ibj.mcauthenticator.model.datasource;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/30/16
 */
public interface UpdateHook {

    void update(UpdatableFlagData me);

}
