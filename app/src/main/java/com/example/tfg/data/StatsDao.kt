package com.example.tfg.data

import androidx.room.Dao
import androidx.room.Query
import com.example.tfg.common.Difficulty
import com.example.tfg.games.Games
import java.time.LocalDateTime

@Dao
interface StatsDao {
    @Query("""
        SELECT COUNT(*) FROM Game
        WHERE (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
        AND (:endDate IS NULL OR endDate <= :endDate)
    """)
    suspend fun getNumberOfGamesStarted(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime?): Int

    @Query("""
        SELECT COUNT(*) FROM Game
        WHERE endDate <= :endDate
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
    """)
    suspend fun getNumberOfGamesEnded(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime): Int

    /*
    If the endDate of the entity is null, that means that game was not finished
    The functions are made so that if you provide endDate = null you get all games
     */

    @Query("""
        SELECT COUNT(*) FROM Game
        WHERE playerWon = :playerWon
        AND (:endDate IS NULL OR endDate <= :endDate)
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
    """)
    suspend fun getNumberOfGames(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime, playerWon: Boolean): Int

    @Query("""
        SELECT SUM(timer) FROM Game
        WHERE playerWon = :playerWon 
        AND (:endDate IS NULL OR endDate <= :endDate)
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
    """)
    suspend fun getTotalTime(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime?, playerWon: Boolean): Int?

    @Query("""
        SELECT SUM(timer) FROM Game
        WHERE (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
    """)
    suspend fun getAbsoluteTotalTime(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?): Int?

    @Query("""
        SELECT MIN(timer) FROM Game
        WHERE playerWon = :playerWon
        AND (:endDate IS NULL OR endDate <= :endDate)
        AND timer > 0
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
    """)
    suspend fun getBestTime(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime?, playerWon: Boolean = true): Int?

    @Query("""
        SELECT AVG(timer) FROM Game
        WHERE playerWon = :playerWon
        AND (:endDate IS NULL OR endDate <= :endDate)
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
    """)
    suspend fun getMeanTime(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime?, playerWon: Boolean = true): Double?

    @Query("""
        SELECT timer FROM Game
        WHERE playerWon = :playerWon
        AND (:endDate IS NULL OR endDate <= :endDate)
        AND timer > 0
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
        ORDER BY timer
    """)
    suspend fun getAllTimes(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime?, playerWon: Boolean = true): List<Int>

    @Query("""
        SELECT SUM(numErrors) FROM Game
        WHERE endDate IS NOT NULL
        AND (:endDate IS NULL OR endDate <= :endDate)
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
""")
    suspend fun getTotalErrorCount(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime?): Int?

    @Query("""
        SELECT AVG(numErrors) FROM Game
        WHERE endDate IS NOT NULL
        AND (:endDate IS NULL OR endDate <= :endDate)
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
""")
    suspend fun getMeanErrorCount(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime?): Double?

    @Query("""
        SELECT numErrors FROM Game
        WHERE endDate IS NOT NULL
        AND (:endDate IS NULL OR endDate <= :endDate)
        AND (:type IS NULL OR type = :type)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:startDate IS NULL OR startDate >= :startDate)
        ORDER BY numErrors
""")
    suspend fun getAllErrorCounts(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime?): List<Int>

    @Query("""
        SELECT wins from WinningStreak
        WHERE endDate IS NULL 
        AND gameEnum = :gameEnum
""")
    suspend fun getActualWinningStreak(gameEnum: Games) : Int?

    @Query("""
        SELECT wins from WinningStreak
        WHERE endDate IS NULL 
        AND gameEnum IS NULL
""")
    suspend fun getActualGeneralWinningStreak() : Int?

    @Query("""
        SELECT wins from WinningStreak
        WHERE endDate IS NULL 
        AND gameEnum = :gameEnum
        AND (:startDate IS NULL OR startDate >= :startDate)
        ORDER BY wins DESC LIMIT 1
""")
    suspend fun getHighestWinningStreakValue(gameEnum: Games, startDate: LocalDateTime?) : Int?

    @Query("""
        SELECT wins from WinningStreak
        WHERE endDate IS NULL 
        AND gameEnum IS NULL
        AND (:startDate IS NULL OR startDate >= :startDate)
        ORDER BY wins DESC LIMIT 1
""")
    suspend fun getHighestGeneralWinningStreakValue(startDate: LocalDateTime?) : Int?

}