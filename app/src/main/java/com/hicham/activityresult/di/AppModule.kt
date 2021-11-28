package com.hicham.activityresult.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.hichamboushaba.suspendactivityresult.ActivityResultManager

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun providesActivityResultManager(): ActivityResultManager {
        return ActivityResultManager.getInstance()
    }
}