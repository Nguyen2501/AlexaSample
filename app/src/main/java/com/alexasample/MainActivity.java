package com.alexasample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

public class MainActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_EXCEPTION = "exception";
    public static final String TAG = MainActivity.class.getName();
    private RequestContext mRequestContext;
    private ImageButton mLoginButton;
    private Button mLogoutButton;
    private Button mConnectButton;
    private TextView mAddrTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestContext = RequestContext.create(this);
        mLoginButton = (ImageButton) findViewById(R.id.loginButton);
        mLogoutButton = (Button) findViewById(R.id.logoutbutton);
        mConnectButton = (Button) findViewById(R.id.connectButton);
        mAddrTextView = (TextView) findViewById(R.id.addressTextView);

        mRequestContext.registerListener(new AuthorizeListener() {
            /*Authorization was completed successfully*/
            @Override
            public void onSuccess(AuthorizeResult authorizeResult) {
                /*Your app is now authorized for the requested scopes*/
                final String authorizationCode = authorizeResult.getAuthorizationCode();
                final String redirectUri = authorizeResult.getRedirectURI();
                final String clientId = authorizeResult.getClientId();

            }

            /*There was an error during the attempt to authorize the application. */
            @Override
            public void onError(AuthError authError) {
                /* Inform the user of the error. */
            }

            /* Authorization was cancelled before it could be completed */
            @Override
            public void onCancel(AuthCancellation authCancellation) {
                /* Reset the UI to a ready-to-login state*/
            }
        });

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String address = mAddrTextView.getText().toString();
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizationManager.authorize(new AuthorizeRequest
                        .Builder(mRequestContext)
                        .addScopes(ProfileScope.profile(), ProfileScope.postalCode())
                        .build());
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizationManager.signOut(getApplicationContext(), new Listener<Void, AuthError>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        /* Set Logged out state in UI */
                    }

                    @Override
                    public void onError(AuthError authError) {
                        /* Log the error */
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Scope[] scopes = {
                ProfileScope.profile(),
                ProfileScope.postalCode(),
        };

        AuthorizationManager.getToken(this, scopes, new Listener<AuthorizeResult, AuthError>() {
            @Override
            public void onSuccess(AuthorizeResult authorizeResult) {
                if (authorizeResult.getAccessToken() != null) {
                    /* The user is signed in */
                } else {
                    /* The user is not signed in */
                }
            }

            @Override
            public void onError(AuthError authError) {
                /* The user is not signed in */
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRequestContext.onResume();
    }

    public static class ErrorDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            Exception exception = (Exception) bundle.getSerializable(BUNDLE_KEY_EXCEPTION);
            String errormessage = exception.getMessage();

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage(errormessage)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .create();
        }
    }
}
