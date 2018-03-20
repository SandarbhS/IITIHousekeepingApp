package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

/**
 * Created by sandarbh on 15/2/18.
 */

public class DrawerFragment extends Fragment {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView drawerList;
    private ImageView topImage;
    private boolean CONTACT_INTENT = false;
    private final int CALL_PERMISSIONS = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View drawerView = inflater.inflate(R.layout.drawer_fragment,container,false);
        topImage =  drawerView.findViewById(R.id.top_image);
        drawerList = drawerView.findViewById(R.id.drawerList);

        DrawerAdapter adapter = new DrawerAdapter();
        drawerList.setAdapter(adapter);
        drawerList.setLayoutManager(new LinearLayoutManager(getContext()));
        setTopImage();
        return drawerView;
    }

    public void initDrawer(DrawerLayout drawerLayout, Toolbar toolbar){

        drawer = drawerLayout;
        drawerToggle = new ActionBarDrawerToggle(getActivity(),drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (CONTACT_INTENT){
                    CONTACT_INTENT = false;
                    Intent contacts = new Intent(getActivity(),ContactSupervisor.class);
                    startActivity(contacts);
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                getActivity().invalidateOptionsMenu();
            }
        };

        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        customizeDrawerToggleAppearance();
    }

    private void customizeDrawerToggleAppearance() {

        DrawerArrowDrawable drawerArroy = drawerToggle.getDrawerArrowDrawable();
        drawerArroy.setArrowHeadLength(drawerArroy.getArrowHeadLength()+10);
        drawerArroy.setArrowShaftLength(drawerArroy.getArrowShaftLength()+10);
        drawerArroy.setBarLength(drawerArroy.getBarLength()+10);
        drawerArroy.setBarThickness(drawerArroy.getBarThickness()+2);
        drawerArroy.setColor(getResources().getColor(R.color.titleColor));
        drawerArroy.setGapSize(drawerArroy.getGapSize()+3);
        drawerToggle.setDrawerArrowDrawable(drawerArroy);
        drawer.addDrawerListener(drawerToggle);
    }

    private void setTopImage(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.drawer_top,options);
        topImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        topImage.setImageBitmap(bmp);
    }

    public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.Holder>{

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

            View drawer_list = getLayoutInflater().inflate(R.layout.drawer_list,parent,false);
            return new Holder(drawer_list);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {

            final int pos = position;
            TypedArray imgArray = getResources().obtainTypedArray(R.array.drawer_icons);
            holder.icon.setImageResource(imgArray.getResourceId(position,-1));

            holder.option.setText(getResources().getStringArray(R.array.drawer_list)[position]);
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder;
                    AlertDialog dialog;
                    switch (pos){

                        case 0 :
                          builder = new AlertDialog.Builder(getActivity())
                                  .setTitle("About")
                                  .setMessage(String.format(getResources().getString(R.string.about),BuildConfig.VERSION_NAME))
                                  .setNeutralButton("OK",null);
                            dialog = builder.create();
                            dialog.show();

                            break;

                        case 1 :
                            CONTACT_INTENT = true;
                            drawer.closeDrawer(Gravity.START);
                            break;

                        case 2 :
                            builder = new AlertDialog.Builder(getActivity())
                                    .setTitle("Log Out")
                                    .setMessage("\nAre you sure you want to Log Out?")
                                    .setCancelable(false)
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseAuth.getInstance().signOut();
                                            SharedPreferences.Editor prefsEditor = getContext().getSharedPreferences(getResources().getString(R.string.shared_prefs), Context.MODE_PRIVATE).edit();
                                            prefsEditor.putBoolean("LOGGED IN",false);
                                            prefsEditor.commit();

                                            Intent goToLoginPage = new Intent(getContext(),Login.class);
                                            goToLoginPage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                            getContext().stopService(Supervisor_Activity.service);
                                            startActivity(goToLoginPage);
                                            getActivity().finish();
                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                            dialog = builder.create();
                            dialog.show();

                            break;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        public class Holder extends RecyclerView.ViewHolder{

            TextView option;
            ImageView icon;
            LinearLayout item;

            public Holder(View itemView) {
                super(itemView);

                option = itemView.findViewById(R.id.drawer_option);
                item = itemView.findViewById(R.id.drawer_item);
                icon = itemView.findViewById(R.id.drawer_icon);
            }
        }
    }
}
