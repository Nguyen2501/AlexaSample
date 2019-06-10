package com.alexasample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();
    private static final String ALEXA_ALL_SCOPE = "alexa:all";
    public static final String BUNDLE_KEY_EXCEPTION = "exception";
    public static final int MIN_CONNECT_PROGRESS_TIME_MS = 1*1000;
    private static final String DEVICE_SERIAL_NUMBER = "deviceSerialNumber";
    private static final String PRODUCT_INSTANCE_ATTRIBUTES = "productInstanceAttributes";
    private static final String PRODUCT_ID = "productID";
    private RequestContext mRequestContext;
    private ImageButton mLoginButton;
    private Button mLogoutButton;
    private Button mConnectButton;
    private EditText mAddrTextView;

    private DeviceProvisioningInfo mDeviceProvisioningInfo;
    private ProvisioningClient mProvisioningClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestContext = RequestContext.create(this);
        mRequestContext.registerListener(new AuthorizeListenerImplement());

        setContentView(R.layout.activity_main);

        mLoginButton = (ImageButton) findViewById(R.id.loginButton);
        mLogoutButton = (Button) findViewById(R.id.logoutbutton);
        mConnectButton = (Button) findViewById(R.id.connectButton);
        mAddrTextView = (EditText) findViewById(R.id.addressTextView);

        try {
            mProvisioningClient = new ProvisioningClient(this);
        } catch (Exception e) {
            showAlertDialog(e);
            Log.e(TAG, "Unable to use Provisioning Client. CA Certificate is incorrect or does not exist", e);
        }

        String savedDevAddress = getPreferences(Context.MODE_PRIVATE)
                .getString(getString(R.string.saved_device_addr), null);

        if (savedDevAddress != null) {
            mAddrTextView.setText(savedDevAddress);
        }

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String address = mAddrTextView.getText().toString();
                mProvisioningClient.setEndpoint(address);

                new AsyncTask<Void, Void, DeviceProvisioningInfo>() {
                    private Exception errorInBackground;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected DeviceProvisioningInfo doInBackground(Void... voids) {
                        try {
                            long startTimeMs = System.currentTimeMillis();
                            DeviceProvisioningInfo response = mProvisioningClient.getDeviceProvisioningInfo();
                            long durationMs = System.currentTimeMillis() - startTimeMs;

                            if (durationMs < MIN_CONNECT_PROGRESS_TIME_MS) {
                                try {
                                    Thread.sleep(MIN_CONNECT_PROGRESS_TIME_MS - durationMs);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            return response;
                        } catch (Exception e) {
                            errorInBackground = e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(DeviceProvisioningInfo deviceProvisioningInfo) {
                        super.onPostExecute(deviceProvisioningInfo);
                        if (deviceProvisioningInfo != null) {
                            mDeviceProvisioningInfo = deviceProvisioningInfo;

                            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                            editor.putString(getString(R.string.saved_device_addr), address);
                            editor.commit();
                        }
                        else {
                            showAlertDialog(errorInBackground);
                        }
                    }
                }.execute();
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject scopeData = new JSONObject();
                final JSONObject productInstanceAttributes = new JSONObject();
                final String codeChallenge = mDeviceProvisioningInfo.getCodeChallenge();
                final String codeChallengeMethod = mDeviceProvisioningInfo.getCodeChallengeMethod();

                try {
                    productInstanceAttributes.put(DEVICE_SERIAL_NUMBER, mDeviceProvisioningInfo.getDsn());
                    scopeData.put(PRODUCT_INSTANCE_ATTRIBUTES, productInstanceAttributes);
                    scopeData.put(PRODUCT_ID, mDeviceProvisioningInfo.getProductId());

                    AuthorizationManager.authorize(new AuthorizeRequest.Builder(mRequestContext)
                            .addScope(ScopeFactory.scopeNamed(ALEXA_ALL_SCOPE, scopeData))
                            .forGrantType(AuthorizeRequest.GrantType.AUTHORIZATION_CODE)
                            .withProofKeyParameters(codeChallenge, codeChallengeMethod)
                            .build());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    protected void showAlertDialog(Exception exception) {
        exception.printStackTrace();
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_KEY_EXCEPTION, exception);
        dialogFragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        dialogFragment.show(fragmentManager, "error_dialog");
    }

    private class AuthorizeListenerImplement extends AuthorizeListener {
        @Override
        public void onSuccess(final AuthorizeResult authorizeResult) {
            final String authorizationCode = authorizeResult.getAuthorizationCode();
            final String redirectUri = authorizeResult.getRedirectURI();
            final String clientId = authorizeResult.getClientId();
            final String sessionId = mDeviceProvisioningInfo.getSessionId();

            final CompanionProvisioningInfo companionProvisioningInfo = new CompanionProvisioningInfo(sessionId,clientId, redirectUri, authorizationCode);

            new AsyncTask<Void, Void, Void>() {
                private Exception errorInBackground;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        mProvisioningClient.postCompanionProvisioningInfo(companionProvisioningInfo);
                    } catch (Exception e) {
                        errorInBackground = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    if (errorInBackground != null) {
                        showAlertDialog(errorInBackground);
                    }
                    else {

                    }
                }
            }.execute();
        }

        @Override
        public void onCancel(AuthCancellation authCancellation) {
            Log.e(TAG, "onCancel: User cancelled authorization");
        }

        @Override
        public void onError(AuthError authError) {
            Log.e(TAG, "onError: AuthError during authorization", authError);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
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
