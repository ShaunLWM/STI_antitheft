Changelog
=========

20/07/14 v3
- Popup message can now be close with `!unlock <password>` command
- `!uninstall` command just in case of any problems.

19/07/14 v2
  - Using [Little Fluffy Location Library](https://code.google.com/p/little-fluffy-location-library/) for retreiving location. Hardcoded location just in case something goes wrong.
  - Added `Test Location` in settings and remove location from `Test SMS`
  - `!status` will now show device unique ID - Either UDID (D) or Serial (S) (if unable to get UDID) 


19/07/14 v1
  - Popup message will now allow user to unlock the phone. After 3 wrong tries, unlock button disappear and the screen/message will stay there EVEN after reboot. Back button has been locked.
  - Retreiving IMEI number using `!status` command.
  - Cut short on `!status` replies. 

---
Todo
==

- Improve location (50%) - **Remove the notification!**
- See if it's possible to lock Home/Recent Apps button on Popup message.