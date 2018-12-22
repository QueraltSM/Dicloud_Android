package es.disoft.disoft;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import es.disoft.disoft.security.Decryptor;
import es.disoft.disoft.security.Encryptor;

public class PruebaCifrado extends AppCompatActivity {

    private final String TABLE_USERS = "USERS";
    private final String DB_NAME = "crypto_test.db";

    private EditText edTextToEncrypt;
    private EditText edAlias;
    private TextView tvEncryptedText;
    private TextView tvDecryptedText;
    private TextView tvKeys;

    private Encryptor encryptor;
    private Decryptor decryptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba_cifrado);

        edTextToEncrypt = findViewById(R.id.ed_text_to_encrypt);
        edAlias = findViewById(R.id.ed_alias);
        tvEncryptedText = findViewById(R.id.tv_encrypted_text);
        tvDecryptedText = findViewById(R.id.tv_decrypted_text);
        tvKeys = findViewById(R.id.tv_keys);

        encryptor = new Encryptor();

        try {
            decryptor = new Decryptor();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException |
                IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Descifra el texto
     *
     * @param v
     */
    public void decryptText(View v) {
        String mAlias = edAlias.getText().toString();

        if (mAlias.isEmpty()) {
            Toast.makeText(this, "Alias...", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, String> data = getData(mAlias);
            if (data.isEmpty()) {
                Toast.makeText(this, "Alias not found", Toast.LENGTH_SHORT).show();
            } else {
                try {

                    String[] decrypt_data = data.get("password").split(Pattern.quote("."));
                    byte[] IV = Base64.decode(decrypt_data[0], Base64.DEFAULT);
                    byte[] encryptedText = Base64.decode(decrypt_data[1], Base64.DEFAULT);

                    tvDecryptedText.setText(decryptor.decryptData(mAlias, encryptedText, IV));
                } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException |
                        NoSuchPaddingException | IOException | InvalidKeyException |
                        IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Toast.makeText(this, "Something went wrong... \n(restart session)", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Cifra el texto y almacena la info en la BBDD
     *
     * @param v
     */
    public void encryptText(View v) {
        String mAlias = edAlias.getText().toString();
        String mText = edTextToEncrypt.getText().toString();

        if (mAlias.isEmpty() || mText.isEmpty()) {
            Toast.makeText(this, "Alias & Text...", Toast.LENGTH_SHORT).show();
        } else {
            try {
                String encryptedText = Base64.encodeToString(
                        encryptor.encryptText(mAlias, mText),
                        Base64.DEFAULT);
                String IV = Base64.encodeToString(
                        encryptor.getIv(),
                        Base64.DEFAULT);

                tvEncryptedText.setText(encryptedText);
                StorageData(mAlias, encryptedText, IV);
            } catch (NoSuchAlgorithmException | NoSuchProviderException |
                    IOException | NoSuchPaddingException | InvalidKeyException |
                    InvalidAlgorithmParameterException |
                    IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Almacena en la BBDD la info del usuario
     *
     * @param mAlias    alias del usuario (coincide con el alias de la KeyStore)
     * @param mPassword password cifrada mediante clave_pub
     * @param mIv       verctor de inicializacion para poder descifrar la password
     */
    private void StorageData(String mAlias, String mPassword, String mIv) {
        DbHelper newUser = new DbHelper(this, DB_NAME, null, 1);
        SQLiteDatabase db = newUser.getWritableDatabase();

        if (db != null) {
            ContentValues values = new ContentValues();
            values.put("alias", mAlias);
            values.put("password", mIv + "." + mPassword);

            db.insert("USERS", null, values);
            db.close();
            newUser.close();
        }
    }

    /**
     * @param mAlias alias que se buscara en la BBDD
     * @return hashmap con los datos del usuario si existe el alias
     */
    private HashMap<String, String> getData(String mAlias) {
        SQLiteDatabase db;
        DbHelper user;

        user = new DbHelper(this, DB_NAME, null, 1);
        db = user.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM USERS WHERE alias = ?", new String[]{mAlias});

        HashMap<String, String> data = new HashMap<>();
        if (db != null) {
            if (c.moveToFirst()) {
                data.put("alias", c.getString(c.getColumnIndex("alias")));
                data.put("password", c.getString(c.getColumnIndex("password")));
                db.close();
            }
        }

        return data;
    }

    /**
     * Busca todos los alias de esta app guardados dentro de KeyStore
     * y los muestra en el textView
     */
    public void showAlias(View v) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            ArrayList<String> aliases = Collections.list(ks.aliases());
            tvKeys.setText("");
            for (String alias : aliases)
                tvKeys.setText(tvKeys.getText() + alias + "\n");
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    class DbHelper extends SQLiteOpenHelper {

        String sqlCreateUsersTable = "CREATE TABLE " + TABLE_USERS +
                "(" +
                "alias TEXT PRIMARY KEY," +
                "password TEXT" +
                ")";

        public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL(sqlCreateUsersTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
