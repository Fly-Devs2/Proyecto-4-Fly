# Fix Google Button Centering

The current implementation of the Google sign-in button uses a `Text("G")` component inside a `Box`. This is problematic because text glyphs have varying baselines and internal padding depending on the device's system font, making it nearly impossible to center perfectly across all devices using simple offsets.

## Proposed Changes

### [New Component] GoogleIcon.kt

Create a new file to hold the vector definition of the Google "G" logo. This ensures the icon is always perfectly centered and uses the official branding colors (which is better for user trust).

#### [GoogleIcon.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/components/GoogleIcon.kt)

- Define `GoogleGLogo` as an `ImageVector`.
- Use the standard 24x24 viewport path data for the 4 segments (Red, Yellow, Green, Blue).

### [Screen] LoginScreen.kt

#### [LoginScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/LoginScreen.kt)

- Replace the `Box` containing `Text("G")` with a simpler `Image` or `Icon` using the new `GoogleGLogo`.
- Remove the manual `offset` and `Box` background if we use the full-color logo, or keep the white circle if we want to stick to the monochrome look (but full color is recommended).

## Verification Plan

### Manual Verification
1. Deploy the app to the device.
2. Observe the Google button on the Login screen.
3. Verify that the "G" logo is perfectly centered within its container.
4. Verify that the colors look correct (if using the multi-color version).
