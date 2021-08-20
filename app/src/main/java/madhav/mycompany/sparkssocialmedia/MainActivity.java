package madhav.mycompany.sparkssocialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private TextView textViewUser,textViewUserid;
    private ImageView mLogo;
    private LoginButton fLoginButton,gbtn;
    private AccessTokenTracker accessTokenTracker;
    private static final String fTAG="Facebook Authentication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth=FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        textViewUser = findViewById(R.id.textView);
        textViewUserid = findViewById(R.id.textView2);
        mLogo= findViewById(R.id.imageView);
        fLoginButton=findViewById(R.id.login_button);
        gbtn = findViewById(R.id.g_btn);
        fLoginButton.setReadPermissions("email","public_profile");
        mCallbackManager=CallbackManager.Factory.create();

        fLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(fTAG,"onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(fTAG,"onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(fTAG,"onError" + error);
            }
        });
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user!=null){
                    updateUI(user);

                }
                else{
                    updateUI(null);
                }
            }
        };

        accessTokenTracker= new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken==null){
                    mFirebaseAuth.signOut();
                }
            }
        };

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookToken(AccessToken token) {
        Log.d(fTAG,"handleFacebookToken"+token);

        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    Log.d(fTAG,"Sign in with credential :successful");
                    FirebaseUser fUser = mFirebaseAuth.getCurrentUser();
                    updateUI(fUser);
                }
                else{
                    Log.d(fTAG,"Sign in with credential :unsuccessful",task.getException());
                    Toast.makeText(MainActivity.this,"Authentication Failed",Toast.LENGTH_LONG).show();
                    updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {

        if (user!= null){
            textViewUser.setText((user.getDisplayName()));
            textViewUserid.setText(user.getEmail());
            if(user.getPhotoUrl()!=null){
                String photoURL= user.getPhotoUrl().toString();
                photoURL=photoURL+"?type=large";
                Picasso.get().load(photoURL).into(mLogo);
                gbtn.setVisibility(View.INVISIBLE);
            }
        }
        else{
            textViewUser.setText("");
            textViewUserid.setText("");
            mLogo.setImageResource(R.drawable.tsf);
            gbtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(authStateListener);
    }
}