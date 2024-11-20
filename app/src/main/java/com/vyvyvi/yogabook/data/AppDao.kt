package com.vyvyvi.yogabook.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppDao {
    @Insert
    suspend fun insertUser(user: User)

    @Insert
    suspend fun insertUserStreak(streak: Streak)

    @Insert
    suspend fun insertPose(pose: Pose): Long

    @Insert
    suspend fun insertRoutine(routine: Routine): Long

    @Insert
    suspend fun insertSession(session: Session): Long

    @Insert
    suspend fun insertSessionItem(session: SessionItem): Long

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM user_table WHERE username = :username")
    suspend fun getUser(username: String): User

    @Update
    suspend fun updateUser(user: User)

    @Update
    suspend fun updatePose(pose: Pose)

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Update
    suspend fun updateSessionItem(sessionItem: SessionItem)

    @Update
    suspend fun updateUserStreak(streak: Streak)

    @Query("UPDATE session_table SET latitude=:latitude, longitude=:longitude, location=:address WHERE session_id=:sessionId")
    suspend fun updateSessionLocation(sessionId: Long, latitude: Double, longitude: Double, address: String)

    @Query("UPDATE session_table SET completed = 1 WHERE session_id = :session_id")
    suspend fun completeSession(session_id: Long)

    @Query("SELECT 1 FROM user_table WHERE username = :username")
    suspend fun userExists(username: String): Boolean?

    @Query("SELECT 1 FROM user_table WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): Boolean?

    @Query("SELECT current_streak FROM streak_table WHERE username = :username")
    suspend fun getStreak(username: String): Int

    @Query("SELECT avatar FROM user_table WHERE username = :username")
    suspend fun getAvatar(username: String): ByteArray

    @Query("DELETE FROM user_table WHERE username = :username")
    suspend fun deleteUserByUsername(username: String)

    @Query("DELETE FROM streak_table WHERE username = :username")
    suspend fun deleteUserStreak(username: String)

    @Query("SELECT * FROM pose_table ORDER BY id")
    suspend fun getAllPoses(): List<Pose>

    @Query("""
       SELECT r.id AS rid, r.username, r.duration, p.*
        FROM routine_table AS r INNER JOIN pose_table AS p
        ON r.pose_id = p.id
        WHERE username = :username ORDER BY r.id
    """)
    suspend fun getRoutineWithPose(username: String): List<RoutineWithPose>

    @Query("""
       SELECT r.id AS rid, r.username, r.duration, p.*
        FROM routine_table AS r INNER JOIN pose_table AS p
        ON r.pose_id = p.id
        WHERE r.id = :id ORDER BY r.id
    """)
    suspend fun getRoutineWithPoseById(id: Long): RoutineWithPose

    @Query("SELECT * FROM session_table WHERE username=:username")
    suspend fun getAllSessions(username: String): List<Session>

    @Query("SELECT * FROM session_item_table WHERE sessionId = :sessionId")
    suspend fun getSessionItems(sessionId: Long): List<SessionItem>

    @Query("SELECT * FROM streak_table WHERE username = :username")
    suspend fun getUserStreak(username: String): Streak

    @Query("DELETE FROM pose_table WHERE id = :pose_id")
    suspend fun deletePose(pose_id:Long)

    @Query("DELETE FROM routine_table WHERE id = :rid")
    suspend fun deleteRoutine(rid: Long)

    suspend fun deleteUser(username: String) {
        deleteUserByUsername(username)
        deleteUserStreak(username)
    }
}