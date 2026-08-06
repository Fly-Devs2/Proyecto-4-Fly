# Admin User Management Implementation Plan

Implement a comprehensive user management system for administrators, allowing them to list, filter, search, view details, block, and delete users.

## User Review Required

- **Disputes and Reports**: As per feedback, "Disputas" is not needed and will be omitted. "Reportes" will be calculated as the number of incidents where the user is the `counterpartId`.
- **Reputation Calculation**: For the "Reputación" percentage, I'll use `(averageRating / 5.0) * 100` as confirmed.
- **Last Activity**: I will update `lastActivity` in `SessionViewModel` whenever the app starts or the user profile is refreshed.

## Proposed Changes

### 1. Domain Layer

#### [User.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/User.kt)
- Add `isActive: Boolean = true` and `lastActivity: Long = 0L` to the `User` data class.

#### [NEW] [IAdminUserRepository.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/repository/IAdminUserRepository.kt)
- Define methods for admin-related user operations:
    - `observeAllUsers(): Flow<List<User>>`
    - `updateUserStatus(uid: String, isActive: Boolean): Result<Unit>`
    - `deleteUser(uid: String): Result<Unit>`
    - `getUserStats(uid: String): Flow<UserAdminStats>` (Custom model for reports/reputation)

#### [NEW] [UserAdminStats.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/UserAdminStats.kt)
- Data class to hold `reportCount`, `completedPurchases`, `publishedSales`, `averageRating`, etc. (Omit `disputeCount`).

---

### 2. Data Layer

#### [NEW] [AdminUserRepositoryImpl.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/AdminUserRepositoryImpl.kt)
- Implement `IAdminUserRepository` using Firestore.
- `observeAllUsers` will fetch the entire `users` collection (since it's ~1300 users, it's manageable for now, but we can add pagination later if needed).
- `getUserStats` will combine data from `user_ratings` and `incidents` collections.

#### [AuthRepositoryImpl.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/AuthRepositoryImpl.kt)
- Update `login` and `getUserProfile` to check `isActive`. If `isActive` is false, throw a specific exception.

---

### 3. Presentation Layer (Shared)

#### [NEW] [AdminUsersUiState.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/adminUsers/AdminUsersUiState.kt)
- Define UI state for the user list screen (search query, filters, list of users).

#### [NEW] [AdminUsersViewModel.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/adminUsers/AdminUsersViewModel.kt)
- Logic for filtering, searching, and fetching users.

#### [NEW] [AdminUserDetailUiState.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/adminUserDetail/AdminUserDetailUiState.kt)
- Define UI state for the user detail screen.

#### [NEW] [AdminUserDetailViewModel.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/adminUserDetail/AdminUserDetailViewModel.kt)
- Logic for blocking/unblocking and deleting users.

#### [SessionViewModel.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/session/SessionViewModel.kt)
- Add logic to check `isActive` and force logout if the user is blocked.
- Update `lastActivity` on initialization.

---

### 4. UI Layer (Compose App)

#### [NEW] [AdminUsersScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/AdminUsersScreen.kt)
- User list UI as per the provided screenshot.

#### [NEW] [AdminUserDetailScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/AdminUserDetailScreen.kt)
- User detail UI as per the provided screenshot.

#### [Routes.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/navigation/Routes.kt)
- Add routes for `AdminUsers` and `AdminUserDetail`.

#### [App.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/App.kt)
- Set up navigation for the new admin screens.

---

### 5. Dependency Injection

#### [DataModule.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/di/modules/DataModule.kt)
- Register `AdminUserRepositoryImpl`.

#### [ViewModelModule.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/di/modules/ViewModelModule.kt)
- Register `AdminUsersViewModel` and `AdminUserDetailViewModel`.

## Verification Plan

### Automated Tests
- No new automated tests are planned, but I will ensure the code compiles and follows existing patterns.

### Manual Verification
1.  **User List**:
    *   Verify search by name and email works.
    *   Verify filters (Todos, Usuarios, Mensajeros, Tiendas, Admins) work.
    *   Verify user counts and sorting.
2.  **User Detail**:
    *   Verify user info is correctly displayed (phone, role, purchases, sales).
    *   Verify activity and risk section (reports, reputation).
    *   Verify blocking/unblocking functionality.
    *   Verify deletion functionality.
3.  **Blocking Logic**:
    *   Block a user and verify they are logged out.
    *   Try to log in as a blocked user and verify the error message "Usuario bloqueado" appears.
