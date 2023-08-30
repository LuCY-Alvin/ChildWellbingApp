package com.mildp.jetpackcompose.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Converters
import java.io.File

@Database(entities = [
    RSSIData::class,         AccessibilityData::class, UsageData::class,  MoodData::class,
    NotificationData::class, AcceleratorData::class,   GyroData::class,   TMTData::class, Debug::class,
    PointData::class,        TMTStopData::class,       ActivityRecognitionData::class], version =1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DataBase : RoomDatabase() {
    abstract fun getDataDao(): Dao

    companion object {
        private var INSTANCE: DataBase? = null

        fun getDatabase(context: Context): DataBase? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, DataBase::class.java, "database")
                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE
        }
    }

    fun backupDatabase(context: Context, day: String): Int {
        var result = -99
        if (INSTANCE == null) return result
        val id = kv.decodeString("subID", "")

        val dbFile = context.getDatabasePath("database")
        val dbWalFile = File(dbFile.path + "-wal")
        val dbShmFile = File(dbFile.path + "-shm")
        val bkpFile = File(dbFile.path + "-$id-backup-$day")
        val bkpWalFile = File(bkpFile.path + "-wal")
        val bkpShmFile = File(bkpFile.path + "-shm")
        if (bkpFile.exists()) bkpFile.delete()
        if (bkpWalFile.exists()) bkpWalFile.delete()
        if (bkpShmFile.exists()) bkpShmFile.delete()
        checkpoint()
        try {
            dbFile.copyTo(bkpFile, true)
            if (dbWalFile.exists()) dbWalFile.copyTo(bkpWalFile, true)
            if (dbShmFile.exists()) dbShmFile.copyTo(bkpShmFile, true)
            result = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun checkpoint() {
        val db = this.openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL);", emptyArray())
        db.query("PRAGMA wal_checkpoint(TRUNCATE);", emptyArray())
    }

}