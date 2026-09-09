package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnonymousMessageEntity
import com.example.data.model.SessionLogEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE (LOWER(username) = LOWER(:identifier) OR LOWER(email) = LOWER(:identifier)) LIMIT 1")
    suspend fun findByIdentifier(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) OR LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun findByUsernameOrEmail(username: String, email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE uniqueLinkSlug = :slug LIMIT 1")
    suspend fun getUserBySlug(slug: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestUser(): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT 1")
    fun getLatestUserFlow(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Long)

    @Query("DELETE FROM users WHERE username = 'Alexandre' AND email = 'alexandre@secretmsg.app'")
    suspend fun deleteDemoUser()
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY loginTimestamp DESC")
    fun getSessionsForUser(userId: Long): Flow<List<SessionLogEntity>>

    @Query("SELECT * FROM sessions ORDER BY loginTimestamp DESC")
    fun getAllSessions(): Flow<List<SessionLogEntity>>

    @Query("SELECT * FROM sessions WHERE sessionToken = :token LIMIT 1")
    suspend fun getSessionByToken(token: String): SessionLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionLogEntity): Long

    @Query("UPDATE sessions SET isActive = 0 WHERE id = :sessionId")
    suspend fun revokeSession(sessionId: Long)

    @Query("UPDATE sessions SET isActive = 0 WHERE userId = :userId AND id != :exceptSessionId")
    suspend fun revokeOtherSessions(userId: Long, exceptSessionId: Long)

    @Query("UPDATE sessions SET lastActiveTimestamp = :time WHERE id = :sessionId")
    suspend fun updateLastActive(sessionId: Long, time: Long)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM anonymous_messages WHERE recipientUserId = :userId ORDER BY receivedAt DESC")
    fun getMessagesForUser(userId: Long): Flow<List<AnonymousMessageEntity>>

    @Query("SELECT * FROM anonymous_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: Long): AnonymousMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AnonymousMessageEntity): Long

    @Query("UPDATE anonymous_messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: Long)

    @Query("UPDATE anonymous_messages SET isFavorite = NOT isFavorite WHERE id = :messageId")
    suspend fun toggleFavorite(messageId: Long)

    @Query("UPDATE anonymous_messages SET isRevealedManually = 1 WHERE id = :messageId")
    suspend fun unlockCoordinatesImmediately(messageId: Long)

    @Query("DELETE FROM anonymous_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("SELECT COUNT(*) FROM anonymous_messages WHERE recipientUserId = :userId")
    fun getMessageCount(userId: Long): Flow<Int>
}
