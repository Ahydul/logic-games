package com.example.tfg.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tfg.common.entities.Action
import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell
import com.example.tfg.common.entities.Game
import com.example.tfg.common.entities.GameState
import com.example.tfg.common.entities.Move
import com.example.tfg.common.entities.relations.BoardCellCrossRef

@Database(entities = [Game::class, GameState::class, Move::class, Action::class, Board::class, Cell::class, BoardCellCrossRef::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object { //Singleton
        @Volatile
        private var Instance: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            // synchronized to avoid possible race conditions
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, GameDatabase::class.java, "game_database")
                    // Destroys the database if Game Entity is changed (schema changes)
                    // To change this behaviour investigate Migration
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                    .also { Instance = it }
            }
        }

        // For preview purposes
        fun getInMemoryDatabase(context: Context): GameDatabase {
            return Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        }

    }

}