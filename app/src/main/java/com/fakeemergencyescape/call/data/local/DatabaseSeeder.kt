package com.fakeemergencyescape.call.data.local

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val templateDao: TemplateDao,
) {
    suspend fun seedTemplatesIfNeeded() {
        if (templateDao.count() == 0) {
            templateDao.insertAll(TemplateSeedData.all())
        }
    }
}
