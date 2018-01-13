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

class AppManagerModelLoader implements ModelLoader<ApplicationInfo, ApplicationInfo> {

    @Nullable
    @Override
    public LoadData<ApplicationInfo> buildLoadData(ApplicationInfo applicationInfo, int width,
                                                   int height, Options options) {
        return new LoadData<>(new CustomKey(applicationInfo.toString()), new CastingDataFetcher(applicationInfo));
    }

    @Override
    public boolean handles(ApplicationInfo s) {
        return s != null;
    }


    public static class AppManagerModelLoaderFactory
            implements ModelLoaderFactory<ApplicationInfo, ApplicationInfo>
    {

        @Override
        public ModelLoader<ApplicationInfo, ApplicationInfo> build(MultiModelLoaderFactory
                                                                  multiFactory) {
            return new AppManagerModelLoader();
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
        private final ApplicationInfo applicationInfo;

        CastingDataFetcher(ApplicationInfo applicationInfo) {
            this.applicationInfo = applicationInfo;
        }


        @Override
        public void loadData(Priority priority, DataCallback<? super ApplicationInfo> callback) {
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
