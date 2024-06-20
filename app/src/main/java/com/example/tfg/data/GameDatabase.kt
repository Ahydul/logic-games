package com.example.tfg.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tfg.common.entities.Action
import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell
import com.example.tfg.common.entities.Game
import com.example.tfg.common.entities.GameState
import com.example.tfg.common.entities.Move
import com.example.tfg.common.entities.WinningStreak
import com.example.tfg.common.entities.relations.BoardCellCrossRef
import com.example.tfg.common.entities.relations.GameStateSnapshot

@Database(
    entities = [Game::class, GameState::class, Move::class, Action::class, Board::class, Cell::class, BoardCellCrossRef::class, GameStateSnapshot::class, WinningStreak::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun limitedGameDao(): LimitedGameDao
    abstract fun statsDao(): StatsDao

    companion object { //Singleton
        @Volatile
        private var Instance: GameDatabase? = null

        // Add difficulty to WinningStreak. Previous WinningStreaks are deleted
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                //Delete all records
                db.execSQL("DELETE FROM WinningStreak")
                //Change table
                db.execSQL("ALTER TABLE WinningStreak ADD COLUMN difficulty TEXT DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): GameDatabase {
            // synchronized to avoid possible race conditions
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, GameDatabase::class.java, "game_database")
                    // Destroys the database if Game Entity is changed (schema changes)
                    // To change this behaviour investigate Migration
                    .allowMainThreadQueries()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL(
                                "CREATE TRIGGER IF NOT EXISTS delete_cells_after_board_delete " +
                                        "AFTER DELETE ON Board " +
                                        "BEGIN " +
                                        "DELETE FROM Cell " +
                                        "WHERE cellId IN (SELECT cellId FROM BoardCellCrossRef WHERE boardId = old.boardId);" +
                                        "END;"
                            )
                        }
                    })
                    .addMigrations(MIGRATION_1_2)
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