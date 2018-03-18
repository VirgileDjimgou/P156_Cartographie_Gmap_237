

package tech.ioengine.Login.Utility;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.View;
import tech.ioengine.Login.R;
import tech.ioengine.Login.buildConfig;
import tech.ioengine.Login.data.Preferences;


/**
 * Helper class, which creates all dialogs.
 */
public class UiUtils {


    public static String getFormattedDistance(float distance, Context context) {
        String distanceStr = String.valueOf(distance) + " ";
        if (Preferences.isMetricUnit(context)) {
            distanceStr += context.getString(R.string.km_string);
        } else {
            distanceStr += context.getString(R.string.mi_string);
        }
        return distanceStr;
    }

    private static SpannableStringBuilder createAboutContent(final Context context) {
        // get app version
        String versionName = buildConfig.VERSION_GIT_DESCRIPTION;

        // build the about body view and append the link to see OSS licenses
        String title = context.getString(R.string.app_name) + " " + context.getString(R.string.guide);
        SpannableStringBuilder aboutBody = new SpannableStringBuilder();
        aboutBody.append(Html.fromHtml(context.getString(R.string.about_version, title, versionName)));

        SpannableString licensesLink = new SpannableString(context.getString(R.string.about_licenses));
        licensesLink.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                showOpenSourceLicenses((Activity) context);
            }
        }, 0, licensesLink.length(), 0);
        aboutBody.append("\n\n");
        aboutBody.append(licensesLink);

        SpannableString creditsLink = new SpannableString(context.getString(R.string.about_credits));
        creditsLink.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                showCredits((Activity) context);
            }
        }, 0, creditsLink.length(), 0);
        aboutBody.append("\n\n");
        aboutBody.append(creditsLink);
        return aboutBody;
    }

    private static void showOpenSourceLicenses(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog_licenses");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        //new OpenSourceLicensesDialog().show(fragmentTransaction, "dialog_licenses");
    }

    private static void showCredits(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("about_credits");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        //new CreditsDialog().show(fragmentTransaction, "about_credits");
    }

    public static void rateApp(Context cnt, boolean googlePlay) {//true if Google Play, false if other
        try {
            if (googlePlay)
                cnt.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                        + cnt.getPackageName())));

        } catch (ActivityNotFoundException exc) {
            try {
                //open browser if no play store installed
                cnt.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "http://play.google.com/store/apps/details?id=" + cnt.getPackageName())));
            } catch (ActivityNotFoundException exc1) {
                //put some logic here for FDroid or Play Store
            }
        }
    }


}
