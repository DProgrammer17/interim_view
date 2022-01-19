package com.accelerexholdings.anp.ui.accountOpening;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.accelerexholdings.anp.R;
import com.accelerexholdings.anp.dao.AppDatabase;
import com.accelerexholdings.anp.model.LGACodeInfo;
import com.accelerexholdings.anp.ui.create_account.UploadPhoto;
import com.accelerexholdings.anp.model.GenericDataInfo;
import com.accelerexholdings.anp.model.LookUpData;
import com.accelerexholdings.anp.model.OpenAccount;
import com.accelerexholdings.anp.ui.Adapter.CustomLanguageAdapter;
import com.accelerexholdings.anp.utils.FormUtility;
import com.accelerexholdings.anp.utils.Globals;
import com.accelerexholdings.anp.utils.OtaUtility;
import com.accelerexholdings.anp.utils.SharedPrefUtil;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.annotations.Until;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class AccountOpening2 extends AppCompatActivity {


    private Spinner banlkSpinner;
    String genderArray[] = {"Male", "Female"};
    String idCard[] = {"        ","International Passport", "National Identify Card","Drivers Licence"};

    OpenAccount openAccount ;
    @BindView(R.id.gender)
    AutoCompleteTextView gender;

    @BindView(R.id.lg_code)
    AutoCompleteTextView lg_code;

    @BindView(R.id.dob)
    TextInputEditText dob;

    @BindView(R.id.identityNum)
    TextInputEditText identityNum;



    //
    @BindView(R.id.phone)
    EditText phone;

    @BindView(R.id.selectID)
    AutoCompleteTextView selectID;




    private List<LookUpData> bankDataArrayList  = new ArrayList<LookUpData>();
    private CustomLanguageAdapter customLanguageAdapter;
    private AppDatabase appDatabase;
    private List<String> lgaCodes;
    String token,language,countryCode;

    SharedPreferences shared;
    Long minDay,dayDifference;


    private View parent_view;
    Activity act;
    Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_opening2);
        appDatabase = AppDatabase.getAppDatabase(this);
        lgaCodes = appDatabase.genericDataDAO().getLGACode();
        ArrayAdapter<String> lgaCodesAdapter = new ArrayAdapter<>(AccountOpening2.this,
                R.layout.item_dropdown, R.id.dropdown_item,lgaCodes );

        lg_code.setAdapter(lgaCodesAdapter);
        initialize();
    }

    private void initialize() {
        ButterKnife.bind(this);
        mActivity= this;
        act= this;
        
        parent_view = findViewById(android.R.id.content);
        openAccount = new OpenAccount();
        ButterKnife.bind(this);
        parent_view = findViewById(android.R.id.content);
        shared =  this.getSharedPreferences(String.valueOf((R.string.app_name)), Context.MODE_PRIVATE);
        token =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getTicketID();
        language =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getLanguage();
        countryCode =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getCountry();
        dob.setText(Globals.DATE_PERTAN);
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker();
            }
        });

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,R.layout.item_dropdown,R.id.dropdown_item,genderArray);
        gender.setAdapter(genderAdapter);
        ArrayAdapter<String> idCardType = new ArrayAdapter<>(this,R.layout.item_dropdown,R.id.dropdown_item,idCard);
        selectID.setAdapter(idCardType);
        
        Intent intent = getIntent();
        openAccount = (OpenAccount) intent.getSerializableExtra(Globals.REGISTER_REQUEST);

        banlkSpinner = (Spinner) findViewById(R.id.banlkSpinner);
        customLanguageAdapter = new CustomLanguageAdapter(this, bankDataArrayList );
        banlkSpinner.setAdapter(customLanguageAdapter);
        BanksListService(countryCode);



        banlkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LookUpData lookUpData =  bankDataArrayList.get(position);
                  LookUpData data = bankDataArrayList.get(position);
                  openAccount.setBankCode(lookUpData.getCode());
               }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void backPress(View view) {

        finish();
    }

    public void next(View view) throws ParseException {
        if((!validateInput())){
            FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.vai), this);
            return;
        }
        else openAccount();

    }


    public void openAccount()  {


        Intent intent = new Intent(AccountOpening2.this, UploadPhoto.class);
        intent.putExtra(Globals.REGISTER_REQUEST, openAccount);
        intent.putExtra(Globals.USER, Globals.CUSTOMER);
        startActivity(intent);
    }


    private void BanksListService(String countryCode){

        String new_token = new StringBuilder().append(Globals.BEARER).append(token).toString();
           String postUrl = getString(R.string.base_url_agent) + Globals.BANKS_ENDPOINT_CODE +countryCode;

        RequestQueue requestQueue = Volley.newRequestQueue(this);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, postUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println(response);
                GenericDataInfo banlkSpinnerResponse  = new Gson().fromJson(response.toString(), GenericDataInfo.class);
                List<LookUpData> bankDataArrayListA = (ArrayList<LookUpData>) banlkSpinnerResponse.getData().getList();

                LookUpData data = new LookUpData();
                data.setName(getString(R.string.select_bank));
                data.setCode(Globals.NON);
                data.setDescription(Globals.NON);
                bankDataArrayList.add(data);




                for (int i = 0; i < banlkSpinnerResponse.getData().getList().size(); i++) {
                    LookUpData LookUpData = bankDataArrayListA.get(i);
                    bankDataArrayList.add(LookUpData);
                }
                customLanguageAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                FormUtility.showMessageBox(error.getMessage(), act, getString(R.string.entity_code));
                FormUtility.ShowSnapMessage(parent_view, getString(R.string.no_internet));
            }
        })

        {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put(Globals.CONTENT_TYPE, Globals.APPLICATIONjASON);
                headers.put(Globals.XCODE, getString(R.string.source_code));
                headers.put(Globals.XCLINT, getString(R.string.client_id));
                headers.put(Globals.XSECRET, getString(R.string.client_secret));
                headers.put(Globals.XENTITY, getString(R.string.entity_code));
                headers.put(Globals.XUSERNAME, getString(R.string.client_secret));
                headers.put(Globals.AUTORIZATION, new_token);

                return headers;
            }

        };

        requestQueue.add(jsonObjectRequest);

    }
    public  void  openDatePicker(View view1) {
          final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(AccountOpening2.this,
                (DatePickerDialog.OnDateSetListener) (view, year1, monthOfYear, dayOfMonth) -> {

                   String dateOB = OtaUtility.AppendZeroToDte( ( dayOfMonth )) + "-" + OtaUtility.AppendZeroToDte( ( monthOfYear + 1)) +"-" + year1 ;
                    dob.setText(dateOB);



                }, year, month, day);
        c.add(Calendar.YEAR, -13);
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    public  void  openDatePicker() {
           final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(AccountOpening2.this,
                (DatePickerDialog.OnDateSetListener) (view, year1, monthOfYear, dayOfMonth) -> {
     //       String dateOB = year1 + "-" +  (monthOfYear + 1)  + "-" +dayOfMonth  ;
            String dateOB = year1 + "-" + OtaUtility.AppendZeroToDte( ( monthOfYear + 1))+ "-" +OtaUtility.AppendZeroToDte( ( dayOfMonth ));

                    dob.setText(dateOB);

                }, year, month, day);
        c.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }



    private boolean validateInput() {

        String dateOfBirth =  dob.getText().toString().trim();

        String CurrentDate= dateOfBirth.toString();// "
        String FinalDate= OtaUtility.CDateToString(new Date()).toString();

        Date date1;
        Date date2;

        try {
            date1 = OtaUtility.dff3.parse(CurrentDate);

            date2 = OtaUtility.dff3.parse(FinalDate);

            //Comparing dates
            long difference = Math.abs(date1.getTime() - date2.getTime());
            long differenceDates = difference / (24 * 60  * 60 * 1000 );
            minDay = Long.valueOf(13*356);

            dayDifference = Long.valueOf(Long.toString(differenceDates));


            if(dayDifference <=  minDay) {
                FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.youngToCome), this);
                    return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

       String string_gender = gender.getText().toString().trim();

        String string_openAccount = openAccount.getBankCode();
        String string_phone = phone.getText().toString().trim();
        String string_selectID = selectID.getText().toString().trim();
        String string_identityNum = identityNum.getText().toString().trim();
        token =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getTicketID();
        language =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getLanguage();
        countryCode =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getCountry();


        try {
        if (string_gender.equals(Globals.NON)) {
            FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.gender) + " " + getString(R.string.not_specified), this);
            return false;
        }
        if (string_phone.equals(Globals.NON)) {
            FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.phone_no) + " " + getString(R.string.not_specified), this);
            return false;
        }
        if (string_openAccount.equals(Globals.NON)) {
            FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.bank) + " " + getString(R.string.not_specified), this);
            return false;
        }
        } catch (Exception e) {
            FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.seeAllFiled), this);
        }
        openAccount.setGender(string_gender);
        openAccount.setMobileNumber(string_phone);
        openAccount.setIdType(string_selectID);
        openAccount.setIdNumber(string_identityNum);
        openAccount.setBankCode(openAccount.getBankCode());
        openAccount.setDateOfBirth(dob.getText().toString().trim());
        openAccount.setId(0);

        return true;
    }


}



