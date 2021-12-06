package dev.hichamboushaba.suspendactivityresult.permission

interface PermissionManager {
    companion object {
        fun getInstance(): PermissionManager = PermissionManagerImpl
    }

    /**
     * Determine whether <em>you</em> have been granted a particular permission.
     *
     * @param permission The name of the permission being checked.
     * @return a [PermissionStatus] to indicate the current status.
     */
    fun hasPermission(permission: String): Boolean

    /**
     * Request a single permission.
     *
     * @param permission The name of the permission to be requested.
     * @return a [PermissionStatus] to indicate the current status.
     *
     * @see [dev.hichamboushaba.suspendactivityresult.requestPermission]
     */
    suspend fun requestPermission(permission: String): PermissionStatus

    /**
     * Request multiple permission.
     *
     * @param permissions the names of permissions to be requested.
     * @return a [Map] containing the [PermissionStatus] of the request indexed by the
     * permission name
     *
     * @see [dev.hichamboushaba.suspendactivityresult.requestPermissions]
     */
    suspend fun requestPermissions(vararg permissions: String): Map<String, PermissionStatus>
}