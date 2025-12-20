package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.TaskEventsService
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.INotificationObjectRepository
import co.bondspot.spbttest.domain.contract.INotificationRepository
import co.bondspot.spbttest.domain.contract.INotificationSubscriptionService
import org.springframework.stereotype.Service

@Service
class TaskEventsService(
    fga: IFgaProvider,
    fts: IFullTextSearchProvider,
    notifSubService: INotificationSubscriptionService,
    notifRepo: INotificationRepository,
    notifObjectRepo: INotificationObjectRepository,
) : TaskEventsService(fga, fts, notifSubService, notifRepo, notifObjectRepo)
