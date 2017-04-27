package com.epson.epos2_printer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import com.epson.epos2.printer.ReceiveListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Handler;

import static com.epson.epos2_printer.R.string.app_name;

public class Test_P extends Activity implements ReceiveListener {

    public static final String MyShPref_PD = "MyShPref_data";
    String mEditTarget;
    private Printer  mPrinter = null;
    private Context mContext = null;

    String st1;
    
    CharSequence text = "Failed to Get Printer!";
    int duration = Toast.LENGTH_LONG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm);

        SharedPreferences s = getSharedPreferences(MyShPref_PD, 0);
        HashMap<String, String> map= (HashMap<String, String>) s.getAll();


        Thread thread = new Thread(new Runnable(){
            public void run() {
                try {

                    URL url = new URL("https://example.com/00012-print.txt");

                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                    StringBuffer stringBuffer = new StringBuffer();
                    String str;

                    while ((str = in.readLine()) != null) {
                        stringBuffer.append(str);
                        stringBuffer.append("\n");

                        System.out.println("Contents of file:");
                        System.out.println(stringBuffer.toString());
                        st1=stringBuffer.toString();

                    }
                    in.close();
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                }
            }
        });

        thread.start();

        if (map != null && (map.containsKey("Target") && map.get("Target") != null))
        {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    try {

                        try {

                            SharedPreferences s = getSharedPreferences(MyShPref_PD, 0);
                            HashMap<String, String> map= (HashMap<String, String>) s.getAll();

                            mEditTarget = map.get("Target");

                            mPrinter.connect(mEditTarget, Printer.PARAM_DEFAULT);
                        }
                        catch (Exception e) {
                            ShowMsg.showException(e, "Please connected", mContext);

                            /*Toast toast = Toast.makeText(context, text, duration);
                            toast.show();*/
                        }


                        SharedPreferences s = getSharedPreferences(MyShPref_PD, 0);
                        HashMap<String, String> map= (HashMap<String, String>) s.getAll();

                        mEditTarget = map.get("Target");

                        System.out.print("gfdgfdgfdggfgfdgdfg=================" + map.get("Target"));

                        mPrinter.connect(mEditTarget, Printer.PARAM_DEFAULT);
                    }
                    catch (Exception e) {
                        //ShowMsg.showException(e, "Connect Your Printer \n Try again", mContext);
                    }

                    runPrintReceiptSequence();

                }
            }, 3000);

            finish();
        }

        else {
            Toast.makeText(getApplicationContext(), "No Printere Found \n Please Setup and Tryagain", Toast.LENGTH_SHORT).show();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("ⓘ  Exit !  " +  getString(app_name));
            alertDialogBuilder
                    .setMessage("No Printer Found \n Please Setup and Try Again")
                    .setCancelable(false)
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    /*moveTaskToBack(true);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(0);*/
                                    finish();
                                }
                            });
        }
    }

    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {

            SharedPreferences s = getSharedPreferences(MyShPref_PD, 0);
            HashMap<String, String> map= (HashMap<String, String>) s.getAll();

            mEditTarget = map.get("Target");

            mPrinter.connect(mEditTarget, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "beginTransaction", mContext);
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }


    private boolean createReceiptData() {
        String method = "";
        StringBuilder textData = new StringBuilder();
        
        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addTextAlign";

            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            method = "addText";
            mPrinter.addText(textData.toString());

            textData.append(st1);

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            ShowMsg.showException(e, method, mContext);
            return false;
        }

        textData = null;

        return true;
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            ShowMsg.showMsg(makeErrorMessage(status), mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "sendData", mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }


    private boolean runPrintReceiptSequence() {
        if (!initializeObject()) {
            return false;
        }

        if (!createReceiptData()) {
            finalizeObject();
            return false;
        }
        if (!printData()) {
            finalizeObject();

            return false;
        }

        finish();
        return true;


    }
    private boolean initializeObject() {
        try {
            mPrinter = new Printer(Printer.TM_T88, Printer.MODEL_ANK, mContext);

        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }


        mPrinter.setReceiveEventListener(null);

        return true;

    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }



    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "endTransaction", mContext);
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "disconnect", mContext);
                }
            });
        }

        finalizeObject();
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        else {
            ;//print available
        }

        return true;
    }

    private String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter);
            msg += getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        EditText edtWarnings = (EditText)findViewById(R.id.edtWarnings);
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }

        edtWarnings.setText(warningsMsg);

        edtWarnings.setText(warningsMsg);

    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, makeErrorMessage(status), mContext);

                dispPrinterWarnings(status);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }

}
