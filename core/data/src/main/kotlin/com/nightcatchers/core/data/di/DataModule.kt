package com.nightcatchers.core.data.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.nightcatchers.core.data.local.NightCatchersDatabase
import com.nightcatchers.core.data.local.dao.MonsterDao
import com.nightcatchers.core.data.local.dao.PendingShareDao
import com.nightcatchers.core.data.local.dao.PetStateDao
import com.nightcatchers.core.data.local.dao.UserDao
import com.nightcatchers.core.data.repository.MonsterRepositoryImpl
import com.nightcatchers.core.data.repository.PetRepositoryImpl
import com.nightcatchers.core.data.repository.ShareRepositoryImpl
import com.nightcatchers.core.data.repository.UserRepositoryImpl
import com.nightcatchers.core.domain.repository.MonsterRepository
import com.nightcatchers.core.domain.repository.PetRepository
import com.nightcatchers.core.domain.repository.ShareRepository
import com.nightcatchers.core.domain.repository.UserRepository
import com.nightcatchers.core.security.DatabaseKeyManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: DatabaseKeyManager,
    ): NightCatchersDatabase {
        val passphrase = keyManager.getOrCreatePassphrase().concatToString().toByteArray(Charsets.UTF_8)
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(context, NightCatchersDatabase::class.java, "nightcatchers.db")
            .openHelperFactory(factory)
            .build()
    }

    @Provides fun provideMonsterDao(db: NightCatchersDatabase): MonsterDao = db.monsterDao()
    @Provides fun providePetStateDao(db: NightCatchersDatabase): PetStateDao = db.petStateDao()
    @Provides fun provideUserDao(db: NightCatchersDatabase): UserDao = db.userDao()
    @Provides fun providePendingShareDao(db: NightCatchersDatabase): PendingShareDao = db.pendingShareDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindMonsterRepository(impl: MonsterRepositoryImpl): MonsterRepository
    @Binds abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository
    @Binds abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds abstract fun bindShareRepository(impl: ShareRepositoryImpl): ShareRepository
}
