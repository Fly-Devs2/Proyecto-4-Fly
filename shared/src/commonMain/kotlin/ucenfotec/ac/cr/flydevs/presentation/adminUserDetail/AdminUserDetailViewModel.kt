package ucenfotec.ac.cr.flydevs.presentation.adminUserDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.IAdminUserRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IReputationRepository

class AdminUserDetailViewModel(
    private val adminUserRepository: IAdminUserRepository,
    private val reputationRepository: IReputationRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUser()
        observeStats()
    }

    private fun loadUser() {
        _uiState.update { it.copy(isLoading = true) }
        reputationRepository.observeUser(userId)
            .onEach { user ->
                _uiState.update { it.copy(isLoading = false, user = user) }
            }
            .catch { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeStats() {
        adminUserRepository.getUserStats(userId)
            .onEach { stats ->
                _uiState.update { it.copy(stats = stats) }
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    fun toggleUserStatus() {
        val user = _uiState.value.user ?: return
        val newStatus = !user.isActive

        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            adminUserRepository.updateUserStatus(userId, newStatus)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            user = user.copy(isActive = newStatus)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            adminUserRepository.deleteUser(userId)
                .onSuccess {
                    _uiState.update { it.copy(isActionLoading = false, actionSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false, errorMessage = error.message) }
                }
        }
    }
}
