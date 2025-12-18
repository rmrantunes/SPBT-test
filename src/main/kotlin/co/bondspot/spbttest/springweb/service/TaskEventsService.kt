package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.TaskEventsService
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.INotificationObjectRepository
import co.bondspot.spbttest.domain.contract.INotificationRepository
import org.springframework.stereotype.Service

@Service
class TaskEventsService(
    fga: IFgaProvider,
    fts: IFullTextSearchProvider,
    notifRepo: INotificationRepository,
    notifObjectRepo: INotificationObjectRepository,
) : TaskEventsService(fga, fts, notifRepo, notifObjectRepo)
