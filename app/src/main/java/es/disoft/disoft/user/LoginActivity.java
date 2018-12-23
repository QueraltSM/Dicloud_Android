package es.disoft.disoft.user;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import es.disoft.disoft.ConnectionAvailable;
import es.disoft.disoft.db.DbHelper;
import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.MainActivity;
import es.disoft.disoft.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mAliasView;
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;


    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpLoginForm();
        preloadData();
        enableSuggestions();
    }


    private void setUpLoginForm() {
        mAliasView = findViewById(R.id.alias);
        mAliasView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mUserView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mUserView = findViewById(R.id.user);
        mUserView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    // If a user tries to log in with a token and fails, their data is preloaded
    private void preloadData() {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT user,dbAlias FROM users WHERE loggedIn=?", new String[]{"1"});

        if(c.moveToFirst()) {
            mAliasView.setText(c.getString(c.getColumnIndex("dbAlias")).toLowerCase());
            mUserView .setText(c.getString(c.getColumnIndex("user"))   .toLowerCase());
        }
        c.close();
    }


    private void enableSuggestions() {
        enableSuggestions(mAliasView, getSuggestions("dbAlias"));
        enableSuggestions(mUserView,  getSuggestions("user"));
    }

    private void enableSuggestions(AutoCompleteTextView textView, ArrayAdapter<String> adapter) {
        textView.setThreshold(1);
        textView.setAdapter(adapter);
    }

    private ArrayAdapter<String> getSuggestions(String suggestionsType) {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT " + suggestionsType + " FROM users", null);

        ArrayList<String> suggestionsAL = new ArrayList<>();
        while(c.moveToNext()) suggestionsAL.add(c.getString(c.getColumnIndex(suggestionsType)).toLowerCase());
        c.close();

        String[] suggestions = suggestionsAL.toArray(new String[0]);
        return new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestions);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        // Check Internet connection
        try {
            if (!(new ConnectionAvailable(getString(R.string.URL_LOGIN)).execute().get())) {
                somethingWrong(5);
                return;
            }
        } catch (ExecutionException | InterruptedException e) {
            somethingWrong(5);
            return;
        }

        // Reset errors.
        ((TextInputLayout) mAliasView.getParent().getParent()).setError(null);
        ((TextInputLayout) mUserView.getParent().getParent()).setError(null);
        ((TextInputLayout) mPasswordView.getParent().getParent()).setError(null);

        // Store values at the time of the login attempt.
        String alias =    mAliasView.getText().toString().trim().toUpperCase();
        String user =     mUserView.getText().toString().trim().toUpperCase();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            ((TextInputLayout) mPasswordView.getParent().getParent()).setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            ((TextInputLayout) mPasswordView.getParent().getParent()).setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid user
        if (TextUtils.isEmpty(user)) {
            ((TextInputLayout) mUserView.getParent().getParent()).setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isUserValid(user)) {
            ((TextInputLayout) mUserView.getParent().getParent()).setError(getString(R.string.error_invalid_user));
            focusView = mUserView;
            cancel = true;
        }

        // Check for a valid alias.
        if (TextUtils.isEmpty(alias)) {
            ((TextInputLayout) mAliasView.getParent().getParent()).setError(getString(R.string.error_field_required));
            focusView = mAliasView;
            cancel = true;
        } else if (!isAliasValid(alias)) {
            ((TextInputLayout) mAliasView.getParent().getParent()).setError(getString(R.string.error_invalid_alias));
            focusView = mAliasView;
            cancel = true;
        }

        // TODO quitar esta linea
        cancel = false;

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            showProgress(true);
            mAuthTask = new UserLoginTask(alias, user, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isAliasValid(String alias) {
        //TODO: Replace this with your own logic
        return !alias.contains(" ");
    }

    private boolean isUserValid(String user) {
        //TODO: Replace this with your own logic
        return !user.contains(" ");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mAlias;
        private final String mUser;
        private final String mPassword;
        private final String mPkcode;

        /**
         * 0: success
         * 1: company error
         * 2: user or password error
         * 3: inactive user error
         * 4: json error
         * 5: internet error
         * 9: unknown error
         */
        protected int exitCode;
        private boolean error = true;
        private JSONObject loginResponse = null;


        UserLoginTask(String alias, String user, String password) {
            mAlias = alias;
            mUser = user;
            mPassword = password;
            mPkcode = mUser + mAlias;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                login();

                if (error) {
                    exitCode = loginResponse == null ? 5 : Integer.parseInt(loginResponse.getString("error_code"));
                } else {
                    updateDDBB();
                    exitCode = 0;
                }
                } catch (JSONException | RuntimeException e) { exitCode = 4; }


//            try {
//                // Simulate network access.
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                return 9;
//            }

            Log.i("login_", "doInBackground: " + exitCode);
            return exitCode;
        }

        @Override
        protected void onPostExecute(final Integer exitCode) {
            mAuthTask = null;

            if (somethingWrong(exitCode)) {
                showProgress(false);
            } else {
                Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(mainActivity);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private void login() throws JSONException {

            Map<String, String> userData = new HashMap<>();
            userData.put("aliasDb",  mAlias);
            userData.put("user",     mUser);
            userData.put("password", mPassword);

            Log.wtf("login_: ", "ENTRA");

            JSONObject userToAuthenticateJson = new JSONObject(userData);

            Log.wtf("login_request: ", userToAuthenticateJson.toString());

            loginResponse = HttpConnections.execute(
                    getString(R.string.URL_LOGIN),
                    userToAuthenticateJson.toString());


            Log.wtf("login_response", loginResponse.toString());

            Log.i("token_", "login: " + loginResponse);

            if (loginResponse != null)
                error = loginResponse.getBoolean("error");
        }


        private void updateDDBB() throws JSONException {
            DbHelper dbHelper = new DbHelper(LoginActivity.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String mName      = loginResponse.getString("name");
            String mLastName  = loginResponse.getString("lastName");
            String mToken     = loginResponse.getString("token");
            int mId           = loginResponse.getInt("id");

            if (db != null) {
                ContentValues values = new ContentValues();
                values.put("pkCode",   mPkcode);
                values.put("user_id",  mId);
                values.put("user",     mUser);
                values.put("name",     mName);
                values.put("lastName", mLastName);
                values.put("dbAlias",  mAlias);
                values.put("token",    mToken);

                try {
                    db.insertOrThrow("users", null, values);
                } catch (SQLiteConstraintException e) {
                    db.execSQL("UPDATE users SET token='" + mToken + "' WHERE Pkcode='" + mPkcode + "'");
                    db.execSQL("UPDATE users SET name='" + mName + "' WHERE Pkcode='" + mPkcode + "'");
                    db.execSQL("UPDATE users SET lastName='" + mLastName + "' WHERE Pkcode='" + mPkcode + "'");
                }


                db.execSQL("UPDATE users SET loggedIn=1 WHERE Pkcode='"  + mPkcode + "'");
                db.execSQL("UPDATE users SET loggedIn=0 WHERE Pkcode!='" + mPkcode + "'");
                db.close();
            }
        }
    }

    private boolean somethingWrong(int exitCode) {
        switch (exitCode) {
            case 0: //success
                break;
            case 1: // company error
                ((TextInputLayout) mAliasView.getParent().getParent()).setError(getString(R.string.error_invalid_alias));
                mAliasView.requestFocus();
                break;
            case 2: // user or password error
                Toast.makeText(context,"Fallo usuario o contraseña MIRAR COMO", Toast.LENGTH_SHORT).show();
                break;
            case 3: // inactive user error
                ((TextInputLayout) mUserView.getParent().getParent()).setError(getString(R.string.error_inactive_user));
                break;
            case 4: // json error
                Toast.makeText(context, getString(R.string.error_json_response), Toast.LENGTH_SHORT).show();
                break;
            case 5: // internet error
                Toast.makeText(context, getString(R.string.error_internet_connection), Toast.LENGTH_SHORT).show();
                break;
            default: // unknown error
                Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_SHORT).show();
        }
        Log.i("LOGIN", "somethingWrong: " +exitCode);
        return exitCode != 0;
    }
}
