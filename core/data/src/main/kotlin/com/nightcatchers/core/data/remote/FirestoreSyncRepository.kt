package com.nightcatchers.core.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.nightcatchers.core.common.Dispatcher
import com.nightcatchers.core.common.NightCatchersDispatchers
import com.nightcatchers.core.data.local.dao.MonsterDao
import com.nightcatchers.core.data.local.dao.PetStateDao
import com.nightcatchers.core.data.local.entity.toFirestoreMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val monsterDao: MonsterDao,
    private val petStateDao: PetStateDao,
    @Dispatcher(NightCatchersDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    fun startPetStateSync(parentUid: String, childId: String, scope: CoroutineScope) {
        // Debounce rapid pet state changes (e.g. multiple interactions in quick succession)
        // before writing to Firestore to stay within write-rate limits.
        petStateDao.observeAllForSync()
            .debounce(5_000L)
            .conflate()
            .onEach { states ->
                states.forEach { entity ->
                    runCatching {
                        firestore
                            .collection("petState")
                            .document("${parentUid}_${childId}_${entity.monsterId}")
                            .set(entity.toFirestoreMap())
                            .await()
                    }
                }
            }
            .launchIn(scope)
    }

    fun startMonsterSync(parentUid: String, childId: String, scope: CoroutineScope) {
        monsterDao.observeAll()
            .debounce(5_000L)
            .conflate()
            .onEach { monsters ->
                monsters.forEach { entity ->
                    runCatching {
                        firestore
                            .collection("monsters")
                            .document("${parentUid}_${childId}_${entity.id}")
                            .set(entity.toFirestoreMap())
                            .await()
                    }
                }
            }
            .launchIn(scope)
    }
}
