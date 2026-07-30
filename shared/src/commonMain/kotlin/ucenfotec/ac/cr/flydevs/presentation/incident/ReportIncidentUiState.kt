package ucenfotec.ac.cr.flydevs.presentation.incident

import ucenfotec.ac.cr.flydevs.domain.model.INCIDENT_DESCRIPTION_MAX_LENGTH

data class ReportIncidentUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val orderCode: String = "",
    val cardCount: Int = 0,
    val counterpartLabel: String = "",
    val description: String = "",
    val hasOpenIncident: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null,
) {
    val maxLength: Int get() = INCIDENT_DESCRIPTION_MAX_LENGTH

    val remainingCharacters: Int get() = maxLength - description.length

    val canSubmit: Boolean
        get() = !isSubmitting && !isLoading && description.isNotBlank()
}
