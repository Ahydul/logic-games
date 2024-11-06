package com.example.tfg.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.example.tfg.games.Factors
import com.example.tfg.games.hakyuu.Hakyuu
import com.example.tfg.games.kendoku.Kendoku

@Database(
    entities = [
        Game::class,
        Hakyuu::class,
        Kendoku::class,
        Factors::class,
        GameState::class,
        Move::class,
        Action::class,
        Board::class,
        Cell::class,
        BoardCellCrossRef::class,
        GameStateSnapshot::class,
        WinningStreak::class],
    version = 1,
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