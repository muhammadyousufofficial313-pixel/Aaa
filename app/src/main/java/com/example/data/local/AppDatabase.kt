package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.WhatsAppDao
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.BusinessEntity
import com.example.data.local.entities.CallLogEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StaffEntity
import com.example.data.local.entities.TemplateEntity

@Database(
    entities = [
        BusinessEntity::class,
        CustomerEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        TemplateEntity::class,
        StaffEntity::class,
        AuditLogEntity::class,
        CallLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun whatsAppDao(): WhatsAppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `call_logs` (" +
                        "`id` TEXT NOT NULL, " +
                        "`conversationId` TEXT NOT NULL, " +
                        "`customerId` TEXT NOT NULL, " +
                        "`customerName` TEXT NOT NULL, " +
                        "`customerPhone` TEXT NOT NULL, " +
                        "`callType` TEXT NOT NULL, " +
                        "`direction` TEXT NOT NULL, " +
                        "`status` TEXT NOT NULL, " +
                        "`durationSeconds` INTEGER NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_call_logs_conversationId` ON `call_logs` (`conversationId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_call_logs_customerId` ON `call_logs` (`customerId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_call_logs_timestamp` ON `call_logs` (`timestamp`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whatsapp_business_inbox.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
