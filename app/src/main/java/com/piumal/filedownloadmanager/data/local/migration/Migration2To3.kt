package com.piumal.filedownloadmanager.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add the 'uri' column to the 'downloads' table
        database.execSQL("ALTER TABLE downloads ADD COLUMN uri TEXT")
        
        // Populate 'uri' column with existing 'filePath' values
        database.execSQL("UPDATE downloads SET uri = filePath")
    }
}
