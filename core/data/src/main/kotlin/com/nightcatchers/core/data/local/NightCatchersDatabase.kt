package com.nightcatchers.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nightcatchers.core.data.local.dao.MonsterDao
import com.nightcatchers.core.data.local.dao.PendingShareDao
import com.nightcatchers.core.data.local.dao.PetStateDao
import com.nightcatchers.core.data.local.dao.UserDao
import com.nightcatchers.core.data.local.entity.MonsterEntity
import com.nightcatchers.core.data.local.entity.PendingShareEntity
import com.nightcatchers.core.data.local.entity.PetStateEntity
import com.nightcatchers.core.data.local.entity.UserProfileEntity

@Database(
    entities = [
        MonsterEntity::class,
        PetStateEntity::class,
        UserProfileEntity::class,
        PendingShareEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NightCatchersDatabase : RoomDatabase() {
    abstract fun monsterDao(): MonsterDao
    abstract fun petStateDao(): PetStateDao
    abstract fun userDao(): UserDao
    abstract fun pendingShareDao(): PendingShareDao
}
