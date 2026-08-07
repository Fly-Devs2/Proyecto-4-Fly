package ucenfotec.ac.cr.flydevs.presentation.adminUsers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.domain.repository.IAdminUserRepository

class AdminUsersViewModel(
    private val adminUserRepository: IAdminUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    init {
        observeUsers()
    }

    private fun observeUsers() {
        _uiState.update { it.copy(isLoading = true) }
        adminUserRepository.observeAllUsers()
            .onEach { users ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = users
                    )
                }
                applyFilters()
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar usuarios"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onRoleFilterSelected(role: UserRole?) {
        _uiState.update { it.copy(selectedRole = role) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.users.filter { user ->
            val matchesSearch = if (state.searchQuery.isBlank()) true else {
                user.name.contains(state.searchQuery, ignoreCase = true) ||
                user.email.contains(state.searchQuery, ignoreCase = true)
            }
            val matchesRole = if (state.selectedRole == null) true else {
                user.userRole == state.selectedRole
            }
            matchesSearch && matchesRole
        }.sortedByDescending { it.lastActivity }

        _uiState.update { it.copy(filteredUsers = filtered) }
    }
}
