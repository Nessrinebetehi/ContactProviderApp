package com.example.contactproviderapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts.db";// Nom du fichier de la base de données
    private static final int DATABASE_VERSION = 1; // Version de la base de données
    private static final String TABLE_CREATE =     // Requête SQL pour créer la table "contacts"
            "CREATE TABLE contacts (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, phone TEXT);";

   //* Constructeur
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //création de la base de donnée
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }
   //Nouvelle version de la base de données
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Supprime l'ancienne table
        db.execSQL("DROP TABLE IF EXISTS contacts");
        // Recrée la table avec la nouvelle structure
        onCreate(db);
    }
}
