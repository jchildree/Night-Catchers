package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.model.BondStage
import com.nightcatchers.core.domain.model.RoomStage
import javax.inject.Inject

class GetBondStageUseCase @Inject constructor() {
    operator fun invoke(trust: Int): BondStage = BondStage.fromTrust(trust)
}

class GetRoomStageUseCase @Inject constructor() {
    operator fun invoke(trust: Int): RoomStage = RoomStage.fromTrust(trust)
}
