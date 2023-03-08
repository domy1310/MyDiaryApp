package com.example.diaryapp.di

import android.content.Context
import androidx.room.Room
import com.example.diaryapp.connectivity.NetworkConnectivityObserver
import com.example.diaryapp.data.database.ImagesDatabase
import com.example.diaryapp.util.Constants.IMAGES_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseObject {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ImagesDatabase {
        return Room.databaseBuilder(
            context = context,
            name = IMAGES_DATABASE,
            klass = ImagesDatabase::class.java
        ).build()
    }

    @Provides
    @Singleton
    fun provideImageToUploadDao(database: ImagesDatabase) = database.imagesToUploadDao()

    @Provides
    @Singleton
    fun provideImageToDeleteDao(database: ImagesDatabase) = database.imageToDeleteDao()

    //Move to network module
    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(@ApplicationContext context: Context) =
        NetworkConnectivityObserver(context)
}