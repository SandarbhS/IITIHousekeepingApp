package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Login extends AppCompatActivity {

    public static Toolbar toolbar;
    private static EditText roomno,username,stupwd,suppwd;
    private static Button stulogin,suplogin;
    private static AlertDialog dialog;
    private TextView title,subtitle;
    private ImageView background;

    private boolean exit = false;
    private static final int STUDENT_LOGIN = 1,SUPERVISOR_LOGIN = 2;
    private static Activity thisActivity;

    private TabLayout chooseLogin;
    private ViewPager pager;

    private static FirebaseAuth authenticate;
    private FirebaseUser user;
    private static SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        thisActivity = this;

        toolbar = findViewById(R.id.actionBar);
        title = findViewById(R.id.toolbar_title);
        title.setTextColor(getResources().getColor(R.color.titleColor));

        subtitle = findViewById(R.id.toolbar_subtitle);
        subtitle.setTextColor(getResources().getColor(R.color.titleColor));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        chooseLogin = findViewById(R.id.chooseLogin);
        pager = findViewById(R.id.pager);

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        chooseLogin.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(chooseLogin));

        authenticate = FirebaseAuth.getInstance();
        dialog = Loading();

        initBG();

    }

    private void initBG(){

        background = findViewById(R.id.bg);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.bg_abstract,options);

        background.setImageBitmap(bmp);
        background.setAlpha((float)1.0);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Log.e("BITMAP",""+bmp.getHeight()+" "+bmp.getWidth());
    }

    @Override
    protected void onStart() {
        super.onStart();

        preferences  = getSharedPreferences(getResources().getString(R.string.shared_prefs),MODE_PRIVATE);
        boolean FIRST_VISIT = preferences.getBoolean("FIRST_VISIT",true);

        boolean logged_in = preferences.getBoolean("LOGGED IN",false);
        if (logged_in) {
            Intent redirect;

            String USERNAME = preferences.getString("USERNAME",null);
            if (USERNAME!=null) {
                if (USERNAME.equals("SUPERVISOR"))
                    redirect = new Intent(thisActivity, Supervisor_Activity.class);

                else
                    redirect = new Intent(thisActivity, Student_Activity.class);
                //redirect.putExtra("User",user.getDisplayName());
                Log.e("DEBUG_Login", "" + USERNAME);

                startActivity(redirect);
                thisActivity.finish();
            }
        }
    }

    public class PagerAdapter extends FragmentStatePagerAdapter{

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            StudentLoginFragment loginFragment = StudentLoginFragment.newFragment(position);
            return loginFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){

                case 0 : return "Student";

                case 1 : return "Supervisor";
            }
            return "Tab : "+position;
        }
    }

    public static class StudentLoginFragment extends Fragment{

        public StudentLoginFragment(){

        }

        public static StudentLoginFragment newFragment(int position){

            StudentLoginFragment frag = new StudentLoginFragment();
            Bundle arguments = new Bundle();
            arguments.putInt("Code",position);

            frag.setArguments(arguments);
            return frag;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            int code = this.getArguments().getInt("Code");
            switch (code){

                case 0 : final View student_login = inflater.inflate(R.layout.student_login,container,false);

                    roomno = student_login.findViewById(R.id.roomno);
                    stupwd = student_login.findViewById(R.id.stupwd);
                    stulogin = student_login.findViewById(R.id.stulogin);

                    stulogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Log.e("BUTTON","Clicked");
                            final String email = roomno.getText().toString().trim();
                            String password = stupwd.getText().toString();

                            if (email.isEmpty() || password.isEmpty()){
                                Log.e("DEBUG", "CANCELLED");
                                CustomToast invalid = new CustomToast(thisActivity);
                                invalid.showToast("Invalid Credentials!");
                            }

                            else {
                                dialog.show();
                                Log.e("DEBUG", "EXECUTING");
                                ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(CONNECTIVITY_SERVICE);
                                if (cm.getActiveNetworkInfo() != null)
                                    loginUser(email,password,STUDENT_LOGIN);
                                else{
                                    dialog.dismiss();
                                    CustomToast noNetwork = new CustomToast(thisActivity);
                                    noNetwork.showToast("No Internet Connection! Please try again.");
                                }
                            }
                        }
                    });

                    stupwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                            if (i == EditorInfo.IME_ACTION_GO){
                                stulogin.performClick();

                                View view = thisActivity.getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }

                                Log.e("KEYBOARD","Clicked");
                                return true;
                            }
                            return false;
                        }
                    });

                    return student_login;

                case 1 : View supervisor_login = inflater.inflate(R.layout.supervisor_login,container,false);

                    username = supervisor_login.findViewById(R.id.username);
                    suppwd = supervisor_login.findViewById(R.id.suppwd);
                    suplogin = supervisor_login.findViewById(R.id.suplogin);

                    suplogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String email = username.getText().toString().trim();
                            String password = suppwd.getText().toString();

                            if (email.isEmpty() || password.isEmpty()){
                                Log.e("DEBUG", "CANCELLED");
                                CustomToast invalid = new CustomToast(thisActivity);
                                invalid.showToast("Invalid Credentials!");
                            }

                            else {
                                dialog.show();
                                Log.e("DEBUG", "EXECUTING");
                                ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(CONNECTIVITY_SERVICE);
                                if (cm.getActiveNetworkInfo() != null)
                                    loginUser(email,password,SUPERVISOR_LOGIN);
                                else{
                                    dialog.dismiss();
                                    CustomToast noNetwork = new CustomToast(thisActivity);
                                    noNetwork.showToast("No Internet Connection! Please try again.");
                                }
                            }
                        }
                    });

                    suppwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                            if (i == EditorInfo.IME_ACTION_GO){
                                suplogin.performClick();

                                View view = thisActivity.getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }

                                Log.e("KEYBOARD","Clicked");
                                return true;
                            }
                            return false;
                        }
                    });

                    return supervisor_login;
            }

            return null;
        }

    }

    private static void loginUser(final String email, String password, final int code){
        authenticate.signInWithEmailAndPassword(email.concat("@sandarbh.firebaseapp.com"), password).addOnCompleteListener(thisActivity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.e("DEBUG", "SUCCESS");
                    dialog.dismiss();

                    SharedPreferences.Editor prefEditor = preferences.edit();
                    prefEditor.putString("USERNAME",email);
                    prefEditor.putBoolean("LOGGED IN",true);
                    prefEditor.apply();

                    Log.e("DEBUG_LOGIN",preferences.getBoolean("LOGGED IN",false)+"");

                    FirebaseUser curr_user = authenticate.getCurrentUser();
                    if (curr_user.getDisplayName() == null) {
                        UserProfileChangeRequest updateProfile = new UserProfileChangeRequest.Builder()
                                .setDisplayName(email).build();

                        curr_user.updateProfile(updateProfile).addOnCompleteListener(thisActivity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.e("DEBUG_LOGIN","USERNAME SET");
                                    redirect(email,code);
                                }
                            }
                        });
                    }
                    else
                        redirect(email,code);

                }
                else {
                    //task.getResult();
                    Log.e("DEBUG", "FAILED");
                    dialog.dismiss();
                    CustomToast invalid = new CustomToast(thisActivity);
                    invalid.showToast("Invalid Credentials!");
                }
            }

        });
    }

    private static void redirect(String email,int code){
        Intent Enter = null;

        switch (code){

            case STUDENT_LOGIN : if (email.equals("SUPERVISOR")) {
                CustomToast invalid = new CustomToast(thisActivity);
                invalid.showToast("Invalid Credentials!");
            }
            else
                Enter = new Intent(thisActivity,Student_Activity.class);

                break;

            case SUPERVISOR_LOGIN : if (email.equals("SUPERVISOR")) {
                Enter = new Intent(thisActivity,Supervisor_Activity.class);
            }
            else {
                CustomToast invalid = new CustomToast(thisActivity);
                invalid.showToast("Invalid Credentials!");
            }

                break;
        }

        if (Enter!=null) {
            Enter.putExtra("User", email);
            thisActivity.startActivity(Enter);
            thisActivity.finish();
        }
    }

    private static AlertDialog Loading(){

        String msg = "Logging in...";
        RelativeLayout dialogLayout = new RelativeLayout(thisActivity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogLayout.setLayoutParams(params);

        View divider = new View(dialogLayout.getContext());
        RelativeLayout.LayoutParams dividerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1);
        dividerParams.topMargin = 20;
        dividerParams.leftMargin = 20;
        dividerParams.rightMargin = 20;
        dividerParams.bottomMargin = 20;
        divider.setLayoutParams(dividerParams);

        divider.setBackgroundResource(R.color.Background);
        divider.setId(R.id.uptime);

        ProgressBar loadingBar = new ProgressBar(dialogLayout.getContext());
        RelativeLayout.LayoutParams barParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        barParams.leftMargin = 70;
        barParams.topMargin = 50;
        barParams.bottomMargin = 50;
        loadingBar.setIndeterminate(true);
        loadingBar.setLayoutParams(barParams);
        loadingBar.setId(R.id.refresh);

        TextView loadingMessage = new TextView(dialogLayout.getContext());
        RelativeLayout.LayoutParams msgParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msgParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        loadingMessage.setText(msg);
        loadingMessage.setLayoutParams(msgParams);
        loadingMessage.setTextSize((float) getDPFromPixels(18));

        dialogLayout.addView(divider);
        dialogLayout.addView(loadingBar);
        dialogLayout.addView(loadingMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity)
                .setView(dialogLayout)
                .setTitle("Loading")
                .setCancelable(false);

        return builder.create();
    }

    private static double getDPFromPixels(double pixels) {
        DisplayMetrics metrics = new DisplayMetrics();
        thisActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        switch(metrics.densityDpi){
            case DisplayMetrics.DENSITY_LOW:
                pixels = pixels * 0.75;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                //pixels = pixels * 1;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                pixels = pixels * 1.5;
                break;
        }
        return pixels;
    }

    @Override
    public void onBackPressed() {

        if (exit){
            finish();
        }
        else{
            CustomToast back = new CustomToast(getBaseContext());
            back.showToast(getResources().getString(R.string.BACK_MSG));

            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            },3*1000);
        }
    }
}
