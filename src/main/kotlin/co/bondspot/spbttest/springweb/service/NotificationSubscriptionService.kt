package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.NotificationSubscriptionService
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import org.springframework.stereotype.Service

@Service
class NotificationSubscriptionService(fts: IFullTextSearchProvider) :
    NotificationSubscriptionService(fts)
