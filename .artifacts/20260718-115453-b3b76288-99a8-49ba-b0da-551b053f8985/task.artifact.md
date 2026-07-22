# Task: Fix crash in PublishGameCardScreen

- [x] Research and Planning
    - [x] Analyze crash cause in `PublishGameCardScreen.kt`
    - [x] Verify `CardGame` enum definition
    - [x] Plan usage correction
- [/] Implementation
    - [/] Fix `SearchableDropdown` usage in `PublishGameCardScreen.kt`
        - [ ] Correct `label` lambda to use `it.label` instead of casting to `String`
        - [ ] Link `onSelect` to `viewModel.onGameChange`
        - [ ] Remove temporary `selectedOption` state and use `state.game`
- [ ] Verification
    - [ ] Verify typing in the game selector no longer crashes the app
    - [ ] Verify selected game is correctly updated in the `ViewModel`
