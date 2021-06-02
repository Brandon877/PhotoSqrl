# PhotoSqrl
Android app to automatically transfer new photos taken from phone to PC

My goal with this App:
1. To create an app that will automatically send new photos taken on a users phone to the users PC without any user interaction after initial setup.
2. To release this on the App Store free of charge, with no ads, and absolutly no collection of any user data. Photos will be sent directly from phone to PC.  No server in            between.
3. To make this app as secure as possible while preserving little to no user interaction.


Where the app is so far:
1. COMPLETED - "SqrlNutz" gallery for the user to view photos awaiting transfer to PC.
2. COMPLETED - Foreground service that monitors for any media uri changes. Once a new photo is detected, a new Job is started that makes a copy of the newly added photos and                      stores it in the "SqrlNutz" gallery.
3. COMPLETED - Class to handle turning on and off phone's local only hotspot.

[!]*******************************************************************************************************************************************************************************
I am developing this app in order the better learn/understand Java and Android development.  Ultimately, I plan on using these skills to seek employement.  As such, if anyone happens to stumble upun this and can spare the time, ANY constructive criticism would be GREATLY appreciated.
[!]******************************************************************************************************************************************************************************
