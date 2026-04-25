package com.fakturkuid.app.data.repository

import com.fakturkuid.app.data.dao.BusinessProfileDao
import com.fakturkuid.app.data.entity.BusinessProfile
import kotlinx.coroutines.flow.Flow

class BusinessProfileRepository(private val dao: BusinessProfileDao) {
    
    fun getProfile(): Flow<BusinessProfile?> {
        return dao.getProfile()
    }

    suspend fun getProfileSync(): BusinessProfile? {
        return dao.getProfileSync()
    }
    
    suspend fun saveProfile(profile: BusinessProfile) {
        dao.saveProfile(profile)
    }

    suspend fun syncLogoFromFile(context: android.content.Context) {
        dao.getProfileSync()?.let { profile ->
            if (profile.logoBlob != null) {
                val logoFile = profile.logoUri?.let { java.io.File(it) }
                if (logoFile == null || !logoFile.exists()) {
                    // File is missing but blob exists! Recreate it.
                    val newPath = com.fakturkuid.app.utils.FileUtil.saveBytesToInternalStorage(
                        context, profile.logoBlob, "business_logo.png"
                    )
                    if (newPath != null) {
                        dao.saveProfile(profile.copy(logoUri = newPath))
                    }
                }
            }
        }
    }
}
