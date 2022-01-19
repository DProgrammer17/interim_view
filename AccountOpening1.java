package com.accelerexholdings.anp.ui.accountOpening;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.accelerexholdings.anp.R;
import com.accelerexholdings.anp.data.remote.ApiInterface;
import com.accelerexholdings.anp.data.remote.RetrofitInstance;
import com.accelerexholdings.anp.model.GenericDataInfo;
import com.accelerexholdings.anp.model.LookUpData;
import com.accelerexholdings.anp.model.OpenAccount;
import com.accelerexholdings.anp.utils.AppController;
import com.accelerexholdings.anp.utils.FormUtility;
import com.accelerexholdings.anp.utils.Globals;
import com.accelerexholdings.anp.utils.SharedPrefUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AccountOpening1 extends AppCompatActivity {
    OpenAccount openAccount ;
    SharedPreferences shared;
    Activity act;
    String token,language,countryCode,username,email;
    String deviceId, entityCode;

    @BindView(R.id.firstName)
    TextInputEditText firstName;

    @BindView(R.id.textMiddleName)
    TextInputEditText textMiddleName;

    @BindView(R.id.edtLastname)
    TextInputEditText edtLastname;

    @BindView(R.id.edt_address)
    TextInputEditText edt_address;

    @BindView(R.id.city)
    TextInputEditText city;

    @BindView(R.id.postcode)
    TextInputEditText postcode;

    @BindView(R.id.email)
    TextInputEditText tv_email;

    @BindView(R.id.accountType)
    AutoCompleteTextView accountType;



    private View parent_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_opening1);
        initialize();

    }

    private void initialize() {
        ButterKnife.bind(this);
        parent_view = findViewById(android.R.id.content);

        shared =  this.getSharedPreferences(String.valueOf((R.string.app_name)), Context.MODE_PRIVATE);
        token =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getTicketID();
        language =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getLanguage();
        countryCode =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getCountry();
        deviceId = FormUtility.GetAndroidId(getApplicationContext(),this);
        entityCode =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getEntityCode();
        username =  SharedPrefUtil.getSharedPrefInstance().getLoginInfo().getUsername();

        getAccountType();
        openAccount = new OpenAccount();
    }

    private void getAccountType() {
        RetrofitInstance.createServiceWithAuth(ApiInterface.class)
                .getCustomerType()
                .enqueue(new Callback<GenericDataInfo>() {
                    @Override
                    public void onResponse(Call<GenericDataInfo> call, Response<GenericDataInfo> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getCode().equals(Globals.RESPONSE_CODE_b)) {
                                List<String> accountTypes = new ArrayList<>();
                                for (LookUpData lookUpData : response.body().getData().getList()) {
                                    accountTypes.add(lookUpData.getName());
                                }
                                ArrayAdapter<String> accountTypeAdapter = new ArrayAdapter<>(AccountOpening1.this,
                                        R.layout.item_dropdown, R.id.dropdown_item, accountTypes);

                                accountType.setAdapter(accountTypeAdapter);
                            }else {
                                Timber.d(Globals.SOMETHIisWrong+ response.body().getName());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericDataInfo> call, Throwable t) {
                        Timber.d(Globals.SOMETHIisWrong+ t.getLocalizedMessage());
                    }
                });
    }

    public void backPress(View view) {

        finish();
    }

    public void next(View view) {
        validateInput();

    }

    private boolean validateInput() {
        String string_firstName =  firstName.getText().toString().trim();
        String string_lastname =  edtLastname.getText().toString().trim();
        String string_fullAddress =  edt_address.getText().toString().trim();
        String middleName =  textMiddleName.getText().toString().trim();
        email =  tv_email.getText().toString().trim();

        String  string_city =  city.getText().toString().trim();
        String  stringAccountType =  accountType.getText().toString();

        shared =  this.getSharedPreferences(String.valueOf((R.string.app_name)), Context.MODE_PRIVATE);
        deviceId = FormUtility.GetAndroidId(getApplicationContext(),this);


        try {

                if (firstName.equals(Globals.NON)) {
                    FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.firstName) + " " + getString(R.string.not_specified), this);
                    return false;
                }
                if (string_firstName.equals(Globals.NON)) {
                    FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.firstName) + " " + getString(R.string.not_specified), this);
                    return false;
                }
                if (string_lastname.equals(Globals.NON)) {
                    FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.lastName) + " " + getString(R.string.not_specified), this);
                    return false;
                }
                if (string_fullAddress.equals(Globals.NON)) {
                    FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.fullAddress) + " " + getString(R.string.not_specified), this);
                    return false;
                }
//                if (middleName.equals(Globals.NON)) {
//                    FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.middleName) + " " + getString(R.string.not_specified), this);
//                    return false;
//                }
                if (string_city.equals(Globals.NON)) {
                    FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.city) + " " + getString(R.string.not_specified), this);
                    return false;
                }

                if (postcode.equals(Globals.NON)) {
                    FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.zip) + " " + getString(R.string.not_specified), this);
                    return false;
                }
                if (stringAccountType.equals(Globals.NON)) {
                    FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.select_an_account_type), this);
                    return false;
                }
                if (!(isValid(email))) {
                    FormUtility.showAlertDialog(this.getString(R.string.title_incomplete_data), this.getString(R.string.enter_correct_email), this);
                    return false;
                }

        }catch (Exception e){
            FormUtility.showAlertDialog(getString(R.string.app_name_agent), getString(R.string.seeAllFiled), this);
            return false;
        }

        String geolocation = AppController.getInstance().getCurrentLocation(this);
        String deviceId = FormUtility.GetAndroidId(getApplicationContext(),this);
        String string_middleName = textMiddleName.getText().toString();
        openAccount.setFirstName(string_firstName);
        openAccount.setLastName(edtLastname.getText().toString().trim());
        openAccount.setMiddleName(string_middleName);
        openAccount.setEntityCode(entityCode);
        openAccount.setUsername(username);
        openAccount.setAccountType(stringAccountType);
        openAccount.setAddress(edt_address.getText().toString());
        openAccount.setEmail(email);
        openAccount.setCity(city.getText().toString());


        Intent intent = new Intent(AccountOpening1.this, AccountOpening2.class);
        intent.putExtra(Globals.REGISTER_REQUEST, openAccount);
        startActivity(intent);

        return true;

    }

    public static boolean isValid(String email) {
        String regex = Globals.EMAILeXPRESION;
        return email.matches(regex);
    }

}