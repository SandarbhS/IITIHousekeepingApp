package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.braille.tesseract.sandarbh.iitihousekeeping.Login.toolbar;

public class ContactSupervisor extends AppCompatActivity {

    private RecyclerView contactList;
    private contactsAdapter contactsAdapter;
    private int CALL_PERMISSIONS = 1;
    private Intent caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_supervisor);

        contactList = findViewById(R.id.contactList);
        contactsAdapter = new contactsAdapter();
        contactList.setAdapter(contactsAdapter);
        contactList.setLayoutManager(new LinearLayoutManager(this));

        initActionBar();
        caller = new Intent(Intent.ACTION_DIAL);
        //initDrawer();
    }

    public void initActionBar(){
        toolbar = findViewById(R.id.actionBar);
        TextView title,subtitle;

        title = findViewById(R.id.toolbar_title);
        title.setTextColor(getResources().getColor(R.color.titleColor));

        subtitle = findViewById(R.id.toolbar_subtitle);
        subtitle.setTextColor(getResources().getColor(R.color.titleColor));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.up_button_icon);
    }

    public class contactsAdapter extends RecyclerView.Adapter<contactsAdapter.Holder>{

        String[] contactInfo = getResources().getStringArray(R.array.contacts_list);
        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view =  getLayoutInflater().inflate(R.layout.contacts,parent,false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(final Holder holder, int position) {

            position *= 3;
            //String[] contactInfo = getResources().getStringArray(R.array.contacts_list);
            holder.name.setText(contactInfo[position]);
            holder.position.setText(contactInfo[position+1]);
            holder.contact.setText(contactInfo[position+2]);
            holder.contactIcon.setText(contactInfo[position].substring(0,1));

            TypedArray iconColor = getResources().obtainTypedArray(R.array.contact_icon_color);
            holder.contactIcon.setBackgroundResource(iconColor.getResourceId(position/3,0));

            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    caller.setData(Uri.parse("tel:"+holder.contact.getText().toString()));

                    if (ActivityCompat.checkSelfPermission(ContactSupervisor.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(ContactSupervisor.this,new String[]{android.Manifest.permission.CALL_PHONE},CALL_PERMISSIONS);
                    }
                    else {
                        startActivity(caller);
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        public class Holder extends RecyclerView.ViewHolder{

            private TextView name,position,contact,contactIcon;
            private LinearLayout item;

            public Holder(View itemView) {
                super(itemView);

                name = itemView.findViewById(R.id.name);
                position = itemView.findViewById(R.id.position);
                contact = itemView.findViewById(R.id.contact);
                item = itemView.findViewById(R.id.contact_item);
                contactIcon = itemView.findViewById(R.id.contact_icon);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startActivity(caller);
        }

        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!shouldShowRequestPermissionRationale(permissions[0])) {
                CustomToast permission_denied = new CustomToast(this);
                permission_denied.showToast("Please go to settings and grant\nCall Permissions!");
            }
        }
            else{
                CustomToast permission_denied = new CustomToast(this);
                permission_denied.showToast("Please grant call permissions!");
            }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}
