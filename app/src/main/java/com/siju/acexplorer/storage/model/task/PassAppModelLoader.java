package com.siju.acexplorer.storage.model.task;


import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.model.helper.AppUtils;

class PassAppModelLoader implements ModelLoader<String, ApplicationInfo> {

    @Nullable
    @Override
    public LoadData<ApplicationInfo> buildLoadData(String path, int width,
                                                   int height, Options options) {
        return new LoadData<>(new CustomKey(path), new CastingDataFetcher(path));
    }

    @Override
    public boolean handles(String s) {
        return s != null;
    }


    public static class PassAppModelLoaderFactory
            implements ModelLoaderFactory<String, ApplicationInfo>
    {

        @Override
        public ModelLoader<String, ApplicationInfo> build(MultiModelLoaderFactory
                                                                  multiFactory) {
            return new PassAppModelLoader();
        }

        @Override
        public void teardown() {
            //no-op
        }
    }

    /**
     * Extremely unsafe, use with care.
     */
    private static class CastingDataFetcher implements DataFetcher<ApplicationInfo> {
        private final String path;

        CastingDataFetcher(String path) {
            this.path = path;
        }


        @Override
        public void loadData(Priority priority, DataCallback<? super ApplicationInfo> callback) {
            ApplicationInfo applicationInfo = AppUtils.getAppInfo(AceApplication.getAppContext(),
                                                                  path);
            callback.onDataReady(applicationInfo);
        }

        @Override
        public void cleanup() {
        }


        @Override
        public void cancel() {
        }

        @NonNull
        @Override
        public Class<ApplicationInfo> getDataClass() {
            return ApplicationInfo.class;
        }


        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }
}
