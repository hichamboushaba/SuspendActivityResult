package dev.hichamboushaba.suspendactivityresult.permission

interface PermissionManager {
    companion object {
        fun getInstance(): PermissionManager = PermissionManagerImpl
    }

    fun hasPermission(permission: String): Boolean
    suspend fun requestPermission(permission: String): PermissionStatus
    suspend fun requestPermissions(vararg permissions: String): Map<String, PermissionStatus>
}