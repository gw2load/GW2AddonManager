package com.gw2tb.manager.services

import com.gw2tb.manager.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationService {

    val notifications: Flow<List<Notification>>

}