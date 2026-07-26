# Task List

- [x] Explore project structure and identify navigation logic
- [x] Investigate menu implementation
- [x] Identify where the "messenger home" and "regular home" routes are defined
- [x] Fix the navigation issue in the menu
	- [x] Update `handleBottomNavNavigation` in `App.kt`
	- [x] Update navigation calls in `App.kt`
	- [x] Fix `MessengerHomeScreen.kt` bottom nav state
- [x] Improve Courier Flow
	- [x] Add QR scan support for Batch Label ID (e.g., L-2212)
	- [x] Hide "ENTREGA ACTIVA" section in `MessengerHomeScreen.kt`
- [x] Fix QR Scan Re-entry Error
	- [x] Update `ScanQrViewModel` to handle already accepted batches
- [x] Verify the fix
