package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.Chapter
import com.example.myapplication.data.Manga
import com.example.myapplication.data.Page
import com.example.myapplication.data.ReadingProgress

@Database(
    // Every entity class you created must be listed here.
    // If you add a new @Entity and forget to list it here,
    // Room won't create the table — silent failure.
    entities = [
        Manga::class,
        Chapter::class,
        Page::class,
        ReadingProgress::class
    ],

    // Increment this every time you change your schema
    // (add/remove a table, add/remove/rename a column).
    // If you change the schema without bumping version,
    // Room throws IllegalStateException at runtime on existing installs.
    version = 4,

    // Exports schema as a JSON file to app/schemas/1.json.
    // This JSON describes every table, column, type, and index.
    // REQUIRED for writing migrations later — Room diffs the JSON
    // files to verify your migration SQL is correct.
    // Commit these JSON files to git — they're your schema history.
    exportSchema = true
)
// Must be abstract — Room generates the concrete implementation via KSP.
// Must extend RoomDatabase.
abstract class MangaDatabase : RoomDatabase() {

    // One abstract function per DAO.
    // Room generates the implementation that wires each function
    // to the actual SQLite calls.
    // These are accessed via Koin: get<MangaDatabase>().mangaDao()
    abstract fun mangaDao() : MangaDao
    abstract fun chapterDao() : ChapterDao
    abstract fun pageDao() : PageDao
    abstract fun readingProgressDao() : ReadingProgressDao

    // Optional: companion object for in-memory test database
    companion object {
        // In-memory DB for unit tests — data is lost when process ends.
        // Use this in your test classes instead of the real DB.
        fun buildTestDb(context : Context) =
            Room.inMemoryDatabaseBuilder(context, MangaDatabase::class.java)
                .allowMainThreadQueries() // only in tests
                .build()
    }
}
    // ── Version 1 → 2: add language column to manga ──────────────
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // SQLite supports ADD COLUMN but NOT:
            //   - DROP COLUMN (before SQLite 3.35)
            //   - RENAME COLUMN (before SQLite 3.25)
            //   - change column type
            // For those, you need to recreate the table (see below).
            db.execSQL(
                "ALTER TABLE manga ADD COLUMN language TEXT NOT NULL DEFAULT 'en'"
            )
        }
    }

    // ── Version 2 → 3: add user_notes column to reading_progress ──
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE reading_progress ADD COLUMN user_notes TEXT"
                // nullable column — no DEFAULT needed
            )
        }
    }

    // ── Version 3 → 4: rename a column (requires table recreation) ─
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Step 1: create new table with correct schema
            db.execSQL("""
            CREATE TABLE manga_new (
                id TEXT PRIMARY KEY NOT NULL,
                cover_image_url TEXT NOT NULL,  -- renamed from cover_url
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                status TEXT NOT NULL,
                content_rating TEXT NOT NULL,
                author TEXT NOT NULL,
                total_chapters INTEGER NOT NULL DEFAULT 0,
                last_updated INTEGER NOT NULL,
                language TEXT NOT NULL DEFAULT 'en',
                user_notes TEXT
            )
        """)
            // Step 2: copy data from old table
            db.execSQL("""
            INSERT INTO manga_new
            SELECT id, cover_url, title, description, status,
                   content_rating, author, total_chapters,
                   last_updated, language, user_notes
            FROM manga
        """)
            // Step 3: drop old table
            db.execSQL("DROP TABLE manga")
            // Step 4: rename new table
            db.execSQL("ALTER TABLE manga_new RENAME TO manga")
        }
    }

