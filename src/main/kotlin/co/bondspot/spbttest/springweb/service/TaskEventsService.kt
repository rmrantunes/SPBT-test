package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.TaskEventsService
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import org.springframework.stereotype.Service

@Service
class TaskEventsService(fga: IFgaProvider, fts: IFullTextSearchProvider) :
    TaskEventsService(fga, fts)
