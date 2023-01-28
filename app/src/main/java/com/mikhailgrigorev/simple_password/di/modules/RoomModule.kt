package com.mikhailgrigorev.simple_password.di.modules

import com.mikhailgrigorev.simple_password.common.Application
import com.mikhailgrigorev.simple_password.data.dao.FolderDao
import com.mikhailgrigorev.simple_password.data.dao.PasswordCardDao
import com.mikhailgrigorev.simple_password.data.database.FolderCardDatabase
import com.mikhailgrigorev.simple_password.data.database.PasswordCardDatabase
import com.mikhailgrigorev.simple_password.data.repository.FolderRepository
import com.mikhailgrigorev.simple_password.data.repository.PasswordCardRepository
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton


@Module
class RoomModule(val application: Application) {

    @Singleton
    @Provides
    fun providesPasswordCardDatabase(): PasswordCardDatabase {
        return PasswordCardDatabase.setInstance(application)
        // return PasswordCardDatabase.getInstance()
    }

    @Singleton
    @Provides
    @Inject
    fun providesPasswordCardDao(database: PasswordCardDatabase): PasswordCardDao {
        return database.PasswordCardDao()
    }

    @Singleton
    @Provides
    @Inject
    fun productPasswordCardRepository(database: PasswordCardDatabase): PasswordCardRepository {
        return PasswordCardRepository(database.PasswordCardDao())
    }

    @Singleton
    @Provides
    fun providesFolderCardDatabase(): FolderCardDatabase {
        return FolderCardDatabase.setInstance(application)
        // return FolderCardDatabase.getInstance()
    }

    @Singleton
    @Provides
    @Inject
    fun providesFolderDao(database: FolderCardDatabase): FolderDao {
        return database.FolderDao()
    }

    @Singleton
    @Provides
    @Inject
    fun productFolderRepository(database: FolderCardDatabase): FolderRepository {
        return FolderRepository(database.FolderDao())
    }

}
