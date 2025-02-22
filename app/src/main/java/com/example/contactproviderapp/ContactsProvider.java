package com.example.contactproviderapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ContactsProvider extends ContentProvider {
    // Autorité du ContentProvide
    private static final String AUTHORITY = "com.example.contactproviderapp.provider";
    // Nom de la table utilisée dans la base de donnée
    private static final String TABLE_NAME = "contacts";
    // URI pour accéder aux contacts via ce ContentProvi
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    // Codes pour identifier les types de requête
    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 2;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // Associe l'URI "content:name et id
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, CONTACTS);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", CONTACT_ID);
    }

    // Base de données SQLite

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();
        return database != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CONTACT_ID:
            // Récupère un contact spécifique en filtrant par ID
                selection = "_id=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("URI inconnue : " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    //Insère un nouveau contact dans la base de données
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long id = database.insert(TABLE_NAME, null, values);
        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Échec d'insertion dans " + uri);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                // Mise à jour de plusieurs contacts
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case CONTACT_ID:
                // Mise à jour d'un seul contact basé sur l'ID
                selection = "_id=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI inconnue : " + uri);
        }

        // Notifie les observateurs des changements
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    //* Supprime un ou plusieurs contacts
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                // Supprime plusieurs contacts
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case CONTACT_ID:
                selection = "_id=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI inconnue : " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
// Cela permet aux autres applications ou composants Android de savoir quel type de données est exposé par le ContentProvider.
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
            case CONTACT_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;
            default:
                throw new IllegalArgumentException("URI inconnue : " + uri);
        }
    }
}
