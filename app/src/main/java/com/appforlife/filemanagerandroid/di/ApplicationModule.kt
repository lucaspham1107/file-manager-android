package com.appforlife.filemanagerandroid.di

import android.app.Application
import android.content.SharedPreferences
import com.appforlife.filemanagerandroid.helpers.Config
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Singleton
    @Provides
    fun provideAppPreferences(app: Application) = Config.newInstance(app)

}