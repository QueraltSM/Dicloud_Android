package es.disoft.dicloud.user;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import es.disoft.dicloud.ConnectionAvailable;
import es.disoft.dicloud.HttpConnections;
import es.disoft.dicloud.R;
import es.disoft.dicloud.Toast;
import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.model.UserDao;

import static es.disoft.dicloud.ConnectionAvailable.isNetworkAvailable;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        myDb = new DbHelper(this);
        setContentView(R.layout.activity_login);
        setUpLoginForm();
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

    private void enableSuggestions() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<User.DbAlias> aliases = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getAllDbAlias();
                ArrayList<String> suggestionsAL = new ArrayList<>();
                for (User.DbAlias alias: aliases) suggestionsAL.add(alias.dbAlias.toLowerCase());
                String[] suggestions = suggestionsAL.toArray(new String[0]);
                enableSuggestions(mAliasView,  new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, suggestions));


                List<User.UserAlias> users = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getAllUserAlias();
                suggestionsAL = new ArrayList<>();
                for (User.UserAlias user: users) suggestionsAL.add(user.userAlias.toLowerCase());
                suggestions = suggestionsAL.toArray(new String[0]);
                enableSuggestions(mUserView,  new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, suggestions));
            }
        }).start();
    }

    private void enableSuggestions(final AutoCompleteTextView textView, final ArrayAdapter<String> adapter) {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setThreshold(1);
                        textView.setAdapter(adapter);
                    }
                });
            }
        }.start();
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) return;

        hideKeyBoard();

        // Check Internet connection

        if (isNetworkAvailable(getApplicationContext())) {
            try {
                if (!(new ConnectionAvailable(getString(R.string.URL_LOGIN)).execute().get())) {
                    somethingWrong(4);
                    return;
                }
            } catch (ExecutionException | InterruptedException e) {
                somethingWrong(4);
                return;
            }
        } else {
                somethingWrong(5);
                return;
        }

        // Reset errors.
        ((TextInputLayout) mAliasView.getParent().getParent()).setError(null);
        ((TextInputLayout) mUserView.getParent().getParent()).setError(null);
        ((TextInputLayout) mPasswordView.getParent().getParent()).setError(null);

        // Store values at the time of the login attempt.
        String alias    = mAliasView.getText().toString().trim().toUpperCase();
        String user     = mUserView.getText().toString().trim().toUpperCase();
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

    private void hideKeyBoard() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //noinspection ConstantConditions

        View focusedView = getCurrentFocus();
        if (focusedView != null) imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    private boolean isAliasValid(String alias) {
        return isValidText(alias);
    }

    private boolean isUserValid(String user) {
        if (user.equalsIgnoreCase("sqlinjection") || user.equalsIgnoreCase("inyeccionsql")) {
            Toast.setText(getApplicationContext(), R.string.hackerino).show();
            return false;
        }
        return isValidText(user);
    }

    private boolean isPasswordValid(String password) {
        if (password.length() >= 3)
            return isValidText(password);
        return false;
    }

    private boolean isValidText(String text) {
        String regex    = "^[a-zA-Z0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(text).matches();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
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
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mAlias;
        private final String mUser;
        private final String mPassword;

        /**
         * 0: success
         * 1: company error
         * 2: user or password error
         * 3: inactive user error
         * 4: json error
         * 5: internet error
         * 9: unknown error
         */
        int exitCode;
        private boolean error = true;
        private JSONObject loginResponse = null;

        UserLoginTask(String alias, String user, String password) {
            mAlias = alias;
            mUser = user;
            mPassword = password;
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

            Log.i("login_", "doInBackground: " + exitCode);
            return exitCode;
        }

        @Override
        protected void onPostExecute(final Integer exitCode) {
            mAuthTask = null;

            if (somethingWrong(exitCode)) {
                showProgress(false);
            } else {
                Intent mainActivity = new Intent(LoginActivity.this, WebViewActivity.class);
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
            userData.put("appSource", getString(R.string.app_name));

            Log.wtf("login_: ", "ENTRA");

            JSONObject userToAuthenticateJson = new JSONObject(userData);

            Log.wtf("login_request: ", userToAuthenticateJson.toString());

            loginResponse = HttpConnections.execute(
                    getString(R.string.URL_LOGIN),
                    userToAuthenticateJson.toString());

            Log.wtf("login_response", loginResponse.toString());

            Log.i("token_", "login: " + loginResponse);

            if (loginResponse != null) error = loginResponse.getBoolean("error");
        }

        private void updateDDBB() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String mfullName = loginResponse.getString("fullName");
                        String mToken    = loginResponse.getString("token");
                        int mId          = loginResponse.getInt("id");
                        User.currentUser = new User(mId, mUser, mfullName, mAlias, mToken);
                        UserDao userDao  = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao();
                        userDao.insert(User.currentUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
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
                Toast.setText(getApplicationContext(), R.string.error_user_or_password).show();
                break;
            case 3: // inactive user error
                ((TextInputLayout) mUserView.getParent().getParent()).setError(getString(R.string.error_inactive_user));
                break;
            case 4: // json error
                Toast.setText(getApplicationContext(), R.string.error_json_response).show();
                break;
            case 5: // internet error
                Toast.setText(getApplicationContext(), R.string.error_internet_connection).show();
                break;
            default: // unknown error
                Toast.setText(getApplicationContext(), R.string.error_unknown).show();
        }

        Log.i("LOGIN", "somethingWrong: " + exitCode);
        return exitCode != 0;
    }
}
