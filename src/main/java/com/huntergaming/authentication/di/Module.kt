package com.huntergaming.authentication.di

import com.huntergaming.authentication.Authentication
import com.huntergaming.authentication.GooglePlayGamesAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class Module {

    @Provides
    internal fun provideGooglePlayGamesAuth(auth: GooglePlayGamesAuth): Authentication = auth
}