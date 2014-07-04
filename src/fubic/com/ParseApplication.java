package fubic.com;

import com.parse.Parse;

import android.app.Application;

public class ParseApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "", "");

		//ParseUser.enableAutomaticUser();
		//ParseACL defaultACL = new ParseACL();
		// Optionally enable public read access.
		// defaultACL.setPublicReadAccess(true);
		//ParseACL.setDefaultACL(defaultACL, true);
	}
}
