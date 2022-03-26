package com.mikhailgrigorev.quickpassword.di.modules

import com.mikhailgrigorev.quickpassword.common.Application
import com.mikhailgrigorev.quickpassword.data.dao.FolderDao
import com.mikhailgrigorev.quickpassword.data.dao.PasswordCardDao
import com.mikhailgrigorev.quickpassword.data.database.FolderCardDatabase
import com.mikhailgrigorev.quickpassword.data.database.PasswordCardDatabase
import com.mikhailgrigorev.quickpassword.data.repository.FolderRepository
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository
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
