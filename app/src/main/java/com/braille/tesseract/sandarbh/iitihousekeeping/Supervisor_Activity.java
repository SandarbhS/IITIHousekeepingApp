package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static com.braille.tesseract.sandarbh.iitihousekeeping.Login.toolbar;
import static com.braille.tesseract.sandarbh.iitihousekeeping.Student_Activity.drawer;

public class Supervisor_Activity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private AlertDialog retry;
    private CustomToast toast;
    private static TextView NAmsg;

    private String USERNAME;
    private boolean exit = false,RETRY = false;

    private ArrayList<Request> requestsList;
    private RecyclerView recyclerView;
    private RecyclerAdapter rAdapter;
    public static SwipeRefreshLayout refreshLayout;

    private DatabaseReference DB,roomUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor);

        toast = new CustomToast(getBaseContext());
        NAmsg = findViewById(R.id.naMsg);
        NAmsg.setText(R.string.blank_page_supervisor);
        NAmsg.setVisibility(GONE);

        requestsList = new ArrayList<>();
        if (savedInstanceState != null)
            requestsList = (ArrayList<Request>) savedInstanceState.getSerializable("Requests");

        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setNestedScrollingEnabled(true);
        refreshLayout.setRefreshing(true);

        initActionBar();
        initDrawer();
        initRecyclerView();
        initDatabase();
        retry= Loading();
        getAvailableRequests();
        checkDailyMessage();

    }

    public void initDatabase(){
        DB = FirebaseDatabase.getInstance().getReference();
        checkDailyMessage();
        USERNAME = getSharedPreferences(getResources().getString(R.string.shared_prefs),MODE_PRIVATE).getString("USERNAME",null);
        assert USERNAME != null;
        roomUser = DB.child(USERNAME);

        Log.e("DEBUG_Sup","Username: "+USERNAME);
    }

    private void checkDailyMessage(){

        DB.child("Message").child("Daily Message").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String message = (String) dataSnapshot.getValue();

                if (!message.equals("N/A")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Supervisor_Activity.this)
                            .setTitle("Daily Message")
                            .setMessage("\n"+message)
                            .setNeutralButton("DISMISS",null);
                    AlertDialog dialog = builder.create();
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DB.child("Message").child("Latest Version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String latestVersion = (String) dataSnapshot.getValue();

                if (!latestVersion.equals(BuildConfig.VERSION_NAME)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Supervisor_Activity.this)
                            .setTitle("UPDATE AVAILABLE")
                            .setMessage("A newer version "+latestVersion+" of app is available. Please update your app.")
                            .setNeutralButton("OK",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getAvailableRequests(){

        if (isConnectedToNetwork()) {
            DB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e("DEBUG", "Retrieving....");

                    for (DataSnapshot rooms : dataSnapshot.getChildren()) {
                        Log.e("cdc","Room : "+rooms.toString());

                        if (rooms.getKey().equals("Message")){
                            continue;
                        }
                        @SuppressWarnings("unchecked")
                        HashMap<String, HashMap> map = (HashMap<String, HashMap>) rooms.getValue();
                        if (map == null) {
                            refreshLayout.setRefreshing(false);
                            NAmsg.setVisibility(View.VISIBLE);
                        } else {
                            NAmsg.setVisibility(GONE);
                            updateRequestlist(map);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("DEBUG_Main", "FIREBASE ERROR : " + databaseError.getDetails());
                    CustomToast error = new CustomToast(getBaseContext());
                    error.showToast("Unexpected Error occured! Please try again.");
                }
            });
        }
        else{
            Log.e("DEBUG","NO NETWORK");
            CustomToast noNetwork = new CustomToast(this);
            noNetwork.showToast("No Internet Connection. Please retry!");
            refreshLayout.setRefreshing(false);
            NAmsg.setVisibility(View.VISIBLE);

            if (retry.isShowing()){
                retry.dismiss();
                uploadDataToDatabase();
            }
        }

    }

    public void updateRequestlist(HashMap<String,HashMap> requestsMap){

        if (refreshLayout.isRefreshing() || RETRY)
            requestsList.clear();

        for (Map.Entry<String,HashMap> entry : requestsMap.entrySet()){

            HashMap final_map = entry.getValue();
            String key = entry.getKey();
            Log.e("DEBUG","Key : "+entry.getKey());
            requestsList.add(new Request(final_map,key));
        }
        sortRequestsList();
        if (RETRY){
            RETRY = false;
            uploadDataToDatabase();
        }
        else {
            if (refreshLayout.isRefreshing())
                refreshLayout.setRefreshing(false);
        }
    }

    public void initActionBar(){
        TextView title,subtitle;

        toolbar = findViewById(R.id.actionBar);
        title = findViewById(R.id.toolbar_title);
        title.setTextColor(getResources().getColor(R.color.titleColor));

        subtitle = findViewById(R.id.toolbar_subtitle);
        subtitle.setTextColor(getResources().getColor(R.color.titleColor));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initDrawer() {
        drawer = findViewById(R.id.drawer);
        DrawerFragment drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.drawer_fragment);
        drawerFragment.initDrawer(drawer, toolbar);
    }

    public void initRecyclerView(){

        Collections.reverse(requestsList);

        recyclerView = findViewById(R.id.recyclerView);
        rAdapter = new RecyclerAdapter(Supervisor_Activity.this,requestsList);
        recyclerView.setAdapter(rAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Supervisor_Activity.this));
    }

    public static void toggleMessageVisibility(){
            NAmsg.setVisibility(View.VISIBLE);
    }

    public void sortRequestsList(){
        Collections.sort(requestsList, new Comparator<Request>() {
            @Override
            public int compare(Request request, Request t1) {
                return (int)(long)(request.uptime-t1.uptime);
            }
        });
    }

    public void uploadDataToDatabase(){

        if (isConnectedToNetwork()) {
            HashMap<String, Request> uploadMap = new HashMap<>();
            for (Request tmp : requestsList) {
                uploadMap.put("Request " + (requestsList.indexOf(tmp) + 1), tmp);
            }
            roomUser.setValue(uploadMap);

            if (retry.isShowing()) {
                retry.dismiss();
                CustomToast done = new CustomToast(this);
                done.showToast("Done!");
            }
            finish();

        }
        else {
            Log.e("DEBUG","NO NETWORK");
            if (retry.isShowing())
                retry.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("No internet connection was detected. If you exit now all the changes made by you may be lost.")
                    .setPositiveButton("EXIT ANYWAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton("RETRY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            showRetryDialog();
                            RETRY = true;
                            getAvailableRequests();
                            //uploadDataToDatabase();
                        }
                    })
                    .setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public boolean isConnectedToNetwork(){

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            Log.e("DEBUG", "OK");
            return true;
        }
        else
            return false;
    }

    private AlertDialog Loading(){

        String msg = "Reconnecting...";
        RelativeLayout dialogLayout = new RelativeLayout(this);
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
        loadingMessage.setTextSize(18);

        dialogLayout.addView(divider);
        dialogLayout.addView(loadingBar);
        dialogLayout.addView(loadingMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setTitle("Loading")
                .setCancelable(false);

        return builder.create();
    }

    private void showRetryDialog(){
        retry.show();
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(Gravity.START)) {
            Log.e("DEBUG", "DRAWER OPEN");
            drawer.closeDrawer(Gravity.START);
        }

        if (exit){
            //uploadDataToDatabase();
            //FirebaseAuth.getInstance().signOut();
            finish();
        }
        else{

            //toast.setToastPosition(Gravity.CENTER_HORIZONTAL,0,520);
            toast.showToast(getResources().getString(R.string.BACK_MSG));
            exit = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            },3*1000);
        }
    }

    @Override
    public void onRefresh() {
        Log.w("R","Refreshing...");
        if (isConnectedToNetwork()){
            getAvailableRequests();
        }
        else {
            refreshLayout.setRefreshing(false);
            CustomToast noNetwork = new CustomToast(this);
            noNetwork.showToast("No Internet Connection. Please retry!");
        }
        rAdapter.notifyItemRangeChanged(0,requestsList.size());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Log.e("DEBUG","DATA SAVED!");
        outState.putSerializable("Requests",requestsList);
    }

}
