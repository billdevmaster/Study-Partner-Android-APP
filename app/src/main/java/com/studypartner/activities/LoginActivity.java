package com.studypartner.activities;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.studypartner.R;
import com.studypartner.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
	private static final String TAG = "LoginActivity";
	
	private final int RC_SIGN_IN = 123;
	
	private final String REMEMBER_ME_ENABLED = "rememberMeEnabled";
	private final String REMEMBER_ME_EMAIL = "rememberMeEmail";
	private final String REMEMBER_ME_PASSWORD = "rememberMePassword";
	
	private SharedPreferences.Editor mEditor;
	
	private TextInputLayout emailTextInput, passwordTextInput;
	private TextInputEditText emailEditText, passwordEditText;
	private CheckBox rememberMe;
	
	private GoogleSignInClient mGoogleSignInClient;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			findViewById(R.id.loginScreenProgressBar).setVisibility(View.VISIBLE);
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = task.getResult(ApiException.class);
				Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
				firebaseAuthWithGoogle(account.getIdToken());
			} catch (ApiException e) {
				findViewById(R.id.loginScreenProgressBar).setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onCreate: Checking internet connection");
		if (isConnected(this)) {
			Log.d(TAG, "onCreate: Internet not connected");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: opening settings for internet");
							startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: closing app");
							finishAffinity();
						}
					});
			builder.show();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		// Configure Google Sign In
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();
		
		mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
		
		Log.d(TAG, "onCreate: Checking internet connection");
		if (isConnected(this)) {
			Log.d(TAG, "onCreate: Internet not connected");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: opening settings for internet");
							startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: closing app");
							finishAffinity();
						}
					});
			builder.show();
		}
		
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			Log.d(TAG, "onCreate: User already logged in");
			
			findViewById(R.id.loginScreenProgressBar).setVisibility(View.VISIBLE);
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
			finishAffinity();
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
		
		SharedPreferences sharedPreferences = getSharedPreferences("SESSIONS", MODE_PRIVATE);
		
		mEditor = sharedPreferences.edit();
		
		//Setting hooks
		
		emailTextInput = findViewById(R.id.loginScreenEmailTextInput);
		emailEditText = findViewById(R.id.loginScreenEmailEditText);
		
		emailEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				emailTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		passwordTextInput = findViewById(R.id.loginScreenPasswordTextInput);
		passwordEditText = findViewById(R.id.loginScreenPasswordEditText);
		
		passwordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		Button loginButton = findViewById(R.id.loginScreenLoginButton);
		Button googleLoginButton = findViewById(R.id.loginScreenGoogleLoginButton);
		Button forgotPasswordButton = findViewById(R.id.loginScreenForgotPasswordButton);
		
		rememberMe = findViewById(R.id.loginScreenRememberMeCheckBox);
		
		if (sharedPreferences.getBoolean(REMEMBER_ME_ENABLED, false)) {
			emailEditText.setText(sharedPreferences.getString(REMEMBER_ME_EMAIL, ""));
			passwordEditText.setText(sharedPreferences.getString(REMEMBER_ME_PASSWORD, ""));
			rememberMe.setChecked(true);
		}
		
		findViewById(R.id.loginScreenCreateAccountButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: creating shared animation on create account button clicked");
				Pair[] pairs = new Pair[6];
				pairs[0] = new Pair<>(findViewById(R.id.loginScreenWelcomeBack), "welcome_transition");
				pairs[1] = new Pair<>(findViewById(R.id.loginScreenCreateAccountButton), "create_account_transition");
				pairs[2] = new Pair<>(findViewById(R.id.loginScreenLoginButton), "login_transition");
				pairs[3] = new Pair<>(findViewById(R.id.loginScreenEmailTextInput), "email_transition");
				pairs[4] = new Pair<>(findViewById(R.id.loginScreenPasswordTextInput), "password_transition");
				pairs[5] = new Pair<>(findViewById(R.id.loginScreenBackgroundRectangle), "bgrect_transition");
				
				ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
				
				startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class), activityOptions.toBundle());
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: login button clicked");
				
				if (validateFields()) {
					login();
				}
			}
		});
		
		googleLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: google login button clicked");
				
				googleLogin();
			}
		});
		
		forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: forget password clicked");
				if (validateEmail(emailEditText.getText().toString()) != null) {
					Log.d(TAG, "onClick: invalid email address entered");
					Toast.makeText(LoginActivity.this, "Put a valid email associated with an account to change the password", Toast.LENGTH_SHORT).show();
				} else {
					FirebaseAuth.getInstance().sendPasswordResetEmail(emailEditText.getText().toString())
							.addOnCompleteListener(new OnCompleteListener<Void>() {
								@Override
								public void onComplete(@NonNull Task<Void> task) {
									if (task.isSuccessful()) {
										Log.d(TAG, "onComplete: email sent to user for password reset");
										Toast.makeText(LoginActivity.this, "A password reset email has been sent to your email address", Toast.LENGTH_SHORT).show();
									} else {
										Log.d(TAG, "onComplete: email for password reset cannot be sent");
										Toast.makeText(LoginActivity.this, "Couldn't send a password reset email", Toast.LENGTH_SHORT).show();
										task.getException().printStackTrace();
									}
								}
							});
				}
			}
		});
		
		Log.d(TAG, "onCreate: ends");
	}
	
	private boolean isConnected(LoginActivity loginActivity) {
		Log.d(TAG, "isConnected: internet check");
		
		ConnectivityManager connectivityManager = (ConnectivityManager) loginActivity.getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		return activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting();
	}
	
	
	private void googleLogin() {
		Log.d(TAG, "googleLogin: starts");
		
		Intent signInIntent = mGoogleSignInClient.getSignInIntent();
		startActivityForResult(signInIntent, RC_SIGN_IN);
		
		Log.d(TAG, "googleLogin: ends");
	}
	
	private void firebaseAuthWithGoogle(String idToken) {
		Log.d(TAG, "firebaseAuthWithGoogle: starts");
		
		AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
		FirebaseAuth.getInstance().signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "signInWithCredential: success");
							storeUserDetails();
							startActivity(new Intent(LoginActivity.this, MainActivity.class));
							finishAffinity();
							overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
						} else {
							findViewById(R.id.loginScreenProgressBar).setVisibility(View.INVISIBLE);
							Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
							task.getException().printStackTrace();
						}
					}
				});
	}
	
	private void storeUserDetails() {
		Log.d(TAG, "storeUserDetails: starts");
		
		GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
		
		if (signInAccount != null) {
			
			String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
			
			User user = new User(signInAccount.getDisplayName(), signInAccount.getEmail(), true);
			
			//Make users database
			FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(user);
			
			//Make usernames database
			FirebaseDatabase.getInstance().getReference().child("usernames").child(uid).setValue(user.getUsername());
		}
		Log.d(TAG, "storeUserDetails: ends");
	}
	
	private void login() {
		Log.d(TAG, "login: starts");
		findViewById(R.id.loginScreenProgressBar).setVisibility(View.VISIBLE);
		
		FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: login successful");
							if (rememberMe.isChecked()) {
								mEditor.putBoolean(REMEMBER_ME_ENABLED, true);
								mEditor.putString(REMEMBER_ME_EMAIL, emailEditText.getText().toString());
								mEditor.putString(REMEMBER_ME_PASSWORD, passwordEditText.getText().toString());
								mEditor.apply();
							}
							updateDetails();
							startActivity(new Intent(LoginActivity.this, MainActivity.class));
							finishAffinity();
							overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
						} else {
							findViewById(R.id.loginScreenProgressBar).setVisibility(View.INVISIBLE);
							Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
							task.getException().printStackTrace();
						}
					}
				});
	}
	
	private void updateDetails() {
		Log.d(TAG, "updateDetails: starts");
		
		String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		
		Log.d(TAG, "updateDetails: updating email verification");
		FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("emailVerified").setValue(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified());
		
		Log.d(TAG, "updateDetails: updating email");
		FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("email").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause: starts");
		super.onPause();
		
		if (emailTextInput.isErrorEnabled()) {
			emailTextInput.setError(null);
		}
		
		if (passwordTextInput.isErrorEnabled()) {
			passwordTextInput.setError(null);
		}
	}
	
	private boolean validateFields() {
		Log.d(TAG, "validateFields: starts");
		String emailValidation = validateEmail(emailEditText.getText().toString());
		String passwordValidation = validatePassword(passwordEditText.getText().toString());
		
		if (emailValidation == null && passwordValidation == null) {
			Log.d(TAG, "validateFields() returned: " + true);
			return true;
		}
		
		if (emailValidation != null) {
			emailTextInput.setError(emailValidation);
		}
		
		if (passwordValidation != null) {
			passwordTextInput.setError(passwordValidation);
		}
		
		Log.d(TAG, "validateFields() returned: " + false);
		return false;
	}
	
	private String validateEmail(String email) {
		if (email.trim().length() == 0) {
			return "Email cannot be empty";
		} else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			return "Invalid Email";
		}
		return null;
	}
	
	private String validatePassword(String password) {
		if (password.trim().length() == 0) {
			return "Password cannot be empty";
		} else if (password.length() < 7) {
			return "Password too small. Minimum length is 7";
		} else if (password.length() > 15) {
			return "Password too long. Maximum length is 15";
		} else if (password.contains(" ")) {
			return "Password cannot contain spaces";
		} else if (!(password.contains("@") || password.contains("#")) || password.contains("$") || password.contains("%") || password.contains("*") || password.contains(".") || password.matches("(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))")) {
			return "Password should contain atleast one uppercase character or one number or any of the special characters from (@, #, $, %, *, .)";
		}
		return null;
	}
}