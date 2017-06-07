package com.example.limin.ehelp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.limin.ehelp.bean.ContactBean;
import com.example.limin.ehelp.networkservice.APITestActivity;
import com.example.limin.ehelp.networkservice.ApiService;
import com.example.limin.ehelp.networkservice.ContactsResult;
import com.example.limin.ehelp.networkservice.EmptyResult;
import com.example.limin.ehelp.utility.CurrentUser;
import com.example.limin.ehelp.utility.ToastUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyContact extends AppCompatActivity {
    private ListView lv;
    private Button btn_back;
    private TextView title;
    private Button add;
    private TextView next;
    private TextView message;
    private String[] username = new String[] {"张三","李四"};
    private String[] phone = new String[] {"18888888888","15111111111"};
    private List<Map<String, Object>> contact = new ArrayList<>();
    private ApiService apiService;
    private List<ContactBean> contactBeen = new ArrayList<>();
    private SimpleAdapter simpleAdapter;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);
        apiService = ApiService.retrofit.create(ApiService.class);
        getData();
        setTitle();

        lv = (ListView)findViewById(R.id.emergency);
        add = (Button)findViewById(R.id.addContact);

        /*for (int i = 0; i < username.length; i++) {
            Map<String, Object> temp = new LinkedHashMap<>();
            temp.put("username",username[i]);
            temp.put("phone", phone[i]);
            contact.add(temp);
        }*/
        simpleAdapter = new SimpleAdapter(this, contact, R.layout.contact_item,
                new String[] {"username", "phone"}, new int[] {R.id.username, R.id.phone});
        lv.setAdapter(simpleAdapter);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.add_dialog, (ViewGroup)findViewById(R.id.add_dialog));
                final EditText addname = (EditText)layout.findViewById(R.id.add_name);
                final EditText addphone = (EditText)layout.findViewById(R.id.add_phone);
                new AlertDialog.Builder(EmergencyContact.this).setTitle("添加紧急联系人")
                        .setView(layout)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String add_name = addname.getText().toString();
                                final String add_phone = addphone.getText().toString();
                                Map<String, Object> temp2 = new LinkedHashMap<>();
                                temp2.put("username", add_name);
                                temp2.put("phone", add_phone);
                                contact.add(temp2);
                                simpleAdapter.notifyDataSetChanged();
                                Call<EmptyResult> call = apiService.requestAddContact(add_name, add_phone);
                                call.enqueue(new Callback<EmptyResult>() {
                                    @Override
                                    public void onResponse(Call<EmptyResult> call, Response<EmptyResult> response) {

                                        if (!response.isSuccessful()) {
                                            ToastUtils.show(EmergencyContact.this, ToastUtils.SERVER_ERROR);
                                            return;
                                        }
                                        if (response.body().status != 200) {
                                            ToastUtils.show(EmergencyContact.this, response.body().errmsg);
                                            return;
                                        }
                                        ToastUtils.show(EmergencyContact.this, new Gson().toJson(response.body()));
                                        SharedPreferences sharedPreferences = getSharedPreferences("econtact", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("username", add_name);
                                        editor.putString("phone", add_phone);
                                        editor.commit();

                                    }
                                    @Override
                                    public void onFailure(Call<EmptyResult> call, Throwable t) {
                                        ToastUtils.show(EmergencyContact.this, t.toString());
                                    }
                                });
                                Toast.makeText(getApplicationContext(),"添加联系人成功",Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("取消",null).show();
            }
        });

    }

    private void getData() {
        ToastUtils.show(EmergencyContact.this, CurrentUser.cookie);

        Call<ContactsResult> call = apiService.requestContacts();
        call.enqueue(new Callback<ContactsResult>() {
            @Override
            public void onResponse(Call<ContactsResult> call, Response<ContactsResult> response) {

                if (!response.isSuccessful()) {
                    ToastUtils.show(EmergencyContact.this, ToastUtils.SERVER_ERROR);
                    return;
                }
                if (response.body().status != 200) {
                    ToastUtils.show(EmergencyContact.this, response.body().errmsg);
                    return;
                }
                ToastUtils.show(EmergencyContact.this, new Gson().toJson(response.body()));
                contactBeen.clear();
                contactBeen = response.body().data;
                contact.clear();
                for (int i = 0; i < contactBeen.size(); i++) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("username", contactBeen.get(i).username);
                    item.put("phone", contactBeen.get(i).phone);
                    contact.add(item);
                }
                simpleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ContactsResult> call, Throwable t) {
                ToastUtils.show(EmergencyContact.this, t.toString());
            }
        });

    }

    private void setTitle() {
        btn_back = (Button) findViewById(R.id.btn_back);
        title = (TextView) findViewById(R.id.tv_title);
        next = (TextView) findViewById(R.id.tv_nextope);
        message = (TextView)findViewById(R.id.add_message);

        next.setText("保存");

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        title.setText("紧急联系人");
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = message.getText().toString();
                //这里需要将个人信息数据传入数据库
                Toast.makeText(EmergencyContact.this, "保存成功!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
}


